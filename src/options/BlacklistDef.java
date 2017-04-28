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
import main.Main;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.TreeMap;

public class BlacklistDef
{
    private TreeMap<Long, ArrayList<String>> table;
    private int dueChanges = 0;
    private int saveThreshold = 10;
    private final File file;
    public BlacklistDef(File file)throws IOException
    {
        table = new TreeMap<>();
        this.file = file;
        if(!file.exists())
        {
            System.out.println("WARN: no blacklisted definitions file");
            return;
        }
        
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        
        for (String line = br.readLine(); line != null; line = br.readLine())
        {
            if(line.split("\t",2).length != 2) continue;
            if(!line.split("\t",2)[1].contains(",")) continue;
            
            Long key = Long.parseLong(line.split("\t", 2)[0]);
            ArrayList<String> defs = new ArrayList<>(Arrays.asList(line.split("\t", 2)[1].split(",")));
            if(defs.size() == 0) continue;
            
            ArrayList<String> real_defs = new ArrayList<>();
            for(String s : defs) if (!s.isEmpty()) real_defs.add(s);
            
            table.put(key, real_defs);
        }
        br.close();
    }
    public void save()throws IOException
    {
        if(dueChanges == 0)return;//don't bother writing if nothing changed
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        for(Entry<Long, ArrayList<String>> entry : table.entrySet())
        {
            fr.append(entry.getKey().toString()).append("\t");
            for(String s : entry.getValue())
                fr.append(s).append(",");
            fr.append("\n");
        }
        fr.close();
        dueChanges = 0;
    }

    public void setSaveThreshold(int saveThreshold)
    {
        this.saveThreshold = saveThreshold;
    }
    public void toggleBlacklist(FoundDef def)
    {
        Long id = def.getDefinition().getID();
        // "dict form" is a borderline misnomer from deconjugation; this is actually referring to the "surface form" of the word if it were uninflected in the original text, which is what we want
        String spelling = def.getDictForm();
        if(table.containsKey(id))
        {
            // removing
            if(table.get(id).contains(spelling))
            {
                System.out.println("BL: adding additional");
                //ArrayList<String> forms = new ArrayList<>(table.get(id));
                ArrayList<String> forms = table.get(id);
                forms.remove(spelling);
                //table.replace(id, forms);
            }
            // adding
            else
            {
                System.out.println("BL: removing");
                //ArrayList<String> forms = new ArrayList<>(table.get(id));
                ArrayList<String> forms = table.get(id);
                forms.add(spelling);
                //table.replace(id, forms);
            }
        }
        // adding
        else
        {
            System.out.println("BL: adding new");
            ArrayList<String> forms = new ArrayList<>();
            forms.add(spelling);
            table.put(id, forms);
        }
        System.out.println("BL: size: " + table.size());
        dueChanges++;
        
        if(dueChanges > saveThreshold || !Main.options.getOptionBool("reduceSave"))
        {
            try
            {
                save();
            }catch(IOException e)
            {
                System.out.println("Failed to write changes: " + e);
                //if this fails, we'll try again on the next change
            }
        }
    }

    public boolean isBlacklisted(Long id, String spelling)
    {
        return (table.containsKey(id) && table.get(id).contains(spelling));
    }
}
