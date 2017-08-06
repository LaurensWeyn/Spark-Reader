package language.dictionary.JMDict;

import language.dictionary.DefTag;

import java.util.Set;

/**
 * Holds a JMDIct 'sense' of a word.
 * In classic EDICT2, these would be the entries separated by (1), (2) etc.
 */
public class Sense
{
    private String meaningLines[];
    private Spelling restrictedSpellings[];
    private Set<DefTag> tags;

    public Sense(String[] meaningLines, Spelling[] restrictedSpellings, Set<DefTag> tags)
    {
        this.meaningLines = meaningLines;
        this.restrictedSpellings = restrictedSpellings;
        this.tags = tags;
    }

    public String[] getMeaningLines()
    {
        return meaningLines;
    }

    public String getMeaningAsLine()
    {
        StringBuilder out = new StringBuilder();
        for(String line:meaningLines)
        {
            //add sep for lines after the first that aren't in brackets
            if(out.length() > 0 && !(line.startsWith("(") && line.endsWith(")")))out.append("; ");
            out.append(line);
        }
        return out.toString();
    }

    public Spelling[] getRestrictedSpellings()
    {
        return restrictedSpellings;
    }

    public Set<DefTag> getTags()
    {
        return tags;
    }
}
