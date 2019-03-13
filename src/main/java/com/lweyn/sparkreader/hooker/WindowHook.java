package com.lweyn.sparkreader.hooker;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.ui.Overlay;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinUser.*;

/**
 * Created by wareya on 2017/06/19.
 */
public class WindowHook
{
    private static Logger logger = Logger.getLogger(WindowHook.class);

    private User32 libuser32;
    private String name;
    private boolean available = true;
    
    public static WindowHook instance = new WindowHook();

    private HookMode mode = HookMode.disabled;
    
    public WindowHook()
    {
        libuser32 = KernelController.getUser32();
        available = libuser32 != null;
    }
    
    public WindowHook setName(String name) // for chaining like myhook.setName(...).update()
    {
        this.name = name;
        if(!available)
            return this;
        mode = HookMode.trackWindow;
        logger.info("Started tracking window with name '" + name + "'");
        updateWindowPointer();
        updateHookMenuItem();
        return this;
    }


    private void updateWindowPointer()
    {
        windowPointer = libuser32.FindWindowW(null, name.toCharArray());
    }

    private WinDef.POINT lastPoint = new POINT(0, 0);
    private Pointer windowPointer;
    public void update()
    {
        switch(mode)
        {
            case disabled:
                break;//no updates when disabled, obv.
            case MainScreen:
                lastPoint.x = 0;
                lastPoint.y = 0;
                break;
            case trackWindow:
                lastPoint.x = 0;
                lastPoint.y = 0;
                if(libuser32.ClientToScreen(windowPointer, lastPoint))
                {
                    //apply final step if successful: scaling
                    lastPoint.x = Overlay.scaleToReal(lastPoint.x);
                    lastPoint.y = Overlay.scaleToReal(lastPoint.y);
                }
                else
                {
                    //failed: disable tracking since tracked window is likely closed
                    logger.warn("Window '" + name + "' no longer found. Auto-disabling window tracking");
                    mode = HookMode.disabled;
                    updateHookMenuItem();
                }
                break;
        }
    }

    private void updateHookMenuItem()
    {
        if(Main.ui == null || Main.ui.menubar == null)
            return;
        JMenuItem hookUIItem = (JMenuItem) Main.ui.menubar.getMenuItem("Connect", "Stick").getComponent();
        JMenuItem hookScreenItem = (JMenuItem) Main.ui.menubar.getMenuItem("Connect", "StickScreen").getComponent();
        switch(mode)
        {
            case MainScreen:
                hookUIItem.setText("Stop sticking to screen corner");
                break;
            case trackWindow:
                hookUIItem.setText("Stop sticking to window");
                break;
            case disabled:
                hookUIItem.setText("Stick to window");
        }
        hookScreenItem.setEnabled(mode != HookMode.MainScreen);
    }

    public int getX()
    {
        return lastPoint.x;
    }

    public int getY()
    {
        return lastPoint.y;
    }

    public void trackMainScreen()
    {
        mode = HookMode.MainScreen;
        updateHookMenuItem();
    }

    public void disableTracking()
    {
        mode = HookMode.disabled;
        updateHookMenuItem();
    }

    public HookMode getHookMode()
    {
        return mode;
    }

    public void sendAdvanceKey()
    {
        if(!available || name == null)return;
        KernelController.sendAdvanceKey(libuser32.FindWindowW(null, name.toCharArray()));
    }

    public String[] getAvailableWindows()
    {
        List<String> names = new ArrayList<String>();
        libuser32.EnumWindows((Pointer hWnd, Pointer userData) ->
        {
            long exStyle, style;
            if(KernelController.is64Bit())
            {
                exStyle = libuser32.GetWindowLongPtr(hWnd, -20).longValue();
                style   = libuser32.GetWindowLongPtr(hWnd, -16).longValue();
            }
            else //32 bit mode
            {
                exStyle = libuser32.GetWindowLong(hWnd, -20).longValue();
                style   = libuser32.GetWindowLong(hWnd, -16).longValue();
            }
            boolean mustBeAppwindow = (exStyle & 0x00040000) != 0;
            boolean notAppWindow = (exStyle & 0x00000080) != 0 || (style & WS_CHILD) != 0 || (style & WS_VISIBLE) == 0;
            if(notAppWindow && !mustBeAppwindow) return true;
            int len = libuser32.GetWindowTextLengthW(hWnd);
            if(len > 0)
            {
                char[] text = new char[len+1];
                libuser32.GetWindowTextW(hWnd, text, len+1);
                String string = new String(text);
                names.add(string);
            }
            return true;
        }, null);
        return names.toArray(new String[0]);
    }

    public enum HookMode
    {
        disabled,
        MainScreen,
        trackWindow
    }

}
