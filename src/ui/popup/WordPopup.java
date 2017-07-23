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
import language.splitter.FoundWord;
import main.Main;
import main.Utils;
import ui.Line;
import ui.UI;
import language.dictionary.DefSource;
import language.dictionary.UserDefinition;
import ui.WordEditUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Laurens Weyn
 */
public class WordPopup extends JPopupMenu
{
    FoundWord word;
    
    JMenuItem addBreak;
    JMenuItem exportLine;
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
                exportLine();
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

        exportLine.setText("Export whole line (" + getExportedCount() + ")");
        
        add(addBreak);
        add(exportLine);
        if(Main.options.getOptionBool("enableKnown"))add(markKnown);
        add(makeDefinition);
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

    private static int exportedThisSession = 0;
    private static int exportedBeforeSession = -1;
    private int getExportedCount()
    {
        if(exportedBeforeSession != -1)return exportedBeforeSession + exportedThisSession;
        //calculate on first call
        if(Main.options.getOption("exportDisplay").equals("external"))
        {
            exportedBeforeSession = Utils.countLines(Main.options.getFile("lineExportPath"));
        }
        else exportedBeforeSession = 0;

        return exportedBeforeSession + exportedThisSession;
    }

    public static void exportLine()
    {
        JFrame frame = Main.ui.disp.getFrame();
        try
        {
            Date date = new Date();
            DateFormat df = new SimpleDateFormat(Main.options.getOption("timeStampFormat"));
            File textFile = new File(Main.options.getOption("lineExportPath"));
            String note = "";

            if(Main.options.getOptionBool("commentOnExportLine"))
            {
                note = (String)JOptionPane.showInputDialog(frame,
                                                           "Enter comment\n(You may also leave this blank)",
                                                           "Exporting line",
                                                           JOptionPane.PLAIN_MESSAGE,
                                                           null,
                                                           null,
                                                           UI.userComment);
                if(note == null)return;//cancelled

                UI.userComment = note;//update for next time
                Thread.sleep(500);//give the popup time to disappear
            }

            Writer fr = new OutputStreamWriter(new FileOutputStream(textFile, true), Charset.forName("UTF-8"));
            fr.append(df.format(date))
                    .append("\t")
                    .append(Main.currPage.getText().replace("\n", "<br>"))
                    .append("\t")
                    .append(note)
                    .append("\n");
            fr.close();
            exportedThisSession++;
            Main.persist.exportCount++;

            //take a screenshot with the exported line
            if(Main.options.getOptionBool("exportImage"))
            {
                File imageFolder = Main.options.getFile("screenshotExportPath");
                if (!imageFolder.exists())
                {
                    boolean success = imageFolder.mkdirs();
                    if (!success) throw new IOException("Failed to create folder(s) for screenshots: directory"
                                                                + Main.options.getOption("screenshotExportPath"));
                }
                Robot robot = new Robot();
                Point pos = frame.getLocationOnScreen();
                Rectangle area;
                if(Main.options.getOptionBool("fullscreenScreenshot"))
                {
                    //whole screen
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    area = new Rectangle(0, 0, screenSize.width, screenSize.height);
                }
                else
                {
                    //game area
                    if(Main.options.getOptionBool("defsShowUpwards"))
                    {
                        area = new Rectangle(pos.x, pos.y + UI.furiganaStartY - Main.options.getOptionInt("maxHeight"),
                                Main.options.getOptionInt("windowWidth"),
                                Main.options.getOptionInt("maxHeight"));
                    }
                    else
                    {
                        area = new Rectangle(pos.x, pos.y + UI.defStartY,
                                Main.options.getOptionInt("windowWidth"),
                                Main.options.getOptionInt("maxHeight"));
                    }
                }

                //hide Spark Reader and take the screenshot
                UI.hidden = true;
                Main.ui.render();
                BufferedImage screenshot = robot.createScreenCapture(area);
                UI.hidden = false;
                Main.ui.render();

                String fileName = imageFolder.getAbsolutePath();
                if(!fileName.endsWith("/") && !fileName.endsWith("\\"))fileName += "/";
                fileName += df.format(date) + ".png";

                System.out.println("saving screenshot as " + fileName);
                ImageIO.write(screenshot, "png", new File(fileName));
            }

        }catch(IOException | AWTException | InterruptedException err)
        {
            JOptionPane.showInputDialog(frame, "Error while exporting line:\n" + err);
        }
    }
    public static void makeDefPopup(Line line)
    {
        JFrame frame = Main.ui.disp.getFrame();
        try
        {
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
