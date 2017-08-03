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
package hooker;

import com.sun.java.swing.plaf.windows.resources.windows;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;
import language.dictionary.Japanese;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Laurens on 2/18/2017.
 */
public class KernelController
{
    private static Kernel32 kernel32;
    private static User32 user32;
    //I/O permissions
    public static int PROCESS_VM_READ = 0x0010;
    public static int PROCESS_VM_WRITE = 0x0020;
    public static int PROCESS_VM_OPERATION = 0x0008;

    private static boolean attemptedLoad = false;

    /**
     * Internal function. Ensures kernel libraries are loaded if possible.
     */
    private static void attemptLoad()
    {
        if(attemptedLoad)return;
        try
        {
            kernel32 = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
            user32 = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.UNICODE_OPTIONS);
        }catch(UnsatisfiedLinkError ignored)
        {
            System.out.println("Native libraries not available");
        }
        attemptedLoad = true;
    }

    /**
     * Checks if this system supports kernel-level operations (I.E. it's a Windows machine)
     * @return true if kernel32 and user32 exist and are loaded
     */
    public static boolean isKernelAvailable()
    {
        attemptLoad();
        return kernel32 != null && user32 != null;
    }

    public static Kernel32 getKernel32()
    {
        attemptLoad();
        return kernel32;
    }

    public static User32 getUser32()
    {
        attemptLoad();
        return user32;
    }

    /**
     * Finds all windows
     * @param onlyJapanese limit window search to ones containing Japanese text
     * @return a map of all titles and their window pointers.
     */
    public static Map<String, Pointer> findAllWindows(boolean onlyJapanese)
    {
        if(!isKernelAvailable())return null;

        HashMap<String, Pointer> windows = new HashMap<>();

        user32.EnumWindows((hWnd, data) ->
        {
            //hWnd is the window pointer, data is usually return data (unused)
            String text = getWindowName(hWnd);
            if(text != null && (!onlyJapanese || Japanese.isJapaneseWriting(text)))
            {
                windows.put(text, hWnd);
            }
            return true;
        }, null);
        return windows;
    }

    /**
     * Gets process ID from a window pointer
     * @param hWnd the window pointer
     * @return the process ID
     */
    public static int getPID(Pointer hWnd)
    {
        if(!isKernelAvailable())return -1;
        IntByReference pid = new IntByReference();
        user32.GetWindowThreadProcessId(hWnd, pid);
        return pid.getValue();
    }

    /**
     * Gets the currently focused window
     * @return pointer to the focused window, or null if not possible
     */
    public static Pointer getFocusedWindow()
    {
        if(!isKernelAvailable())return null;
        return user32.GetForegroundWindow();
    }

    private static char[] windowText = new char[256];

    /**
     * Gets the (possibly unicode) name of a window
     * @param hWnd the window pointer
     * @return the name of the window, null if it has no name
     */
    public static String getWindowName(Pointer hWnd)
    {
        if(!isKernelAvailable() || hWnd == null)return null;
        int len = user32.GetWindowTextW(hWnd, windowText, 256);
        if(len == 0)return null;//unnamed window
        return new String(windowText, 0, len);//Charset.forName("UTF-8").decode(bb).toString();
    }

    public static Rectangle getWindowArea(Pointer hWnd)
    {
        if(!isKernelAvailable() || hWnd == null)return null;
        WinDef.RECT rect = new WinDef.RECT();
        user32.GetWindowRect(hWnd, rect);
        return new Rectangle(rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
    }

    /**
     * Sends key to advance dialogue to a window, even if that window is not focused.
     * @param hWnd the window to send the key to.
     */
    public static void sendAdvanceKey(Pointer hWnd)
    {
        if(!isKernelAvailable() || hWnd == null)return;

        int WM_KEYDOWN = 0x0100;
        int WM_KEYUP = 0x0101;
        //keycodes: https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
        int VK_RETURN = 0x0D;

        user32.PostMessage(hWnd, WM_KEYDOWN, VK_RETURN, 0);
        user32.PostMessage(hWnd, WM_KEYUP, VK_RETURN, 0);
    }
}
