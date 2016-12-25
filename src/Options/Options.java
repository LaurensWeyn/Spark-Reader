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

import static UI.UI.maxHeight;
import static UI.UI.splitLines;
import static UI.UI.windowWidth;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
import java.util.Map.Entry;
import java.util.Set;
import static UI.UI.defWidth;

/**
 *
 * @author Laurens Weyn
 */
public class Options
{
    private HashMap<String, String> options;
    private File file;
    private static Options defaults = new Options();
    public Options()
    {
        options = new HashMap<>();
        file = null;
        ///////////////////////////////////////
        //all default settings hardcoded here//
        ///////////////////////////////////////
        
        options.put("textFont", "Meiryo, A, 30");
        options.put("furiFont", "Meiryo,, 15");
        options.put("defFont", "Meiryo,, 15");

        options.put("markerCol", "255, 255, 0, 200");
        options.put("noMarkerCol", "255, 255, 0, 1");
        options.put("furiCol", "0, 255, 255, 255");
        options.put("furiBackCol", "0, 0, 0, 128");

        options.put("textBackCol", "0, 0, 255, 128");
        options.put("knownTextBackCol", "0, 0, 255, 200");
        options.put("textCol", "255, 0, 0, 255");

        options.put("defReadingCol", "0, 255, 255, 255");
        options.put("defKanjiCol", "255, 255, 255, 255");
        options.put("defTagCol", "255, 255, 255, 255");
        options.put("defCol", "255, 255, 0, 255");
        options.put("defBackCol", "0, 0, 0, 128");
        
        
        options.put("edictPath", "edict2");
        options.put("customDictPath", "custom.txt");
        options.put("kanjiPath", "kanji.csv");
        options.put("ankiExportPath", "forAnki.csv");
        
        options.put("windowWidth", "1280");
        options.put("maxHeight", "720");
        options.put("defWidth", "250");
        
        options.put("splitLines", "false");
        options.put("showFurigana", "true");
        options.put("showOnNewLine", "true");
    }
    public Options(File file)throws IOException
    {
        this();//start with defaults
        this.file = file;
        if(file.exists())
        {
            load();
        }
    }
    public void save()throws IOException
    {
        Set<String> keysLeft = new HashSet<>(defaults.options.keySet());
        String output = "";
        if(file.exists())
        {
            FileInputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            String line = br.readLine();
            while(line != null)
            {
                String bits[] = line.split("#")[0].split("=");//remove all after hash (comments), split between =
                if(bits.length == 2)//if this line has an option
                {
                    String key = bits[0].trim();
                    if(options.containsKey(bits[0].trim()))
                    {
                        output += key + " = " + options.get(key) + "\n";//replace with new value
                        keysLeft.remove(key);
                    }
                    else output += line + "\n";//unknown option, keep it in I guess
                }
                else output += line + "\n";//keep comments and such the way it is
                line = br.readLine();
            }
            br.close();
        }
        //now overwrite the file
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        fr.append(output.trim() + "\n");
        //add all leftover keys (or all keys if file does not exist)
        for(String key:keysLeft)
        {
            System.out.println("appending lefover " + key);
            fr.append(key + " = " + defaults.options.get(key) + "\n");
        }
        fr.close();
    }
    public String getOption(String tag)
    {
        //find, or return default option if it doesn't exist
        return options.getOrDefault(tag, defaults.options.get(tag));
    }
    public boolean getOptionBool(String tag)
    {
        return getOption(tag).toLowerCase().startsWith("t") || getOption(tag).toLowerCase().startsWith("y");
    }
    public int getOptionInt(String tag)
    {
        return Integer.parseInt(getOption(tag));
    }
    public Color getColor(String tag)
    {
        String bits[] = getOption(tag).split(",");
        if(bits.length != 4)bits = defaults.options.get(tag).split(",");
        //RGBA
        return new Color(Integer.parseInt(bits[0].trim()),
                         Integer.parseInt(bits[1].trim()),
                         Integer.parseInt(bits[2].trim()),
                         Integer.parseInt(bits[3].trim()));
    }
    public Font getFont(String tag)
    {
        //font format: name, tags, size
        String bits[] = getOption(tag).split(",");
        int mods = 0;
        bits[1] = bits[1].trim().toUpperCase();
        for (int i = 0; i < bits[1].length(); i++)
        {
            switch(bits[1].charAt(i))
            {
                case 'B':
                    mods |= Font.BOLD;
                    break;
                case 'I':
                    mods |= Font.ITALIC;
                    break;
                    //Anti-Alisasing isn't applied to Font object, applied seperately
            }
            
        }
        return new Font(bits[0].trim(), mods, Integer.parseInt(bits[2].trim()));
    }
    public boolean getFontAA(String tag)
    {
        return getOption(tag).split(",")[1].toUpperCase().contains("A");
    }
    public void getFontAA(Graphics2D g, String tag)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
          getFontAA(tag)?RenderingHints.VALUE_ANTIALIAS_ON:RenderingHints.VALUE_ANTIALIAS_OFF);
    }
    public void setOption(String tag, String value)
    {
        options.put(tag, value);
    }

    public void load()throws IOException
    {
        options = new HashMap<>();
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        while(line != null)
        {
            String bits[] = line.split("#")[0].split("=");//remove all after hash (comments), split between =
            if(bits.length == 2)//if this line has an option
            {
                options.put(bits[0].trim(), bits[1].trim());//add it
            }
            line = br.readLine();
        }
        br.close();
    }
    
}
