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

import java.util.*;

/**
 * Holds a possible valid conjugation if a word exists with the valid tags
 * @author Laurens Weyn
 */
public class ValidWord
{
    private String word, originalWord;
    private Set<DefTag> neededTags;
    private Set<String> seenForms;
    private List<DefTag> conjugationTags;
    private List<DeconRule> process;

    public ValidWord(String originalWord, String word, HashSet<String> seenForms, Set<DefTag> neededTags,
                     ArrayList<DefTag> conjugationTags, List<DeconRule> process)
    {
        this.originalWord = originalWord;
        this.seenForms = seenForms;
        this.conjugationTags = conjugationTags;
        this.word = word;
        this.neededTags = neededTags;
        this.process = process;
    }
    public ValidWord(String word)
    {
        this(word, new LinkedList<>());
    }
    public ValidWord(String word, LinkedList<DeconRule> process)
    {
        this.originalWord = word;
        this.word = word;
        this.neededTags = new HashSet<>();
        this.seenForms = new HashSet<>();
        this.conjugationTags = new ArrayList<>();
        this.process = process;
    }
    public int getNumConjugations()
    {
        return process.size();
    }
    public String getOriginalWord()
    {
        return originalWord;
    }
    public String getWord()
    {
        return word;
    }

    public Set<DefTag> getNeededTags()
    {
        return neededTags;
    }
    public Set<String> getSeenForms()
    {
        return seenForms;
    }
    public List<DefTag> getConjugationTags()
    {
        return conjugationTags;
    }

    boolean hasSeenForm(String test)
    {
        return seenForms.contains(test);
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
            if(needed.toString().equals("")) return false;
            if(!def.getTags().contains(needed))
            {
                return false;
            }
        }
        return true;
    }
    public List<DeconRule> getProcess()
    {
        return process;
    }
    public String getProcessText()
    {
        StringBuilder text = new StringBuilder();
        int i = 1;
        for(DeconRule rule:process)
        {
            //attempt to copy old process toString
            if(i == process.size())//last item
            {
                //TODO make these braces a 'hidden' flag or something instead of literal text
                if(rule.getProcessName().startsWith("("))
                {
                    if(text.length() != 0)text.append(' ');
                    text.append(rule.getProcessName().replace("(", "").replace(")",""));
                }
                else
                {
                    if(text.length() != 0)text.append(' ');
                    text.append(rule.getProcessName());
                }
            }
            else if(!rule.getProcessName().startsWith("("))
            {
                if(text.length() != 0)text.append(' ');
                text.append(rule.getProcessName());
            }
            i++;
        }
        return text.toString();
    }

    @Override
    public String toString()
    {
        // hide non-freestanding weak forms and remove parens from freestanding ones
        /*String temp = process;
        if(temp.startsWith("("))
        {
            temp = temp.replaceFirst("[(]", "");
            temp = temp.replaceFirst("[)]", "");
        }
        temp = temp.replaceAll("[(].*?[)]", "");*/

        return word + "―" + getProcessText();
    }
}
