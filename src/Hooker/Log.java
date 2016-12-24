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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 *
 * @author Laurens Weyn
 */
public class Log
{
    LinkedList<String> log;
    int maxLen;
    ListIterator<String> pos;
    String lastLine = "";
    boolean lastDirBack = true;
    public Log(int maxLen)
    {
        this.maxLen = maxLen;
        log = new LinkedList<>();
    }
    
    public void addLine(String line)
    {
        log.addFirst(line);
        if(log.size() > maxLen)log.removeLast();
        
        pos = log.listIterator();
        pos.next();//skip over this element
        lastDirBack = true;
        
    }
    public String back()
    {
        if(!lastDirBack)
        {
            pos.next();
            lastDirBack = true;
        }
        if(pos.hasNext())
        {
            return pos.next();
        }
        else
        {
            pos.previous();
            return pos.next();
        }
    }
    public String forward()
    {
        if(lastDirBack)
        {
            pos.previous();
            lastDirBack = false;
        }
        if(pos.hasPrevious())
        {
            return pos.previous();
        }
        else
        {
            pos.next();
            return pos.previous();
        }
    }
    public int linePos(String line)
    {
        int index = log.lastIndexOf(line);//TODO is this counting from the right side?
        if(index == -1)return -1;
        else return log.size() - index - 1;
    }
    public int getSize()
    {
        return log.size();
    }
}
