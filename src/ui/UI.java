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

import language.dictionary.Japanese;
import language.splitter.FoundWord;
import main.Main;
import ui.input.JNativeKeyHandler;
import ui.input.KeyHandler;
import ui.input.MouseHandler;
import ui.input.SwingMouseHandler;

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

    
    public FoundWord selectedWord = null;
    
    public static int furiganaStartY = 0;
    public static int textStartY = 0;
    public static int textEndY = 0;
    public static int defStartY = 0;//now auto-recalculated in render
    
    public static int lineHeight = 1;//default 1 to stop division by 0
    public static int textHeight = 0;
    public static int furiHeight = 0;
    
    public static int buttonStartX;
    
    public static final Color CLEAR = new Color(0, 0, 0, 0);

    public static String userComment;

    public static int optionsButtonWidth = 10;
    public static boolean renderBackground = true;
    public static boolean tempIgnoreMouseExit = false;

    public MouseHandler mouseHandler;
    public KeyHandler keyHandler;

    public UI()
    {
        currPage = new Page();
        disp = new Overlay(options.getOptionInt("windowWidth") + options.getOptionInt("defWidth"),
                options.getOptionInt("maxHeight"));
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
        buttonStartX = options.getOptionInt("windowWidth") - optionsButtonWidth - 1;
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
            //render background unless it's supposed to be for dropshadows only
            if(renderBackground && !(options.getOption("textBackMode").equals("dropshadow") || options.getOption("textBackMode").equals("outline")))
            {
                g.setColor(options.getColor("textBackCol"));
                g.fillRect(0, textStartY - 1, options.getOptionInt("windowWidth"), currPage.getLineCount() * lineHeight - furiHeight + 1);
                g.setColor(options.getColor("windowBackCol"));
                int i = 1;
                while(i < currPage.getLineCount())
                {
                    g.clearRect(0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, options.getOptionInt("windowWidth"), furiHeight - 1);
                    g.fillRect (0, (textStartY - 1) + (i * lineHeight) - furiHeight + 1, options.getOptionInt("windowWidth"), furiHeight - 1);
                    i++;
                }
            }
            //render furigana/window bar
            options.getFont(g, "furiFont");

            g.setColor(options.getColor("furiBackCol"));
            g.fillRect(0, furiganaStartY, options.getOptionInt("windowWidth"), furiHeight - 1);
            if(currPage.getText().equals(""))
            {
                g.setColor(options.getColor("furiCol"));
                g.drawString("Spark Reader " + VERSION + ", by Laurens Weyn. Waiting for text...", 0,furiganaStartY + g.getFontMetrics().getAscent());
            }
            
            int yOff = 0;
            //render lines
            for(Line line:currPage)
            {
                line.render(g, xOffset, yOff);
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
            
            //render settings icon
            //TODO do this better
            options.getFont(g, "furiFont");
            String cog = "三";
            g.setColor(Color.white);
            g.drawString(cog, buttonStartX, g.getFontMetrics().getAscent() + furiganaStartY - 1);
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

        
        try
        {
            if(options.getOptionBool("useNativeUI"))javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            //fall back to default if this fails
        }
        
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
        while(true)
        {
            //check clipboard
            String clip = hook.check();
            if(clip != null)
            {
                //if we're here, we have a new line of text
                userComment = null;

                if(options.getOptionBool("showOnNewLine"))
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
            try
            {
                Thread.sleep(100);
            }catch(InterruptedException ignored){}

            //UI has become inaccessible (most likely closed via alt+f4)
            if(!ui.disp.getFrame().isVisible() && !ui.tray.isShowing())
            {
                System.out.println("UI inaccessible");
                Main.exit();
            }
        }
        
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
        int maxChars = (options.getOptionInt("windowWidth") - options.getOptionInt("defWidth")) / mainFontSize;
        int maxX = (currPage.getMaxTextLength() - maxChars) * mainFontSize;
        if(-xOffset > maxX)xOffset = Math.min(-maxX, 0);
    }

    public int getLineIndex(Point pos)
    {
        return (pos.y - textStartY)/ lineHeight;
    }
}
