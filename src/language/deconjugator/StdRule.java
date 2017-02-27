package language.deconjugator;

import language.dictionary.DefTag;
import java.util.HashSet;

/**
 * Original (legacy) deconjugation rule.
 * Created by Laurens on 2/18/2017.
 */
public class StdRule implements DeconRule
{
    private String ending, replace, change;
    private DefTag neededTag, impliedTag;
    public StdRule(String ending, String replace, String change, DefTag neededTag)
    {
        this(ending, replace, change, neededTag, null);
    }
    public StdRule(String ending, String replace, String change, DefTag neededTag, DefTag impliedTag)
    {
        this.ending = ending;
        this.replace = replace;
        this.change = change;
        this.neededTag = neededTag;
        this.impliedTag = impliedTag;
    }
    public StdRule(String ending, String replace, String change)
    {
        this(ending, replace, change, null);
    }

    @Override
    public ValidWord process(ValidWord word)
    {
        if(word.getProcess().contains(change))
        {
            return null;//don't stack the same conjugation onto itself
        }
        //ending matches:
        if(word.getWord().endsWith(ending))
        {
            //add tag and return
            HashSet<DefTag> tags = new HashSet<DefTag>(word.getNeededTags());
            HashSet<DefTag> impliedTags = new HashSet<DefTag>(word.getImpliedTags());
            if(neededTag != null)tags.add(neededTag);
            if(impliedTag != null)impliedTags.add(impliedTag);
            String newProcess = word.getProcess() + " " + change;
            return new ValidWord(word.getWord().substring(0, word.getWord().length() - ending.length()) + replace, tags, impliedTags, newProcess);
        }
        //doesn't match, don't add new word
        return null;
    }
}
