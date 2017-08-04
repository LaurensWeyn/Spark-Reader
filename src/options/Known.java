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
package options;

import language.dictionary.Definition;
import language.dictionary.JMDict.Spelling;
import language.dictionary.Japanese;
import language.splitter.FoundDef;
import language.splitter.FoundWord;
import main.Main;
import main.Utils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

/**
 * Holds a list of words the user knows
 * @author Laurens Weyn
 */
public class Known
{
    private HashSet<String> table;
    private int dueChanges = 0;
    private int saveThreshold = 5;
    private final File file;
    public Known(File file)throws IOException
    {
        table = new HashSet<>();
        this.file = file;
        if(file == null || !file.exists())return;
        
        loadFile(file);
    }
    private void loadFile(File input)throws IOException
    {
        BufferedReader br = Utils.UTF8Reader(input);
        String line = br.readLine();
        while(line != null)
        {
            if(!line.equals(""))table.add(line);
            line = br.readLine();
        }
        br.close();
    }
    public void importCsv(File input, String sep)throws IOException
    {
        BufferedReader br = Utils.UTF8Reader(input);
        String line = br.readLine();
        while(line != null)
        {
            if(!line.equals("") && Japanese.isJapanese(line))//not empty, has Japanese characters
            {
                //String word = line.split(sep)[col - 1];
                for(String word:line.split(sep))
                {
                    addWord(word);//attempt to add every word (only Japanese words will work of course)
                }
                //addWord(word);
            }
            line = br.readLine();
        }
        br.close();
        save();
    }
    private void addWord(String word)
    {
        List<Definition> defs = Main.dict.find(word);
        if(defs != null)//if this word isn't known, don't bother adding it as a known word
        {
            for(Spelling match:defs.get(0).getSpellings())
            {
                if(match.getText().equals(word))
                {
                    table.add(match.getText());
                    dueChanges++;
                }
            }
        }
    }

    boolean hasEntryFor(String text)
    {
        return table.contains(text);
    }
    
    public void save()throws IOException
    {
        if(dueChanges == 0)return;//don't bother writing if nothing changed
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        for(String word:table)
        {
            fr.append(word).append("\n");
        }
        fr.close();
        dueChanges = 0;
    }
    public void setKnown(FoundWord word)
    {
        for(FoundDef def:word.getFoundDefs())
        {
            if(!table.contains(def.getDictForm()))dueChanges++;
            table.add(def.getDictForm());            
        }
        if(dueChanges > saveThreshold || !Main.options.getOptionBool("reduceSave"))
        {
            try
            {
                save();
            }catch(IOException e)
            {
                //try again on next set
            }
        }
    }
    public void setUnknown(FoundWord word)
    {
        for(FoundDef def:word.getFoundDefs())
        {
            if(table.contains(def.getDictForm()))dueChanges++;
            table.remove(def.getDictForm());            
        }
        if(dueChanges > saveThreshold || !Main.options.getOptionBool("reduceSave"))
        {
            try
            {
                save();
            }catch(IOException e)
            {
                //try again on next set
            }
        }
    }
    public boolean isKnown(FoundWord word)
    {
        if(Japanese.hasOnlyKatakana(word.getText()) && Main.options.getOptionBool("knowKatakana"))return true;
        if(Japanese.hasOnlyKana(word.getText()) && word.getText().length() <= Main.options.getOptionInt("knownKanaLength"))return true;

        if(word.getFoundDefs() == null)return false;
        
        for(FoundDef def:word.getFoundDefs())
        {
            if(!table.contains(def.getDictForm()))
            {
                return false;//a definition isn't found, assume this word is different
            }
        }
        return true;//all defs known
    }
    
    
}
