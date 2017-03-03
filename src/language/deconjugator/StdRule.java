package language.deconjugator;

import language.dictionary.DefTag;
import java.util.HashSet;
import java.util.ArrayList;

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
    // Adds an inner-more conjugation
    public ValidWord process(ValidWord word)
    {
        if(word.equals("")) return null; // can't deconjugate emptiness
        // these numbers can probably be reduced a lot with no harm at all. however the first one has to be at least +1 and the second one is subjective.
        // don't allow the deconjugation to become too much longer than the original text
        if(word.getWord().length() > word.getOriginalWord().length()+10)
        {
            System.out.println("bogus length");
            System.out.println(word.getProcess());
            return null;
        }
        // don't allow the deconjugator to make impos sibly information-dense conjugations
        if(word.getNumConjugations() > word.getOriginalWord().length()+6)
        {
            System.out.println("bogus complexity");
            System.out.println(word.getProcess());
            return null;
        }
        //rule matches
        boolean deconjugates;
        if(word.getConjugationTags().size() > 0)
            deconjugates = word.getConjugationTags().get(word.getConjugationTags().size()-1).equals(impliedTag);
        else
            deconjugates = true;
        if(word.getWord().endsWith(ending) && deconjugates)
        {
            //add tag and return
            HashSet<DefTag> tags = new HashSet<>();
            ArrayList<DefTag> conjugationTags = new ArrayList<>(word.getConjugationTags());
            
            if(neededTag != null)
            {
                tags.add(neededTag);
                conjugationTags.add(neededTag);
            }
            
            String newProcess = word.getProcess();
            if(newProcess.equals(""))
                newProcess = change;
            else
                newProcess = newProcess + " " + change;

            return new ValidWord(word.getNumConjugations()+1, word.getOriginalWord(), word.getWord().substring(0, word.getWord().length() - ending.length()) + replace, tags, conjugationTags, newProcess);
        }
        //doesn't match, don't add new word
        return null;
    }
}
