package language.dictionary;

import language.splitter.FoundDef;

import java.io.*;
import java.util.HashMap;

public class FrequencySink
{
    public static class FreqData
    {
        public double ppm;
        public int rank;

        FreqData(double ppm, int rank)
        {
            this.ppm = ppm;
            this.rank = rank;
        }
        
        @Override
        public String toString()
        {
            return String.format("#%d, %.2fppm", rank, ppm);
        }
    }
    static HashMap<String, FreqData> mapping = new HashMap<>();
    public static void load(File freqFile, int spelling_column, int reading_column, int ppm_column)
    {
        try
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(freqFile), "UTF-8"));
            String str;
            int i = 1;
            while((str = reader.readLine()) != null)
            {
                String[] fields = str.split("\t");
                // e.g. ベッド-bed
                if(fields[spelling_column].equals("ベッド-bed"))
                {
                    System.out.println(fields[spelling_column]);
                    System.out.println(fields[spelling_column].split("\\-")[0]);
                }
                String key = fields[spelling_column].split("\\-")[0]+"-"+fields[reading_column];
                if(!mapping.containsKey(key))
                    mapping.put(key, new FreqData(Double.parseDouble(fields[ppm_column]), i));
                i++;
            } 
        }
        catch (IOException e)
        {
            System.out.println("ioexception loading freq data"); 
        }
    }
    
    public static FreqData get(FoundDef def) 
    {
        String spelling = def.getFoundForm().getWord();
        String reading = def.getDefinition().getFurigana();
        System.out.println(spelling);
        System.out.println(def.toString());
        // spelling is in kana, need to check if there's a non-kana spelling for this word within the definition
        if(spelling.equals(Japanese.toHiragana(spelling, true)) || spelling.equals(Japanese.toKatakana(spelling, true)))
        {
            reading = Japanese.toHiragana(spelling, true);
            if(def.getDefinition() instanceof EDICTDefinition)
            {
                EDICTDefinition realdef = (EDICTDefinition)def.getDefinition();
                String realSpelling = "";
                // ordered from most normal/common to least normal/common when possible
                for(String edictSpelling : realdef.cleanOrderedSpellings)
                {
                    boolean doublebreak = false;
                    if(realdef.spellings.containsKey(edictSpelling))
                    {
                        for(EDICTDefinition.TaggedReading testReading : realdef.spellings.get(edictSpelling).readings)
                        {
                            if(testReading.reading.equals(reading))
                            {
                                realSpelling = edictSpelling;
                                doublebreak = true;
                                break;
                            }
                        }
                    }
                    if(doublebreak) break;
                }
                
                if(realSpelling.equals("")) // no non-kana spelling assigned, probably just a normal katakana word
                    ;
                else // there is a real non-kana spelling for this word, use it
                    spelling = realSpelling;
            }
        }
             
        String text = spelling + "-" + Japanese.toKatakana(reading, true);
        if(mapping.containsKey(text))
            return mapping.get(text);
        else
            return null;
    }
}
