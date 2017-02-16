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

import fuku.eb4j.Result;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.Hook;

import java.util.HashSet;

/**
 *
 * @author Laurens Weyn
 */
public class EPWINGDefinition extends Definition
{
    String[] spellings;
    String[] defLines;
    SubBook book;
    long id;
    public EPWINGDefinition(Result result, SubBook book, HashSet<Character> blacklist)
    {
        this.book = book;
        id = result.getTextPosition();//guaranteed to be unique within book at least
        
        Hook hook = new EpwingHook(book, blacklist);
        String lines[] = (String[])hook.getObject();
        
        spellings = Japanese.splitJapaneseWriting(lines[0]);
        defLines = new String[lines.length - 1];
        System.arraycopy(lines, 0, defLines, 1, defLines.length);
    }
    @Override
    public String getFurigana()
    {
        for(String spelling:spellings)
        {
            if(Japanese.isKana(spelling))return spelling;
        }
        return "";//TODO or is it null?
    }

    @Override
    public int getID()
    {
        return (int)(id);//TODO encode source here?
    }

    @Override
    public int getSourceNum()
    {
        return 3;//TODO depend on subBook
    }

    @Override
    public String[] getSpellings()
    {
        return spellings;
    }

    @Override
    public String[] getMeaning()
    {
        return defLines;
    }

    @Override
    public String getMeaningLine()
    {
        StringBuilder sb = new StringBuilder();
        for(String line:defLines)
        {
            if(sb.length() == 0)sb.append(line);
            sb.append("\n").append(line);
        }
        return sb.toString();
    }
    
}
