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
import language.dictionary.Epwing.EPWINGDefinition;
import language.dictionary.JMDict.JMParser;
import language.dictionary.JMDict.Spelling;
import main.Main;
import main.Utils;

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
    private HashMap<String, Object> lookup;
    //can query for words
    private List<SubBook> books;

    private static int loadedWordCount = 0;

    //TODO move this to the UserDict DefSource
    public static String userdictFilename = "dictionaries/customDict.txt";

    public Dictionary()throws IOException
    {
        this(0);
    }
    public Dictionary(int expectedHashSize)
    {
        lookup = new HashMap<>(expectedHashSize + expectedHashSize / 4 + 100);// >125%
        books = new ArrayList<>();
    }
    public Dictionary(File dictFolder)throws IOException
    {
        this(dictFolder, 0);
    }
    public Dictionary(File dictFolder, int expectedHashSize)throws IOException
    {
        this(expectedHashSize);
        loadDirectory(dictFolder);
        loadEpwing(dictFolder);//special case: this folder may itself be a valid epwing dictionary
    }
    public void loadDirectory(File dictFolder)throws IOException
    {
        File[] fileList = dictFolder.listFiles();
        if(fileList == null)throw new IOException(dictFolder + " not a valid directory");
        for(File file:fileList)
        {
            if(file.isDirectory())//either epwing or subdir
            {
                //is this directory an epwing root?
                if(!loadEpwing(file))
                {
                    //if not, recurse to find more dictionaries
                    loadDirectory(file);
                }
            }
            else if(file.getName().equals("kanji.txt"))
            {
                Kanji.load(file, Main.options.getOptionBool("addKanjiAsDef")?this:null, DefSource.getSource("Kanji deck"));
            }
            else if(file.getName().equals("freqlist.tsv"))
            {
                System.out.println("loading freq data");
                FrequencySink.load(file, 6, 7, 0);
            }
            else if(file.getName().equalsIgnoreCase("edict2"))
            {
                //edict file encoding
                //loadEdict(file, DefSource.getSource("Edict"));
            }
            else if(file.getName().equalsIgnoreCase("JMDict"))
            {
                JMParser.parseJMDict(file, this, DefSource.getSource("Edict"));
            }
            else if(file.getName().contains("want"))
            {
                //target word list
                if(Main.options.getOptionBool("enableWantList"))Main.wantToLearn.addFile(file);
            }
            else if(file.getName().endsWith(".txt"))
            {
                //UTF-8 dictionary
                userdictFilename = file.getAbsolutePath();
                loadUserDict(file, DefSource.getSource("Custom"));
            }
            else if(file.getName().endsWith(".tsv"))
            {
                loadSimpleDict(file);
            }
        }
    }
    public boolean loadEpwing(File file)
    {
        try
        {
            books.addAll(Arrays.asList(new Book(file).getSubBooks()));
            return true;
        }catch(EBException ignored)
        {
            return false;
        }
    }
    public void loadEdict(File file, DefSource source)throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("EUC-JP"));
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
    public void loadUserDict(File file, DefSource source)throws IOException
    {
        BufferedReader reader = Utils.UTF8Reader(file);
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
    public void loadSimpleDict(File file)throws IOException
    {
        BufferedReader reader = Utils.UTF8Reader(file);
        DefSource source = DefSource.getSource(reader.readLine());//first line is source
        String line = reader.readLine();
        while(line != null)
        {
            //generate and insert definition
            SimpleDefinition definition = new SimpleDefinition(line, source);
            insertDefinition(definition);
            source.attach(definition);
            line = reader.readLine();
        }
        System.out.println("loaded " + lookup.keySet().size() + " entries so far");
    }

    /**
     * Inserts a generic definition into the lookup data structure
     * @param def the definition to insert
     */
    public void insertDefinition(Definition def)
    {
        insertDefinition(def.getSpellings(), def);
    }

    /**
     * Inserts a definition only for specific spellings
     * @param spellings the spellings of the definition to insert
     * @param def the definition to insert
     */
    @SuppressWarnings("unchecked")
    public void insertDefinition(Spelling[] spellings, Definition def)
    {
        for(Spelling spelling:spellings)
        {
            String key = spelling.getText();
            Object result = lookup.get(key);
            if(result == null)//not yet in lookup
            {
                lookup.put(key, def);//put single instance
            }
            else if(result instanceof Definition)//already in lookup - not yet list
            {
                LinkedList multi = new LinkedList<>();
                multi.add(result);//add original entry
                multi.add(def);//add new entry
                lookup.put(key, multi);
            }
            else//already in lookup as list
            {
                LinkedList multi = (LinkedList) result;
                multi.add(def);//add to existing list
            }
            loadedWordCount++;
        }
    }

    /**
     * Removes a definition from the lookup data structure
     * @param def the definition to remove
     */
    public void removeDefinition(Definition def)
    {
        removeDefinition(def.getSpellings(), def);
    }
    /**
     * Removes a definition only for specific spellings
     * @param spellings the spellings of the definition to remove
     * @param def the definition to remove
     */
    public void removeDefinition(Spelling[] spellings, Definition def)
    {
        for(Spelling spelling:spellings)
        {
            String key = spelling.getText();
            Object result = lookup.get(key);
            if(result == null)continue;//not in lookup
            else if(result instanceof Definition)//in lookup - alone
            {
                lookup.remove(key);
            }
            else//in lookup as list
            {
                LinkedList multi = (LinkedList) result;
                multi.remove(def);//remove from list
                if(multi.size() <= 1)//LinkedList no longer needed
                {
                    lookup.remove(key);
                }
                if(multi.size() == 1)//1 entry still needed in structure
                {
                    lookup.put(key, multi.get(0));
                }
            }
            loadedWordCount--;
        }
    }

    public void loadKanji(KanjiDefinition def)
    {
        String kanji = def.getSpellings()[0].getText().charAt(0) + "";
        //create if it doesn't exist
        insertDefinition(new Spelling[]{new Spelling(kanji)}, def);
    }
    @SuppressWarnings("unchecked")
    public List<Definition> find(String word)
    {
        Object found = lookup.get(word);
        if(found == null)return null;
        if(found instanceof List)return (List)found;
        else return Collections.singletonList((Definition)found);
    }
    public boolean hasEpwingDef(String word)
    {
        //System.out.println("checking epwing for " + word);
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

    public int getHashSize()
    {
        return lookup.size();
    }

    /**
     * Access the internal data structure of the Dictionary.
     * Don't use this method unless you really need to as the structure may change.
     * @return the HashMap holding all loaded definitions
     */
    public HashMap<String, Object> getInternalLookup()
    {
        return lookup;
    }
}
