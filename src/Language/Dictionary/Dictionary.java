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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains words and their definitions
 * @author laure
 */
public class Dictionary
{
    //text -> definition
    private HashMap<String, ArrayList<Definition>> lookup;

    public Dictionary()throws IOException
    {
        lookup = new HashMap<>();
    }
    public void loadEdict(File file, String encoding, int sourceNum)throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName(encoding));
        BufferedReader reader = new BufferedReader(isr);
        reader.readLine();//first line is copyright stuff
        String line = reader.readLine();
        
        
        while(line != null)
        {
            //generate definition
            Definition def = new Definition(line, sourceNum);
            for(String spelling:def.getSpellings())//for each possible spelling...
            {
                ArrayList<Definition> meanings = lookup.get(spelling);
                if(meanings == null)
                {
                    meanings = new ArrayList<>();//create if it doesn't exist
                    lookup.put(spelling, meanings);
                }
                meanings.add(def);//add this definition for this spelling
            }                   
            line = reader.readLine();
        }
        System.out.println("loaded " + lookup.keySet().size() + " entries so far");
    }
    public ArrayList<Definition> find(String word)
    {
        //System.out.println("looking up " + word);
        return lookup.get(word);
    }
}
