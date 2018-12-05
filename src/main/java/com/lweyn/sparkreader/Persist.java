package com.lweyn.sparkreader;


import com.lweyn.sparkreader.ui.Line;
import com.lweyn.sparkreader.ui.Page;
import com.lweyn.sparkreader.ui.UI;
import com.lweyn.sparkreader.ui.menubar.MenubarBuilder;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Persistence object. Stored in binary(Java serial) form.
 */
public class Persist implements Serializable
{
    private static Logger logger = Logger.getLogger(Persist.class);

    public static int exportedThisSession = 0;
    public static int exportedBeforeSession = -1;


    //useless statistics
    public Date firstStartup;
    public long startupCount;
    public long linesCaught;
    public long wordsEncountered;
    public long exportCount;
    public long manualSpacesPlaced;

    //useful persist data
    public int lastDictSize;//to estimate startup %
    public int lastDictHashSize;//to presize hashtable
    public int lastWindowWidth;//to use when live resizing works
    public Point lastWindowPos;
    public String lastWindowHookName;

    //non persist data (but persist specific settings)
    private static final long serialVersionUID = 4155401967378296134L;
    private transient long lastSyncTime;
    private static long syncPeriod = 600000L;//10 minutes

    public Persist()
    {
        firstStartup = new Date();
        lastDictSize = 377089;//good first guess
    }

    public static Persist load(File file)
    {
        try
        {
            FileInputStream streamIn = new FileInputStream(file);
            ObjectInputStream objectinputstream = new ObjectInputStream(streamIn);
            Persist loaded = (Persist) objectinputstream.readObject();
            objectinputstream.close();
            streamIn.close();
            loaded.lastSyncTime = System.currentTimeMillis();
            return loaded;
        }
        catch(FileNotFoundException | ClassNotFoundException ignored)
        {
            logger.warn("No persistence file found, creating a new one");
        }
        catch(Exception e)
        {
            logger.error("error reading persistence data; rebuilding", e);
        }
        return new Persist();
    }

    public static int getLineExportCount()
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

                logger.info("Saving screenshot as " + fileName);
                ImageIO.write(screenshot, "png", new File(fileName));
            }

        }catch(IOException | AWTException | InterruptedException err)
        {
            JOptionPane.showInputDialog(frame, "Error while exporting line:\n" + err);
        }

        MenubarBuilder.updateExportCount();
    }

    public void checkForSave()
    {
        if(System.currentTimeMillis() - lastSyncTime > syncPeriod)save(Main.options.getFile("persistPath"));
    }
    public void save()
    {
        save(Main.options.getFile("persistPath"));
    }
    public void save(File file)
    {
        try
        {
            logger.info("Writing to persistence file");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
            lastSyncTime = System.currentTimeMillis();
        }
        catch(Exception ex)
        {
            logger.error("Error writing persistence data", ex);
        }
    }

    public void statsOnPage(Page currPage)
    {
        for(int i = 0; i < currPage.getLineCount(); i++)
        {
            Line line = currPage.getLine(i);
            wordsEncountered += line.getDisplayedWords().size();
            //more stat collecting stuff would go here
        }
    }

    @Override
    public String toString()
    {
        return  "First startup: " + firstStartup +
                "\nTotal startup count: " + startupCount +
                "\nTotal lines of text processed: " + linesCaught +
                "\nTotal words encountered: " + wordsEncountered +
                "\nTotal word and line exports: " + exportCount +
                "\nManual splits placed: " + manualSpacesPlaced +
                "";
    }
}
