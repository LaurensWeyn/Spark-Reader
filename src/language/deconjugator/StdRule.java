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
    protected String ending, replace, change;
    protected DefTag neededTag, impliedTag;
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

    public String getReplace()
    {
        return replace;
    }

    public DefTag getNeededTag()
    {
        return neededTag;
    }

    @Override
    // Verifies and adds this rule as an inner-more conjugation in a deconjugated word
    public ValidWord process(ValidWord word)
    {
        if(word.getWord().equals("")) return null; // can't deconjugate emptiness
        if(!word.getWord().endsWith(ending)) return null; // can't possibly be valid

        // these numbers can probably be reduced a lot with no harm at all. however the first one has to be at least +1 and the second one is subjective.

        if(word.getWord().length() > word.getOriginalWord().length()+10)
        {   // don't allow the deconjugation to become too much longer than the original text
            //System.out.println("bogus length");
            //System.out.println(word.getProcess());
            return null;
        }

        if(word.getNumConjugations() > word.getOriginalWord().length()+6)
        {   // don't allow the deconjugator to make impossibly information-dense conjugations
            //System.out.println("bogus complexity");
            //System.out.println(word.getProcess());
            return null;
        }

        //whether rule matches relevant tags
        boolean deconjugates;
        //noinspection SimplifiableIfStatement
        if(word.getConjugationTags().size() > 0)
            deconjugates = (word.getConjugationTags().get(word.getConjugationTags().size()-1)).equals(impliedTag);
        else
            deconjugates = true;

        if(deconjugates)
        {
            String newForm = word.getWord().substring(0, word.getWord().length() - ending.length()) + replace;
            // short circuit process if it would make a lexical form we've already seen in this deconjugation tree
            if(word.hasSeenForm(newForm + impliedTag)) return null;

            //add tag and return
            HashSet<DefTag> tags = new HashSet<>();
            ArrayList<DefTag> conjugationTags = new ArrayList<>(word.getConjugationTags());
            
            if(neededTag != null)
            {
                tags.add(neededTag);
                if(conjugationTags.size()==0)
                    conjugationTags.add(impliedTag);
                conjugationTags.add(neededTag);
            }
            
            String newProcess = word.getProcess();
            if(newProcess.equals(""))
                newProcess = change;
            else
                newProcess = newProcess + " " + change;

            HashSet<String> forms = new HashSet<>(word.getSeenForms());
            forms.add(newForm + impliedTag);

            return new ValidWord(word.getNumConjugations()+1, word.getOriginalWord(), newForm, forms, tags, conjugationTags, newProcess);
        }
        //doesn't match, don't add new word
        //System.out.println("-Doesn't work");
        return null;
    }
}
