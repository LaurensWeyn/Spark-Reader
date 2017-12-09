package language.dictionary.JMDict;

import language.deconjugator.ValidWord;
import language.dictionary.DefSource;
import language.dictionary.DefTag;
import language.dictionary.Definition;
import language.dictionary.Japanese;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JMDictDefinition extends Definition
{
    private long ID;
    //private long quickTags;//planned optimization
    private Sense senses[];
    private Spelling spellings[];
    public static DefSource source;

    public JMDictDefinition(long ID, Sense[] senses, Spelling[] spellings)
    {
        this.ID = ID;
        this.senses = senses;
        this.spellings = spellings;

        //quickTags = 0;
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
        return "";
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
    public List<Spelling> getSpellings(ValidWord context)
    {
        List<Spelling> spellings = new ArrayList<>();
        for(Spelling spelling:this.spellings)
        {
            if(spelling.getDependencies() == null)
            {
                spellings.add(spelling);
            }
            else
            {
                boolean match = false;
                for(Spelling dependency:spelling.getDependencies())
                {
                    if(dependency.getText().equals(context.getWord()))
                    {
                        match = true;
                        break;
                    }
                }
                if(match)spellings.add(spelling);
            }
        }
        return spellings;
    }

    @Override
    public Sense[] getMeanings()
    {
        return senses;
    }

    @Override
    public List<Sense> getMeanings(ValidWord context)
    {
        List<Sense> meanings = new ArrayList<>();
        boolean isKanji = Japanese.hasKanji(context.getWord());
        for(Sense meaning:senses)
        {
            if(meaning.getRestrictedSpellings() == null)
            {
                meanings.add(meaning);
            }
            else
            {
                boolean match = false;
                int checks = 0;
                for(Spelling dependency:meaning.getRestrictedSpellings())
                {
                    if(isKanji != dependency.isKanji())
                        continue;//can't check dependency in wrong writing system
                    else
                        checks++;
                    if(dependency.getText().equals(context.getWord()))
                    {
                        match = true;
                        break;
                    }
                }
                if(match || checks == 0)
                    meanings.add(meaning);
            }
        }
        return meanings;
    }

    @Override
    public Set<DefTag> getTags()
    {
        Set<DefTag> tags = new HashSet<>();
        for(Spelling spelling:spellings)
        {
            if(spelling.getTags() != null)
                tags.addAll(spelling.getTags());
        }
        for(Sense sense:senses)
        {
            if(sense.getTags() != null)
                tags.addAll(sense.getTags());
        }

        return tags;
    }

    @Override
    public Set<DefTag> getTags(ValidWord context)
    {
        Set<DefTag> tags = new HashSet<>();
        //check senses
        for(Sense meaning:senses)
        {
            if(meaning.getTags() == null)continue;
            if(meaning.getRestrictedSpellings() == null)
            {
                tags.addAll(meaning.getTags());
            }
            else
            {
                boolean match = false;
                for(Spelling dependency:meaning.getRestrictedSpellings())
                {
                    if(dependency.getText().equals(context.getWord()))
                    {
                        match = true;
                        break;
                    }
                }
                if(match)tags.addAll(meaning.getTags());
            }
        }

        //check spellings
        for(Spelling spelling:this.spellings)
        {
            if(spelling.getTags() == null)continue;
            if(spelling.getDependencies() == null)
            {
                tags.addAll(spelling.getTags());
            }
            else
            {
                boolean match = false;
                for(Spelling dependency:spelling.getDependencies())
                {
                    if(dependency.getText().equals(context.getWord()))
                    {
                        match = true;
                        break;
                    }
                }
                if(match)tags.addAll(spelling.getTags());
            }
        }

        return tags;
    }

    @Override
    public String toString()
    {
        return spellings[0].getText();
    }
}
