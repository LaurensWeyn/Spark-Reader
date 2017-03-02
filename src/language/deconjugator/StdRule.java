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
    // Adds inner-more (closer to base) meaning tag
    // So, "(infinitive)" will get added after "past"
    public ValidWord process(ValidWord word)
    {
        // these numbers can probably be reduced a lot with no harm at all. however the first one has to be at least +1 and the second one is subjective.
        // don't allow the deconjugation to become too much longer than the original text
        if(word.getWord().length() > word.getOriginalWord().length()+10)
        {
            return null;
        }
        // don't allow the deconjugator to make impossibly information-dense conjugations
        if(word.getNeededTags().size() > word.getOriginalWord().length()+6)
        {
            return null;
        }
        //ending matches:
        if(word.getWord().endsWith(ending))
        {
            //add tag and return
            HashSet<DefTag> tags = new HashSet<DefTag>(word.getNeededTags());
            HashSet<DefTag> impliedTags = new HashSet<DefTag>(word.getImpliedTags());
            
            if(neededTag != null)tags.add(neededTag);
            if(impliedTag != null)impliedTags.add(impliedTag);
            
            String newProcess = word.getProcess();
            if(newProcess.equals(""))
                newProcess = change;
            else
                newProcess = newProcess + " " + change;

            return new ValidWord(word.getOriginalWord(), word.getWord().substring(0, word.getWord().length() - ending.length()) + replace, tags, impliedTags, newProcess);
        }
        //doesn't match, don't add new word
        return null;
    }
}
