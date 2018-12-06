package com.lweyn.sparkreader.ui.input;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.ui.DisplayedWord;
import com.lweyn.sparkreader.ui.Line;
import com.lweyn.sparkreader.ui.TextBlockRenderer;
import com.lweyn.sparkreader.ui.UI;
import com.lweyn.sparkreader.ui.menubar.Menubar;
import com.lweyn.sparkreader.ui.popup.DefPopup;
import com.lweyn.sparkreader.ui.popup.WordPopup;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.Set;

import static com.lweyn.sparkreader.ui.UI.*;

/**
 * Handles keyboard based control of Spark Reader. <br>
 * This class is abstract and requires an implementation to get key events, since there are a few ways of doing this,
 * some of which are platform dependent.
 */
public abstract class MouseHandler
{
    private static Logger logger = Logger.getLogger(MouseHandler.class);

    protected UI ui;
    protected Point mousePos;
    protected int mouseLine = -1;
    protected DisplayedWord mousedWord;

    protected int resizeEdgeSize = 5;
    protected boolean resizeState = false;//true if cursor is <-> icon


    public MouseHandler(UI ui)
    {
        this.ui= ui;
    }

    public abstract void addListeners();

    public void leftClick()
    {
        if(mousePos != null)
            leftClick(mousePos);
    }
    public void rightClick()
    {
        if(mousePos != null)
            rightClick(mousePos);
    }
    public void middleClick()
    {
        if(mousePos != null)
            middleClick(mousePos);
    }
    public void mouseScroll(int scrollDir)
    {
        if(mousePos != null)
            mouseScroll(scrollDir, mousePos);
    }

    public void leftClick(Point pos)
    {
        //minimise button
        if(pos.y < textStartY && pos.x > minimiseStartX)
        {
            ui.minimise();
            return;
        }

        if(UI.showMenubar)//on menubar
        {
            //TODO avoid triggering this when the user intends to move the window, not click a Menubar item
            ui.menubar.processClick(pos);
            return;
        }


        if(pos.y >= textStartY && pos.y <= textEndY)
        {
            //int charIndex = toCharPos(pos.x);
            int lineIndex = ui.getLineIndex(pos);
            ui.selectedWord = null;//to recalculate
            //TODO move this over to Page functions?

            //reset selection on all unselected lines:
            int i = 0;
            for(Line line:Main.currPage)
            {
                if(i != lineIndex)line.resetSelection();
                i++;
            }
            //toggle on selected line:
            DisplayedWord word = Main.currPage.getLine(lineIndex).getWordAt(pos.x);
            for(DisplayedWord word2:Main.currPage.getLine(lineIndex).getDisplayedWords())
            {
                if(word2 == word)
                {
                    word.toggleWindow();
                    if(word.isShowingDef())ui.selectedWord = word;
                }
                else
                    word2.showDef(false);
            }
            
            ui.render();
        }
    }
    public void rightClick(Point pos)
    {
        //word
        if(onText(pos))
        {
            WordPopup popup = null;
            int lineIndex = ui.getLineIndex(pos);
            Line line = Main.currPage.getLine(lineIndex);
            DisplayedWord word = line.getWordAt(pos.x);
            
            if(word != null)
                popup = new WordPopup(line, word, ui);
            
            if(popup != null)
                popup.show(pos.x, pos.y);
        }
        //definition
        else if(onDefinition(pos))
        {
            DefPopup popup = new DefPopup(ui.selectedWord, ui, pos.y);
            popup.show(pos.x, pos.y);
        }
    }
    public void middleClick(Point pos)
    {
        if(onText(pos))//place marker
        {
            //int point = toCharPos(pos.x + mainFontSize/2);
            int lineIndex = ui.getLineIndex(pos);
            int point = Main.currPage.getLine(lineIndex).getCharAt(pos.x);
            Set<Integer> markers = Main.currPage.getLine(lineIndex).getMarkers();
            //toggle markers
            if(markers.contains(point))markers.remove(point);
            else
            {
                Main.persist.manualSpacesPlaced++;
                markers.add(point);
            }

            ui.updateText(Main.currPage.getText());//reflow (TODO don't pass text to itself)
            ui.render();//redraw
        }
    }


    public void mouseMove(Point pos)
    {
        if(pos == null) return;
        mousePos = pos;//keep track of where the mouse is
        boolean reRender = false;//true if re-render needed

        int lineIndex = ui.getLineIndex(pos);

        if(!UI.tempIgnoreMouseExit)
        {
            if(pos.getY() < UI.textStartY)//over furigana bar
            {
                if(!UI.showMenubar)
                {
                    UI.showMenubar = true;
                    Menubar.ignoreNextClick = null;
                    reRender = true;//render menu instead
                }
            }else//not over furigana bar
            {
                if(UI.showMenubar)
                {
                    UI.showMenubar = false;
                    reRender = true;//render furigana instead
                }
            }
        }
        
        if(onDefinition(pos))//over definition text
        {
            reRender |= clearWordMouseover();//disable any mouseover effects
        }
        else
        {
            DisplayedWord word = Main.currPage.getLine(lineIndex).getWordAt(mousePos.x);
            if(lineIndex != mouseLine || (mousedWord != null && mousedWord != word))
            {
                if(mousedWord != null)
                {
                    mousedWord.setMouseover(false);
                    if(mousedWord.updateOnMouse()) reRender = true;
                }
                mousedWord = null;//to recalculate
                
                mousedWord = word;
                mouseLine = lineIndex;
    
                if(mousedWord != null)
                {
                    mousedWord.setMouseover(true);
                    if(mousedWord.updateOnMouse())reRender = true;
                }

                if(reRender)ui.render();
            }
        }
        //TODO could be more efficient, revisit when width is consistent
        boolean newResizeState = pos.getY() >= UI.textStartY && pos.getX() >= Main.options.getOptionInt("windowWidth") - resizeEdgeSize;
        if(newResizeState != resizeState)
        {
            resizeState = newResizeState;
            if(resizeState)
            {
                //show that we can resize
                //uncomment to display resize cursor
                //com.lweyn.sparkreader.ui.disp.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            }
            else
            {
                //return to normal
                ui.disp.getFrame().setCursor(Cursor.getDefaultCursor());
            }
        }
        if(reRender)
            ui.render();
    }

    public void mouseExit()
    {
        boolean rerender = false;
        if(UI.showMenubar && !UI.tempIgnoreMouseExit)
        {
            UI.showMenubar = false;
            rerender = true;
        }
        //temporary ignore loose focus
        if(tempIgnoreMouseExit)
            return;

        //collapse definitions
        if(ui.selectedWord != null && Main.options.getOptionBool("hideDefOnMouseLeave"))
        {
            if(!(isTouchMode() && dragOngoing))//swipes to offscreen shouldn't close the definition panel in touch mode
            {
                ui.selectedWord.showDef(false);
                ui.selectedWord = null;
                rerender = true;
            }
        }
        rerender |= clearWordMouseover();

        if(rerender)
            ui.render();
    }

    public void mouseScroll(int scrollDir, Point pos)
    {
        //scroll up/down definition
        if(onDefinition(pos) && ui.selectedWord != null)
        {
            if(scrollDir > 0)
                ui.selectedWord.getCurrentDef().scrollDown();
            if(scrollDir < 0)
                ui.selectedWord.getCurrentDef().scrollUp();
            ui.render();
        }

        //scroll through definitions
        else if(onText(pos) && ui.selectedWord != null)
        {
            if(ui.selectedWord == Main.currPage.getLine(ui.getLineIndex(pos)).getWordAt(pos.x))
            {
                if(scrollDir > 0)
                    ui.selectedWord.scrollDown();
                if(scrollDir < 0)
                    ui.selectedWord.scrollUp();
            }
            else//not over this word: close definition and scroll text instead
            {
                ui.selectedWord.showDef(false);
                ui.xOffset += scrollDir * -mainFontSize;
                ui.boundXOff();
                ui.selectedWord = null;
            }
            ui.render();
        }
        else if(onText(pos) && ui.selectedWord == null)//scroll text
        {
            ui.xOffset += scrollDir * -mainFontSize;
            ui.boundXOff();
            ui.render();
            mouseMove(mousePos);//update highlighted word since text moved (kind of a hack right now)
        }
        else if(onFuriBar(pos))//scroll history
        {
            String historyLine;

            if(scrollDir < 0)//scroll up
                historyLine = Main.log.back();
            else
                historyLine = Main.log.forward();

            if(!Main.options.getOptionBool("splitLines"))historyLine = historyLine.replace("\n", "");//all on one line if not splitting
            logger.info("loading line " + historyLine + " from log");
            Main.currPage.clearMarkers();//markers not relevant for this text
            ui.updateText(historyLine);//flow new text
            ui.xOffset = 0;//scroll back to front
            ui.render();//update
        }
    }
    protected boolean clearWordMouseover()
    {
        boolean rerender = false;
        if(mousedWord != null)
        {
            mousedWord.setMouseover(false);
            rerender = mousedWord.updateOnMouse();
            mousedWord = null;
            if(rerender)
                ui.render();
        }
        mouseLine = -1;
        mousePos = null;
        return rerender;
    }


    private int lastDragScrollDistance = 0;
    private boolean dragOngoing = false;
    public void mouseDrag(Point start, Point current)
    {
        dragOngoing = true;
        if(isTouchMode())
        {
            //if dragging left/right from text:
            if(onText(start) && (angleCloseTo(start, current, ANGLE_LEFT) || angleCloseTo(start, current, ANGLE_RIGHT)))
            {
                int dragScrollDistance = (start.x - current.x) / UI.mainFontSize;//units to scroll by
                //simulate scrolling from start pos (which is on text), AKA scroll through text:
                mouseScroll(dragScrollDistance - lastDragScrollDistance, start);
                lastDragScrollDistance = dragScrollDistance;//keep track of how much we've scrolled with this swipe
            }
            //if dragging up/down from definition:
            else if(onDefinition(start) && (angleCloseTo(start, current, ANGLE_UP) || angleCloseTo(start, current, ANGLE_DOWN)))
            {
                int dragScrollDistance = (start.y - current.y) / TextBlockRenderer.getLastFontHeight();//units to scroll by
                //simulate scrolling from start pos (which is on definition), AKA scroll through definition:
                mouseScroll(dragScrollDistance - lastDragScrollDistance, start);
                lastDragScrollDistance = dragScrollDistance;//keep track of how much we've scrolled with this swipe
            }
        }
    }

    private static final double ANGLE_UP = 90;
    private static final double ANGLE_DOWN = -90;
    private static final double ANGLE_RIGHT = -180;
    private static final double ANGLE_LEFT = 0;
    private static final double ANGLE_TOL = 30;

    public void dragComplete(Point start, Point end)
    {
        dragOngoing = false;
        if(isTouchMode())
        {
            lastDragScrollDistance = 0;//reset for next drag

            //swipe down from text detected:
            if(onText(start) && angleCloseTo(start, end, ANGLE_DOWN))
                middleClick(start);//count that as a middle click on the text: place a marker
            //swipe left/right on definition:
            else if(onDefinition(start))
            {
                //move to next/previous definition based on direction:
                if(angleCloseTo(start, end, ANGLE_LEFT))
                    ui.selectedWord.scrollDown();
                else if(angleCloseTo(start, end, ANGLE_RIGHT))
                    ui.selectedWord.scrollUp();
                ui.render();//update UI after scrolling
            }
        }
        else
        {
            //place splits at start and end points of drag
            //TODO place split points
            //TODO show first split point when starting the drag
            //TODO select word on release
        }
    }


    private static boolean onDefinition(Point point)
    {
        return (Main.options.getOptionBool("defsShowUpwards") ? (point.y < defStartY) : (point.y > defStartY));
    }

    private static boolean onText(Point point)
    {
        return point.y >= textStartY && point.y <= textEndY;
    }
    private static boolean onFuriBar(Point point)
    {
        return point.y <= textStartY && point.y > furiganaStartY;
    }

    private static double angleBetween(Point start, Point end)
    {
        return Math.toDegrees(Math.atan2(start.y - end.y, start.x - end.x));
    }

    private static boolean angleCloseTo(Point start, Point end, double expectedAngle)
    {
        return Math.abs(angleBetween(start, end) - expectedAngle) <= ANGLE_TOL;
    }
    
    private static boolean isTouchMode()
    {
        return Main.options.getOptionBool("touchMode");
    }
}
