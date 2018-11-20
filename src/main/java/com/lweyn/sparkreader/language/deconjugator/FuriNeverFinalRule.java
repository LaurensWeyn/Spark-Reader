package com.lweyn.sparkreader.language.deconjugator;

import com.lweyn.sparkreader.language.dictionary.DefTag;
import org.apache.log4j.Logger;

/**
 * Deconjugation rule which may never be the final rule (i.e. the last chronologically, the first one to be deconjugated).
 */
public class FuriNeverFinalRule extends NeverFinalRule
{
    private static Logger logger = Logger.getLogger(FuriNeverFinalRule.class);

    String kana_ending, kana_replace;
    public FuriNeverFinalRule(String ending, String kana_ending, String replace, String kana_replace, String change, DefTag neededTag, DefTag impliedTag)
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
