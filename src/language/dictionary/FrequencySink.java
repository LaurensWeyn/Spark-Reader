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
        String reading = def.getFurigana();
        System.out.println(spelling);
        System.out.println(def.toString());
        
        if(reading.equals("") && Japanese.hasOnlyKana(spelling))
            reading = Japanese.toKatakana(spelling, false);
             
        String text = spelling + "-" + Japanese.toKatakana(reading, false);
        // Fast lane: easy successful lookup
        if(mapping.containsKey(text))
        {
            System.out.println("simple mapping");
            return mapping.get(text);
        }
        else
        {
            System.out.println("not a simple mapping");
            // Slow lane: look for alternate spellings of this word in the edict definition
            if(def.getDefinition() instanceof EDICTDefinition)
            {
                System.out.println("is edict");
                EDICTDefinition realdef = (EDICTDefinition)def.getDefinition();
                // ordered from most normal/common to least normal/common when possible
                for(String edictSpelling : realdef.cleanOrderedSpellings) // Loop over ORDERED spellings (more common first)
                {
                    if(realdef.spellings.containsKey(edictSpelling))  
                    {
                        System.out.println("definition match contains this spelling");
                        if(realdef.spellings.get(edictSpelling).readings.size() > 0)
                        {
                            System.out.println("Type: reading was looked up");
                            for(EDICTDefinition.TaggedReading testReading : realdef.spellings.get(edictSpelling).readings)
                            {
                                System.out.println("Comparing:");
                                System.out.println(testReading.reading);
                                System.out.println(reading);
                                if(testReading.reading.equals(reading))
                                {
                                    System.out.println("spelling has right reading");
                                    text = edictSpelling + "-" + Japanese.toKatakana(reading, false);
                                    if(mapping.containsKey(text))
                                    {
                                        System.out.println("we have frequency info for this spelling reading pair");
                                        return mapping.get(text);
                                    }
                                }
                            }
                        }
                        else if(Japanese.hasOnlyKana(edictSpelling) && Japanese.hasOnlyKana(edictSpelling))
                        {
                            System.out.println("Type: Reading is just the text itself");
                            reading = Japanese.toHiragana(edictSpelling, false);
                            String text1 = Japanese.toHiragana(edictSpelling, false) + "-" + Japanese.toKatakana(reading, false);
                            String text2 = Japanese.toKatakana(edictSpelling, false) + "-" + Japanese.toKatakana(reading, false);
                            if(mapping.containsKey(text1))
                            {
                                System.out.println("we have frequency info for this spelling reading pair");
                                return mapping.get(text1);
                            }
                            if(mapping.containsKey(text2))
                            {
                                System.out.println("we have frequency info for this spelling reading pair");
                                return mapping.get(text2);
                            }
                        }
                        else
                        {
                            System.out.println("Type: Reading not known or disgusting EDICT entry");
                        }
                    }
                }
            }
        }
        System.out.println("Failed to find frequency info");        
        return null;
    }
}
