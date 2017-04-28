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
package language.dictionary;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds a word's definition and other properties obtained from the dictionary file.
 * @author Laurens Weyn
 */
public class EDICTDefinition extends Definition
{
    protected String[] word, reading;// spellings;
    protected Set<DefTag> tags;

    protected boolean showReading = true;
    protected final DefSource source;
    protected long ID;
    
    //String[] meaningArr;
    protected String meaning;
    
    private static final Pattern FIND_TAGS = Pattern.compile("\\((.*?)\\)");//regex vodoo magic
    public EDICTDefinition(String line, DefSource source)
    {
        //parses EDICT2 definition lines
        //TODO deal with that (P) thing at the end properly (currently using workarounds)
        tags = new HashSet<>();
        this.source = source;
        
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
                    if(found != null && !tags.contains(found))
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
        //reconstruct meaning list
        StringBuilder meaningBuilder = new StringBuilder();
        for(String part:meaningArr)
        {
            if(!part.equals("(P)"))
            {
                if(meaningBuilder.length() == 0)meaningBuilder.append(part);
                else meaningBuilder.append("/").append(part);
            }
        }
        meaning = meaningBuilder.toString();
        
        //process ID
        // The field has the format: EntLnnnnnnnnX.
        // The EntL is a unique string to help identify the field.
        // The "X", if present, indicates that an audio clip of the entry reading is available from the JapanesePod101.com site.
        String IDCode = bits[bits.length - 1].replaceFirst("EntL", "").replaceFirst("X", "");
        try
        {
            ID = Long.parseLong(IDCode);
        }catch(NumberFormatException e)
        {
            ID = IDCode.hashCode();
        }
        
        
        
        
    }
    @Override
    public String getFurigana()
    {
        if(showReading && reading.length != 0)
        {
            return reading[0];
        }
        else return "";
    }

    @Override
    public long getID()
    {
        return ID;
    }

    @Override
    public DefSource getSource()
    {
        return source;
    }
    
    @Override
    public Set<DefTag> getTags()
    {
        return tags;
    }

    @Override
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
    
    @Override
    public String[] getMeaning()
    {
        return meaning.split("/");
    }
    @Override
    public String getMeaningLine()
    {
        String result = meaning.replace("/", ", ").replace(", (", "<br>(");
        if(result.startsWith("<br>"))result = result.replaceFirst("<br>", "");
        if(result.startsWith(", "))result = result.replaceFirst(", ", "");//TODO clean this up
        if(result.endsWith(", "))result = result.substring(0, result.length() - 2);
        return result;
    }
    public String[] getReadings()
    {
        return reading;
    }
    
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(word[0]);
        if(reading != null && reading.length > 0)
        {
            builder.append("[").append(reading[0]).append("]");
        }
        builder.append(": ").append(meaning.split("/")[0]);
        return builder.toString();
    }
    
    
}
