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
package ui.popup;

import hooker.ClipboardHook;
import language.dictionary.DefTag;
import language.dictionary.FrequencySink;
import language.dictionary.Japanese;
import language.dictionary.Kanji;
import language.splitter.FoundDef;
import language.splitter.FoundWord;
import main.Main;
import main.Utils;
import ui.UI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Set;

import static language.dictionary.Japanese.isJapanese;
import static main.Main.options;

/**
 * When right clicking on the definition window
 * @author Laurens Weyn
 */
public class DefPopup extends JPopupMenu
{
    private UI ui;
    private FoundDef def;
    private JMenuItem anki, copy, copyAll, lookup;
    private JCheckBoxMenuItem setDef, setBlacklist;

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
                Main.prefDef.setPreferred(def);
                word.resortDefs();
                word.resetScroll();
                ui.render();
            }
        });
        setDef.setSelected(word.isShowingFirstDef());

        setBlacklist = new JCheckBoxMenuItem(new AbstractAction("Blacklist definition for this spelling")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.blacklistDef.toggleBlacklist(def);
                //word.resortDefs();
                //word.resetScroll();
                ui.render();
            }
        });
        setBlacklist.setSelected(Main.blacklistDef.isBlacklisted(word.getCurrentDef().getDefinition().getID(), word.getCurrentDef().getDictForm()));

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
                //remove all non-Japanese text (not relevant in lookup)
                String defLineMin = String.join("…", Japanese.splitJapaneseWriting(defLine));
                ClipboardHook.setClipBoardAndUpdate(defLineMin);
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

        anki.setText("Add as flashcard (" + getExportedCount() + ")");

        add(anki);
        add(setDef);
        add(setBlacklist);
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

    private static int exportedThisSession = 0;
    private static int exportedBeforeSession = -1;
    private int getExportedCount()
    {
        if(exportedBeforeSession != -1)return exportedBeforeSession + exportedThisSession;
        //calculate on first call
        if(Main.options.getOption("exportDisplay").equals("external"))
        {
            exportedBeforeSession = Utils.countLines(Main.options.getFile("ankiExportPath"));
        }
        else exportedBeforeSession = 0;

        return exportedBeforeSession + exportedThisSession;
    }

    public static void ankiExport(FoundWord word)
    {
        JFrame UIParent =  null;
        if(Main.ui != null)
        {
            UIParent = Main.ui.disp.getFrame();
        }
        try
        {
            File file = Main.options.getFile("ankiExportPath");
            Writer fr = new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8"));

            FoundDef def = word.getCurrentDef();
            String kanji = def.getDictForm();
            String reading = def.getFurigana();
            String definition = def.getDefinition().getMeaningLine();

            StringBuilder tagList = new StringBuilder();
            Set<DefTag> tags = def.getDefinition().getTags();
            if(tags != null)for(DefTag tag:tags)
            {
                tagList.append(tag.name()).append(" ");
            }


            StringBuilder kanjiDetails = new StringBuilder();
            int i = 0;
            while(i != kanji.length())
            {
                String lookup = Kanji.lookup(kanji.charAt(i));
                if(lookup != null)
                {
                    if(kanjiDetails.toString().equals("")) kanjiDetails = new StringBuilder(kanji.charAt(i) + " 【" + lookup + "】");
                    else kanjiDetails.append("<br>").append(kanji.charAt(i)).append(" 【").append(lookup).append("】");
                }
                i++;
            }

            String note = "";

            if(Main.options.getOptionBool("commentOnExport"))
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
            
            if(Main.options.getOptionBool("ankiExportEdictID"))
                fr.append(String.format("%d\t", def.getDefinition().getID()));
                        
            fr.append(kanji)
              .append("\t").append(reading)
              .append("\t").append(definition)
              .append("\t").append(tagList.toString())
              .append("\t").append(Main.currPage.getText().replace("\n", "<br>"))
              .append("\t").append(kanjiDetails.toString())
              .append("\t").append(note);
            
            if(Main.options.getOptionBool("ankiExportFreqData"))
            {
                // TODO: make FrequencySink.get take a FoundDef or something so it can check all possible furigana/spelling
                FrequencySink.FreqData freqdata = FrequencySink.get(word.getCurrentDef());
                if(freqdata != null)
                    fr.append(String.format("\t%d\t%.2f", freqdata.rank, freqdata.ppm));
                else
                    fr.append("\t\t");
            }
            
            fr.append("\n");

            fr.close();
            exportedThisSession++;

            if(Main.options.getOptionBool("exportMarksKnown"))
            {
                Main.known.setKnown(word);
                Main.ui.render();//display change
            }
        }catch(IOException err)
        {
            JOptionPane.showMessageDialog(UIParent, "Error exporting word: " + err, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}