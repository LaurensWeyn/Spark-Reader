/* 
 * Copyright (C) 2017 Laurens Weyn
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
package language.dictionary;

import main.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Contains methods for Kanji lookup if available
 * @author Laurens Weyn
 */
public class Kanji
{
    private static HashMap<Character, String> kanjiDefs = new HashMap<>();

    /**
     * Loads a Kanji deck
     * @param file exported deck file. If file does not exist, Kanji are not loaded
     * @param dict dictionary to add KanjiDefinitions to (null to not add definitions)
     * @throws IOException if an error occurs while loading the file
     */
    public static void load(File file, Dictionary dict, DefSource source)throws IOException
    {
        if(!file.exists())return;//leave kanjiDefs empty

        System.out.println("loading Kanji");
        //C:\Users\Laurens\Desktop\Databases\KanjiDB\anki.csv
        BufferedReader br = Utils.UTF8Reader(file);
        //kanjiDefs = new HashMap<>();
        br.readLine();//first line is header, skip
        String line = br.readLine();
        int count = 0;
        while(line != null)
        {
            String bits[] = line.split("\t");
            kanjiDefs.put(bits[4].charAt(0), bits[0] + ": " + bits[3]);//Kanji = ####: meaning
            if(dict != null)dict.loadKanji(new KanjiDefinition(line, source));
            line = br.readLine();
            count++;
        }
        System.out.println("loaded " + count + " Kanji");
    }
    public static String lookup(char kanji)
    {
        return kanjiDefs.get(kanji);
    }
}
