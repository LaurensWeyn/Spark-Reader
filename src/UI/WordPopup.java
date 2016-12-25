/* 
 * Copyright (C) 2016 Laurens Weyn
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
package UI;

import Hooker.ClipboardHook;
import Language.Dictionary.DefTag;
import Language.Splitter.FoundDef;
import Language.Splitter.FoundWord;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

/**
 *
 * @author Laurens Weyn
 */
public class WordPopup extends JPopupMenu
{
    FoundWord word;
    
    JMenuItem addBreak;
    JMenuItem copy;
    JMenuItem append;
    JCheckBoxMenuItem markKnown;
    JMenuItem anki;
    JMenuItem setDef;
    int x, y;
    UI ui;
    public WordPopup(FoundWord word, UI ui)
    {
        this.word = word;
        this.ui = ui;
        String clipboard = ClipboardHook.getClipboard();
        //TODO set markKnown as selected depending on if the word is known or not
        //TODO set actions for all buttons
        anki = new JMenuItem(new AbstractAction("Add as flashcard")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    ankiExport(word);
                }catch(IOException err)
                {
                    JOptionPane.showMessageDialog(ui.disp.getFrame(), "Error exporting word: " + err, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        copy = new JMenuItem(new AbstractAction("Copy to clipboard")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(word.getText());
            }
        });
        append = new JMenuItem(new AbstractAction("Append to clipboard")
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
                ui.mouseClicked(new MouseEvent(ui.disp.getFrame(), -1, System.currentTimeMillis(), 0, x, y, 1, true, 2));
            }
        });
        setDef = new JMenuItem(new AbstractAction("Set definition as default")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.prefDef.setPreferred(word.getCurrentDef());
                word.sortDefs();
            }
        });
        markKnown = new JCheckBoxMenuItem(new AbstractAction("I know this word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(markKnown.isSelected())
                {
                    UI.known.setKnown(word);
                }
                else
                {
                    UI.known.setUnknown(word);
                }
                ui.render();//change color of word
            }
        });
        markKnown.setSelected(UI.known.isKnown(word));
        
        add(addBreak);
        add(markKnown);
        add(setDef);
        add(copy);
        if(UI.text.contains(clipboard + word.getText()))add(append);
        add(anki);
    }
    public void show(int x, int y)
    {
        this.x = x;
        this.y = y;
        show(ui.disp.getFrame(), x, y);
    }
    
    
    public void ankiExport(FoundWord word)throws IOException
    {
        File file = new File(UI.options.getOption("ankiExportPath"));
        boolean newFile = !file.exists();
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8"));
        
        if(newFile)
        {
            //fr.append("Word\tReading\tDefinition\tTags\tContext\n");//Anki ignores this, no point in adding it
        }

        FoundDef def = word.getCurrentDef();
        String kanji = def.getDictForm();
        String reading = def.getFurigana();
        String definition = def.getDefinition().getMeaningLine();
        String tagList = "";
        for(DefTag tag:def.getDefinition().getTags())
        {
            tagList += tag.name() + " ";
        }
        fr.append(kanji + "\t" + reading + "\t" + definition +"\t" + tagList + "\t" + UI.text + "\n");
        
        fr.close();
    }
    
}
