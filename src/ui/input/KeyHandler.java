package ui.input;

import ui.UI;

/**
 * Handles keyboard based control of Spark Reader. <br>
 * This class is abstract and requires an implementation to get key events, since there are a few ways of doing this,
 * some of which are platform dependent.
 */
public abstract class KeyHandler
{
    private UI ui;
    public KeyHandler(UI ui)
    {
        this.ui= ui;
    }
    public abstract void addListeners();

    public void keyAction(KeyEvent event)
    {
        switch(event)
        {
            case simMouseMiddle:
                ui.mouseHandler.middleClick();
                break;
            case simMouseScrollUp:
                ui.mouseHandler.mouseScroll(-1);
                break;
            case simMouseScrollDown:
                ui.mouseHandler.mouseScroll(+1);
                break;

            case showWindow:
                UI.hidden = false;
                ui.tray.hideTray();
                ui.render();
                break;
            case hideWindow:
                UI.hidden = true;
                ui.tray.showTray();
                ui.render();
                break;
            case toggleWindow:
                if(UI.hidden)keyAction(KeyEvent.showWindow);
                else keyAction(KeyEvent.hideWindow);
                break;


        }
    }

    /**
     * List of actions that can be assigned to keys. <br>
     * Exists to abstract the actual key from the action to allow easy remapping or triggering.
     */
    public enum KeyEvent
    {
        //mouse button alternatives
        simMouseScrollUp,
        simMouseScrollDown,
        simMouseMiddle,

        //window controls
        hideWindow,
        showWindow,
        toggleWindow,

        //keyboard only-controls
        //to be implemented later, rough below

        nextWord,       //or next char in charMode
        prevWord,       //or prev char in charMode
        nextDef,        //when def not selected: line down
        prevDef,        //when def not selected: line up
        scrollDefUp,    //when def not selected: log up
        scrollDefDown,  //when def not selected: log down
        select,         //essentially 'left click' current word or place marker in charMode
        charMode,       //held down modifier for splitting words

    }
}
