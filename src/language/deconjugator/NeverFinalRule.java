package language.deconjugator;

import language.dictionary.DefTag;

/**
 * Deconjugation rule which may never be the final rule (i.e. the last chronologically, the first one to be deconjugated).
 */
public class NeverFinalRule extends StdRule
{
    public NeverFinalRule(String ending, String replace, String change, DefTag neededTag, DefTag impliedTag)
    {
        super(ending, replace, change, neededTag, impliedTag);
    }

    // Verifies and adds this rule as an inner-more conjugation in a deconjugated word
    public ValidWord process(ValidWord word)
    {
        if(word.getSeenForms().size()==0) return null;

        return super.process(word);
    }
}
