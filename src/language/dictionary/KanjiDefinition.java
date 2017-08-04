package language.dictionary;

import language.deconjugator.ValidWord;
import language.dictionary.JMDict.Sense;
import language.dictionary.JMDict.Spelling;
import language.splitter.FoundWord;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by Laurens on 1/27/2017.
 */
public class KanjiDefinition extends Definition
{
    private final DefSource source;
    Spelling spellings[];
    Sense meaning;
    String constituents;
    private static final Pattern stripHTML = Pattern.compile("<(?:.|\\n)*?>");
    private int id;

    public KanjiDefinition(String line, DefSource source)
    {
        this.source = source;
        String bits[] = line.split("\t");
        try
        {
            id = Integer.parseInt(bits[0]);
        }catch(NumberFormatException e)
        {
            System.out.println("WARN: " + bits[0] + " not an ID!");
            id = bits[0].hashCode();
        }
        constituents = bits[7];
        String[] meanings = new String[5];
        meanings[0] = ">" + clean(bits[3]) + "<";//heading/meaning line
        meanings[1] = clean(bits[10]);//my story
        meanings[2] = clean(bits[11]);//Heisig
        meanings[3] = clean(bits[13]);//Koohii 1
        meanings[4] = clean(bits[14]);//Koohii 2
        meaning = new Sense(meanings, null, null);
        ArrayList<Spelling> spellList = new ArrayList<>();
        spellList.add(new Spelling(true, bits[4]));
        for(String onYomi:bits[17].split("、"))
        {
            spellList.add(new Spelling(false, onYomi.split("\\.")[0]));
        }
        for(String kunYomi:bits[18].split("、"))
        {
            spellList.add(new Spelling(false, kunYomi.split("\\.")[0]));
        }
        spellings = spellList.toArray(new Spelling[spellList.size()]);
    }
    private static String clean(String input)
    {
        return stripHTML.matcher(input).replaceAll("");
    }
    @Override
    public String getFurigana(ValidWord context)
    {
        return spellings[1].getText();//spelling 0 is Kanji
    }

    @Override
    public long getID()
    {
        return id;
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
        return new Sense[]{meaning};
    }

    @Override
    public String getTagLine()
    {
        return constituents;
    }

}
