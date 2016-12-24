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
package Language.Deconjugator;

import Language.Dictionary.DefTag;
import java.util.ArrayList;

/**
 * Holds a possible valid conjugation if a word exists with the valid tags
 * @author Laurens Weyn
 */
public class ValidWord
{
    private String word;
    private ArrayList<DefTag> neededTags;
    private String process;

    public ValidWord(String word, ArrayList<DefTag> neededTags, String process)
    {
        this.word = word;
        this.neededTags = neededTags;
        this.process = process.trim();
        if(process.contains("i stem"))
        {
            neededTags.remove(DefTag.v5s);//"su" ending not needed anymore (note: su ending i stem seceretly doesn't produce exactly "i stem")
        }
    }
    public ValidWord(String word, DefTag neededTag, String process)
    {
        this.word = word;
        neededTags = new ArrayList<>();
        this.process = process.trim();
        if(neededTag != null)neededTags.add(neededTag);
    }
    public ValidWord(String word, String process)
    {
        this.word = word;
        neededTags = new ArrayList<>();
        this.process = process;
    }
    public String getWord()
    {
        return word;
    }

    public ArrayList<DefTag> getNeededTags()
    {
        return neededTags;
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
