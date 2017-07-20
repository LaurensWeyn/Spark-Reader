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
        String reading = def.getFurigana();
        
        if(reading.equals("") && Japanese.hasOnlyKana(spelling))
            reading = Japanese.toKatakana(spelling, false);
             
        String text = spelling + "-" + Japanese.toKatakana(reading, false);
        // Fast lane: easy successful lookup
        if(mapping.containsKey(text))
        {
            return mapping.get(text);
        }
        else
        {
            // Slow lane: look for alternate spellings of this word in the edict definition
            if(def.getDefinition() instanceof EDICTDefinition)
            {
                EDICTDefinition realdef = (EDICTDefinition)def.getDefinition();
                // ordered from most normal/common to least normal/common when possible
                for(String edictSpelling : realdef.cleanOrderedSpellings) // Loop over ORDERED spellings (more common first)
                {
                    if(realdef.spellings.containsKey(edictSpelling))  
                    {
                        if(realdef.spellings.get(edictSpelling).readings.size() > 0)
                        {
                            for(EDICTDefinition.TaggedReading testReading : realdef.spellings.get(edictSpelling).readings)
                            {
                                if(testReading.reading.equals(reading))
                                {
                                    text = edictSpelling + "-" + Japanese.toKatakana(reading, false);
                                    if(mapping.containsKey(text))
                                        return mapping.get(text);
                                }
                            }
                        }
                        else if(Japanese.hasOnlyKana(edictSpelling) && Japanese.hasOnlyKana(edictSpelling))
                        {
                            reading = Japanese.toHiragana(edictSpelling, false);
                            String text1 = Japanese.toHiragana(edictSpelling, false) + "-" + Japanese.toKatakana(reading, false);
                            String text2 = Japanese.toKatakana(edictSpelling, false) + "-" + Japanese.toKatakana(reading, false);
                            if(mapping.containsKey(text1))
                                return mapping.get(text1);
                            if(mapping.containsKey(text2))
                                return mapping.get(text2);
                        }
                    }
                }
            }
        }
        return null;
    }
}
