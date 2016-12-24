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
package Language.Splitter;

import Language.Dictionary.DefTag;
import Language.Dictionary.Definition;
import Language.Dictionary.Dictionary;
import Language.Deconjugator.ValidWord;
import Language.Deconjugator.WordScanner;
import java.io.File;
import java.util.ArrayList;
import java.util.SortedSet;

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
    public ArrayList<FoundWord> split(String text)
    {
        return split(0, text);
    }
    public ArrayList<FoundWord> split(int off, String text)
    {
        //Can we do better than O(N^2)? Probably not needed but would be nice.
        //TODO: optimisations like give up on grammar
        
        ArrayList<FoundWord> words = new ArrayList<>();
        int pos = 0;
        int len = 0;
        FoundWord bestWord = new FoundWord("", null, 0, 0);
        while(pos < text.length())
        {
            try
            {
                WordScanner word = new WordScanner(text.substring(pos, pos + len));//may crash (in which case, decide the winner)
                FoundWord foundWord = new FoundWord(word.getWord(), pos + off, pos + len + off);//prototype definition
                for(ValidWord match:word.getMatches())//for each possible conjugation...
                {
                    
                    //System.out.println("serching for form that needs " + match.getNeededTags());
                    ArrayList<Definition> defs = dict.find(match.getWord());
                    if(defs != null)for(Definition def:defs)//for each possible definition...
                    {
                        //System.out.println("found def " + def);
                        //check if it meets the tag requirements of this conjugation:
                        boolean pass = true;
                        for(DefTag needed:match.getNeededTags())
                        {
                            if(def.getTags().contains(needed) == false)
                            {
                                if(match.getProcess().contains("negative") && needed == DefTag.adj_i)
                                {
                                    //when negative, it conjugates like an i adjective. do not reject!
                                }
                                else
                                {
                                    pass = false;
                                    break;
                                }
                            }
                        }
                        if(pass)//we've found a meaning for this word!
                        {
                            //System.out.println("REQUIREMENTS MET");
                            foundWord.addDefinition(new FoundDef(match, def));//add the definition and how we got the form for it
                        }
                    }
                }
                foundWord.sortDefs();//sort newly found definitions
                
                //a word is better if it's longer and has a definition
                if(foundWord.getText().length() >= bestWord.getText().length() && foundWord.getDefinitionCount() != 0)
                {
                    bestWord = foundWord;
                }
            }catch(StringIndexOutOfBoundsException e)
            {
                len = 0;
                pos += bestWord.getText().length();//skip ahead
                if(bestWord.getText().length() == 0)//didn't find it
                {
                    words.add(new FoundWord(text.charAt(pos) + "", null, pos + off, pos + 1 + off));//add it as a single character "word"
                    pos++;//skip this letter, not inportant
                }
                else
                {
                    words.add(bestWord);//only add actual definitions
                    bestWord = new FoundWord("", null, 0, 0);//reset
                }
            }
            len++;
        }
        return words;
    }
    public ArrayList<FoundWord> split(String text, SortedSet<Integer> breaks)
    {
        ArrayList<FoundWord> words = new ArrayList<>();
        int lastPos = 0;
        
        for(Integer pos:breaks)
        {
            String substr = text.substring(lastPos, pos);
            words.addAll(split(lastPos, substr));
            lastPos = pos;
        }
        if(lastPos != text.length())
        {
            String substr = text.substring(lastPos, text.length());
            words.addAll(split(lastPos, substr));
        }
        return words;
    }
}
