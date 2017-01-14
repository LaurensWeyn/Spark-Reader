/* 
 * Copyright (C) 2016 Laurens Weyn
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
package UI;

import Language.Splitter.FoundWord;
import Language.Splitter.WordSplitter;
import Language.Dictionary.Dictionary;
import Language.Dictionary.Kanji;
import Hooker.ClipboardHook;
import Hooker.Log;
import Multiplayer.Client;
import Multiplayer.Host;
import Options.Known;
import Options.Options;
import Options.PrefDef;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author Laurens Weyn
 */
public class UI implements MouseListener, MouseMotionListener, MouseWheelListener
{
    public static final String VERSION = "Beta 0.3";
    
    public static UI instance;
    
    
    public static Client clientMode;
    public static Host hostMode;
    public static Thread mpThread;
    public static String mpText;
    
    public static boolean hidden = false;
    
    
    Overlay disp;
    Tray tray;
    
    ArrayList<Line> lines;
    int longestLine = 0;
    
    public static int mainFontSize = 0;
    int xOffset = 0;
    boolean lMouseClick = false;
    boolean lMouseState = false;
    Point dragReference;
    
    FoundWord selectedWord = null;
    
    public static int furiganaStartY = 0;
    public static int textStartY = 20;
    public static int defStartY = 0;//now auto-recalculated in render
    
    public static int lineHeight = 0;
    public static int textHeight = 0;
    public static int furiHeight = 0;
    
    public static int buttonStartX;
    
    public static final Color CLEAR = new Color(0, 0, 0, 0);

    public static String text;
    public static Log log;
    public static ClipboardHook hook = new ClipboardHook();
    
    public static WordSplitter splitter;
    public static Dictionary dict;
    
    public static Known known;
    public static PrefDef prefDef;
    public static Options options;
    
    public static int optionsButtonWidth = 10;
    

    
    public static boolean renderBackground = true;
    
    public void loadDictionaries()
    {
        try
        {
            dict = new Dictionary();//clear old defs if needed
            dict.loadEdict(new File(options.getOption("customDictPath")), "UTF-8", 1);
            dict.loadEdict(new File(options.getOption("edictPath")), "EUC-JP", 2);
            Kanji.load(new File(options.getOption("kanjiPath")));
            System.out.println("loaded dictionaries");
        }catch(IOException e)
        {
            JOptionPane.showMessageDialog(disp.getFrame(), "Error loading dictionaries: " + e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public UI()
    {
        lines = new ArrayList<>();
        lines.add(new Line());
        
        try
        {
            //load config
            options = new Options(new File("settings.txt"));
            known = new Known(new File("knownWords"));
            prefDef = new PrefDef(new File("preferredDefs"));
            options.save();
            disp = new Overlay(options.getOptionInt("windowWidth") + options.getOptionInt("defWidth"), options.getOptionInt("maxHeight"));
            log = new Log(50);//TODO let user override this
            
            loadDictionaries();
            splitter = new WordSplitter(dict);
            
            
            //textFont = new Font("Meiryo", Font.PLAIN, 30);
            
        }catch(Exception e)
        {
            System.out.println("error init UI");
            e.printStackTrace();
        }
        updateText("");//load in default text
    }
    public void registerListeners()
    {
        disp.getFrame().addMouseListener(this);
        disp.getFrame().addMouseMotionListener(this);
        disp.getFrame().addMouseWheelListener(this);
        
        tray = new Tray(this);//manages tray icon
    }
    public void render()
    {
        Graphics2D g = disp.getGraphics();
        g.setBackground(CLEAR);
        disp.getFrame().setVisible(!hidden);
        
        textHeight = g.getFontMetrics(options.getFont("textFont")).getHeight();
        furiHeight = g.getFontMetrics(options.getFont("furiFont")).getHeight();
        lineHeight = textHeight + furiHeight;
        
        options.getFont(g, "textFont");
        mainFontSize = g.getFontMetrics().charWidth('べ');
        defStartY = lineHeight * lines.size();
        
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
            textStartY = g.getFontMetrics().getHeight();
            g.setColor(options.getColor("furiBackCol"));
            g.fillRect(0, 0, options.getOptionInt("windowWidth"), textStartY - furiganaStartY - 1);
            if(text.equals(""))
            {
                g.setColor(options.getColor("furiCol"));
                g.drawString("Spark Reader " + VERSION + ", by Laurens Weyn. Waiting for text...", 0, g.getFontMetrics().getAscent());
            }
            
            int yOff = 0;
            //render lines
            for(Line line:lines)
            {
                line.render(g, xOffset, yOff);
                yOff += lineHeight;
            }

            //render MP text (if running, there's text and no def's open)
            if(mpThread != null && mpText != null && selectedWord == null)
            {
                options.getFont(g, "furiFont");
                g.setColor(options.getColor("furiBackCol"));
                g.fillRect(0, defStartY, g.getFontMetrics().stringWidth(mpText), g.getFontMetrics().getHeight());
                g.setColor(options.getColor("furiCol"));
                g.drawString(mpText, 0, defStartY + g.getFontMetrics().getAscent());

            }
            
            //render settings icon
            options.getFont(g, "furiFont");
            String cog = "三";//TODO use an icon for this, not a character
            g.setColor(Color.white);//TODO don't hardcode
            buttonStartX = options.getOptionInt("windowWidth") - optionsButtonWidth;
            g.drawString(cog, buttonStartX, g.getFontMetrics().getAscent());
        }
        disp.refresh();
    }
    public void updateText(String newText)
    {
        text = newText;
        //TODO allow for reflow to fit in text box (after splitting)
        String bits[] = newText.split("\n");
        longestLine = 0;
        int i = 0;
        for(String bit:bits)
        {
            if(bit.length() > longestLine)longestLine = bit.length();
            
            if(i == lines.size())
            {
                lines.add(new Line(splitter.split(bit)));
            }
            else
            {
                lines.get(i).setWords(splitter.split(bit, lines.get(i).getMarkers()));
            }
            i++;
        }
        //clear all leftover lines (TODO more efficiently?)
        while(i < lines.size())
        {
            lines.remove(i);
        }
    }
    public static void main(String[] args)throws Exception
    {
        System.out.println(VERSION);
        
        try
        {
            if(options.getOptionBool("useNaitiveUI"))javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            //fall back to default if this fails
        }
        
        UI ui = new UI();
        instance = ui;
        ui.registerListeners();
        //random sample text to copy for testing
        System.out.println("ひかり「暁斗たちと遊んでて夕飯のギョーザを食べ損ねて、\n悔しかったから、星座にしてやったんだよね」");
        
        //center window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ui.disp.getFrame().setLocation(screenSize.width / 2 - options.getOptionInt("windowWidth") / 2,
                screenSize.height / 2 - (options.getOptionInt("maxHeight")) / 2);
        
        ui.render();//TODO make this not need 2 render calls to properly align stuff
        ui.render();
        System.gc();//cleanup after loading in dictionaries and such
        //update loop
        while(true)
        {
            //check clipboard
            String clip = hook.check();
            if(clip != null)
            {
                //if we're here, we have a new line of text
                
                if(options.getOptionBool("showOnNewLine"))
                {
                    hidden = false;//force visiblility on new line if needed
                    ui.tray.hideTray();
                }
                
                
                clip = clip.replace("\r", "");
                if(!options.getOptionBool("splitLines"))clip = clip.replace("\n", "");//all on one line if not splitting
                for(Line line:ui.lines)
                {
                    line.getMarkers().clear();//clear all markers
                }
                ui.updateText(clip);//reflow text on defaults
                log.addLine(clip);//add line to log
                ui.xOffset = 0;//scroll back to front
                ui.render();
                
            }
            //check MP systems
            if(clientMode != null)
            {
                String newText = clientMode.getStatusText();
                if(!newText.equals(mpText))
                {
                    mpText = newText;
                    ui.render();//refresh mp status
                }
                if(clientMode.running == false)
                {
                    clientMode = null;
                    mpThread = null;
                }
            }
            else if(hostMode != null)
            {
                String newText = hostMode.getStatusText();
                if(!newText.equals(mpText))
                {
                    mpText = newText;
                    ui.render();//refresh mp status
                }
                if(hostMode.running == false)
                {
                    hostMode = null;
                    mpThread = null;
                }
            }else mpText = null;
            try
            {
                Thread.sleep(100);
            }catch(InterruptedException e){}
        }
        
    }
    public int toCharPos(int x)
    {
        x -= xOffset;
        x /= mainFontSize;
        return x;
    }
    
    //////////////////////////////
    //begin mouse event handlers//
    //////////////////////////////
    @Override
    public void mouseClicked(MouseEvent e)
    {
        if(e.getButton() == 1)lMouseState = false;
        
        if(e.getButton() == 1 && lMouseClick)//if left click (and wasn't drag)
        {
            //settings button
            if(e.getY() < textStartY && e.getX() > buttonStartX)
            {
                new MenuPopup(this).show();
            }
            
            if(e.getY() >= textStartY && e.getY() <= defStartY)
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
        }else if(e.getY() > textStartY && e.getY() < defStartY && e.getButton() == 2)//middle click: place marker
        {
            int pos = toCharPos(e.getX() + mainFontSize/2);
            int lineIndex = getLineIndex(e.getPoint());
            Set<Integer> markers = lines.get(lineIndex).getMarkers();
            //toggle markers
            if(markers.contains(pos))markers.remove(pos);
            else markers.add(pos);
            
            updateText(text);//reflow
            render();//redraw
        }else if(e.getButton() == 3)//right click: extra option menu
        {
            //settings button
            if(e.getY() < textStartY && e.getX() > buttonStartX)
            {
                new MenuPopup(this).show();
            }
            else if(!hidden && (e.getY() >= textStartY && e.getY() <= defStartY))//word
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
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        dragReference = e.getPoint();
        if(e.getButton() == 1)
        {
            lMouseClick = true;
            if(e.getY() <= textStartY)//only furigana bar draggable
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
        //collapse definitions
        if(selectedWord != null)
        {
            selectedWord.showDef(false);
            selectedWord = null;
            render();
        }
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

    @Override
    public void mouseMoved(MouseEvent e)
    {
        //TODO allow for definitions to appear without clicking on them
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if(hidden)return;
        
        if(e.getY() >= defStartY && selectedWord != null)//scroll up/down definition
        {
            if(e.getWheelRotation() > 0)selectedWord.getCurrentDef().scrollDown();
            if(e.getWheelRotation() < 0)selectedWord.getCurrentDef().scrollUp();
            render();
        }
        else if(e.getY() <= defStartY && e.getY() > furiganaStartY && selectedWord != null)//scroll through definitions
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
        else if(selectedWord == null && e.getY() >= textStartY)//scroll text
        {
            xOffset += e.getWheelRotation() * -mainFontSize;
            boundXOff();
            render();
        }
        else if(e.getY() <= textStartY)//scroll history
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
    public void boundXOff()
    {
        if(xOffset > 0)xOffset = 0;
        int maxChars = (options.getOptionInt("windowWidth") - options.getOptionInt("defWidth")) / mainFontSize;
        int maxX = (longestLine - maxChars) * mainFontSize;
        if(-xOffset > maxX)xOffset = Math.min(-maxX, 0);
    }
    public int getLineIndex(Point pos)
    {
        int index = (pos.y - textStartY)/ lineHeight;
        return index;
    }
}
