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
package options;

import language.dictionary.DefSource;
import main.Main;
import main.Utils;
import options.option.*;
import options.page.OptionPage;
import options.page.Page;
import options.page.PageGroup;
import options.page.UserDefPage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

/**
 *
 * @author Laurens Weyn
 */
public class OptionsUI extends JFrame
{
    private final JTree leftMenu;
    private final JPanel rightOptions;
    private final JScrollPane optionScroll;
    private final JScrollPane menuScroll;
    private final JPanel lowerButtons;
    public static final int SPACING = 1;
    public static int optionWidth = 0;
    private final PageGroup root;

    private final static String mouseoverConfig ="always=Always visible;mouseover=Only visible on mouseover;never=Never visible";
    private final static String exportDisplayConfig ="external=Total in export file;internal=Exported since starting Spark Reader";

    private final static String parserConfig ="full=Full;partial=Basic;none=Disable";
    private final static String deconConfig ="recursive=Recursive (better, slow);legacy=Legacy (faster, less accurate)";
    private final static String furiConfig ="sameForm=Conjugated as in text;original=Dictionary form;stripKana=Kanji readings only";
    private final static String textBackConfig ="normal=Simple background;dropshadow=Dropshadow;outline=Outline";

    public OptionsUI() throws HeadlessException
    {
        super("Spark Reader options");
        root = new PageGroup("Root", "");
        //TODO load this mess from an XML file or something instead of hardcoding it all


        OptionPage display = new OptionPage("General");
            display.add(new OptionLabel("Window properties:", null));
            display.add(new ToggleOption("useNativeUI", "Use System UI (requires restart)", "Themes the UI like the operating system. Looks a bit weird at the moment."));
            display.add(new NumberOption("windowWidth", "Window width (requires restart)", "I recommend setting this to the width of the window you plan to overlay this on."));
            display.add(new NumberOption("maxHeight", "Maximum height (requires restart)", "<html>Definitions will not be longer than this.<br>I recommend setting this to the height of the window you plan to overlay this on."));
            display.add(new ToggleOption("takeFocus", "Take focus when clicked (requires restart)", "If the game under the overlay is still receiving clicks, try turning this on."));
            display.add(new ToggleOption("showOnNewLine", "Restore window on new Japanese text", "If on, the window will automatically reappear if new Japanese text is detected."));
            display.add(new ToggleOption("hideOnOtherText", "Minimise window on new non-Japanese text", "If on, the window will automatically minimise if non-Japanese text is put into the clipboard."));
            display.add(new ToggleOption("startInTray", "Start in tray if there's no Japanese text on startup", "By default, the window will be visible after startup even if there is no text to display."));
            display.add(new OptionLabel("Other:", null));
            display.add(new ToggleOption("reduceSave", "Reduce file I/O", "<html>If ticked, writing to files is avoided until the program is closed or a lot of changes have been made.<br>Turning this on will improve performance, but if the program crashes some progress may be lost."));
            display.add(new ToggleOption("hookKeyboard", "Enable keyboard controls", "<html>If enabled, Spark Reader uses a tool to listen for keystrokes while the window is unfocused.<br>Requires restart."));
            display.add(new ToggleOption("forwardKeys", "Forward enter key if focused", "<html>Sends enter presses to the hooked window.<br>Use for games that need Spark Reader to stay focused.<br>Requires 'keyboard controls', 'take focus', and an active window hook to be useful."));
        root.add(display);
        PageGroup window = new PageGroup("Overlay", "Graphical settings related to the on-screen overlay window");

            OptionPage furigana = new OptionPage("Furigana");
            furigana.add(new RadioOption("furiMode", furiConfig, "Furigana type", null));
            furigana.add(new OptionLabel("Visibility:", null));
            furigana.add(new RadioOption("unknownFuriMode", mouseoverConfig, "Furigana display mode (unknown words)", null));
            furigana.add(new RadioOption("knownFuriMode", mouseoverConfig, "Furigana display mode (known words)", null));
            furigana.add(new OptionLabel("Theme:", null));
            furigana.add(new FontOption("furiFont", "Furigana font", "Also decides the size of the furigana bar."));
            furigana.add(new ColourOption("furiCol", "Main text colour", "The colour used for the furigana text."));
            furigana.add(new ColourOption("furiBackCol", "Main bar colour", "The colour used for the main window bar/first furigana bar."));
            furigana.add(new ColourOption("windowBackCol", "additional bar colour", "The colour used for additional furigana bars if there are multiple lines of text."));
            window.add(furigana);

            OptionPage mainUI = new OptionPage("Main text");

            mainUI.add(new ToggleOption("splitLines", "Retain newlines", "If disabled, all text is shown on one line, making the UI more compact"));
            //mainUI.add(new ToggleOption("reflowToFit", "Move text to next line if it doesn't fit", "If disabled, you can scroll through the text to see the rest of the line."));
            mainUI.add(new OptionLabel("Theme:", null));
            mainUI.add(new ToggleOption("unparsedWordsAltColor", "Color unparsed text like known words", "If enabled, segments with no definitions will be rendered as if they're marked as known."));
            mainUI.add(new ToggleOption("textFontUnhinted", "Don't hint main text", "If disabled, main text will not be hinted if antialiasing is enabled. Works well with outlines. Uses a different text rendering method."));
            mainUI.add(new ColourOption("textCol", "Main text colour", "The colour used for the main text."));
            mainUI.add(new ColourOption("knownTextCol", "Known text colour", "The colour used for main text if it's marked as known."));
            mainUI.add(new FontOption("textFont", "Main text font", "The font used for the captured Japanese text."));
            window.add(mainUI);

            OptionPage backs = new OptionPage("Background colours");

            backs.add(new RadioOption("textBackMode", textBackConfig, "Text background mode", null));
            backs.add(new NumberOption("textBackVariable", "Dropshadow/outline distance/thickness", "", NumberOption.NumberPreset.posOnly));

            backs.add(new OptionLabel("Text:", null));
            backs.add(new ColourOption("textBackCol", "Main text background colour", "The colour used for normal words."));
            backs.add(new ColourOption("knownTextBackCol", "Known text background colour", "Colour used for words marked as known."));
            
            backs.add(new ColourOption("clickedTextBackCol", "selected word colour", "Colour used for words while their definition is visible."));
            backs.add(new OptionLabel("Word splits:", null));
            backs.add(new ColourOption("markerCol", "Manual seperator colour", "These are the word spacers you place when you middle click on text."));
            backs.add(new ColourOption("noMarkerCol", "Auto seperator colour", "These are spaces assumed by the word splitter."));
            window.add(backs);

        root.add(window);
        OptionPage splitter = new OptionPage("Text splitter");
            splitter.add(new RadioOption("splitterMode", parserConfig, "Auto text splitter mode", null)); // If enabled, text will undergo a basic automatic parsing pass. Note that punctuation always causes segmentations.
            splitter.add(new RadioOption("deconMode", deconConfig, "Deconjugation mode", null)); // If enabled, text will undergo a basic automatic parsing pass. Note that punctuation always causes segmentations.
        root.add(splitter);

        OptionPage wordList = new OptionPage("Word lists");
        wordList.add(new OptionLabel("Known words:", null));
        wordList.add(new ToggleOption("enableKnown", "Enable known word tracking (requires restart)", "If unticked, all words are treated the same and the known word functions are hidden."));;
        wordList.add(new NumberOption("knownKanaLength", "Automatically mark Kana-only 'words' of this length or less as known", "<html>For people who don't want to mark every particle as known.<br>Set to 0 to disable. Set to some high value to only let Kanji words be unknown.", NumberOption.NumberPreset.posOnly));
        wordList.add(new ToggleOption("knowKatakana", "Automatically mark Katakana-only words as known", null));
        wordList.add(new ToggleOption("exportMarksKnown", "Mark exported words as known", "If ticked, exported words are also added to the known word list."));
        wordList.add(new ColourOption("knownTextBackCol", "known word colour", "Colour used for words marked as known."));
        wordList.add(new OptionLabel("Want to learn list:", null));
        wordList.add(new ToggleOption("enableWantList", "Enable want to learn tracking (requires restart)", "If ticked, you can import a list of words (e.g. a JLPT list) to help in seeing which words you should export as flashcards."));;
        //wordList.add(new ToggleOption("exportUnmarksWant", "Unmark exported words", "Consider words 'being learnt' as you export them."));;
        wordList.add(new ColourOption("wantTextBackCol", "known word colour", "Colour used for words you want to learn."));
        root.add(wordList);

        PageGroup defs = new PageGroup("Definitions", "Settings related to displaying and storing definitions");
            OptionPage defWindow = new OptionPage("Window");
            defWindow.add(new ToggleOption("defsShowUpwards", "Show definitions above text (requires restart)", "I'd recommend this on if you prefer to keep Spark Reader on the lower half of your screen."));
            defWindow.add(new ToggleOption("resetDefScroll", "Scroll to top when closing/changing definition", "If disabled, the scroll position is remembered until words are re-split."));
            defWindow.add(new NumberOption("defWidth", "Definition popup width", "Determines how wide the definition popup window is."));
            defWindow.add(new ToggleOption("hideDefOnMouseLeave", "Hide definition when mouse leaves the screen", "If unticked, the definition popup will remain visible until you manually close it."));
            defs.add(defWindow);
        OptionPage defFormat = new OptionPage("Format");
            defFormat.add(new ToggleOption("showAllKanji", "Show all possible Kanji for a word", "If unticked, only kana readings are shown."));
            defFormat.add(new ToggleOption("showDefID", "Show definition ID", "Shows the ID code of definitions. Mainly for debug purposes."));
            defFormat.add(new ToggleOption("splitDefLines", "split edict phrases into lines", "if unchecked, they will be seperated by semicolons on the same line."));
            defFormat.add(new OptionLabel("Edict tags:", null));
            defFormat.add(new ToggleOption("showAllTags", "show tag summary", "Show a summary of all tags at the top of the word. Not recommended if the options below are used."));
            defFormat.add(new ToggleOption("showTagsOnDef", "show tags on definition points", "Tags for each definition are shown on a line before the definition text."));
            defFormat.add(new ToggleOption("showTagsOnReading", "show tags for readings", "Reading specific tags are listed next to each reading."));
            defs.add(defFormat);
        OptionPage defTheme = new OptionPage("Theme");
            defTheme.add(new ColourOption("defBackCol", "Background colour", "Colour for overlay background."));
            defTheme.add(new FontOption("defFont", "Font", "Used for definition popup text."));
            defTheme.add(new ColourOption("defCol", "Definition colour", "Colour of text defining the word."));
            defTheme.add(new ColourOption("defTagCol", "Tag colour", "Word tag color (e.g. godan, noun, etc)"));
            defTheme.add(new ColourOption("defReadingCol", "Reading colour", "Colour of readings and dictionary form."));
            defTheme.add(new ColourOption("defKanjiCol", "Kanji colour", "(Heisig mode) for Kanji reference in popup."));
            defs.add(defTheme);
            OptionPage defSources = new OptionPage("Sources");
            defSources.add(new TextOption("dictionaryPath", "Dictionary folder", "Path to the folder containing dictionaries."));
            defSources.add(new ToggleOption("addKanjiAsDef", "Add Kanji to definitions", "<html>If you have a heisig Kanji file in the dictionary folder, this will also add those individual characters as 'definitions'<br>If disabled but kanji.txt is found, they will still display up on other definitions."));
            defSources.add(new OptionLabel("Priority:", "Higher numbers will appear at the top when displaying definitions."));
            defSources.add(new NumberOption("customSourcePriority", "Custom dictionary" ,"<html>The priority of the custom dictionary.<br>Set this higher than the rest to have your definitions appear at the top.", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("importedSourcePriority", "Imported character names" ,"<html>The priority of names imported from VNDB.", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("edictSourcePriority", "Edict" ,"<html>The priority of the 'stock' dictionary.<br>Default is 0 (neutral)", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("epwingSourcePriority", "Epwing (if available)" ,"<html>The priority of all epwing dictionaries.<br>Ignored if none are present.", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("kanjideckSourcePriority", "Kanji deck (if enabled)" ,"<html>The priority of kanji definitions.<br>Ignored if there is no kanji file or if disabled above.", NumberOption.NumberPreset.posNeg));
            defSources.add(new OptionLabel("Epwing:", "Only relevant if Ewping dictionaries are installed, otherwise ignored."));
            defSources.add(new TextOption("epwingStartBlacklist", "Blacklisted starting characters", "Some dictionaries will give a lot of information/examples. If they start with some special kind of bullet, put them here to make the popup a bit less crowded.", 5));
            defSources.add(new NumberOption("epwingBlacklistMinLines", "Apply blacklist when lines exceed this number", "Set to 0 to always apply. Useful to stop removing too much information.", NumberOption.NumberPreset.posOnly));
            defs.add(defSources);
            defs.add(new UserDefPage(DefSource.getSource("Custom")));

        root.add(defs);
        OptionPage xport = new OptionPage("Export");
            xport.add(new RadioOption("exportDisplay", exportDisplayConfig, "Export counter mode", "Requires restart to apply."));
            xport.add(new OptionLabel("Words:", null));
            xport.add(new ToggleOption("commentOnExport", "Ask for comment when exporting words", "<html>If ticked, you will be prompted for extra information when exporting a word."
                                                   + "<br>If unticked, this field is always left blank"));
            xport.add(new OptionLabel("Lines:", null));
            xport.add(new ToggleOption("commentOnExportLine", "Ask for comment when exporting lines", "<html>If ticked, you will be prompted for extra information when exporting a word."
                    + "<br>If unticked, this field is always left blank"));
            xport.add(new ToggleOption("exportImage", "Take screenshot along with the line", "Automatically takes a screenshot when a line is exported. The date code will match the one in the exported line field."));
            xport.add(new ToggleOption("fullscreenScreenshot", "Make a screenshot of the whole screen, not just the window", "<html>Spark Reader assumes it's placed on the top of the game window, with the width and max height being the window resolution.<br>Tick this if that is not the case or you want the whole screen captured."));
            xport.add(new TextOption("timeStampFormat", "Timestamp format", "Timestamp used on cards and images to connect them. (Uses Java DateFormat syntax)", 15));
        root.add(xport);

        leftMenu = new JTree(new OptionTree(root));
        
        leftMenu.setRootVisible(false);
        //expand all
        for (int i = 0; i < leftMenu.getRowCount(); i++)
        {
            leftMenu.expandRow(i);
        }
        menuScroll = new JScrollPane(leftMenu);
        rightOptions = new JPanel();
        rightOptions.setBorder(new EmptyBorder(3, 3, 3, 3));
        optionScroll = new JScrollPane(rightOptions);
        lowerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        optionScroll.getVerticalScrollBar().setUnitIncrement(12);
        menuScroll.getVerticalScrollBar().setUnitIncrement(12);
        lowerButtons.add(new JButton(new AbstractAction("OK")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveChanges();
                closeWindow();
            }
        }));
        lowerButtons.add(new JButton(new AbstractAction("Apply")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                saveChanges();
            }
        }));
        lowerButtons.add(new JButton(new AbstractAction("Cancel")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                revertChanges();
                closeWindow();

            }
        }));
    }

    private void revertChanges()
    {
        try
        {
            changedOptions.load();
        } catch (IOException err)
        {
            JOptionPane.showMessageDialog(Main.getParentFrame(), "Error restoring options:\n" + err);
            err.printStackTrace();
        }

        //reload old settings into all components:
        for(Page page:root.getPages())
        {
            page.update();
        }
    }

    private void saveChanges()
    {
        try
        {
            changedOptions.save();
            if(Main.options != null) Main.options.load();
        }catch(IOException err)
        {
            JOptionPane.showMessageDialog(Main.getParentFrame(), "Error applying changes:\n" + err);
            err.printStackTrace();
        }
        if(Main.ui != null)
        {
            Main.ui.render();//update settings
            Main.ui.render();//twice to ensure things render correctly
        }
    }

    private void closeWindow()
    {
        dispose();
    }

    public void initComponents()
    {
        setSize(720, 480);

        setLayout(new BorderLayout(SPACING, SPACING));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        rightOptions.setLayout(new BoxLayout(rightOptions, BoxLayout.Y_AXIS));
        rightOptions.add(new JLabel("Choose an option group on the left to change its settings"));
        rightOptions.add(new JLabel("Mouseover options to see what they do, or check the manual."));
        add(menuScroll, BorderLayout.WEST);
        add(optionScroll, BorderLayout.CENTER);
        add(lowerButtons, BorderLayout.SOUTH);
        
        addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                //resize components
                menuScroll.setPreferredSize(new Dimension(getWidth() / 4, getHeight() - lowerButtons.getHeight()));
                optionWidth = getWidth() - getWidth() / 4;
                optionScroll.setPreferredSize(new Dimension(optionWidth - SPACING * 2, getHeight() - lowerButtons.getHeight()));
            }
        });
        leftMenu.addTreeSelectionListener(e ->
        {
            Page page = (Page)e.getPath().getLastPathComponent();
            optionScroll.setViewportView(page.getComponent());
        });
    }
    private Options changedOptions;
    public static void showOptions(Options options) throws IOException
    {
        Options ops = new Options(Options.SETTINGS_FILE);
        UIOption.setTable(ops);
        OptionsUI o = new OptionsUI();
        o.changedOptions = ops;
        o.initComponents();
        o.setIconImage(ImageIO.read(o.getClass().getResourceAsStream("/ui/icon.gif")));
        Utils.centerWindow(o);
        o.setVisible(true);
    }
    public static void main(String[] args)throws Exception
    {
        showOptions(new Options(Options.SETTINGS_FILE));
        
        System.out.println("done");
        while(true)
        {
            Thread.sleep(10);
        }
    }
    
}
