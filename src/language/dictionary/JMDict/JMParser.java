package language.dictionary.JMDict;

import language.dictionary.DefSource;
import language.dictionary.DefTag;
import language.dictionary.Dictionary;
import main.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JMParser
{
    public static void parseJMDict(File file, Dictionary target, DefSource source)throws IOException
    {
        JMDictDefinition.source = source;
        BufferedReader br = Utils.UTF8Reader(file);
        //noinspection StatementWithEmptyBody
        while(!br.readLine().equals("<JMdict>"));//skip to start of data

        String line = br.readLine();
        int count = 0;
        while(!isTag("/JMdict", line))
        {
            readSubtag("entry", line);
            long id = Long.parseLong(readCDATA("ent_seq", br.readLine()));
            List<Spelling> spellings = new LinkedList<>();
            line = br.readLine();
            while(isTag("k_ele", line))
            {
                parseKanjiSpellings(br, spellings);
                line = br.readLine();
            }
            while(isTag("r_ele", line))
            {
                parseKanaSpellings(br, spellings);
                line = br.readLine();
            }
            List<Sense> senses = new LinkedList<>();
            while(isTag("sense", line))
            {
                parseSense(br, spellings, senses);
                line = br.readLine();
            }
            readSubtag("/entry", line);

            JMDictDefinition newDef = new JMDictDefinition(id,
                    senses.toArray(new Sense[senses.size()]),
                    spellings.toArray(new Spelling[spellings.size()]));

            //System.out.println("loaded " + newDef.getID());
            target.insertDefinition(newDef);
            count++;

            line = br.readLine();
        }
        System.out.println("Loaded " + count + " JMDict entries.");
    }
    private static void parseSense(BufferedReader br, List<Spelling> spellings, List<Sense> senses)throws IOException
    {
        List<Spelling> readingRestrictions = null;
        List<String> defLines = new LinkedList<>();
        Set<DefTag> tags = new HashSet<>();
        String line = br.readLine();
        while(!isTag("/sense", line))
        {
            //definition text
            if(isTag("gloss", line))
            {
                String defLine = readCDATA("gloss", line);
                boolean accept = true;
                if(defLine.startsWith("xml:lang"))
                {
                    //TODO accepted language check here
                    accept = false;
                    //if(accept)defLine = defLine.subString(?, ?);
                }
                if(accept)defLines.add(defLine);
            }
            //tag data
            else if(isTag("pos", line))//part of speech tag
            {
                tags.add(DefTag.toTag(readTagData("pos", line)));
            }
            else if(isTag("field", line))//specialized field tag
            {
                tags.add(DefTag.toTag(readTagData("field", line)));
            }
            else if(isTag("misc", line))//misc. tag
            {
                tags.add(DefTag.toTag(readTagData("misc", line)));
            }
            else if(isTag("dial", line))//dialect specific tag
            {
                tags.add(DefTag.toTag(readTagData("dial", line)));
            }
            //extra notes
            else if(isTag("xref", line))//refer to other word
            {
                defLines.add("(see " + readCDATA("xref", line) + ")");
            }
            else if(isTag("ant", line))//antonym
            {
                defLines.add("(antonym: " + readCDATA("ant", line) + ")");
            }
            else if(isTag("s_inf", line))//misc. comments
            {
                defLines.add("(" + readCDATA("s_inf", line) + ")");
            }
            //restricted to certain spelling
            else if(isTag("stagk", line))
            {
                String toFind = readCDATA("stagk", line);
                if(readingRestrictions == null)readingRestrictions = new LinkedList<>();
                for(Spelling spelling:spellings)
                {
                    if(spelling.getText().equals(toFind))
                    {
                        readingRestrictions.add(spelling);
                        break;
                    }
                }
            }
            else if(isTag("stagr", line))
            {
                String toFind = readCDATA("stagr", line);
                if(readingRestrictions == null)readingRestrictions = new LinkedList<>();
                for(Spelling spelling:spellings)
                {
                    if(spelling.getText().equals(toFind))
                    {
                        readingRestrictions.add(spelling);
                        break;
                    }
                }
            }
            line = br.readLine();
        }

        senses.add(new Sense(
                defLines.toArray(new String[defLines.size()]),
                readingRestrictions == null?null:readingRestrictions.toArray(new Spelling[readingRestrictions.size()]),
                tags));
    }
    private static void parseKanjiSpellings(BufferedReader br, List<Spelling> spellings)throws IOException
    {
        String line = br.readLine();
        while(!isTag("/k_ele", line))
        {
            Spelling newSpelling = new Spelling(true, readCDATA("keb", line));
            line = br.readLine();
            while(isTag("ke_inf", line))//writing-specific tags
            {
                newSpelling.addTag(DefTag.toTag(readCDATA("ke_inf", line)));
                line = br.readLine();
            }
            while(isTag("ke_pri", line))//commonness data
            {
                //TODO collect common reading data here
                line = br.readLine();
            }
            spellings.add(newSpelling);
        }
    }

    private static void parseKanaSpellings(BufferedReader br, List<Spelling> spellings)throws IOException
    {
        String line = br.readLine();
        while(!isTag("/r_ele", line))
        {
            List<String> readingRestrictions = null;

            Spelling newSpelling = new Spelling(false, readCDATA("reb", line));
            line = br.readLine();

            if(isTag("re_nokanji/", line))//kana spelling not valid for any Kanji
            {
                readingRestrictions = new ArrayList<>();//init list with no entries - no Kanji match
                line = br.readLine();
            }

            while(isTag("re_restr", line))//reading restriction
            {
                if(readingRestrictions == null)readingRestrictions = new ArrayList<>();
                readingRestrictions.add(readCDATA("re_restr", line));
                line = br.readLine();
            }
            while(isTag("re_inf", line))//writing-specific tags
            {
                newSpelling.addTag(DefTag.toTag(readCDATA("re_inf", line)));
                line = br.readLine();
            }
            while(isTag("re_pri", line))//commonness data
            {
                //TODO collect common reading data here
                line = br.readLine();
            }
            if(readingRestrictions != null)//link up required spellings for reading
            {
                Spelling[] reqSpellings = new Spelling[readingRestrictions.size()];
                for(int i = 0; i < reqSpellings.length; i++)
                {
                    for(Spelling check:spellings)
                    {
                        if(check.getText().equals(readingRestrictions.get(i)))reqSpellings[i] = check;
                    }
                }
                newSpelling.setDependencies(reqSpellings);
            }
            spellings.add(newSpelling);
        }
    }

    /**
     * Enable for sanity checks.
     * Disable to save a few milliseconds.
     */
    public static boolean checkTags = true;

    private static boolean isTag(String tag, String line)
    {
        return line.length() > tag.length() + 1 && line.substring(1, 1 + tag.length()).equals(tag);
    }
    private static void readSubtag(String tag, String line)
    {
        if(checkTags && !(line.equals("<" + tag + ">")))
            throw new IllegalStateException("Expected " + tag + " tag, found \"" + line + "\"");
    }
    private static String readTagData(String tag, String line)
    {
        if(checkTags && !(line.startsWith("<" + tag + ">") && line.endsWith("</" + tag + ">")))
            throw new IllegalStateException("Expected " + tag + " tag, found \"" + line + "\"");

        return line.substring(tag.length() + 3, line.length() - tag.length() - 4);
    }
    private static String readCDATA(String tag, String line)
    {
        if(checkTags && !(line.startsWith("<" + tag) && line.endsWith("</" + tag + ">")))
            throw new IllegalStateException("Expected " + tag + " tag, found \"" + line + "\"");

        return line.substring(tag.length() + 2, line.length() - tag.length() - 3);
    }

    public static void main(String[] args)throws Exception
    {
        long startTime = System.currentTimeMillis();
        parseJMDict(new File("C:\\Users\\Laurens\\Desktop\\Databases\\JMDict\\JMDict"), null, null);
        long stopTime = System.currentTimeMillis();

        System.out.println("Time taken: " + (stopTime - startTime) + " ms");
    }
}
