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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JOptionPane;

/**
 *
 * @author Laurens Weyn
 */
public class UI implements MouseListener, MouseMotionListener, MouseWheelListener
{
    public static UI instance;
    
    
    public static Client clientMode;
    public static Host hostMode;
    public static Thread mpThread;
    public static String mpText;
    
    public static boolean hidden = false;
    
    
    Overlay disp;
    Tray tray;
    
    SortedSet<Integer> markers;
    ArrayList<FoundWord> words;
    public static int mainFontSize = 0;
    int xOffset = 0;
    boolean lMouseClick = false;
    boolean lMouseState = false;
    Point dragReference;
    
    FoundWord selectedWord = null;
    
    public static int furiganaStartY = 0;
    public static int textStartY = 20;
    public static int defStartY = 60;//now auto-recalculated in render
    public static int buttonStartX;
    
    public static Font textFont = new Font("Meiryo", Font.PLAIN, 30);
    public static Font furiFont = new Font("Meiryo", 0, 15);
    public static Font defFont = new Font("Meiryo", 0, 15);
    
    public static Color markerCol = new Color(255, 255, 0, 200);
    public static Color noMarkerCol = new Color(255, 255, 0, 1);
    public static Color furiCol = new Color(0, 255, 255);
    public static Color furiBackCol = new Color(0, 0, 0, 128);
    
    public static Color textBackCol = new Color(0, 0, 255, 128);
    public static Color knownTextBackCol = new Color(0, 0, 255, 128);
    public static Color clickedTextBackCol = new Color(0, 100, 0, 128);
    public static Color textCol = new Color(255, 0, 0, 255);
    
    public static Color defReadingCol = new Color(0, 255, 255);
    public static Color defKanjiCol = new Color(255, 255, 255);
    public static Color defTagCol = new Color(255, 255, 255);
    public static Color defCol = new Color(255, 255, 0);
    public static Color defBackCol = new Color(0, 0, 0, 128);
    
    public static String text;
    public static Log log;
    public static ClipboardHook hook = new ClipboardHook();
    
    public static WordSplitter splitter;
    public static Dictionary dict;
    
    public static Known known;
    public static PrefDef prefDef;
    public static Options options;
    
    public static int windowWidth = 1280;
    public static int totalWidth = 1600;
    public static int maxHeight = 720;
    
    public static int defWidth = 250;
    public static int optionsButtonWidth = 10;
    
    public static boolean splitLines = true;
    public static boolean showFurigana = true;
    public static boolean showOnNewLine = true;
    public static boolean useNaitiveUI = false;
    public static boolean takeFocus = true;
    public void loadOptions(Options o)
    {
        textFont = o.getFont("textFont");
        furiFont = o.getFont("furiFont");
        defFont = o.getFont("defFont");
        
        markerCol = o.getColor("markerCol");
        noMarkerCol = o.getColor("noMarkerCol");
        furiCol = o.getColor("furiCol");
        furiBackCol = o.getColor("furiBackCol");
        
        textBackCol = o.getColor("textBackCol");
        knownTextBackCol = o.getColor("knownTextBackCol");
        clickedTextBackCol = o.getColor("clickedTextBackCol");
        textCol = o.getColor("textCol");
        
        defReadingCol = o.getColor("defReadingCol");
        defKanjiCol = o.getColor("defKanjiCol");
        defTagCol = o.getColor("defTagCol");
        defCol = o.getColor("defCol");
        defBackCol = o.getColor("defBackCol");
        
        //other options
        windowWidth = o.getOptionInt("windowWidth");
        maxHeight = o.getOptionInt("maxHeight");
        defWidth = o.getOptionInt("defWidth");
        
        splitLines = o.getOptionBool("splitLines");
        showFurigana = o.getOptionBool("showFurigana");
        showFurigana = o.getOptionBool("showOnNewLine");
        takeFocus = o.getOptionBool("takeFocus");
        //TODO override height too
        totalWidth = windowWidth + defWidth;
        //totalWidth = textWidth;
        
        hidden = false;//needed to recalc most font-specific parameters in rendering
    }
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
        markers = new TreeSet<>();
        
        try
        {
            //dict.loadDefs(new File("C:\\Users\\laure\\Downloads\\edict2"), "EUC-JP");
            //load config
            options = new Options(new File("settings.txt"));
            known = new Known(new File("knownWords"));
            prefDef = new PrefDef(new File("preferredDefs"));
            loadOptions(options);
            options.save();
            disp = new Overlay(totalWidth, maxHeight);
            log = new Log(50);//TODO let user override this
            
            loadDictionaries();
            splitter = new WordSplitter(dict);
            
            
            textFont = new Font("Meiryo", Font.PLAIN, 30);
            //textFont = new Font("HanaMinA Regular", Font.PLAIN, 30);
            
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
        disp.getFrame().setVisible(!hidden);
        if(!hidden)
        {
            //render furigana/window bar
            g.setFont(furiFont);
            options.getFontAA(g, "furiFont");
            textStartY = g.getFontMetrics().getHeight();
            g.setColor(furiBackCol);
            g.fillRect(0, 0, windowWidth, textStartY - furiganaStartY);
            if(text.equals(""))
            {
                g.setColor(furiCol);
                g.drawString("Spark Reader Beta 0.2, by Laurens Weyn. Waiting for text...", 0, g.getFontMetrics().getAscent());
            }


            //render markers
            Set<Integer> splitPoints = new HashSet<>();
            for(FoundWord word:words)
            {
                splitPoints.add(word.startX());
            }
            g.setClip(0, 0, UI.windowWidth, UI.maxHeight);//render only over window
            for (int i = 0; i < text.length(); i++)
            {
                if(markers.contains(i) || splitPoints.contains(i))//only draw on actual points
                {
                    g.setColor(markers.contains(i)?markerCol:noMarkerCol);
                    g.fillRect(xOffset + i * mainFontSize - 1, textStartY, 2, defStartY - textStartY);//TODO make markers variable size
                }
            }

            //render words
            g.setFont(textFont);
            mainFontSize = g.getFontMetrics().charWidth('べ');
            defStartY = g.getFontMetrics().getHeight() + textStartY;
            for(FoundWord word:words)
            {
                word.render(g, xOffset);
                splitPoints.add(word.startX());
            }

            //render MP text (if running, there's text and no def's open
            if(mpThread != null && mpText != null && selectedWord == null)
            {
                g.setFont(furiFont);
                options.getFontAA(g, "furiFont");
                g.setColor(furiBackCol);
                g.fillRect(0, defStartY, g.getFontMetrics().stringWidth(mpText), g.getFontMetrics().getHeight());
                g.setColor(furiCol);
                g.drawString(mpText, 0, defStartY + g.getFontMetrics().getAscent());

            }
            
            //render settings icon
            g.setFont(furiFont);
            options.getFontAA(g, "furiFont");
            String cog = "三";//TODO use an icon for this, not a character
            g.setColor(Color.white);//TODO don't hardcode
            buttonStartX = windowWidth - optionsButtonWidth;
            g.drawString(cog, buttonStartX, g.getFontMetrics().getAscent());
        }
        disp.refresh();
    }
    public void updateText(String newText)
    {
        text = newText;
        words = splitter.split(text, markers);
    }
    public static void main(String[] args)throws Exception
    {
        try
        {
            if(useNaitiveUI)javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        }catch(Exception e)
        {
            //fall back to default if this fails
        }
        UI ui = new UI();
        instance = ui;
        ui.registerListeners();
        //random sample text to copy for testing
        System.out.println("ひかり「暁斗たちと遊んでて夕飯のギョーザを食べ損ねて、\n悔しかったから、星座にしてやったんだよね」");
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
                
                if(showOnNewLine)
                {
                    hidden = false;//force visiblility on new line if needed
                    ui.tray.hideTray();
                }
                
                if(splitLines)
                {
                    String lines[] = clip.replace("\r", "").split("\n");
                    String lastLine = "";
                    for(String line:lines)
                    {
                        log.addLine(line);
                        lastLine = line;
                    }
                    ui.markers.clear();//clear markers
                    ui.updateText(lastLine);//show last line by default
                    ui.xOffset = 0;//scroll back to front
                    ui.render();
                }
                else
                {
                    clip = clip.replace("\n", "").replace("\r", "");
                    ui.markers.clear();//clear markers
                    ui.updateText(clip);//reflow text on defaults
                    log.addLine(clip);//add line to log
                    ui.xOffset = 0;//scroll back to front
                    ui.render();
                }
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
                
            //within word area (TODO: support multiple lines?)
            if(e.getY() >= textStartY && e.getY() <= defStartY)
            {
                int pos = toCharPos(e.getX());
                selectedWord = null;//recalulate
                for(FoundWord word:words)
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
            //change markers
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
                for(FoundWord word:words)
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
            markers.clear();//clear markers (not relevant for this text)
            updateText(historyLine);//flow new text
            xOffset = 0;//scroll back to front
            render();//update
        }
        
    }
    public void boundXOff()
    {
        if(xOffset > 0)xOffset = 0;
        int maxChars = (windowWidth - defWidth) / mainFontSize;
        int maxX = (text.length() - maxChars) * mainFontSize;
        if(-xOffset > maxX)xOffset = Math.min(-maxX, 0);
    }
    
}
