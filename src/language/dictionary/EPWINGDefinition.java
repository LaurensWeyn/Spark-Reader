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

import fuku.eb4j.EBException;
import fuku.eb4j.Result;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.Hook;
import main.Main;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Laurens Weyn
 */
public class EPWINGDefinition extends Definition
{
    private String[] spellings;
    private String[] defLines;
    private SubBook book;
    private DefSource source;
    private long id;
    private static Set<Character> blacklist = new HashSet<>();
    private Set<DefTag> tags = null;

    public static void setBlacklist(Set<Character> blacklist)
    {
        EPWINGDefinition.blacklist = blacklist;
    }

    public static void loadBlacklist()
    {
        String option = Main.options.getOption("epwingStartBlacklist");
        blacklist = new HashSet<>();
        for(int i = 0; i < option.length(); i++)
        {
            blacklist.add(option.charAt(i));
        }
    }

    public EPWINGDefinition(Result result, SubBook book, DefSource source)throws EBException
    {
        this.book = book;
        this.source = source;
        id = result.getTextPosition();//guaranteed to be unique within book at least
        
        Hook hook = new EpwingAdapter(book, new HashSet<>());
        result.getText(hook);
        String lines[] = (String[])hook.getObject();
        if(lines.length >= Main.options.getOptionInt("epwingBlacklistMinLines"))//safe to filter
        {
            //Blacklist must've blocked the only text. Re-parse without blacklist
            hook = new EpwingAdapter(book, blacklist);
            result.getText(hook);
            lines = (String[])hook.getObject();
        }

        if(lines.length == 0)
        {
            throw new Error("EPWING dictionary gave no text in response");
        }
        else
        {
            defLines = lines;
            //first line presumably contains spellings
            spellings = Japanese.splitJapaneseWriting(lines[0]);
        }

    }
    @Override
    public String getFurigana()
    {
        for(String spelling:spellings)
        {
            if(!Japanese.hasKanji(spelling))return spelling;
        }
        return "";
    }

    @Override
    public long getID()
    {
        return id ^ (long)book.getTitle().hashCode();
    }

    @Override
    public DefSource getSource()
    {
        return source;
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
            sb.append("<br>").append(line);
        }
        return sb.toString();
    }

    @Override
    public Set<DefTag> getTags()
    {
        return tags;
    }

    /**
     * Assign tags to this EPWING definition (can have none by default)
     * @param tags tags to assign
     */
    public void setTags(Set<DefTag> tags)
    {
        this.tags = tags;
    }
}
