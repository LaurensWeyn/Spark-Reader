package language.dictionary;

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
    public String[] getSpellings()
    {
        return word;
    }
    public String[] getReadings()
    {
        return reading;
    }
    public void setSpellings(String[] word)
    {
        this.word = word;
    }
    public void setReadings(String[] reading)
    {
        this.reading = reading;
    }



}
