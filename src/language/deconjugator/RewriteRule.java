package language.deconjugator;

import language.dictionary.DefTag;

/**
 * Deconjugation rule which may only replace an entire word.
 */
public class RewriteRule extends StdRule
{
    public RewriteRule(String ending, String replace, String change, DefTag neededTag, DefTag impliedTag)
    {
        super(ending, replace, change, neededTag, impliedTag);
    }

    // Verifies and adds this rule as an inner-more conjugation in a deconjugated word
    public ValidWord process(ValidWord word)
    {
        if(!word.getWord().equals(this.ending)) return null;

        return super.process(word);
    }
}
