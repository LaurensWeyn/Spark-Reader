package language.dictionary;

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
                String key = fields[spelling_column].split("-")[0]+"-"+fields[reading_column];
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
    
    public static FreqData get(String spelling, String reading) 
    {
        String text = spelling + "-" + Japanese.toKatakana(reading, true);
        if(mapping.containsKey(text))
            return mapping.get(text);
        else
            return null;
    }
}
