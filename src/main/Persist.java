package main;


import ui.Line;
import ui.Page;

import java.awt.*;
import java.io.*;
import java.util.Date;

/**
 * Persistence object. Stored in binary(Java serial) form.
 */
public class Persist implements Serializable
{
    //useless statistics
    public Date firstStartup;
    public long startupCount;
    public long linesCaught;
    public long wordsEncountered;
    public long exportCount;
    public long manualSpacesPlaced;

    //useful persist data
    public int lastDictSize;//to estimate startup %
    public int lastWindowWidth;//to use when live resizing works
    public Point lastWindowPos;
    public String lastWindowHookName;

    //non persist data (but persist specific settings)
    private static long serialVersionUID = 1L;
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
        catch(FileNotFoundException ignored)
        {
            System.out.println("No persistence file found, creating a new one");
        }
        catch(Exception e)
        {
            System.out.println("error reading persistence data");
            e.printStackTrace();
        }
        return new Persist();
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
            System.out.println("Writing persistence");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
            lastSyncTime = System.currentTimeMillis();
        }
        catch(Exception ex)
        {
            System.out.println("error writing persistence data");
            ex.printStackTrace();
        }
    }

    public void statsOnPage(Page currPage)
    {
        for(int i = 0; i < currPage.getLineCount(); i++)
        {
            Line line = currPage.getLine(i);
            wordsEncountered += line.getWords().size();
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
