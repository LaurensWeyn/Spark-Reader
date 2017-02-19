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
package language.deconjugator;

import language.dictionary.DefTag;
import language.dictionary.Definition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Holds a possible valid conjugation if a word exists with the valid tags
 * @author Laurens Weyn
 */
public class ValidWord
{
    private String word;
    private Set<DefTag> neededTags, impliedTags;
    private String process;

    public ValidWord(String word, Set<DefTag> neededTags, Set<DefTag> impliedTags, String process)
    {
        this.word = word;
        this.neededTags = neededTags;
        this.impliedTags = impliedTags;
        this.process = process.trim();
        if(process.contains("i stem"))
        {
            neededTags.remove(DefTag.v5s);//"su" ending not needed anymore (note: su ending i stem seceretly doesn't produce exactly "i stem")
        }
    }
    public ValidWord(String word, DefTag neededTag, String process)
    {
        this.word = word;
        neededTags = new HashSet<>();
        impliedTags = new HashSet<>();
        this.process = process.trim();
        if(neededTag != null)neededTags.add(neededTag);
    }
    public ValidWord(String word, String process)
    {
        this.word = word;
        neededTags = new HashSet<>();
        impliedTags = new HashSet<>();
        this.process = process;
    }
    public String getWord()
    {
        return word;
    }

    public Set<DefTag> getNeededTags()
    {
        return neededTags;
    }

    public Set<DefTag> getImpliedTags()
    {
        return impliedTags;
    }

    /**
     * Checks if a definition is valid for this word, assuming this matches one of the spellings
     * @param def the definition to check
     * @return true if this is a valid definition, false otherwise
     */
    public boolean defMatches(Definition def)
    {

        if(def.getTags() == null && getNeededTags().isEmpty())return true;//still accept if no tags needed
        else if (def.getTags() == null)return false;//does not have needed tags

        for(DefTag needed:getNeededTags())
        {
            if(!def.getTags().contains(needed) &&!getImpliedTags().contains(needed))
            {
                return false;
            }
        }
        return true;
    }

    public String getProcess()
    {
        return process;
    }

    @Override
    public String toString()
    {
        return word + "(" + process + ")";
    }
    
    
}
