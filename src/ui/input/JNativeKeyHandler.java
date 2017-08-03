package ui.input;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.dispatcher.SwingDispatchService;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import ui.UI;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets key events using the JNativeHook library. <br>
 * This library is multiplatform and will get key events even when the window is not focused.<br>
 * Problems include:
 *     wastes resources monitoring the mouse.
 *     cursor slowdowns during debug, general usage.
 */
public class JNativeKeyHandler extends KeyHandler implements NativeKeyListener
{
    public JNativeKeyHandler(UI ui)
    {
        super(ui);
        // Get the logger for "org.jnativehook" and set the level to off.
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        //Change the level for all handlers attached to the default logger.
        Handler[] handlers = Logger.getLogger("").getHandlers();
        for(Handler handler : handlers)
        {
            handler.setLevel(Level.OFF);
        }
    }

    @Override
    public void addListeners()
    {
        // Set the event dispatcher to a swing safe executor service.
        GlobalScreen.setEventDispatcher(new SwingDispatchService());
        try
        {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        }
        catch (NativeHookException ex)
        {
            System.err.println("Error starting KeyHandler:\n" + ex);
            System.err.println("Keyboard controls disabled");
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent)
    {
        //this never fires due to some keyboard locale issue, at least on my machine
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent)
    {
        //FIXME this is a temporary implementation
        //TODO use some user defined lookup table for key mappings
        //System.out.println("Key pressed: " + nativeKeyEvent.getKeyCode());
        //this seems to do key repeats as well. Convenient
        switch(nativeKeyEvent.getKeyCode())
        {
            case 30://a
                keyAction(KeyEvent.simMouseScrollUp);
                break;
            case 44://z
                keyAction(KeyEvent.simMouseScrollDown);
                break;
            case 45://x
                keyAction(KeyEvent.simMouseMiddle);
                break;
            case NativeKeyEvent.VC_ESCAPE:
                keyAction(KeyEvent.hideWindow);
                break;
            case NativeKeyEvent.VC_ENTER:
                keyAction(KeyEvent.advanceText);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent)
    {

    }
}
