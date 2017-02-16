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

import java.util.ArrayList;

/**
 * Holds a rule for deconjugating a word
 * @author Laurens Weyn
 */
public class DeconRule
{
    private String ending, replace, change;
    private DefTag neededTag;
    public DeconRule(String ending, String replace, String change, DefTag neededTag)
    {
        this.ending = ending;
        this.replace = replace;
        this.change = change;
        this.neededTag = neededTag;
    }
    public DeconRule(String ending, String replace, String change)
    {
        this(ending, replace, change, null);
    }
    
    public ValidWord process(ValidWord word)
    {
        if(word.getProcess().contains(change))
        {
            return null;//don't stack the same conjugation onto itself
        }
        //ending matches:
        if(word.getWord().endsWith(ending))
        {
            //add tag and return
            ArrayList<DefTag> tags = (ArrayList<DefTag>)word.getNeededTags().clone();
            if(neededTag != null)tags.add(neededTag);
            return new ValidWord(word.getWord().substring(0, word.getWord().length() - ending.length()) + replace, tags, word.getProcess() + " " + change);
        }
        //doesn't match, don't add new word
        return null;
    }

    public String getEnding()
    {
        return ending;
    }

    public String getReplace()
    {
        return replace;
    }

    public String getChange()
    {
        return change;
    }

    public DefTag getNeededTag()
    {
        return neededTag;
    }
    
    
    
}
