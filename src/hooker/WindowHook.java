package hooker;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinUser.*;

/**
 * Created by wareya on 2017/06/19.
 */
public class WindowHook
{
    //TODO this could use some work later
    User32 libuser32 = null;
    String name;
    boolean available = true;
    
    public static WindowHook hook = new WindowHook();
    
    public WindowHook()
    {
        libuser32 = KernelController.getUser32();
        available = libuser32 != null;
    }
    
    public WindowHook setName(String name) // for chaining like myhook.setName(...).getCoord()
    {
        this.name = name;
        return this;
    }
    public List<Integer> getCoord()
    {
        if(!available || libuser32 == null)
            return null;
        WinDef.POINT point = new WinDef.POINT(0,0);
        if(libuser32.ClientToScreen(libuser32.FindWindowW(null, name.toCharArray()), point))
        {
            List<Integer> coord = new ArrayList<>(); 
            coord.add(point.x);
            coord.add(point.y);
            return coord;
        }
        else
            return null;
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
}
