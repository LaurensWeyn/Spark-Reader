package ui.input;

import language.splitter.FoundWord;
import ui.Line;
import ui.UI;
import ui.popup.DefPopup;
import ui.popup.MenuPopup;
import ui.popup.WordPopup;

import java.awt.*;
import java.util.Set;

import static main.Main.*;
import static ui.UI.*;

/**
 * Handles keyboard based control of Spark Reader. <br>
 * This class is abstract and requires an implementation to get key events, since there are a few ways of doing this,
 * some of which are platform dependent.
 */
public abstract class MouseHandler
{
    protected UI ui;
    protected Point mousePos;

    public MouseHandler(UI ui)
    {
        this.ui= ui;

    }
    public abstract void addListeners();

    public void leftClick()
    {
        if(mousePos != null)leftClick(mousePos);
    }
    public void rightClick()
    {
        if(mousePos != null)rightClick(mousePos);
    }
    public void middleClick()
    {
        if(mousePos != null)middleClick(mousePos);
    }

    public void leftClick(Point pos)
    {
        //settings button
        if(pos.y < textStartY && pos.x > buttonStartX)
        {
            new MenuPopup(ui).display();
        }

        if(pos.y >= textStartY && pos.y <= textEndY)
        {
            int charIndex = toCharPos(pos.x);
            int lineIndex = ui.getLineIndex(pos);
            ui.selectedWord = null;//to recalculate
            //TODO move this over to Page functions?

            //reset selection on all unselected lines:
            int i = 0;
            for(Line line:currPage)
            {
                if(i != lineIndex)line.resetSelection();
                i++;
            }
            //toggle on selected line:
            for(FoundWord word:currPage.getLine(lineIndex).getWords())
            {
                word.toggleWindow(charIndex);
                if(word.isShowingDef())ui.selectedWord = word;
            }
            ui.render();
        }
    }
    public void rightClick(Point pos)
    {
        //settings button
        if(pos.y > furiganaStartY && pos.y < textStartY)
        {
            new MenuPopup(ui).display(pos);//no longer requires button; right click anywhere on bar works
        }
        //word
        else if(pos.y >= textStartY && pos.y <= textEndY)
        {
            WordPopup popup = null;
            int lineIndex = ui.getLineIndex(pos);
            for(FoundWord word:currPage.getLine(lineIndex).getWords())
            {
                int index = toCharPos(pos.x);
                if(word.inBounds(index))
                {
                    popup = new WordPopup(word, ui);
                    break;
                }
            }

            if(popup != null)
            {
                popup.show(pos.x, pos.y);
            }
        }
        //definition
        else if(options.getOptionBool("defsShowUpwards") ? (pos.y < defStartY):(pos.y > defStartY))
        {
            DefPopup popup = new DefPopup(ui.selectedWord, ui, pos.y);
            popup.show(pos.x, pos.y);
        }
    }
    public void middleClick(Point pos)
    {
        if(pos.y > textStartY && pos.y < textEndY)//place marker
        {
            int point = toCharPos(pos.x + mainFontSize/2);
            int lineIndex = ui.getLineIndex(pos);
            Set<Integer> markers = currPage.getLine(lineIndex).getMarkers();
            //toggle markers
            if(markers.contains(point))markers.remove(point);
            else markers.add(point);

            ui.updateText(currPage.getText());//reflow (TODO don't pass text to itself)
            ui.render();//redraw
        }
    }

    public void mouseScroll(int scrollDir)
    {
        if(mousePos == null)return;
        mouseScroll(scrollDir, mousePos);
    }

    public void mouseScroll(int scrollDir, Point pos)
    {
        boolean onTextRange = (pos.y < textEndY && pos.y >= textStartY);

        //scroll up/down definition
        if((options.getOptionBool("defsShowUpwards") ? (pos.y < defStartY):
                (pos.y > defStartY)) && ui.selectedWord != null)
        {
            if(scrollDir > 0)ui.selectedWord.getCurrentDef().scrollDown();
            if(scrollDir < 0)ui.selectedWord.getCurrentDef().scrollUp();
            ui.render();
        }

        //scroll through definitions
        else if(onTextRange && ui.selectedWord != null)
        {
            if(ui.selectedWord.inBounds(toCharPos(pos.x)))
            {
                if(scrollDir > 0)ui.selectedWord.scrollDown();
                if(scrollDir < 0)ui.selectedWord.scrollUp();
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
        else if(onTextRange && ui.selectedWord == null)//scroll text
        {
            ui.xOffset += scrollDir * -mainFontSize;
            ui.boundXOff();
            ui.render();
        }
        else if(pos.y <= textStartY && pos.y > furiganaStartY)//scroll history
        {
            String historyLine;
            if(scrollDir < 0)//scroll up
            {
                historyLine = log.back();
            }
            else
            {
                historyLine = log.forward();
            }
            if(!options.getOptionBool("splitLines"))historyLine = historyLine.replace("\n", "");//all on one line if not splitting
            System.out.println("loading line " + historyLine);
            currPage.clearMarkers();//markers not relevant for this text
            ui.updateText(historyLine);//flow new text
            ui.xOffset = 0;//scroll back to front
            ui.render();//update
        }
    }

    protected int toCharPos(int x)
    {
        x -= ui.xOffset;
        x /= mainFontSize;
        return x;
    }
}
