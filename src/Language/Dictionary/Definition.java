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
package Language.Dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds a word's definition and other properties.
 * @author laure
 */
public class Definition
{
    private String[] word, reading;// spellings;
    Set<DefTag> tags;
    
    private boolean showReading = true;
    private final int sourceNum;
    private int ID;
    
    //String[] meaningArr;
    String meaning;
    
    private static final Pattern FIND_TAGS = Pattern.compile("\\((.*?)\\)");//regex vodoo magic
    public Definition(String line, int sourceNum)
    {
        //parses EDICT2 definition lines
        //TODO make a parser for EPWING (easier said than done)
        //TODO deal with that (P) thing at the end properly (currently using workarounds)
        tags = new HashSet<>();
        this.sourceNum = sourceNum;
        
        String bits[] = line.split("/");
        String[] meaningArr = new String[bits.length - 2];
        
        
        //remove tags and process lines
        for (int i = 0; i < bits.length - 1; i++)
        {
            String defLine = bits[i];
            Matcher bracketFinder = FIND_TAGS.matcher(defLine);

            while (bracketFinder.find())
            {
               String tag = bracketFinder.group(1);
               int findCount = 0;
               for(String subTag:tag.split(","))
               {
                    DefTag found = DefTag.toTag(subTag);
                    if(found != null && tags.contains(found) == false)
                    {
                        tags.add(found);
                        findCount++;
                    }
               }
               if(findCount != 0 || i == 0)
               {
                   
                   defLine = defLine.replace("(" + tag + ")", "");
               }
               
            }
            
            if(i == 0)
            {
                String readingClasses[] = defLine.split("\\[");
                word = readingClasses[0].trim().split(";");//Kanji readings
                if(readingClasses.length == 2)//kana readings exist
                {
                    reading = readingClasses[1].replace("] ", "").replace("]", "").split(";");
                }else reading = new String[0];
            }else meaningArr[i - 1] = defLine.trim();
            
        }
        //reconstruct meaning list (save RAM)
        meaning = "";
        for(String part:meaningArr)
        {
            if(part.equals("(P)") == false)
            {
                if(part.equals(""))meaning += part;
                else meaning += "/" + part;
            }
        }
        //process ID
        String IDCode = bits[bits.length - 1].replaceFirst("Ent", "");
        try
        {
            ID = Integer.parseInt(IDCode);
        }catch(NumberFormatException e)
        {
            ID = IDCode.hashCode();
        }
        
        
        
        
    }
    public String getFurigana()
    {
        if(showReading && reading.length != 0)
        {
            return reading[0];
        }
        else return "";
    }

    public int getID()
    {
        return ID;
    }

    public int getSourceNum()
    {
        return sourceNum;
    }
    
    
    public Set<DefTag> getTags()
    {
        return tags;
    }

    public String[] getSpellings()
    {
        //now made on the fly on request instead of being stored
        String[] spellings = new String[word.length + reading.length];
        System.arraycopy(word, 0, spellings, 0, word.length);
        System.arraycopy(reading, 0, spellings, word.length, reading.length);
        return spellings;
    }

    public String[] getWord()
    {
        return word;
    }
    
    public String[] getMeaning()
    {
        return meaning.split("/");
    }
    public String getMeaningLine()
    {
        String result = meaning.replace("/", ", ").replace(", (", "<br>(");
        if(result.startsWith("<br>"))result = result.replaceFirst("<br>", "");
        if(result.startsWith(", "))result = result.replaceFirst(", ", "");//TODO clean this up
        return result;
    }
    public String[] getReadings()
    {
        return reading;
    }
    
    
    @Override
    public String toString()
    {
        return meaning;
    }
    
    
}
