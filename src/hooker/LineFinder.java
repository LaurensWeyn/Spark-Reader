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

import com.sun.jna.Pointer;
import language.dictionary.Japanese;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Laurens Weyn
 */
public class LineFinder implements Runnable
{
    Pointer process;
    private static final int BUFFSIZE = 1024 * 1024;
    public LineFinder(Pointer process)
    {
        this.process = process;
    }
    private HashMap<String, ArrayList<Integer>> lineMapping;
    private ArrayList<Integer> addresses = null;
    private ArrayList<LineListener> listeners = new ArrayList<>();
    public void addListener(LineListener listener)
    {
        listeners.add(listener);
    }
    
    private boolean runSearch = true;

    public void setRunSearch(boolean runSearch)
    {
        this.runSearch = runSearch;
    }
    
    @Override
    public void run()
    {
        if(hasAddresses())scan();
        else findAll();
    }
    /**
     * Scans know addresses for updated text
     * @return the current text if one line was found, null otherwise
     */
    public String scan()
    {
        lineMapping = new HashMap<>();
        MemoryBuffer buff = new MemoryBuffer(BUFFSIZE, process);
        for(int startAddr:addresses)
        {
            int addr = startAddr;
            char c = buff.getChar(addr);
            StringBuilder sb = new StringBuilder().append(c);
            addr += 2;
            while(c != 0)
            {
                if(!Character.isDefined(c))continue;//skip nonprinting character matches
                sb.append(c);
                c = buff.getChar(addr);
                addr += 2;
            }
            placeFoundLine(sb, startAddr);
        }
        if(lineMapping.size() == 1)return lineMapping.keySet().iterator().next();
        else return null;
    }
    /**
     * Search all memory for valid text
     */
    public void findAll()
    {
        lineMapping = new HashMap<>();
        MemoryBuffer buff = new MemoryBuffer(BUFFSIZE, process);
        int pos = 1;
        int startAddr = 1;
        StringBuilder sb = new StringBuilder();
        State state = State.waitOpen;
        while(runSearch && pos >= 0)
        {
            //System.out.println("read");
            char c = buff.getChar(pos);
            pos+= 2;
            switch(state)
            {
                case waitOpen:
                    if(c == OPEN)
                    {
                        sb = new StringBuilder().append(OPEN);
                        state = State.collectText;
                        startAddr = pos;//store this in case we match
                    }
                    break;
                case collectText:
                    if(c == CLOSE)
                    {
                        //end of sequence
                        sb.append(CLOSE);
                        state = State.nextNull;
                    }
                    else if(Japanese.isJapanese(c))
                    {
                        sb.append(c);
                        //state unchanged
                    }
                    else
                    {
                        //not Japanese, abort
                        state = State.waitOpen;
                    }
                    break;
                case nextNull:
                    if(c == 0)
                    {
                        //sequence end
                        placeFoundLine(sb, startAddr);
                        
                    }
                    else
                    {
                        //wasn't null, go back to waiting
                        state = State.waitOpen;
                    }
                    break;
            }
            //System.out.print(buff.getChar(pos++));
        }
        System.out.println("search done");
    }
    private void placeFoundLine(StringBuilder sb, int startAddr)
    {
        String resultText = sb.toString();
        if(!lineMapping.containsKey(resultText))
        {
            //give line listeners the new line
            for(LineListener ll:listeners)
            {
                ll.newLine(resultText);
            }
            lineMapping.put(resultText,new ArrayList<>());
        }
        lineMapping.get(resultText).add(startAddr);
    }
    char OPEN = '「';
    char CLOSE = '」';
    /**
     * Reset the LineFinder to search everything from the top
     */
    public void reset()
    {
        runSearch = true;
        lineMapping = new HashMap<>();
        addresses = null;
    }
    /**
     * Checks if we're doing a full search or not
     * @return 
     */
    public boolean hasAddresses()
    {
        return addresses != null;
    }
    /**
     * From the list of found text threads, select the valid one(s)
     * @param text the text the user has deemed valid
     */
    public void setValidText(String text)
    {
        if(!lineMapping.containsKey(text))
        {
            throw new IllegalArgumentException("No addresses pointing to text " + text + " found");
        }
        addresses = lineMapping.get(text);
    }
    /**
     * Get the address mapping(s) for the specified text
     * @param key the text the mapping is needed for
     * @return a list of addresses where this text can be found at
     */
    public List<Integer> getLineMapping(String key)
    {
        return lineMapping.get(key);
    }
    /**
     * Provides a list of all possible texts in the textbox. use getLineMapping to find their addresses
     * @return all unique text found
     */
    public Set<String> getPossibleMatches()
    {
        return lineMapping.keySet();
    }
    private enum State
    {
        waitOpen,
        collectText,
        nextNull;
    }
    public interface LineListener
    {
        void newLine(String line);
        
    }
}
