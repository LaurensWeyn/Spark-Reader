package language.dictionary;

import language.deconjugator.ValidWord;
import language.dictionary.JMDict.Sense;
import language.dictionary.JMDict.Spelling;

import java.util.HashSet;

/**
 * Simple internal definition format. Sufficient for user dictionary entries and such.
 */
public class SimpleDefinition extends Definition
{
    private Spelling reading;
    private Spelling[] spellingList;
    private long id;
    private DefSource source;

    private Sense meaning;

    public SimpleDefinition(String line, DefSource source)
    {
        //format: writing:reading \t tag:list \t ID \t definition \t text \t etc.
        //spellings
        String bits[] = line.split("\t");
        String spellings[] = bits[0].split(":");
        spellingList = new Spelling[spellings.length];
        for(int i = 0; i < spellings.length; i++)
        {
            spellingList[i] = new Spelling(spellings[i]);
        }

        //tags
        HashSet<DefTag> tags = new HashSet<>();
        String tagTexts[] = bits[1].split(":");
        for(String tagText:tagTexts)
        {
            tags.add(DefTag.toTag(tagText));
        }

        //id
        this.id = Long.parseLong(bits[2]);

        //definition
        String defLines[] = new String[bits.length - 3];
        System.arraycopy(bits, 3, defLines, 0, defLines.length);

        meaning = new Sense(defLines, null, tags);
        this.source = source;
    }

    @Override
    public String getFurigana(ValidWord context)
    {
        for(Spelling spelling:spellingList)
        {
            if(!spelling.isKanji())return spelling.getText();
        }
        return "";
    }

    @Override
    public long getID()
    {
        return 0;
    }

    @Override
    public DefSource getSource()
    {
        return source;
    }

    @Override
    public Spelling[] getSpellings()
    {
        return spellingList;
    }

    @Override
    public Sense[] getMeanings()
    {
        return new Sense[]{meaning};
    }
}
