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
package ui;

import hooker.WindowHook;
import language.splitter.FoundWord;
import main.Main;
import ui.input.JNativeKeyHandler;
import ui.input.KeyHandler;
import ui.input.MouseHandler;
import ui.input.SwingMouseHandler;
import ui.menubar.Menubar;
import ui.menubar.MenubarBuilder;

import javax.swing.*;
import java.awt.*;

import static main.Main.*;

/**
 * Main Spark Reader UI
 * @author Laurens Weyn
 */
public class UI
{



    public static boolean hidden = false;


    public static String mpStatusText;


    public Overlay disp;
    public Tray tray;

    public static int mainFontSize = 1;//1 default to stop division by 0
    public int xOffset = 0;

    // window sizing
    public static int currentWidth = -1;
    public static int currentMaxHeight = -1;
    // for scrolling
    public static int widestLineWidth = -1;
    
    public FoundWord selectedWord = null;
    
    public static int furiganaStartY = 0;
    public static int textStartY = 0;
    public static int textEndY = 0;
    public static int defStartY = 0;//now auto-recalculated in render
    
    public static int lineHeight = 1;//default 1 to stop division by 0
    public static int textHeight = 0;
    public static int furiHeight = 0;
    
    //public static int exitStartX;
    public static int minimiseStartX;

    public static final Color CLEAR = new Color(0, 0, 0, 0);

    public static String userComment;

    public static int optionsButtonWidth = 12;
    public static boolean renderBackground = true;
    public static boolean tempIgnoreMouseExit = false;

    public MouseHandler mouseHandler;
    public KeyHandler keyHandler;
    
    public static java.util.List<Integer> stickToWindow = null;

    public static boolean showMenubar = false;
    public Menubar menubar;

    public UI()
    {
        currPage = new Page();
        
        disp = new Overlay(getDesiredWindowWidth(), options.getOptionInt("maxHeight"));
        
        menubar = MenubarBuilder.buildMenu();
    }
    
    private int getDesiredWindowWidth()
    {
        int windowWidth;
        if(options.getOptionBool("defConstrainPosition"))
            windowWidth = Math.max(options.getOptionInt("windowWidth"), options.getOptionInt("defWidth"));
        else
            windowWidth = options.getOptionInt("windowWidth") + options.getOptionInt("defWidth");
        return windowWidth;
    }

    private void registerListeners()
    {
        mouseHandler = new SwingMouseHandler(this);
        mouseHandler.addListeners();

        if(options.getOptionBool("hookKeyboard"))
        {
            keyHandler = new JNativeKeyHandler(this);
            keyHandler.addListeners();
        }

        tray = new Tray(this);//manages tray icon
    }

    public void calculateSizes(Graphics2D g)
    {
        textHeight = g.getFontMetrics(options.getFont("textFont")).getHeight();
        furiHeight = g.getFontMetrics(options.getFont("furiFont")).getHeight();
        lineHeight = textHeight + furiHeight;

        options.getFont(g, "textFont");
        mainFontSize = g.getFontMetrics().charWidth('べ');
        if(options.getOptionBool("defsShowUpwards"))
        {
            furiganaStartY = options.getOptionInt("maxHeight") - lineHeight * Math.max(options.getOptionInt("expectedLineCount"), currPage.getLineCount());
            defStartY = furiganaStartY - 2;
            textStartY = furiHeight + furiganaStartY;
            textEndY = textStartY + lineHeight * currPage.getLineCount();
        }
        else
        {
            textStartY = furiHeight + furiganaStartY;
            textEndY = textStartY + lineHeight * currPage.getLineCount() - furiHeight;
            defStartY = textEndY;
        }
        minimiseStartX = options.getOptionInt("windowWidth") - optionsButtonWidth - 1;
        if(currentWidth != options.getOptionInt("windowWidth") || currentMaxHeight != options.getOptionInt("maxHeight"))
        {
            currentWidth = options.getOptionInt("windowWidth");
            currentMaxHeight = options.getOptionInt("maxHeight");
            disp.setSize(getDesiredWindowWidth(), currentMaxHeight);
        }
    }
    public void render()
    {
        Graphics2D g = disp.getGraphics();
        g.setBackground(CLEAR);

        disp.getFrame().setFocusableWindowState(false); // prevent setVisible(true) from focusing window when window was previously not visible
        disp.getFrame().setVisible(!hidden);
        disp.getFrame().setFocusableWindowState(Main.options.getOptionBool("takeFocus")); // restore setting

        calculateSizes(g);

        if(!hidden)
        {
            Color generalColor;
            if(options.getOptionBool("unparsedWordsAltColor"))
                generalColor = options.getColor("knownTextBackCol");
            else
                generalColor = options.getColor("textBackCol");
            g.setColor(generalColor);
            
            //render background unless it's supposed to be for dropshadows only
            if(renderBackground && !(options.getOption("textBackMode").equals("dropshadow") || options.getOption("textBackMode").equals("outline")))
                g.fillRect(0, textStartY - 1, currentWidth, currPage.getLineCount() * lineHeight - furiHeight + 1); // general background beside short text
            
            options.getFont(g, "furiFont");
            int i = 0;
            while(i < currPage.getLineCount())
            {
                // furigana bar for lines other than the first
                if (i != 0)
                {
                    g.setColor(options.getColor("windowBackCol"));
                    g.clearRect(0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, currentWidth, furiHeight);
                    g.fillRect (0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, currentWidth, furiHeight);
                }
                i++;
            }
            
            //render furigana/window bar
            if(showMenubar)menubar.render(g);
            else
            {
                options.getFont(g, "furiFont");

                g.setColor(options.getColor("furiBackCol"));
                g.fillRect(0, furiganaStartY, currentWidth, furiHeight);
            }
            
            int yOff = 0;
            //render lines
            widestLineWidth = -1;
            for(Line line:currPage)
            {
                int lineWidth = line.render(g, xOffset, yOff);
                widestLineWidth = Math.max(widestLineWidth, lineWidth);
                yOff += lineHeight;
            }

            //render MP text (if running, there's text and no def's open)
            //TODO ensure this works for reversed text
            if(mpThread != null && mpStatusText != null && selectedWord == null)
            {
                options.getFont(g, "furiFont");
                g.setColor(options.getColor("furiBackCol"));
                g.fillRect(0, defStartY, g.getFontMetrics().stringWidth(mpStatusText), g.getFontMetrics().getHeight());
                g.setColor(options.getColor("furiCol"));
                g.drawString(mpStatusText, 0, defStartY + g.getFontMetrics().getAscent());

            }
            //TODO Exit icon? ╳
            //minimise button
            options.getFont(g, "furiFont");
            String symbol = "－";
            g.setColor(Color.white);
            g.drawString(symbol, minimiseStartX, g.getFontMetrics().getAscent() + furiganaStartY - 1);
        }
        disp.refresh();
    }
    public void updateText(String newText)
    {
        currPage.setText(newText);
    }

    /**
     * Moves a word from an old line to a new one, maintaining word position and relevant markers
     * @param oldLine line being copied from
     * @param newLine line being copied to
     * @param word word to copy over
     */
    private void addWord(Line oldLine, Line newLine, FoundWord word)
    {
        int newStartX = newLine.calcLength();
        if(oldLine.getMarkers().contains(word.startX()))newLine.getMarkers().add(newStartX);
        word.setStartX(newStartX);
        newLine.addWord(word);
    }

    public static void runUI()throws Exception
    {
        ui = new UI();
        ui.registerListeners();
        //random sample text to copy for testing
        System.out.println("ひかり「暁斗たちと遊んでて夕飯のギョーザを食べ損ねて、\n悔しかったから、星座にしてやったんだよね」");
        
        //center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ui.disp.getFrame().setLocation(screenSize.width / 2 - options.getOptionInt("windowWidth") / 2,
                screenSize.height / 2 - (options.getOptionInt("maxHeight")) / 2);
        
        ui.render();//TODO make this not need 2 render calls to properly align stuff
        ui.render();

        Main.doneLoading();
        //update loop
        Timer mainLoop = new Timer(Main.options.getOptionInt("uiThrottleMilliseconds"), e ->
        {
            if(stickToWindow != null)
            {
                stickToWindow = WindowHook.hook.getCoord();
                ui.disp.getFrame().setLocation(stickToWindow.get(0), stickToWindow.get(1));
            }
            //check clipboard
            String clip = hook.check();
            if(clip != null)
            {
                //if we're here, we have a new line of text
                Main.persist.linesCaught++;
                Main.persist.statsOnPage(Main.currPage);//take stats of 'completed' page before moving to the next one
                userComment = null;

                if(options.getOptionBool("showOnNewLine") && !tempIgnoreMouseExit)
                {
                    hidden = false;//force visibility on new line if needed
                    ui.tray.hideTray();
                }
                
                //clip = Japanese.toFullWidth(clip);

                log.addLine(clip);//add line to log
                if(!options.getOptionBool("splitLines"))clip = clip.replace("\n", "");//all on one line if not splitting
                currPage.clearMarkers();
                ui.updateText(clip);//reflow text on defaults
                ui.xOffset = 0;//scroll back to front
                ui.render();
                
            }
            //check MP systems
            if(mpManager != null)
            {
                String newText = mpManager.getStatusText();
                if(!newText.equals(mpStatusText))
                {
                    mpStatusText = newText;
                    ui.render();//refresh mp status
                }
                if(!mpManager.running)
                {
                    mpManager = null;
                    mpThread = null;
                }
            }
            else mpStatusText = null;

            //UI has become inaccessible (most likely closed via alt+f4)
            if(!ui.disp.getFrame().isVisible() && !ui.tray.isShowing())
            {
                System.out.println("UI inaccessible");
                Main.exit();
            }

            //check persist update
            if(!hidden)persist.checkForSave();
        });
        mainLoop.setRepeats(true);
        mainLoop.start();
        
    }


    
    public void minimise()
    {
        hidden = true;
        tray.showTray();
        render();
        persist.save();
    }

    public void restore()
    {
        hidden = false;
        tray.hideTray();
        render();
    }

    public void boundXOff()
    {
        if(options.getOptionBool("reflowToFit"))
        {
            //do not allow scrolling when text always fits
            xOffset = 0;
            return;
        }
        if(xOffset > 0)xOffset = 0;
        int maxX = widestLineWidth - currentWidth;
        if(-xOffset > maxX)xOffset = Math.min(-maxX, 0);
    }

    public int getLineIndex(Point pos)
    {
        return (pos.y - textStartY)/ lineHeight;
    }
}
