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

import language.deconjugator.ValidWord;
import language.dictionary.JMDict.Sense;
import language.dictionary.JMDict.Spelling;

import java.util.*;

/**
 *
 * @author Laurens Weyn
 */
public abstract class Definition
{
    public abstract String getFurigana(ValidWord context);

    /**
     * Get the unique ID of this definition.
     * @return the ID
     */
    public abstract long getID();

    /**
     * Get the dictionary this definition was sourced from
     * @return the source of this entry
     */
    public abstract DefSource getSource();

    /**
     * Get a list of all possible spellings
     * @return all spellings associated with this definition
     */
    public abstract Spelling[] getSpellings();

    /**
     * Get a list of all spellings valid for the given context
     * @param context the context the word appears in
     * @return a list of relevant spellings
     */
    public List<Spelling> getSpellings(ValidWord context)
    {
        return Arrays.asList(getSpellings());
    }

    public abstract Sense[] getMeanings();

    public List<Sense> getMeanings(ValidWord context)
    {
        List<Sense> meanings = new ArrayList<>();
        for(Sense sense:getMeanings())
        {
            if(sense.getRestrictedSpellings() != null)
            {
                boolean match = false;
                for(Spelling restrict:sense.getRestrictedSpellings())
                {
                    if(restrict.getText().equals(context.getWord()))
                    {
                        match = true;
                        break;
                    }
                }
                if(!match)continue;//invalid; skip
            }
            meanings.add(sense);
        }
        return meanings;
    }
    
    public String getMeaningLine()
    {
        Sense[] senses = getMeanings();
        if(senses.length == 0)return null;
        if(senses.length == 1)return senses[0].getMeaningAsLine();
        StringBuilder output = new StringBuilder("1) " + senses[0].getMeaningAsLine());
        for(int i = 1; i < senses.length; i++)
        {
            output.append('\n').append(i + 1).append(") ").append(senses[i].getMeaningAsLine());
        }
        return output.toString();
    }

    public Set<DefTag> getTags()
    {
        return null;
    }
    
    public Set<DefTag> getTags(ValidWord context)
    {
        return getTags();
    }


    public String getTagLine()
    {
        Set<DefTag> tags = getTags();
        if(tags == null)return null;

        StringBuilder tagList = new StringBuilder();
        for(DefTag tag:tags)
        {
            if(tag != null)
                tagList.append(tag).append(" ");
        }
        return tagList.toString().trim();
    }


}
