
package hooker;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;
import com.sun.jna.win32.W32APIOptions;
import java.util.List;

public interface User32 extends W32APIOptions {
    Pointer FindWindowA(String winClass, String title);
    Pointer FindWindowW(String winClass, char[] title);  //unicode version
    Pointer FindWindowExW(Pointer hwndParent, Pointer hwndChildAfter, byte[] lpszClass, byte[] lpszWindow);  //unicode version
    int GetClassName(Pointer hWnd, byte[] lpClassName, int nMaxCount);  
    public static class GUITHREADINFO extends Structure {  
        public int cbSize = size();  
        public int flags;  
        Pointer hwndActive;  
        Pointer hwndFocus;  
        Pointer hwndCapture;  
        Pointer hwndMenuOwner;  
        Pointer hwndMoveSize;  
        Pointer hwndCaret;  
        RECT rcCaret;
        @Override
        protected List getFieldOrder()
        {
            return null;
        }
    }  
    boolean GetGUIThreadInfo(int idThread, GUITHREADINFO lpgui);  
    public static class WINDOWINFO extends Structure {  
        public int cbSize = size();  
        public RECT rcWindow;  
        public RECT rcClient;  
        public int dwStyle;  
        public int dwExStyle;  
        public int dwWindowStatus;  
        public int cxWindowBorders;  
        public int cyWindowBorders;  
        public short atomWindowType;  
        public short wCreatorVersion;
        @Override
        protected List getFieldOrder()
        {
            return null;
        }
    }  
    boolean GetWindowInfo(Pointer hWnd, WINDOWINFO pwi);  
    boolean GetWindowRect(Pointer hWnd, RECT rect);  
    int GetWindowTextW(Pointer hWnd, char[] lpString, int nMaxCount);
    int GetWindowTextLength(Pointer hWnd);  
    int GetWindowModuleFileName(Pointer hWnd, byte[] lpszFileName, int cchFileNameMax);  
    int GetWindowThreadProcessId(Pointer hWnd, IntByReference lpdwProcessId);  
    interface WNDENUMPROC extends StdCallCallback {  
        /**
         * Return whether to continue enumeration.
         */  
        boolean callback(Pointer hWnd, Pointer data);  
    }  
    boolean EnumWindows(WNDENUMPROC lpEnumFunc, Pointer data);//can be used to find all windows 
    boolean EnumThreadWindows(int dwThreadId, WNDENUMPROC lpEnumFunc, Pointer data);
    Pointer GetForegroundWindow();

    /**
     * Defines the x- and y-coordinates of a point.
     */  
    public static class POINT extends Structure {  
        public int x, y;  
        @Override
        protected List getFieldOrder()
        {
            return null;
        }
    }  
    /**
     * Specifies the width and height of a rectangle.
     */  
    public static class SIZE extends Structure {  
        public int cx, cy;  
        @Override
        protected List getFieldOrder()
        {
            return null;
        }
    }  

    //There's a library better suited for unfocused key data and seems to be multiplatform.
    //Perhaps use that instead of User32 calls...

    //get state of all keys
    boolean GetKeyboardState(byte[] state);
    //get state of one key
    short GetAsyncKeyState(int vKey);  
}