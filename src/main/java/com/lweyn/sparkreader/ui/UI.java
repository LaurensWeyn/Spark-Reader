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
package com.lweyn.sparkreader.ui;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.hooker.WindowHook;
import com.lweyn.sparkreader.language.splitter.FoundWord;
import com.lweyn.sparkreader.ui.input.JNativeKeyHandler;
import com.lweyn.sparkreader.ui.input.KeyHandler;
import com.lweyn.sparkreader.ui.input.MouseHandler;
import com.lweyn.sparkreader.ui.input.SwingMouseHandler;
import com.lweyn.sparkreader.ui.menubar.Menubar;
import com.lweyn.sparkreader.ui.menubar.MenubarBuilder;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

import static com.lweyn.sparkreader.Main.currPage;

/**
 * Main Spark Reader UI
 * @author Laurens Weyn
 */
public class UI
{
    private static Logger logger = Logger.getLogger(UI.class);


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
    
    public DisplayedWord selectedWord = null;
    
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
        
        disp = new Overlay(getDesiredWindowWidth(), Main.options.getOptionInt("maxHeight"));
        
        menubar = MenubarBuilder.buildMenu();
    }
    
    private int getDesiredWindowWidth()
    {
        int windowWidth;
        if(Main.options.getOptionBool("defConstrainPosition"))
            windowWidth = Math.max(Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("defWidth"));
        else
            windowWidth = Main.options.getOptionInt("windowWidth") + Main.options.getOptionInt("defWidth");
        return windowWidth;
    }

    private void registerListeners()
    {
        mouseHandler = new SwingMouseHandler(this);
        mouseHandler.addListeners();

        if(Main.options.getOptionBool("hookKeyboard"))
        {
            keyHandler = new JNativeKeyHandler(this);
            keyHandler.addListeners();
        }

        tray = new Tray(this);//manages tray icon
    }

    public void calculateSizes(Graphics2D g)
    {
        textHeight = g.getFontMetrics(Main.options.getFont("textFont")).getHeight();
        furiHeight = g.getFontMetrics(Main.options.getFont("furiFont")).getHeight();
        lineHeight = textHeight + furiHeight;

        Main.options.getFont(g, "textFont");
        mainFontSize = g.getFontMetrics().charWidth('べ');
        if(Main.options.getOptionBool("defsShowUpwards"))
        {
            furiganaStartY = Main.options.getOptionInt("maxHeight") - lineHeight * Math.max(Main.options.getOptionInt("expectedLineCount"), currPage.getLineCount());
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
        minimiseStartX = Main.options.getOptionInt("windowWidth") - optionsButtonWidth - 1;
        if(currentWidth != Main.options.getOptionInt("windowWidth") || currentMaxHeight != Main.options.getOptionInt("maxHeight"))
        {
            currentWidth = Main.options.getOptionInt("windowWidth");
            currentMaxHeight = Main.options.getOptionInt("maxHeight");
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
            if(Main.options.getOptionBool("unparsedWordsAltColor"))
                generalColor = Main.options.getColor("knownTextBackCol");
            else
                generalColor = Main.options.getColor("textBackCol");
            g.setColor(generalColor);
            
            //render background unless it's supposed to be for dropshadows only
            if(renderBackground && !(Main.options.getOption("textBackMode").equals("dropshadow") || Main.options.getOption("textBackMode").equals("outline")))
                g.fillRect(0, textStartY - 1, currentWidth, currPage.getLineCount() * lineHeight - furiHeight + 1); // general background beside short text
            
            Main.options.getFont(g, "furiFont");
            int i = 0;
            while(i < currPage.getLineCount())
            {
                // furigana bar for lines other than the first
                if (i != 0)
                {
                    g.setColor(Main.options.getColor("windowBackCol"));
                    g.clearRect(0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, currentWidth, furiHeight);
                    g.fillRect (0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, currentWidth, furiHeight);
                }
                i++;
            }
            
            //render furigana/window bar
            if(showMenubar)menubar.render(g);
            else
            {
                Main.options.getFont(g, "furiFont");

                g.setColor(Main.options.getColor("furiBackCol"));
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
            if(Main.mpThread != null && mpStatusText != null && selectedWord == null)
            {
                Main.options.getFont(g, "furiFont");
                g.setColor(Main.options.getColor("furiBackCol"));
                g.fillRect(0, defStartY, g.getFontMetrics().stringWidth(mpStatusText), g.getFontMetrics().getHeight());
                g.setColor(Main.options.getColor("furiCol"));
                g.drawString(mpStatusText, 0, defStartY + g.getFontMetrics().getAscent());

            }
            //TODO Exit icon? ╳
            //minimise button
            Main.options.getFont(g, "furiFont");
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
    private void addWord(Line oldLine, Line newLine, DisplayedWord word)
    {
        int newStartX = newLine.calcCharLength();
        if(oldLine.getMarkers().contains(word.startX()))newLine.getMarkers().add(newStartX);
        word.setStartX(newStartX);
        newLine.addWord(word);
    }

    public static void runUI()throws Exception
    {
        Main.ui = new UI();
        Main.ui.registerListeners();
        //random sample text to copy for testing
        logger.debug("ひかり「暁斗たちと遊んでて夕飯のギョーザを食べ損ねて、\n悔しかったから、星座にしてやったんだよね」");
        
        //center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Main.ui.disp.getFrame().setLocation(screenSize.width / 2 - Main.options.getOptionInt("windowWidth") / 2,
                screenSize.height / 2 - (Main.options.getOptionInt("maxHeight")) / 2);
        
        Main.ui.render();//TODO make this not need 2 render calls to properly align stuff
        Main.ui.render();

        Main.doneLoading();
        //update loop
        Timer mainLoop = new Timer(Main.options.getOptionInt("uiThrottleMilliseconds"), e ->
        {
            if(stickToWindow != null)
            {
                stickToWindow = WindowHook.hook.getCoord();
                Main.ui.disp.getFrame().setLocation(stickToWindow.get(0), stickToWindow.get(1));
            }
            //check clipboard
            String clip = Main.hook.check();
            if(clip != null)
            {
                //if we're here, we have a new line of text
                Main.persist.linesCaught++;
                Main.persist.statsOnPage(Main.currPage);//take stats of 'completed' page before moving to the next one
                userComment = null;

                if(Main.options.getOptionBool("showOnNewLine") && !tempIgnoreMouseExit)
                {
                    hidden = false;//force visibility on new line if needed
                    Main.ui.tray.hideTray();
                }

                //preprocessing
                //clip = Japanese.toFullWidth(clip);
                clip = clip.replace('●', '○')
                           .replace('◯', '○');//Needed this for a scene - don't judge me

                Main.log.addLine(clip);//add line to log
                if(!Main.options.getOptionBool("splitLines"))clip = clip.replace("\n", "");//all on one line if not splitting
                currPage.clearMarkers();
                Main.ui.updateText(clip);//reflow text on defaults
                Main.ui.xOffset = 0;//scroll back to front
                Main.ui.render();
                
            }
            //check MP systems
            if(Main.mpManager != null)
            {
                String newText = Main.mpManager.getStatusText();
                if(!newText.equals(mpStatusText))
                {
                    mpStatusText = newText;
                    Main.ui.render();//refresh mp status
                }
                if(!Main.mpManager.running)
                {
                    Main.mpManager = null;
                    Main.mpThread = null;
                }
            }
            else mpStatusText = null;

            //UI has become inaccessible (most likely closed via alt+f4)
            if(!Main.ui.disp.getFrame().isVisible() && !Main.ui.tray.isShowing())
            {
                logger.warn("UI has become inaccessible, closing program");
                Main.exit();
            }

            //check persist update
            if(!hidden)Main.persist.checkForSave();
        });
        mainLoop.setRepeats(true);
        mainLoop.start();
        
    }


    
    public void minimise()
    {
        hidden = true;
        tray.showTray();
        render();
        Main.persist.save();
    }

    public void restore()
    {
        hidden = false;
        tray.hideTray();
        render();
    }

    public void boundXOff()
    {
        if(Main.options.getOptionBool("reflowToFit"))
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
