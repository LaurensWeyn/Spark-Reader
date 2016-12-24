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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary  
{  
    // description from msdn  
    //BOOL WINAPI WriteProcessMemory(  
    //__in   HANDLE hProcess,  
    //__in   LPVOID lpBaseAddress,  
    //__in   LPCVOID lpBuffer,  
    //__in   SIZE_T nSize,  
    //__out  SIZE_T *lpNumberOfBytesWritten  
    //);  
    boolean WriteProcessMemory(Pointer p, long address, Pointer buffer, int size, IntByReference written);  
     
     
    //BOOL WINAPI ReadProcessMemory(  
    //          __in   HANDLE hProcess,  
    //          __in   LPCVOID lpBaseAddress,  
    //          __out  LPVOID lpBuffer,  
    //          __in   SIZE_T nSize,  
    //          __out  SIZE_T *lpNumberOfBytesRead  
    //        );  
    boolean ReadProcessMemory(Pointer hProcess, long inBaseAddress, Pointer outputBuffer, int nSize, IntByReference outNumberOfBytesRead);  
     
     
    //HANDLE WINAPI OpenProcess(  
    //  __in  DWORD dwDesiredAccess,  
    //  __in  BOOL bInheritHandle,  
    //  __in  DWORD dwProcessId  
    //);  
    Pointer OpenProcess(int desired, boolean inherit, int pid);  
     
    /* derp */  
    int GetLastError();  
}