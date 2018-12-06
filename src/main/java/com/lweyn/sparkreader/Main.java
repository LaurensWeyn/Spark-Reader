package com.lweyn.sparkreader;

import com.lweyn.sparkreader.hooker.ClipboardHook;
import com.lweyn.sparkreader.hooker.Hook;
import com.lweyn.sparkreader.hooker.Log;
import com.lweyn.sparkreader.language.deconjugator.WordScanner;
import com.lweyn.sparkreader.language.dictionary.Dictionary;
import com.lweyn.sparkreader.language.dictionary.Epwing.EPWINGDefinition;
import com.lweyn.sparkreader.language.splitter.WordSplitter;
import com.lweyn.sparkreader.network.MPController;
import com.lweyn.sparkreader.options.*;
import com.lweyn.sparkreader.ui.Page;
import com.lweyn.sparkreader.ui.UI;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * Main entry point and holds global program objects and methods.
 */
public class Main
{

    private static Logger logger = Logger.getLogger(Main.class);

    public static final double VERSION_NUM = 0.8;
    public static final String VERSION = "Beta " + VERSION_NUM;
    public static final String ABOUT = "Spark Reader " + VERSION + "\n\n" +
            "Lead developer: Laurens Weyn\n" +
            "Contributions: Alexander Nadeau\n\n";//TODO mention EDICT, libraries, links

    public static UI ui;
    /**
     * Used for multiplayer. Null if not connected/hosting
     */
    public static MPController mpManager;
    public static Thread mpThread;
    /**
     * Text line history
     */
    public static Log log;
    /**
     * The current line of text
     */
    public static Page currPage;
    /**
     * Source used for new lines of text
     */
    public static Hook hook;
    public static Dictionary dict;
    public static Known known;
    public static WantToLearn wantToLearn;
    public static PrefDef prefDef;
    public static BlacklistDef blacklistDef;
    /**
     * The currently active configuration
     */
    public static Options options;
    public static WordSplitter splitter;

    /**
     * Machine readable only persistence data (stats, last window hooked to etc.)
     */
    public static Persist persist;


    public static void main(String[] args)throws Exception
    {
        logger.info(VERSION);
        options = new Options(Options.SETTINGS_FILE);
        persist = Persist.load(options.getFile("persistPath"));
        try
        {
            if(options.getOptionBool("useOpenGL"))System.setProperty("sun.java2d.opengl","True");
            if(options.getOptionBool("useNativeUI"))javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            //fall back to default if this fails
        }
        initLoadingScreen();
        //try
        {
            //load in configuration
            known = new Known(options.getOptionBool("enableKnown")? options.getFile("knownWordsPath"):null);
            wantToLearn = new WantToLearn(known);
            prefDef = new PrefDef(options.getFile("preferredDefsPath"));
            blacklistDef = new BlacklistDef(options.getFile("blacklistDefsPath"));

            hook = new ClipboardHook();//default hook
            log = new Log(50);//new log

            loadDictionaries();
            splitter = new WordSplitter(dict);

        }
        //catch(Exception err)
        //{
        //    JOptionPane.showMessageDialog(null, "Error starting Spark Reader:\n" + err, "Error", JOptionPane.ERROR_MESSAGE);
        //    System.exit(1);
        //}
        logger.info("Init done");
        persist.startupCount++;
        UI.runUI();
    }
    private static void loadDictionaries()throws IOException
    {
        EPWINGDefinition.loadBlacklist();

        dict = new Dictionary(new File(Main.options.getOption("dictionaryPath")), persist.lastDictHashSize);
        logger.info("Loaded " + Dictionary.getLoadedWordCount() + " words in total, HashTable " + dict.getHashSize());
        persist.lastDictSize = Dictionary.getLoadedWordCount();//keep this in mind for next startup estimate
        persist.lastDictHashSize = dict.getHashSize();
        WordScanner.init();
    }

    /**
     * A call to this method cleanly exits the program, saving changes if possible.
     */
    public static void exit()
    {
        JFrame frame = getParentFrame();
        try
        {
            if(known != null)Main.known.save();
            if(prefDef != null)Main.prefDef.save();
            if(blacklistDef != null)Main.blacklistDef.save();
            persist.save();
        }catch(IOException err)
        {
            JOptionPane.showMessageDialog(getParentFrame(), "Error while saving changes:\n" + err);
            logger.error("Error while saving changes:\n" + err, err);
        }
        if(frame != null)frame.setVisible(false);
        System.exit(0);
    }

    /**
     * Gets the GUI frame if available.
     * For use in functions that need a parent frame, like {@link JOptionPane}
     */
    public static JFrame getParentFrame()
    {
        //TODO change usages of com.lweyn.sparkreader.ui.disp.getFrame() to use this one instead

        if(ui != null && ui.disp != null)return ui.disp.getFrame();
        return null;
    }

    //loading screen-specific variables
    private static JDialog loadScreen;
    private static JProgressBar loadProgress;
    private static JLabel loadStatus;
    private static boolean doneLoading = false;
    private static volatile Timer loadUpdater;

    public static void doneLoading()
    {
        doneLoading = true;
    }

    private static void initLoadingScreen()throws IOException
    {
        loadScreen = new JDialog((JFrame) null, "Starting Spark Reader");
        loadProgress = new JProgressBar(0, persist.lastDictSize);
        loadStatus = new JLabel("Loading dictionaries...");
        JPanel mainPanel = new JPanel(new BorderLayout());
        loadScreen.setContentPane(mainPanel);
        mainPanel.add(loadStatus, BorderLayout.WEST);
        mainPanel.add(new JLabel(VERSION), BorderLayout.EAST);
        mainPanel.add(loadProgress, BorderLayout.SOUTH);
        loadScreen.setSize(300,100);
        Utils.centerWindow(loadScreen);
        loadScreen.setIconImage(ImageIO.read(Main.class.getResourceAsStream("/com/lweyn/sparkreader/ui/icon.gif")));
        loadScreen.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                if(!doneLoading)
                {
                    logger.info("Startup aborted");
                    exit();
                }
            }
        });
        loadScreen.setVisible(true);

        loadUpdater = new Timer(50, e ->
        {
            if(dict == null)
            {
                //still loading dictionaries
                loadProgress.setValue(Dictionary.getLoadedWordCount());
            }
            else
            {
                loadProgress.setValue(loadProgress.getMaximum());
                loadStatus.setText("Loading main UI...");
            }
            if(doneLoading)
            {
                loadUpdater.stop();
                loadScreen.setVisible(false);
                loadScreen.dispose();
            }
        });
        loadUpdater.setRepeats(true);
        loadUpdater.start();
    }
}
