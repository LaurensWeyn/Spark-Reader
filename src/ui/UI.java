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

import language.splitter.FoundWord;
import main.Main;
import main.Utils;
import ui.popup.DefPopup;
import ui.popup.MenuPopup;
import ui.popup.WordPopup;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static main.Main.*;

/**
 * Main Spark Reader UI
 * @author Laurens Weyn
 */
public class UI implements MouseListener, MouseMotionListener, MouseWheelListener
{



    public static boolean hidden = false;


    public static String mpStatusText;


    public Overlay disp;
    public Tray tray;
    
    ArrayList<Line> lines;
    int longestLine = 0;
    
    public static int mainFontSize = 1;//1 default to stop division by 0
    public int xOffset = 0;
    boolean lMouseClick = false;
    boolean lMouseState = false;
    Point dragReference;
    
    FoundWord selectedWord = null;
    
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


    public UI()
    {
        lines = new ArrayList<>();
        lines.add(new Line());
        disp = new Overlay(options.getOptionInt("windowWidth") + options.getOptionInt("defWidth"),
                options.getOptionInt("maxHeight"));
    }
    private void registerListeners()
    {
        disp.getFrame().addMouseListener(this);
        disp.getFrame().addMouseMotionListener(this);
        disp.getFrame().addMouseWheelListener(this);
        
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
            furiganaStartY = options.getOptionInt("maxHeight") - lineHeight * Math.max(options.getOptionInt("expectedLineCount"), lines.size());
            defStartY = furiganaStartY - 2;
            textStartY = furiHeight + furiganaStartY;
            textEndY = textStartY + lineHeight * lines.size();
        }
        else
        {
            textStartY = furiHeight + furiganaStartY;
            textEndY = textStartY + lineHeight * lines.size() - furiHeight;
            defStartY = textEndY;
        }
        buttonStartX = options.getOptionInt("windowWidth") - optionsButtonWidth - 1;
    }
    public void render()
    {
        Graphics2D g = disp.getGraphics();
        g.setBackground(CLEAR);
        disp.getFrame().setVisible(!hidden);
        
        calculateSizes(g);


        if(!hidden)
        {
            //render background
            if(renderBackground)
            {
                g.setColor(options.getColor("textBackCol"));
                g.fillRect(0, textStartY - 1, options.getOptionInt("windowWidth"), lines.size() * lineHeight - furiHeight + 1);
                g.setColor(options.getColor("windowBackCol"));
                int i = 1;
                while(i < lines.size())
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
            if(text.equals(""))
            {
                g.setColor(options.getColor("furiCol"));
                g.drawString("Spark Reader " + VERSION + ", by Laurens Weyn. Waiting for text...", 0,furiganaStartY + g.getFontMetrics().getAscent());
            }
            
            int yOff = 0;
            //render lines
            for(Line line:lines)
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
    private void updateText(String newText)
    {
        text = newText;
        String bits[] = newText.split("\n");
        longestLine = 0;
        int i = 0;
        for(String bit:bits)
        {
            if(bit.length() > longestLine)longestLine = bit.length();
            
            if(i == lines.size())
            {
                lines.add(new Line(splitter.split(bit, new HashSet<>())));
            }
            else
            {
                lines.get(i).setWords(splitter.split(bit, lines.get(i).getMarkers()));
            }
            i++;
        }
        //clear all leftover lines
        while(i < lines.size())
        {
            lines.remove(i);
        }
        //reflow if needed
        int maxLineLength = options.getOptionInt("windowWidth") / mainFontSize;
        if(longestLine > mainFontSize && options.getOptionBool("reflowToFit"))
        {
            ArrayList<Line> newLines = new ArrayList<>(lines.size());
            for(Line line:lines)
            {
                Line newLine = new Line();
                for(FoundWord word:line.getWords())
                {
                    if(newLine.calcLength() + word.getLength() > maxLineLength)
                    {
                        newLines.add(newLine);
                        newLine = new Line();
                    }
                    addWord(line, newLine, word);
                }
                if(newLine.calcLength() != 0)newLines.add(newLine);
            }
            lines = newLines;
        }
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
                

                clip = clip.replace("\r", "");
                log.addLine(clip);//add line to log
                if(!options.getOptionBool("splitLines"))clip = clip.replace("\n", "");//all on one line if not splitting
                for(Line line:ui.lines)
                {
                    line.getMarkers().clear();//clear all markers
                }
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
        }
        
    }

    private int toCharPos(int x)
    {
        x -= xOffset;
        x /= mainFontSize;
        return x;
    }
    
    //////////////////////////////
    //begin mouse event handlers//
    //////////////////////////////
    private long lastClickTime = 0;
    private static final long MAX_CLICK_DELAY = 1000;
    @Override
    public void mouseClicked(MouseEvent e)
    {
        long clickTime = System.nanoTime();
        if(clickTime - lastClickTime < MAX_CLICK_DELAY)
        {
            System.out.println("stop double event");
            return;//[attempt to]stop accidental double click
        }
        lastClickTime = clickTime;

        if(e.getButton() == 1)lMouseState = false;
        
        if(e.getButton() == 1 && lMouseClick)//if left click (and wasn't drag)
        {
            //settings button
            if(e.getY() < textStartY && e.getX() > buttonStartX)
            {
                new MenuPopup(this).display();
            }
            
            if(e.getY() >= textStartY && e.getY() <= textEndY)
            {
                int pos = toCharPos(e.getX());
                int lineIndex = getLineIndex(e.getPoint());
                selectedWord = null;//to recalulate
                
                //reset selection on all unselected lines:
                int i = 0;
                for(Line line:lines)
                {
                    if(i != lineIndex)line.resetSelection();
                    i++;
                }
                //toggle on selected line:
                for(FoundWord word:lines.get(lineIndex).getWords())
                {
                    word.toggleWindow(pos);
                    if(word.isShowingDef())selectedWord = word;
                }
                render();
            }
            lMouseClick = false;
        }
        else if(e.getY() > textStartY && e.getY() < textEndY && e.getButton() == 2)//middle click: place marker
        {
            int pos = toCharPos(e.getX() + mainFontSize/2);
            int lineIndex = getLineIndex(e.getPoint());
            Set<Integer> markers = lines.get(lineIndex).getMarkers();
            //toggle markers
            if(markers.contains(pos))markers.remove(pos);
            else markers.add(pos);
            
            updateText(text);//reflow
            render();//redraw
        }
        else if(e.getButton() == 3)//right click: extra option menu
        {
            //settings button
            if(e.getY() > furiganaStartY && e.getY() < textStartY)
            {
                new MenuPopup(this).display(e);//no longer requires button; right click anywhere on bar works
            }
            //word
            else if(e.getY() >= textStartY && e.getY() <= textEndY)
            {
                WordPopup popup = null;
                int lineIndex = getLineIndex(e.getPoint());
                for(FoundWord word:lines.get(lineIndex).getWords())
                {
                    int pos = toCharPos(e.getX());
                    if(word.inBounds(pos))
                    {
                        popup = new WordPopup(word, this);
                        break;
                    }
                }

                if(popup != null)
                {
                    popup.show(e.getX(), e.getY());
                }
            }
            //definition
            else if(options.getOptionBool("defsShowUpwards") ? (e.getY() < defStartY):(e.getY() > defStartY))
            {
                DefPopup popup = new DefPopup(selectedWord, this, e.getY());
                popup.show(e.getX(), e.getY());
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        dragReference = e.getPoint();
        if(e.getButton() == 1)
        {
            lMouseClick = true;
            if(e.getY() >= furiganaStartY && e.getY() <= textStartY)//only furigana bar draggable
            {
                lMouseState = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

        double dist = dragReference.distanceSq(e.getPoint());
        if((dist != 0 || lMouseState) && dist < 100)//only moved a little
        {
            if(e.getButton() == 1)lMouseClick = true;
            lMouseState = false;
            mouseClicked(e);//pass this over as a click
        }
        lMouseState = false;
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        //temporary ignore loose focus
        if(tempIgnoreMouseExit)
        {
            return;
        }
        //collapse definitions
        if(selectedWord != null && options.getOptionBool("hideDefOnMouseLeave"))
        {
            selectedWord.showDef(false);
            selectedWord = null;
            render();
        }
        if(mousedWord != null)
        {
            mousedWord.setMouseover(false);
            boolean rerender = mousedWord.updateOnMouse();
            mousedWord = null;
            if(rerender)render();
        }
        mouseLine = -1;
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(lMouseState)
        {
            Point moveTo = e.getLocationOnScreen();
            moveTo.translate(-dragReference.x, -dragReference.y);
            disp.getFrame().setLocation(moveTo);
            lMouseClick = false;//no longer a click
        }
    }


    private int mouseLine = -1;
    private FoundWord mousedWord;
    @Override
    public void mouseMoved(MouseEvent e)
    {
        int pos = toCharPos(e.getX());
        int lineIndex = getLineIndex(e.getPoint());
        if(lineIndex >= lines.size() || lineIndex < 0)return;
        if(lineIndex != mouseLine || (mousedWord!= null && !mousedWord.inBounds(pos)))
        {
            boolean reRender = false;
            if(mousedWord != null)
            {
                mousedWord.setMouseover(false);
                if(mousedWord.updateOnMouse())reRender = true;
            }
            mousedWord = null;//to recalulate
            //toggle on selected line:
            for (FoundWord word : lines.get(lineIndex).getWords())
            {
                if (word.inBounds(pos))
                {
                    mousedWord = word;
                    break;
                }
            }
            mouseLine = lineIndex;

            if(mousedWord != null)
            {
                //System.out.println("mouseover'd word changed to " + mousedWord.getText());
                mousedWord.setMouseover(true);
                if(mousedWord.updateOnMouse())reRender = true;
            }

            if(reRender)render();
        }
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if(hidden)return;
        boolean onTextRange = (e.getY() < textEndY && e.getY() >= textStartY);

        //scroll up/down definition
        if((options.getOptionBool("defsShowUpwards") ? (e.getY() < defStartY):
                                                            (e.getY() > defStartY)) && selectedWord != null)
        {
            if(e.getWheelRotation() > 0)selectedWord.getCurrentDef().scrollDown();
            if(e.getWheelRotation() < 0)selectedWord.getCurrentDef().scrollUp();
            render();
        }

        //scroll through definitions
        else if(onTextRange && selectedWord != null)
        {
            if(selectedWord.inBounds(toCharPos(e.getX())))
            {
                if(e.getWheelRotation() > 0)selectedWord.scrollDown();
                if(e.getWheelRotation() < 0)selectedWord.scrollUp();
            }
            else//not over this word: close definition and scroll text instead
            {
                selectedWord.showDef(false);
                xOffset += e.getWheelRotation() * -mainFontSize;
                boundXOff();
                selectedWord = null;
            }
            render();
        }
        else if(onTextRange && selectedWord == null)//scroll text
        {
            xOffset += e.getWheelRotation() * -mainFontSize;
            boundXOff();
            render();
        }
        else if(e.getY() <= textStartY && e.getY() > furiganaStartY)//scroll history
        {
            String historyLine;
            if(e.getWheelRotation() < 0)//scroll up
            {
                historyLine = log.back();
            }
            else
            {
                historyLine = log.forward();
            }
            if(!options.getOptionBool("splitLines"))historyLine = historyLine.replace("\n", "");//all on one line if not splitting
            System.out.println("loading line " + historyLine);
            for(Line line:lines)
            {
                line.getMarkers().clear();//clear markers (not relevant for this text)
            }
            updateText(historyLine);//flow new text
            xOffset = 0;//scroll back to front
            render();//update
        }
        
    }

    private void boundXOff()
    {
        if(options.getOptionBool("reflowToFit"))
        {
            //do not allow scrolling when text always fits
            xOffset = 0;
            return;
        }
        if(xOffset > 0)xOffset = 0;
        int maxChars = (options.getOptionInt("windowWidth") - options.getOptionInt("defWidth")) / mainFontSize;
        int maxX = (longestLine - maxChars) * mainFontSize;
        if(-xOffset > maxX)xOffset = Math.min(-maxX, 0);
    }

    private int getLineIndex(Point pos)
    {
        return (pos.y - textStartY)/ lineHeight;
    }
}
