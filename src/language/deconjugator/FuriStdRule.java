package language.deconjugator;

import language.dictionary.DefTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Original (legacy) deconjugation rule.
 * Created by Laurens on 2/18/2017.
 */
public class FuriStdRule extends StdRule
{
    String kana_ending, kana_replace;
    public FuriStdRule(String ending, String kana_ending, String replace, String kana_replace, String change, DefTag neededTag, DefTag impliedTag)
    {
        super(ending, replace, change, neededTag, impliedTag);
        this.kana_ending = kana_ending;
        this.kana_replace = kana_replace;
    }
    
    public String conjugate(String word)
    {
        if(word == null)
            return "";
        if(word.endsWith(replace))
            return word.substring(0, word.length() - replace.length()) + ending;
        else if (word.endsWith(kana_replace))
            return word.substring(0, word.length() - kana_replace.length()) + kana_ending;
        else
        {
            System.out.println("Problem!");
            System.out.println(word);
            System.out.println(ending);
            System.out.println(replace);
            System.out.println(kana_ending);
            System.out.println(kana_replace);
            return null;
        }
    }
}
