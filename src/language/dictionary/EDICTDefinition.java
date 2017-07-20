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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds a word's definition and other properties obtained from the dictionary file.
 * @author Laurens Weyn
 */
public class EDICTDefinition extends Definition
{
    private class TagInfo
    {
        Set<DefTag> tags;
        Set<String> strings;
        String text;
        TagInfo(String s)
        {
            tags = new HashSet<>();
            strings = new HashSet<>();
            text = s;
            Matcher bracketFinder = FIND_TAGS.matcher(text);

            while (bracketFinder.find())
            {
                String tag = bracketFinder.group(1);
                for(String subTag:tag.split(","))
                {
                    DefTag found = DefTag.toTag(subTag);
                    if(found == null)
                        strings.add(subTag);
                    else if(!tags.contains(found))
                        tags.add(found);
                }
                text = text.replaceFirst(Pattern.quote("(" + tag + ")"), "");
            }
        }
    }
    public class TaggedReading
    {
        String reading;
        Set<DefTag> tags;
        Set<String> restrictSpelling;
        TaggedReading(String spelling, Set<String> restrictSpelling, Set<DefTag> tags)
        {
            reading = spelling;
            this.restrictSpelling = restrictSpelling;
            this.tags = tags;
        }
        
        @Override
        public String toString()
        {
            return reading;
        }
    }
    public class TaggedSpelling
    {
        String word;
        ArrayList<TaggedReading> readings;
        boolean isCommon;
        String tagstring;
        Set<DefTag> tags;
        TaggedSpelling(String spelling, Set<DefTag> tags)
        {
            word = spelling;
            if(word.equals("皆"))
            {
                System.out.println("minna");
                System.out.println(tags);
            }
            this.readings = new ArrayList<>();
            this.tags = tags;
            
            if(tags != null)
            {
                for(DefTag t : tags)
                {
                    if(t == DefTag.P)
                        isCommon = true;
                }
                if(tags.size() == 0)
                    tagstring = spelling;
                else
                {
                    StringJoiner joiner = new StringJoiner(")(", "(", ")");
                    for(DefTag t : tags) joiner.add(t.toString());
                    tagstring = spelling + " " + joiner.toString();
                }
            }
        }
    }
    protected String[] word, reading;
    protected Set<DefTag> tags;

    protected boolean showReading = true;
    protected final DefSource source;
    protected long ID;
    
    protected String meaning;
    
    private static final Pattern FIND_TAGS = Pattern.compile("\\((([^()]*)|(([^(]*\\([^)]*\\)[^(]*)*))\\)");//regex voodoo magic

    public EDICTDefinition(String line, DefSource source)
    {
        //parses EDICT2 definition lines
        tags = new HashSet<>();
        this.source = source;
        
        String bits[] = line.split("/");
        String[] meaningArr = new String[bits.length - 2];
        
        //remove tags and process lines
        for (int i = 0; i < bits.length - 1; i++)
        {
            if(i == 0)
            {
                MakeDefinitions(bits[i]);
            }
            else
            {
                TagInfo info = new TagInfo(bits[i]);
                tags.addAll(info.tags);
                meaningArr[i - 1] = info.text.trim();
            }
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
            System.out.println("WARN: cannot format EDICT ID " + bits[bits.length - 1]);
            ID = Math.abs(IDCode.hashCode());
        }

    }
    
    Map<String, TaggedSpelling> spellings;
    ArrayList<String> cleanOrderedSpellings;
    ArrayList<TaggedReading> readings;
    // takes a full unchanged spelling-reading string from edict, e.g. "障害(P);障がい;障碍;障礙 [しょうがい(P);しょうげ(障碍,障礙)]"
    private void MakeDefinitions(String format)
    {
        String readingClasses[] = format.split("\\[");
        word = readingClasses[0].trim().split(";");//Kanji readings
        if(readingClasses.length == 2)//kana readings exist
            reading = readingClasses[1].replace("] ", "").replace("]", "").split(";");
        else
            reading = new String[0];
        
        spellings = new HashMap<>();
        readings = new ArrayList<>();
        cleanOrderedSpellings = new ArrayList<>();
        
        for(String s : word)
        {
            TagInfo info = new TagInfo(s);
            TaggedSpelling spelling = new TaggedSpelling(info.text, info.tags);
            spellings.put(spelling.word, spelling);
            cleanOrderedSpellings.add(spelling.word);
        }
        for(String s : reading)
        {
            TagInfo info = new TagInfo(s);
            TaggedReading reading = new TaggedReading(info.text, info.strings, info.tags);
            readings.add(reading);
            if(reading.restrictSpelling.size() == 0)
            {
                for(Map.Entry<String, TaggedSpelling> entry : spellings.entrySet())
                {
                    TaggedSpelling spelling = entry.getValue();
                    spelling.readings.add(reading);
                }
            }
            else
            {
                for(String text : reading.restrictSpelling)
                {
                    if(!spellings.containsKey(text)) continue;
                    TaggedSpelling spelling = spellings.get(text);
                    
                    spelling.readings.add(reading);
                }
            }
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
    
    public String getFurigana(String text)
    {
        if(showReading && reading.length != 0)
        {
            if(spellings.containsKey(text))
                return spellings.get(text).readings.get(0).reading;
            else
                return getFurigana();
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
        ArrayList<String> readings = new ArrayList<>();
        HashSet<String> already_added_readings = new HashSet<>();
        if(showReading)
        {
            if(reading.length != 0)
            {
                for(Map.Entry<String, TaggedSpelling> entry : spellings.entrySet())
                {
                    String spelling = entry.getValue().word;
                    if(!already_added_readings.contains(spelling))
                    {
                        readings.add(spelling);
                        already_added_readings.add(spelling);
                    }
                    
                    // Edict has a couple malformed entries where not all spellings are given readings
                    // like １コマ;一コマ;１こま;一こま;一齣;一駒(iK) [ひとコマ(一コマ);ひとこま(一こま,一齣,一駒)]  
                    if(entry.getValue().readings.size() == 0) continue;
                        
                    for(TaggedReading taggedreading : entry.getValue().readings)
                    { 
                        String reading = taggedreading.reading;
                        if(!already_added_readings.contains(reading))
                        {
                            readings.add(reading);
                            already_added_readings.add(reading);
                        }
                    }
                }
            }
            else
            {
                for(Map.Entry<String, TaggedSpelling> entry : spellings.entrySet())
                {
                    String text = entry.getValue().word;
                    if(text.equals(Japanese.toHiragana(text, true)))
                        readings.add(text);
                    if(text.equals(Japanese.toKatakana(text, true)))
                        readings.add(text);
                }
            }
        }
        return readings.toArray(new String[0]);
    }
    public String[] getSpellings(String text)
    {
        ArrayList<String> readings = new ArrayList<>();
        HashSet<String> already_added_readings = new HashSet<>();
        if(showReading && reading.length != 0)
        {
            for(Map.Entry<String, TaggedSpelling> entry : spellings.entrySet())
            {
                String spelling = entry.getValue().word;
                if(!already_added_readings.contains(spelling))
                {
                    readings.add(spelling);
                    already_added_readings.add(spelling);
                }
            }
            if(spellings.get(text) != null)
            {
                if(spellings.get(text).readings.size() != 0) 
                {
                    String reading = spellings.get(text).readings.get(0).reading;
                    if(!already_added_readings.contains(reading))
                    {
                        readings.add(reading);
                        already_added_readings.add(reading);
                    }
                }
            }
            else
            {
                readings.add(text);
                if(!already_added_readings.contains(text))
                {
                    readings.add(text);
                    already_added_readings.add(text);
                }
            }
        }
        return readings.toArray(new String[0]);
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
