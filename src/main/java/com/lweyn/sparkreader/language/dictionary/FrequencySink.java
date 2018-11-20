package com.lweyn.sparkreader.language.dictionary;

import com.lweyn.sparkreader.Utils;
import com.lweyn.sparkreader.language.deconjugator.ValidWord;
import com.lweyn.sparkreader.language.dictionary.JMDict.Spelling;
import com.lweyn.sparkreader.language.splitter.FoundDef;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;

public class FrequencySink
{
    private static Logger logger = Logger.getLogger(FrequencySink.class);

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
            BufferedReader reader = Utils.UTF8Reader(freqFile);
            String str;
            int i = 1;
            while((str = reader.readLine()) != null)
            {
                String[] fields = str.split("\t");
                // e.g. ベッド-bed
                if(fields[spelling_column].equals("ベッド-bed"))
                {
                    logger.trace(fields[spelling_column]);
                    logger.trace(fields[spelling_column].split("\\-")[0]);
                }
                String key = fields[spelling_column].split("\\-")[0]+"-"+fields[reading_column];
                if(!mapping.containsKey(key))
                    mapping.put(key, new FreqData(Double.parseDouble(fields[ppm_column]), i));
                i++;
            } 
        }
        catch (IOException e)
        {
            logger.warn("IOException loading freq data");
        }
    }
    
    public static FreqData get(FoundDef def) 
    {
        return get(def, def.getFurigana());
    }
    
    public static FreqData get(FoundDef def, String forceReading) 
    {
        ValidWord foundForm = def.getFoundForm();
        String forceReadingKatakana = Japanese.toKatakana(forceReading, false);
        
        // Make sure right frequency is used for words that are easy to look up
        if(!foundForm.getWord().equals(forceReading))
        {
            String asintext = foundForm.getWord() + "-" + forceReadingKatakana;
            if(mapping.containsKey(asintext))
            {
                logger.trace("A: "+asintext);
                return mapping.get(asintext);
            }
        }
        
        // Otherwise find the first match the definition wants to give us
        boolean noKanjiAvailable = true; // We don't want to check reading-reading from the JMDict definition if there were kanji-reading pairs available that failed
        for(Spelling spelling : def.getDefinition().getSpellings()) // Handles kanji spellings first, then readings
        {
            String spellingtext = spelling.getText();
            if(spelling.getReadings().size() > 0 && spelling.getReadings().get(0) != spelling)
            {
                noKanjiAvailable = false;
                for(Spelling reading : spelling.getReadings())
                {
                    String readingtext = Japanese.toKatakana(reading.getText(), false);
                    if(!forceReadingKatakana.equals(readingtext)) continue;
                    
                    String text = spellingtext + "-" + readingtext;
                    if(mapping.containsKey(text))
                    {
                        logger.trace("B: "+text);
                        return mapping.get(text);
                    }
                }
            }
            else if(noKanjiAvailable)
            {
                String readingtext = Japanese.toKatakana(spellingtext, false);
                if(!forceReadingKatakana.equals(readingtext)) continue;
                String text = spellingtext + "-" + readingtext;
                if(mapping.containsKey(text))
                {
                    logger.trace("C: "+text);
                    return mapping.get(text);
                }
            }
        }
        return null;
    }
}
