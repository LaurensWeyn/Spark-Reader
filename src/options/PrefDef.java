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
import language.splitter.FoundDef;
import ui.UI;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Holds the user's preferred definition table
 * @author Laurens Weyn
 */
public class PrefDef
{
    private HashMap<String, Integer> table;
    private int dueChanges = 0;
    private int saveThreshold = 10;
    private final File file;
    public PrefDef(File file)throws IOException
    {
        table = new HashMap<>();
        this.file = file;
        if(!file.exists())return;
        
        
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while(line != null)
        {
            String bits[] = line.split("=");
            if(bits.length == 2)table.put(bits[0], Integer.parseInt(bits[1]));
            line = br.readLine();
        }
        br.close();
    }
    public void save()throws IOException
    {
        if(dueChanges == 0)return;//don't bother writing if nothing changed
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        for(Entry<String, Integer> entries: table.entrySet())
        {
            fr.append(entries.getKey()).append("=").append(String.valueOf(entries.getValue())).append("\n");
        }
        fr.close();
        dueChanges = 0;
    }

    public void setSaveThreshold(int saveThreshold)
    {
        this.saveThreshold = saveThreshold;
    }
    public void setPreferred(FoundDef def)
    {
        String spelling = def.getDictForm();
        System.out.println(spelling + " for " + def.getDefinition().getID()+ " set");
        table.put(spelling, def.getDefinition().getID());
        dueChanges++;
        
        if(dueChanges > saveThreshold || !UI.options.getOptionBool("reduceSave"))
        {
            try
            {
                save();
            }catch(IOException e)
            {
                //if this fails, wel'll try again on the next change
            }
        }
    }
    public void setPreferred(Definition def)
    {
        
    }
    public boolean isPreferred(Definition def)
    {
        for(String spelling:def.getSpellings())
        {
            if(table.containsKey(spelling) && table.get(spelling) == def.getID())return true;
        }
        return false;
    }
}
