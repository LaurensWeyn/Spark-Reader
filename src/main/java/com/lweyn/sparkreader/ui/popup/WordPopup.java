/* 
 * Copyright (C) 2017 Laurens Weyn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.lweyn.sparkreader.ui.popup;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.Persist;
import com.lweyn.sparkreader.hooker.ClipboardHook;
import com.lweyn.sparkreader.language.splitter.FoundWord;
import com.lweyn.sparkreader.ui.Line;
import com.lweyn.sparkreader.ui.UI;
import com.lweyn.sparkreader.ui.WordEditUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 * @author Laurens Weyn
 */
public class WordPopup extends JPopupMenu
{
    FoundWord word;
    
    JMenuItem addBreak;
    JMenuItem exportLine;
    JMenuItem exportWord;
    JMenuItem makeDefinition;
    JMenuItem copy;
    JMenuItem append;
    JCheckBoxMenuItem markKnown;
    int x, y;
    UI ui;
    public WordPopup(Line line, FoundWord word, UI ui)
    {
        this.word = word;
        this.ui = ui;
        String clipboard = ClipboardHook.getClipboard();
        exportLine = new JMenuItem(new AbstractAction("Export whole line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                setVisible(false);//ensure this menu's already gone for the screenshot
                Persist.exportLine();
            }
        });
        exportWord = new JMenuItem(new AbstractAction("Add word as flashcard")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DefPopup.ankiExport(word);
            }
        });
        makeDefinition = new JMenuItem(new AbstractAction("Create new userdict entry")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                makeDefPopup(line);
            }
        });
        copy = new JMenuItem(new AbstractAction("Copy word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(word.getText());
            }
        });
        append = new JMenuItem(new AbstractAction("Append word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(clipboard + word.getText());
            }
        });
        addBreak = new JMenuItem(new AbstractAction("Toggle break here (middle click)")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                System.out.println("adding break");
                //simulate middle button press at window origin (placing a break there)
                ui.mouseHandler.middleClick(new Point(x, y));
            }
        });
        
        markKnown = new JCheckBoxMenuItem(new AbstractAction("I know this word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(markKnown.isSelected())
                {
                    Main.known.setKnown(word);
                }
                else
                {
                    Main.known.setUnknown(word);
                }
                ui.render();//change color of word
            }
        });
        markKnown.setSelected(Main.known.isKnown(word));

        exportLine.setText("Export whole line (" + Persist.getLineExportCount() + ")");
        exportWord.setText("Add word as flashcard (" + DefPopup.getDefExportCount() + ")");

        add(addBreak);
        //add(exportLine);
        add(exportWord);
        if(Main.options.getOptionBool("enableKnown"))
            add(markKnown);
        //FIXME disabled for now as it causes problems
        //add(makeDefinition);
        add(new Separator());
        add(copy);
        if(Main.currPage.getText().contains(clipboard) && Main.currPage.getText().contains(clipboard + word.getText()))add(append);

        addPopupMenuListener(new IgnoreExitListener());
    }
    public void show(int x, int y)
    {
        this.x = x;
        this.y = y;
        show(ui.disp.getFrame(), x, y);
    }

    public static void makeDefPopup(Line line)
    {
        JFrame frame = Main.ui.disp.getFrame();
        try
        {
            //TODO move this line substring select UI to the WordEditUI
            String note = (String)
            JOptionPane.showInputDialog(frame,
                "Cut line down to undefined word",
                "Exporting line",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                line.toString());
            if(note == null)return;//cancelled

            Thread.sleep(500);//give the popup time to disappear
            
            new WordEditUI(note);
        }
        catch(InterruptedException err)
        {
            JOptionPane.showInputDialog(frame, "Error while exporting line:\n" + err);
        }
    }

}
