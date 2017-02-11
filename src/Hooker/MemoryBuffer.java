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
package Hooker;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;


/**
 *
 * @author Laurens Weyn
 */
public class MemoryBuffer
{
    private final int buffSize;
    private int startPoint, endPoint;
    private final Memory memory;
    private final Pointer process;
    private boolean allRead = true;
    public MemoryBuffer(int buffSize, Pointer process)
    {
        this.buffSize = buffSize;
        this.process = process;
        memory = new Memory(buffSize);
        startPoint = -1;
        endPoint = -1;
        
    }
    public char getChar(int pos)
    {
        if(!inRange(pos))
        {
            startPoint = pos;
            endPoint = pos + buffSize;
            IntByReference memoryRead = new IntByReference();
            MemoryHook.kernel32.ReadProcessMemory(process, startPoint, memory, buffSize, memoryRead);
            //allRead = memoryRead.getValue() == 0;
            //System.out.println("read: " + memoryRead.getValue());
            //System.out.println();
        }
        return (char)memory.getShort(pos - startPoint);
    }

    public boolean isAllRead()
    {
        return allRead;
    }
    
    private boolean inRange(int pos)
    {
        return pos >= startPoint && pos < endPoint - 2;
    }
    
}
