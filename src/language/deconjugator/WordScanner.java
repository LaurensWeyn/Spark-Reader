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
import language.dictionary.Japanese;

import java.util.ArrayList;
import java.util.TreeSet;

import static language.dictionary.Japanese.isKana;
import static main.Main.options;

/**
 * Produces all possible deconjugations of a word for lookup in dictionaries
 * @author Laurens Weyn
 */

interface SubScanner
{
    void subinit();
    void ScanWord(String word);
}


public class WordScanner
{
    protected static ArrayList<ValidWord> matches;
    protected String word;

    protected static ArrayList<DeconRule> ruleList;

    static private SubScanner subscanner = null;
    static boolean parserIsNew = false;
    static void init()
    {
        if(subscanner == null || parserIsNew == options.getOptionBool("useOldParser"))
        {
            System.out.println("Reinitializing deconjugator");
            parserIsNew = !options.getOptionBool("useOldParser");
            ruleList = null;
            if(parserIsNew)
                subscanner = new WordScannerNew();
            else
                subscanner = new WordScannerOld();
            subscanner.subinit();
        }
    }

    // fixme? java uses code units, not codepoints, so this will never accept astral unicode characters
    public static boolean isAcceptableCharacter(char c)
    {
        return (isKana(c) || c=='来');
    }

    // build and return a list of the possible "original" endings for conjugations
    public static TreeSet<String> possibleEndings()
    {
        TreeSet<String> ret = new TreeSet<>();
        init();
        for(DeconRule rule:ruleList)
        {
            if(rule.getClass() == StdRule.class)
            {
                StdRule cast_rule = ((StdRule) rule);
                String ending = cast_rule.getReplace();
                if(!ending.equals("") && cast_rule.getNeededTag().getGroup() != -1)
                    ret.add(ending);
            }
        }
        return ret;
    }
    public WordScanner()
    {

    }
    public WordScanner(String word)
    {
        init();

        matches = new ArrayList<>();
        this.word = word;

        subscanner.ScanWord(word);
    }

    public ArrayList<ValidWord> getMatches()
    {
        return matches;
    }

    public String getWord()
    {
        return word;
    }
    
    public static void main(String[] args)
    {
        System.out.println();
        for(ValidWord vw: new WordScanner("分からない").getMatches())
        {
            System.out.println(vw.toString() + " " + vw.getNeededTags());
        }
    }
    
}
