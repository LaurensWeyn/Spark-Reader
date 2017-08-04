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
import language.splitter.FoundDef;
import main.Main;
import main.Utils;

import javax.swing.*;
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
    private HashMap<String, Long> table;
    private int dueChanges = 0;
    private int saveThreshold = 10;
    private final File file;
    public PrefDef(File file)throws IOException
    {
        table = new HashMap<>();
        this.file = file;
        if(!file.exists())
        {
            System.out.println("WARN: no preferred definition file");
            return;
        }


        BufferedReader br = Utils.UTF8Reader(file);
        String line = br.readLine();
        while(line != null)
        {
            String bits[] = line.split("=");
            if(bits.length == 2)
            {
                if(bits[1].startsWith("-"))//negative numbers: files from 0.6 and below
                {
                    int option = JOptionPane.showConfirmDialog(Main.getParentFrame(), "Warning: Spark Reader 0.7 and up's preferred definition files are not backwards compatible with older versions.\n" +
                            "Select yes to reset your preferred definitions. Pressing no will close the program.");
                    if(option == JOptionPane.YES_OPTION)
                    {
                        table.clear();
                        br.close();
                        file.delete();
                        return;
                    }
                    else Main.exit();
                }
                table.put(bits[0], Long.parseLong(bits[1]));
            }
            line = br.readLine();
        }
        br.close();
    }
    public void save()throws IOException
    {
        if(dueChanges == 0)return;//don't bother writing if nothing changed
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        for(Entry<String, Long> entries: table.entrySet())
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
        System.out.println(spelling + " for " + def.getDefinition().getID() + " set");
        table.put(spelling, def.getDefinition().getID());
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

    public boolean isPreferred(Definition def)
    {
        for(Spelling spelling:def.getSpellings())
        {
            if(table.containsKey(spelling.getText()) && table.get(spelling.getText()) == def.getID())
            {
                return true;
            }
        }
        return false;
    }
}
