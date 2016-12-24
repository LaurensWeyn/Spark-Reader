/* 
 * Copyright (C) 2016 Laurens Weyn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Language.Dictionary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Contains methods for Kanji lookup if available
 * @author Laurens Weyn
 */
public class Kanji
{
    static HashMap<Character, String> kanjiDefs = new HashMap<>();
    public static void load(File file)throws IOException
    {
        if(!file.exists())return;//leave kanjiDefs empty
        
        
        //C:\Users\Laurens\Desktop\Databases\KanjiDB\anki.csv
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        //kanjiDefs = new HashMap<>();
        br.readLine();//first line is header, skip
        String line = br.readLine();
        while(line != null)
        {
            String bits[] = line.split("\t");
            kanjiDefs.put(bits[6].charAt(0), bits[0] + ": " + bits[5]);//Kanji = ####: meaning
            line = br.readLine();
        }
    }
    public static String lookup(char kanji)
    {
        return kanjiDefs.get(kanji);
    }
}
