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
package language.splitter;

import language.deconjugator.ValidWord;
import language.deconjugator.WordScanner;
import language.dictionary.*;
import language.dictionary.Dictionary;

import java.util.*;

/**
 * Takes in text and splits it into individual words
 * @author Laurens Weyn
 */
public class WordSplitter
{
    private final Dictionary dict;

    public WordSplitter(Dictionary dict)
    {
        this.dict = dict;
    }
    public static void recalcPositions(List<FoundWord> words)
    {
        int x = 0;
        for(FoundWord word:words)
        {
            word.setStartX(x);
            x = word.endX();
        }
    }
    private List<FoundWord> splitSection(String text, boolean firstSection)
    {
        ArrayList<FoundWord> words = new ArrayList<>();
        int start = 0;
        //until we've covered all words
        while(start < text.length())
        {
            int pos = text.length();
            FoundWord matchedWord = null;
            //until we've tried all lengths and failed
            while(pos > start)
            {
                WordScanner word = new WordScanner(text.substring(start, pos));//deconjugate
                matchedWord = new FoundWord(word.getWord());//prototype definition
                attachDefinitions(matchedWord, word);//add cached definitions
                attachEpwingDefinitions(matchedWord, word);//add epwing definitions

                if(matchedWord.getDefinitionCount() == 0)
                {
                    matchedWord = null;
                    pos--;//try shorter word
                }
                else//found a word
                {
                    start = pos;//start next definition from here
                    break;//stop searching and add this word
                }
            }
            if(matchedWord == null)//if we failed
            {
                words.add(new FoundWord(text.charAt(start) + ""));//add the character as an 'unknown word'
                firstSection = false;
                start++;
            }
            else words.add(matchedWord);

        }
        return words;
    }

    private void attachDefinitions(FoundWord word, WordScanner conjugations)
    {
        for(ValidWord match:conjugations.getMatches())//for each possible conjugation...
        {
            List<Definition> defs = dict.find(match.getWord());
            if(defs != null)for(Definition def:defs)//for each possible definition...
            {
                //check if it meets the tag requirements of this conjugation
                if(match.defMatches(def))
                {
                    word.addDefinition(new FoundDef(match, def));//add the definition and how we got the form for it
                }
            }
        }
    }
    private void attachEpwingDefinitions(FoundWord word, WordScanner conjugations)
    {
        //build a list of all known valid tags
        Set<DefTag> knownTags = new HashSet<>();
        if(word.getFoundDefs() != null)for(FoundDef foundDef:word.getFoundDefs())
        {
            Set<DefTag> tags = foundDef.getDefinition().getTags();
            if(tags != null)knownTags.addAll(tags);
        }
        for(ValidWord match:conjugations.getMatches())//for each possible conjugation...
        {
            List<EPWINGDefinition> defs = dict.findEpwing(match.getWord());
            if(defs != null)for(EPWINGDefinition def:defs)//for each possible definition...
            {
                def.setTags(knownTags);//set all valid tags for matching
                //check if it meets the tag requirements of this conjugation
                if(match.defMatches(def))
                {
                    //TODO let user decide on tag list?
                    def.setTags(match.getNeededTags());//set tag list to needed tags for user's info
                    word.addDefinition(new FoundDef(match, def));//add the definition and how we got the form for it
                }
            }
        }
    }

    public List<FoundWord> split(String text, Set<Integer> breaks)
    {
        System.out.println("reflow, breaks " + breaks);
        ArrayList<FoundWord> words = new ArrayList<>();
        int pos = 0;
        int start = 0;
        while(pos < text.length())
        {
            if(breaks.contains(pos))
            {
                String section = text.substring(start, pos);
                words.addAll(splitSection(section, true));
                start = pos;
            }
            else if(!Japanese.isJapaneseWriting(text.charAt(pos)))
            {
                String section = text.substring(start, pos);
                words.addAll(splitSection(section, false));
                words.add(new FoundWord(text.charAt(pos) + ""));
                pos++;//skip over grammar
                start = pos;
            }
            pos++;
        }
        recalcPositions(words);
        return words;
    }

    public static void main(String[] args)
    {
        new WordSplitter(null).split("こんにちは、世界！", new HashSet<>());
    }
}
