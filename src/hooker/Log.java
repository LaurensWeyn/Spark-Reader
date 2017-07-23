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

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Holds the VN backlog. Essentially an abstracted LinkedList iterator.
 * @author Laurens Weyn
 */
public class Log
{
    private LinkedList<String> log;
    private final int maxLen;
    private ListIterator<String> pos;
    private boolean lastDirBack = true;

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

    /**
     * Find the position of a line in the backlog
     * @param line The line to search for
     * @return -1 if line not found, index in log if found
     */
    public int linePos(String line)
    {
        return log.indexOf(line);
    }

    public int getSize()
    {
        return log.size();
    }

    /**
     * Gets the most recent line in the backlog. Does not effect the current position in the log.
     * @return The most recently added line, or a blank String if the log is empty
     */
    public String mostRecent()
    {
        if(log.isEmpty())return "";
        else return log.getFirst();
    }
}
