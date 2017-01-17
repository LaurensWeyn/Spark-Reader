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
package Options;

import Language.Dictionary.Definition;
import Language.Dictionary.Japanese;
import Language.Splitter.FoundDef;
import Language.Splitter.FoundWord;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import UI.UI;
import java.util.ArrayList;
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
        if(file.exists() == false)return;
        
        loadFile(file);
    }
    private void loadFile(File input)throws IOException
    {
        FileInputStream is = new FileInputStream(input);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while(line != null)
        {
            if(line.equals("") == false)table.add(line);
            line = br.readLine();
        }
        br.close();
    }
    public void importCsv(File input, String sep)throws IOException
    {
        FileInputStream is = new FileInputStream(input);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while(line != null)
        {
            if(line.equals("") == false && Japanese.isJapanese(line))//not empty, has Japanese characters
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
        ArrayList<Definition> defs = UI.dict.find(word);
        if(defs != null)//if this word isn't known, don't bother adding it as a known word
        {
            for(String match:defs.get(0).getSpellings())
            {
                if(match.equals(word))
                {
                    table.add(word);//the things I do to save RAM...
                    dueChanges++;
                }
            }
        }
    }
    
    public void save()throws IOException
    {
        if(dueChanges == 0)return;//don't bother writing if nothing changed
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        for(String word:table)
        {
            fr.append(word + "\n");
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
        if(dueChanges > saveThreshold || UI.options.getOptionBool("reduceSave") == false)
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
        if(dueChanges > saveThreshold || UI.options.getOptionBool("reduceSave") == false)
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
