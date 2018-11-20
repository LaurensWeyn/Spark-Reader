package com.lweyn.sparkreader.language.deconjugator;

import com.lweyn.sparkreader.language.dictionary.DefTag;
import org.apache.log4j.Logger;

/**
 * Deconjugation rule with arbitrary access to context to invalidate itself.
 */
public class FuriContextRule extends ContextRule
{
    private static Logger logger = Logger.getLogger(FuriContextRule.class);

    String kana_ending, kana_replace;
    public FuriContextRule(String ending, String kana_ending, String replace, String kana_replace, String change, DefTag neededTag, DefTag impliedTag, ContextChecker checker)
    {
        super(ending, replace, change, neededTag, impliedTag, checker);
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
            logger.error("Problem!");
            logger.error(word);
            logger.error(ending);
            logger.error(replace);
            logger.error(kana_ending);
            logger.error(kana_replace);
            return null;
        }
    }
}
