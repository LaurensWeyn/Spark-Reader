package language.dictionary.JMDict;

import language.dictionary.DefTag;

import java.util.Set;

/**
 * Holds a JMDIct 'sense' of a word.
 * In classic EDICT2, these would be the entries separated by (1), (2) etc.
 */
public class Sense
{
    String meaningLines[];
    Spelling restrictedSpellings[];
    Set<DefTag> tags;

    public Sense(String[] meaningLines, Spelling[] restrictedSpellings, Set<DefTag> tags)
    {
        this.meaningLines = meaningLines;
        this.restrictedSpellings = restrictedSpellings;
        this.tags = tags;
    }
}
