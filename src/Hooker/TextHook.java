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
package Hooker;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 *
 * @author laure
 */
public class TextHook
{
    //found base address: "AdvHD.exe" + 000F8E1C
    //offset list: 654 + 4 + 4 + 6D0 + 10
    //on step 6:(PW=098712)
    
    final static long baseAddress = 0x000F8E1C;
    static Kernel32 kernel32 = (Kernel32) Native.loadLibrary("kernel32",Kernel32.class);
    static User32 user32 = (User32) Native.loadLibrary("user32", User32.class);
    
    // I/O permissions
    public static int PROCESS_VM_READ= 0x0010;
    public static int PROCESS_VM_WRITE = 0x0020;
    public static int PROCESS_VM_OPERATION = 0x0008;
    
    public static void main(String[] args)
    {
        int pid = getProcessId("AdvHD.exe");//THIS WORKS!

        System.out.println("pid: " + pid);
        Pointer process = openProcess(PROCESS_VM_READ|PROCESS_VM_WRITE|PROCESS_VM_OPERATION, pid);
        System.out.println("process: " + process);
        //System.out.println("process pointer: " + Integer.toHexString(process.getInt(0)));
        //read  (this doesn't work!)
        Memory addr = new Memory(4);
        kernel32.ReadProcessMemory(process, baseAddress, addr, 4, null);
        System.out.println("pointer address found as " + Integer.toHexString(addr.getInt(0)));
        Memory firstChar = new Memory(2);
        kernel32.ReadProcessMemory(process, addr.getInt(0), addr, 4, null);
        System.out.println("first char: " + (char)firstChar.getInt(0));
    }
    
    public static int getProcessId(String window)
    {
        IntByReference pid = new IntByReference(0);
        user32.GetWindowThreadProcessId(user32.FindWindowA(null, window), pid);
        return pid.getValue();
    }

    public static Pointer openProcess(int permissions, int pid)
    {
        Pointer process = kernel32.OpenProcess(permissions, true, pid);
        return process;
    }
}
