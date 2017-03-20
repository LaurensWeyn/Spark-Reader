package language.deconjugator;

import language.dictionary.DefTag;

/**
 * Deconjugation rule with arbitrary access to context to invalidate itself.
 */
public class ContextRule extends StdRule
{
    public interface ContextChecker {
        boolean l(StdRule it, ValidWord conjugated_term);
    }
    private ContextChecker checker;
    public ContextRule(String ending, String replace, String change, DefTag neededTag, DefTag impliedTag, ContextChecker checker)
    {
        super(ending, replace, change, neededTag, impliedTag);
        this.checker = checker;
    }

    // Verifies and adds this rule as an inner-more conjugation in a deconjugated word
    public ValidWord process(ValidWord word)
    {
        if(!checker.l(this, word)) return null;

        return super.process(word);
    }
}
