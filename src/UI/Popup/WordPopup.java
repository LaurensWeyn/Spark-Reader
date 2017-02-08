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
package UI.Popup;

import Hooker.ClipboardHook;
import Language.Dictionary.DefTag;
import Language.Dictionary.Kanji;
import Language.Splitter.FoundDef;
import Language.Splitter.FoundWord;
import UI.UI;

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
    JMenuItem copyFull;
    JCheckBoxMenuItem markKnown;
    int x, y;
    UI ui;
    public WordPopup(FoundWord word, UI ui)
    {
        this.word = word;
        this.ui = ui;
        String clipboard = ClipboardHook.getClipboard();
        
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
        copyFull = new JMenuItem(new AbstractAction("Copy whole line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //TODO move this elsewhere when possible
                ClipboardHook.setClipboard(UI.text);
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
        add(new Separator());
        add(copy);
        if(UI.text.contains(clipboard) && UI.text.contains(clipboard + word.getText()))add(append);
        add(copyFull);
        
        addPopupMenuListener(new IgnoreExitListener());
    }
    public void show(int x, int y)
    {
        this.x = x;
        this.y = y;
        show(ui.disp.getFrame(), x, y);
    }
}
