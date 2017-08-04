package language.dictionary.JMDict;

import language.dictionary.DefTag;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds both Kanji and Kana versions of a word.
 * Kana readings can depend on certain Kanji readings to be valid.
 */
public class Spelling
{
    private boolean isKanji;
    private String spelling;
    private Spelling[] dependencies;
    private Set<DefTag> tags;

    public Spelling(boolean isKanji, String spelling)
    {
        this.isKanji = isKanji;
        this.spelling = spelling;
        dependencies = null;
        tags = null;
    }

    public void setDependencies(Spelling[] dependencies)
    {
        this.dependencies = dependencies;
    }

    public void addTag(DefTag tag)
    {
        if(tags == null)tags = new HashSet<>();
        tags.add(tag);
    }

    public boolean isKanji()
    {
        return isKanji;
    }

    public String getText()
    {
        return spelling;
    }

    public Spelling[] getDependencies()
    {
        return dependencies;
    }

    public Set<DefTag> getTags()
    {
        return tags;
    }
}
