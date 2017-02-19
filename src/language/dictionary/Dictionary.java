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

import fuku.eb4j.*;
import ui.UI;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Contains words and their definitions
 * @author laure
 */
public class Dictionary
{
    //text -> definition
    private HashMap<String, ArrayList<Definition>> lookup;
    //can query for words
    private List<SubBook> books;
    public static final DefSource EPWING_SOURCE = new DefSource(-1, "Epwing");

    public Dictionary()throws IOException
    {
        lookup = new HashMap<>();
        books = new ArrayList<>();
    }
    public Dictionary(File dictFolder)throws IOException
    {
        this();
        loadDirectory(dictFolder);
    }
    public void loadDirectory(File dictFolder)throws IOException
    {
        File[] fileList = dictFolder.listFiles();
        if(fileList == null)throw new IOException(dictFolder + " not a valid directory");
        for(File file:fileList)
        {
            if(file.isDirectory())
            {
                try
                {
                    //perhaps it's an epwing dictionary
                    loadEpwing(file);
                }catch(EBException ignored)
                {
                    //failed, try load subdirectory
                    loadDirectory(file);
                }
            }
            else if(file.getName().equals("kanji.txt"))
            {
                Kanji.load(file, UI.options.getOptionBool("addKanjiAsDef")?this:null, new DefSource(-5, "Kanji deck"));
            }
            else if(file.getName().equalsIgnoreCase("edict2"))
            {
                //edict file encoding
                loadEdict(file, "EUC-JP", new DefSource(0, "Edict"));
            }
            else if(file.getName().endsWith(".txt"))
            {
                //UTF-8 dictionary
                loadEdict(file, "UTF-8", new DefSource(1, "Custom"));
            }



        }
    }
    public void loadEpwing(File file)throws EBException
    {
        books.addAll(Arrays.asList(new Book(file).getSubBooks()));
    }
    public void loadEdict(File file, String encoding, DefSource source)throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName(encoding));
        BufferedReader reader = new BufferedReader(isr);
        reader.readLine();//first line is copyright stuff
        String line = reader.readLine();
        while(line != null)
        {
            //generate definition
            Definition def = new EDICTDefinition(line, source);
            for(String spelling:def.getSpellings())//for each possible spelling...
            {
                //create if it doesn't exist
                ArrayList<Definition> meanings = lookup.computeIfAbsent(spelling, k -> new ArrayList<>());
                meanings.add(def);//add this definition for this spelling
            }
            line = reader.readLine();
        }
        System.out.println("loaded " + lookup.keySet().size() + " entries so far");
    }
    public void loadKanji(KanjiDefinition def)
    {
        String kanji = def.getSpellings()[0].charAt(0) + "";
        //create if it doesn't exist
        ArrayList<Definition> meanings = lookup.computeIfAbsent(kanji, k -> new ArrayList<>());
        meanings.add(def);//add this definition for this spelling
    }
    public List<Definition> find(String word)
    {
        //System.out.println("looking up " + word);
        return lookup.get(word);
    }

    public List<EPWINGDefinition> findEpwing(String word)
    {
        ArrayList<EPWINGDefinition> defs = new ArrayList<>();
        for(SubBook book:books)
        {
            try
            {
                Searcher search = book.searchExactword(word);
                Result result = search.getNextResult();
                while(result != null)
                {
                    defs.add(new EPWINGDefinition(result, book, EPWING_SOURCE));
                    result = search.getNextResult();
                }
            }catch(EBException ignored){}
        }
        return defs;
    }
}
