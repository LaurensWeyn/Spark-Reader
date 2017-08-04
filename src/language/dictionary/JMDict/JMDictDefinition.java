package language.dictionary.JMDict;

import language.dictionary.DefSource;
import language.dictionary.Definition;

public class JMDictDefinition extends Definition
{
    private long ID;
    private int quickTags;
    private Sense senses[];
    private Spelling spellings[];
    public static DefSource source;

    public JMDictDefinition(long ID, Sense[] senses, Spelling[] spellings)
    {
        this.ID = ID;
        this.senses = senses;
        this.spellings = spellings;
    }

    @Override
    public String getFurigana()
    {
        return null;//TODO make this (and other methods) require current spelling as context
    }

    @Override
    public long getID()
    {
        return ID;
    }

    @Override
    public DefSource getSource()
    {
        return source;
    }

    @Override
    public String[] getSpellings()
    {
        return new String[0];
    }

    @Override
    public String[] getMeaning()
    {
        return new String[0];
    }

    @Override
    public String getMeaningLine()
    {
        return null;
    }

    @Override
    public String toString()
    {
        return spellings[0].getSpelling();
    }
}
