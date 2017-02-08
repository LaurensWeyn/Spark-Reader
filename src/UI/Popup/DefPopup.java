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
package UI.Popup;

import Hooker.ClipboardHook;
import Language.Dictionary.DefTag;
import Language.Dictionary.Kanji;
import Language.Splitter.FoundDef;
import Language.Splitter.FoundWord;
import UI.UI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.swing.*;

import static Language.Dictionary.Japanese.isJapanese;

/**
 *
 * @author Laurens Weyn
 */
public class DefPopup extends JPopupMenu
{
    private UI ui;
    private FoundDef def;
    private JMenuItem anki, copy, copyAll, lookup;
    private JCheckBoxMenuItem setDef;
    public DefPopup(FoundWord word, UI ui, int mouseY)
    {
        this.ui = ui;
        def = word.getCurrentDef();
        String defLine = getDefText(mouseY);

        setDef = new JCheckBoxMenuItem(new AbstractAction("Set definition as default")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.prefDef.setPreferred(def);
                word.sortDefs();
                word.resetScroll();
                ui.render();
            }
        });
        setDef.setSelected(word.isShowingFirstDef());

        anki = new JMenuItem(new AbstractAction("Add as flashcard")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                DefPopup.ankiExport(word);
            }
        });
        copy = new JMenuItem(new AbstractAction("Copy line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(defLine);
            }
        });
        lookup = new JMenuItem(new AbstractAction("<html> lookup in <i>Spark Reader")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipBoardAndUpdate(defLine);
            }
        });
        copyAll = new JMenuItem(new AbstractAction("Copy full definition")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard("Definition for " + word.getText() + ":\n" + getDefText(-1));
            }
        });
        //TODO add option to close def after selecting an option?
        
        add(anki);
        add(setDef);
        add(new Separator());
        add(copy);
        if(isJapanese(defLine))add(lookup);
        add(copyAll);
        
        addPopupMenuListener(new IgnoreExitListener());
    }
    private String getDefText(int lineY)
    {
        def.setCapturePoint(lineY);
        ui.render();//needed to calculate where this is in the def text
        return def.getCapture();
    }
    public void show(int x, int y)
    {
        show(ui.disp.getFrame(), x, y);

    }
    
    public static void ankiExport(FoundWord word)
    {
        JFrame UIParent =  null;
        if(UI.instance != null)
        {
            UI.instance.disp.getFrame();
        }
        try
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


            String kanjiDetails = "";
            int i = 0;
            while(i != kanji.length())
            {
                String lookup = Kanji.lookup(kanji.charAt(i));
                if(lookup != null)
                {
                    if(kanjiDetails.equals("")) kanjiDetails = kanji.charAt(i) + " 【" + lookup + "】";
                    else kanjiDetails += "<br>" +  kanji.charAt(i) + " 【" + lookup + "】";
                }
                i++;
            }

            String note = "";

            if(UI.options.getOptionBool("commentOnExport"))
            {
                note = (String)JOptionPane.showInputDialog(UIParent,
                                            "Enter comment\n(You may also leave this blank)",
                                            "Adding " + kanji,
                                            JOptionPane.PLAIN_MESSAGE,
                                            null,
                                            null,
                                            UI.userComment);
                UI.userComment = note;//update for next time
            }

            if(note == null)return;//cancel export on pressing cancel

            fr.append(kanji + "\t"
                    + reading + "\t"
                    + definition +"\t"
                    + tagList + "\t"
                    + UI.text.replace("\n", "<br>") + "\t"
                    + kanjiDetails + "\t"
                    + note + "\n");

            fr.close();

            if(UI.options.getOptionBool("exportMarksKnown"))
            {
                UI.known.setKnown(word);
                UI.instance.render();//show change
            }
        }catch(IOException err)
        {
            JOptionPane.showMessageDialog(UIParent, "Error exporting word: " + err, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}