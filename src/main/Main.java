package main;

import hooker.ClipboardHook;
import hooker.Hook;
import hooker.Log;
import language.dictionary.Dictionary;
import language.dictionary.EPWINGDefinition;
import language.splitter.WordSplitter;
import multiplayer.MPController;
import options.Known;
import options.Options;
import options.PrefDef;
import ui.UI;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by Laurens on 2/19/2017.
 */
public class Main
{
    public static final String VERSION = "Beta 0.6";

    public static UI ui;
    /**
     * Used for multiplayer. Null if not connected/hosting
     */
    public static MPController mpManager;
    public static Thread mpThread;
    /**
     * The currently displayed line of text
     */
    public static String text = "";
    /**
     * Text line history
     */
    public static Log log;
    /**
     * Source used for new lines of text
     */
    public static Hook hook;
    public static Dictionary dict;
    public static Known known;
    public static PrefDef prefDef;
    /**
     * The currently active configuration
     */
    public static Options options;
    public static WordSplitter splitter;

    public static void main(String[] args)throws Exception
    {
        System.out.println(VERSION);
        try
        {
            //load in configuration
            options = new Options(Options.SETTINGS_FILE);
            known = new Known(options.getFile("knownWordsPath"));
            prefDef = new PrefDef(options.getFile("preferredDefsPath"));

            hook = new ClipboardHook();//default hook
            log = new Log(50);//new log

            loadDictionaries();
            splitter = new WordSplitter(dict);

        }catch(Exception err)
        {
            JOptionPane.showMessageDialog(null, "Error starting Spark Reader:\n" + err, "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        UI.runUI();
    }
    private static void loadDictionaries()throws IOException
    {
        EPWINGDefinition.loadBlacklist();

        //TODO display some sort of progress bar during this operation
        dict = new Dictionary(new File(Main.options.getOption("dictionaryPath")));
        System.out.println("loaded dictionaries");

    }
}
