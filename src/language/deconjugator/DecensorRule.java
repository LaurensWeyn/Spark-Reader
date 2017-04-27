package language.deconjugator;

import java.util.LinkedList;

/**
 * Replaces maru characters with kana characters.
 * Simple but surprisingly effective for simple cases of censorship in text.
 */
public class DecensorRule implements DeconRule
{
    private char replace;

    public DecensorRule(char replace)
    {
        this.replace = replace;
    }

    @Override
    public ValidWord process(ValidWord word)
    {
        if(word.getNumConjugations() != 0)return null;//only work on base form
        if(!word.getWord().contains("○"))return null;

        LinkedList<DeconRule> process = new LinkedList<>();
        process.add(this);
        return new ValidWord(word.getWord().replace('○', replace), process);
    }

    @Override
    public String getProcessName()
    {
        return "censored";
    }

    @Override
    public String conjugate(String word)
    {
        return word.replace(replace, '○');
    }
}
