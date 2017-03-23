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
    private final static String parserConfig ="full=Full;partial=Basic;none=None";

    public OptionsUI() throws HeadlessException
    {
        super("Spark Reader options");
        root = new PageGroup("Root", "");
        //TODO load this mess from an XML file or something instead of hardcoding it all

        //TODO have some consistency with the tip messages (capitalisation/full stop usage)
        
        OptionPage display = new OptionPage("General");
            display.add(new OptionLabel("Window properties:", null));
            display.add(new NumberOption("windowWidth", "Window width (requires restart)", "I recommend setting this to the width of the window you plan to overlay this on"));
            display.add(new NumberOption("maxHeight", "Maximum height (requires restart)", "<html>Definitions will not be longer than this.<br>I recommend setting this to the height of the window you plan to overlay this on"));        root.add(display);
            display.add(new ToggleOption("takeFocus", "Take focus when clicked (requires restart)", "If the game under the overlay is still receiving clicks, try turning this on"));
            display.add(new ToggleOption("showOnNewLine", "Restore window on new Japanese text", "If on, the window will automatically reappear if new Japanese text is detected."));
            display.add(new ToggleOption("hideOnOtherText", "Minimise window on new non-Japanese text", "If on, the window will automatically minimise if non-Japanese text is put into the clipboard."));
            display.add(new ToggleOption("startInTray", "Start in tray if there's no Japanese text on startup", "By default, the window will be visible after startup even if there is no text to display."));
            display.add(new OptionLabel("Other:", null));
            display.add(new ToggleOption("reduceSave", "Reduce file I/O", "<html>If ticked, writing to files is avoided until the program is closed or a lot of changes have been made.<br>Turning this on will improve performance, but if the program crashes some progress may be lost"));
        PageGroup window = new PageGroup("Overlay", "Graphical settings related to the on-screen overlay window");

            OptionPage furigana = new OptionPage("Furigana");
            furigana.add(new RadioOption("unknownFuriMode", mouseoverConfig, "Furigana mode (unknown words)", null));
            furigana.add(new RadioOption("knownFuriMode", mouseoverConfig, "Furigana mode (known words)", null));//TODO add tips
            furigana.add(new OptionLabel("Theme:", null));
            furigana.add(new FontOption("furiFont", "Furigana font", "Also decides the size of the furigana bar"));
            furigana.add(new ColourOption("furiCol", "Main text colour", "the colour used for the furigana text"));
            furigana.add(new ColourOption("furiBackCol", "Main bar colour", "the colour used for the main window bar/first furigana bar"));
            furigana.add(new ColourOption("windowBackCol", "additional bar colour", "the colour used for additional furigana bars if there are multiple lines of text"));
            window.add(furigana);

            OptionPage mainUI = new OptionPage("Main text");
                
            mainUI.add(new ToggleOption("splitLines", "Retain newlines", "If disabled, all text is shown on one line, making the UI more compact"));
            mainUI.add(new RadioOption("automaticallyParse", parserConfig, "Parser", null)); // If enabled, text will undergo a basic automatic parsing pass. Note that punctuation always causes segmentations.
            //mainUI.add(new ToggleOption("reflowToFit", "Move text to next line if it doesn't fit", "If disabled, you can scroll through the text to see the rest of the line."));
            mainUI.add(new OptionLabel("Theme:", null));
            mainUI.add(new ColourOption("textCol", "Main text colour", "the colour used for the main font"));
            mainUI.add(new FontOption("textFont", "Main text font", "The font used for the captured Japanese text"));
            window.add(mainUI);

            OptionPage backs = new OptionPage("Background colours");

            backs.add(new OptionLabel("Text:", null));
            backs.add(new ColourOption("textBackCol", "Main text background colour", "the colour used for normal words"));
            backs.add(new ColourOption("knownTextBackCol", "known word colour", "Colour used for words marked as known"));
            backs.add(new ColourOption("clickedTextBackCol", "selected word colour", "Colour used for words while their definition is visible"));
            backs.add(new OptionLabel("Word splits:", null));
            backs.add(new ColourOption("markerCol", "Manual seperator colour", "These are the word spacers you place when you middle click on text"));
            backs.add(new ColourOption("noMarkerCol", "Auto seperator colour", "These are spaces assumed by the word splitter"));
            window.add(backs);

        root.add(window);
        //OptionPage splitter = new OptionPage("Text splitter");
        //root.add(splitter);

        PageGroup defs = new PageGroup("Definitions", "Settings related to displaying and storing definitions");
            OptionPage defWindow = new OptionPage("Window");
            defWindow.add(new ToggleOption("defsShowUpwards", "Show definitions above text (requires restart)", "I'd reccomend this on if you prefer to keep Spark Reader on the lower half of your screen"));
            defWindow.add(new ToggleOption("resetDefScroll", "Scroll to top when closing/changing definition", "If disabled, the scroll position is remembered until words are re-split."));
            defWindow.add(new NumberOption("defWidth", "Definition popup width", "Determines how wide the definition popup window is"));
            defWindow.add(new ToggleOption("hideDefOnMouseLeave", "Hide definition when mouse leaves the screen", "If unticked, the definition popup will remain visible until you manually close it"));
            defWindow.add(new ToggleOption("showAllKanji", "Show all possible Kanji for a word", "If unticked, only kana readings are shown"));
            defWindow.add(new OptionLabel("Theme:", null));
            defWindow.add(new ColourOption("defBackCol", "Background colour", "colour for overlay background"));
            defWindow.add(new FontOption("defFont", "Font", "used for definition popup text"));
            defWindow.add(new ColourOption("defCol", "Definition colour", "Colour of text defining the word"));
            defWindow.add(new ColourOption("defTagCol", "Tag colour", "Word tag color (e.g. godan, noun, etc)"));
            defWindow.add(new ColourOption("defReadingCol", "Reading colour", "Colour of readings and dictionary form"));
            defWindow.add(new ColourOption("defKanjiCol", "Kanji colour", "(Heisig mode) for Kanji reference in popup"));
            defs.add(defWindow);
            OptionPage defSources = new OptionPage("Sources");
            defSources.add(new TextOption("dictionaryPath", "Dictionary folder", "path to the folder containing dictionaries."));
            defSources.add(new ToggleOption("addKanjiAsDef", "Add Kanji to definitions", "<html>If you have a heisig Kanji file in the dictionary folder, this will also add those individual characters as 'definitions'<br>If disabled but kanji.txt is found, they will still display up on other definitions."));
            defSources.add(new OptionLabel("Priority:", "Higher numbers will appear at the top when displaying definitions"));
            defSources.add(new NumberOption("customSourcePriority", "Custom dictionary" ,"<html>The priority of the custom dictionary.<br>Set this higher than the rest to have your definitions appear at the top.", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("edictSourcePriority", "Edict" ,"<html>The priority of the 'stock' dictionary.<br>Default is 0 (neutral)", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("epwingSourcePriority", "Epwing (if available)" ,"<html>The priority of all epwing dictionaries.<br>Ignored if none are present.", NumberOption.NumberPreset.posNeg));
            defSources.add(new NumberOption("kanjideckSourcePriority", "Kanji deck (if enabled)" ,"<html>The priority of kanji definitions.<br>Ignored if there is no kanji file or if disabled above.", NumberOption.NumberPreset.posNeg));
            defSources.add(new OptionLabel("Epwing:", "Only relevant if Ewping dictionaries are installed, otherwise ignored."));
            defSources.add(new TextOption("epwingStartBlacklist", "Blacklisted starting chracters", "Some dictionaries will give a lot of information/examples. If they start with some special kind of bullet, put them here to make the popup a bit less crowded.", 5));
            defSources.add(new NumberOption("epwingBlacklistMinLines", "Apply blacklist when lines exceed this number", "Set to 0 to always apply. Useful to stop removing too much information.", NumberOption.NumberPreset.posOnly));
            defs.add(defSources);
            defs.add(new UserDefPage(DefSource.getSource("Custom")));

        root.add(defs);
        OptionPage xport = new OptionPage("Import and Export");
            xport.add(new OptionLabel("Export words:", null));
            xport.add(new ToggleOption("commentOnExport", "Ask for comment when exporting words", "<html>If ticked, you will be prompted for extra information when exporting a word."
                                                   + "<br>If unticked, this field is always left blank"));
            xport.add(new ToggleOption("exportMarksKnown", "Automatically mark exported words as known", "If ticked, exported words are also added to the known word list"));
            xport.add(new OptionLabel("Export lines:", null));
            xport.add(new ToggleOption("commentOnExportLine", "Ask for comment when exporting lines", "<html>If ticked, you will be prompted for extra information when exporting a word."
                    + "<br>If unticked, this field is always left blank"));
            xport.add(new ToggleOption("exportImage", "Take screenshot along with the line", "Automatically takes a screenshot when a line is exported. The date code will match the one in the exported line field."));
            xport.add(new ToggleOption("fullscreenScreenshot", "Make a screenshot of the whole screen, not just the window", "<html>Spark Reader assumes it's placed on the top of the game window, with the width and max height being the window resolution.<br>Tick this if that is not the case or you want the whole screen captured"));
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
        optionScroll = new JScrollPane(rightOptions);
        lowerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        optionScroll.getVerticalScrollBar().setUnitIncrement(8);
        menuScroll.getVerticalScrollBar().setUnitIncrement(8);

        lowerButtons.add(new JButton(new AbstractAction("Revert changes")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    changedOptions.load();
                } catch (IOException ex)
                {
                    //TODO deal with this error
                }
                
                //reload old settings into all components:
                for(Page page:root.getPages())
                {
                    page.update();
                }
                
            }
        }));
        lowerButtons.add(new JButton(new AbstractAction("Apply changes")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    changedOptions.save();
                    if(Main.options != null) Main.options.load();
                }catch(IOException err)
                {
                    //TODO deal with this error
                }
                if(Main.ui != null)
                {
                    Main.ui.render();//update settings
                    Main.ui.render();//twice to ensure things render correctly
                }
            }
        }));
    }
    public void initComponents()
    {
        setSize(720, 480);
        //TODO centre window on screen
        
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
