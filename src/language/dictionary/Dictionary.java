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
import main.Main;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Contains words and their definitions
 * @author laure
 */
public class Dictionary
{
    //text -> definition
    private HashMap<String, List<Definition>> lookup;
    //can query for words
    private List<SubBook> books;


    private static int loadedWordCount = 0;

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
                Kanji.load(file, Main.options.getOptionBool("addKanjiAsDef")?this:null, DefSource.getSource("Kanji deck"));
            }
            else if(file.getName().equalsIgnoreCase("edict2"))
            {
                //edict file encoding
                loadEdict(file, "EUC-JP", DefSource.getSource("Edict"));
            }
            else if(file.getName().endsWith(".txt"))
            {
                //UTF-8 dictionary
                loadUserDict(file, "UTF-8", DefSource.getSource("Custom"));
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
            //generate and insert definition
            insertDefinition(new EDICTDefinition(line, source));
            line = reader.readLine();
        }
        System.out.println("loaded " + lookup.keySet().size() + " entries so far");
    }
    public void loadUserDict(File file, String encoding, DefSource source)throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName(encoding));
        BufferedReader reader = new BufferedReader(isr);
        reader.readLine();//first line is design info
        String line = reader.readLine();
        while(line != null)
        {
            //generate and insert definition
            UserDefinition definition = new UserDefinition(line, source);
            insertDefinition(definition);
            source.attach(definition);
            line = reader.readLine();
        }
        System.out.println("loaded " + lookup.keySet().size() + " entries so far");
    }

    /**
     * Inserts a definition into the lookup data structure
     * @param def the definition to insert
     */
    public void insertDefinition(Definition def)
    {
        for(String spelling:def.getSpellings())//for each possible spelling...
        {
            //create if it doesn't exist
            List<Definition> meanings = lookup.computeIfAbsent(spelling, k -> new LinkedList<>());
            meanings.add(def);//add this definition for this spelling
            loadedWordCount++;
        }
    }

    /**
     * Removes a definition from the lookup data structure
     * @param def the definition to remove
     */
    public void removeDefinition(Definition def)
    {
        for(String spelling:def.getSpellings())//for each possible spelling...
        {
            List<Definition> meanings = lookup.get(spelling);
            if(meanings == null)continue;
            meanings.remove(def);
            loadedWordCount--;
            if(meanings.isEmpty())lookup.remove(spelling);
        }
    }

    public void loadKanji(KanjiDefinition def)
    {
        String kanji = def.getSpellings()[0].charAt(0) + "";
        //create if it doesn't exist
        List<Definition> meanings = lookup.computeIfAbsent(kanji, k -> new ArrayList<>());
        meanings.add(def);//add this definition for this spelling
    }
    public List<Definition> find(String word)
    {
        //System.out.println("looking up " + word);
        return lookup.get(word);
    }
    public boolean hasEpwingDef(String word)
    {
        for(SubBook book:books)
        {
            try
            {
                Searcher search = book.searchExactword(word);
                Result result = search.getNextResult();
                if(result != null)return true;
            }catch(Exception ignored){}
        }
        return false;
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
                    defs.add(new EPWINGDefinition(result, book, DefSource.getSource("Epwing")));
                    result = search.getNextResult();
                }
            }catch(EBException ignored){}
        }
        return defs;
    }

    public static int getLoadedWordCount()
    {
        return loadedWordCount;
    }
}
