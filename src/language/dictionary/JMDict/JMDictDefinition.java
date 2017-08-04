package language.dictionary.JMDict;

import language.deconjugator.ValidWord;
import language.dictionary.DefSource;
import language.dictionary.DefTag;
import language.dictionary.Definition;

import java.util.HashSet;
import java.util.Set;

public class JMDictDefinition extends Definition
{
    private long ID;
    private long quickTags;
    private Sense senses[];
    private Spelling spellings[];
    public static DefSource source;

    public JMDictDefinition(long ID, Sense[] senses, Spelling[] spellings)
    {
        this.ID = ID;
        this.senses = senses;
        this.spellings = spellings;

        quickTags = 0;
        //for(Spelling spelling:spellings)quickTags |= DefTag.toQuickTag(spelling.getTags());
        //for(Sense sense:senses)quickTags |= DefTag.toQuickTag(sense.getTags());
    }

    @Override
    public String getFurigana(ValidWord context)
    {
        for(Spelling spelling:spellings)
        {
            if(spelling.isKanji())continue;//Kanji can't be furigana
            if(spelling.getDependencies() != null)//reading depends on context - check if this context meets the criteria
            {
                boolean matches = false;
                for(Spelling dependsOn:spelling.getDependencies())
                {
                    if(dependsOn.getText().equals(context.getWord()))matches = true;
                }
                if(!matches)continue;//not valid furigana for this form
            }
            return spelling.getText();
        }
        return null;
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
    public Spelling[] getSpellings()
    {
        return spellings;
    }

    @Override
    public Sense[] getMeanings()
    {
        return senses;
    }

    @Override
    public long getQuickTags()
    {
        return quickTags;
    }

    @Override
    public Set<DefTag> getTags(ValidWord context)
    {
        Set<DefTag> tags = new HashSet<>();
        //TODO take context into account
        for(Spelling spelling:spellings)
        {
            if(spelling.getTags() != null)tags.addAll(spelling.getTags());
        }
        for(Sense sense:senses)
        {
            tags.addAll(sense.getTags());
        }

        return tags;
    }

    @Override
    public String toString()
    {
        return spellings[0].getText();
    }
}
