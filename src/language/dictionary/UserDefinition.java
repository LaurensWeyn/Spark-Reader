package language.dictionary;

import java.util.Set;

/**
 * An editable EDICT-style definition
 * Created by Laurens on 2/25/2017.
 */
public class UserDefinition extends EDICTDefinition
{
    public UserDefinition(String line, DefSource source)
    {
        super(line, source);
    }
    public UserDefinition(DefSource source)
    {
        super(" [] //0/", source);
    }
    public void setMeaningRaw(String meaning)
    {
        this.meaning = meaning;
    }
    public String getMeaningRaw()
    {
        return meaning;
    }
    public void setSpellings(String[] word)
    {
        this.word = word;
    }
    public void setReadings(String[] reading)
    {
        this.reading = reading;
    }
    public void setTags(Set<DefTag> tags)
    {
        this.tags = tags;
    }
    public void setID(long id)
    {
        this.ID = id;
    }

    public String[] getSpellingsRaw()
    {
        return word;
    }
}
