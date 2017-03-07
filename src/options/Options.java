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

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Laurens Weyn
 */
public class Options implements Cloneable
{
    private HashMap<String, String> options;
    private File file;
    private static Options defaults = new Options();

    public static final File SETTINGS_FILE = new File("preferences/settings.txt");
    public Options()
    {
        options = new HashMap<>();
        file = null;
        
        ///////////////////////////////////////
        //all default settings hardcoded here//
        ///////////////////////////////////////

        options.put("textFont", "Meiryo, A, 30");
        options.put("furiFont", "MS Gothic,, 12");
        options.put("defFont", "Meiryo,, 15");

        options.put("markerCol", "255, 255, 0, 200");
        options.put("noMarkerCol", "50, 50, 50, 150");
        options.put("furiCol", "0, 255, 255, 255");
        options.put("furiBackCol", "0, 0, 0, 128");
        options.put("windowBackCol", "0, 0, 0, 175");

        options.put("textBackCol", "0, 0, 0, 200");
        options.put("knownTextBackCol", "0, 0, 100, 200");
        options.put("clickedTextBackCol", "0, 100, 0, 200");
        options.put("textCol", "255, 255, 255, 255");

        options.put("defReadingCol", "0, 255, 255, 255");
        options.put("defKanjiCol", "255, 255, 255, 255");
        options.put("defTagCol", "255, 255, 255, 255");
        options.put("defCol", "255, 255, 0, 255");
        options.put("defBackCol", "0, 0, 0, 128");
        

        options.put("dictionaryPath", "dictionaries/");
        options.put("customSourcePriority", "1");
        options.put("edictSourcePriority", "0");
        options.put("epwingSourcePriority", "-1");
        options.put("kanjideckSourcePriority", "-5");



        options.put("ankiExportPath", "forAnki.csv");
        options.put("knownWordsPath", "preferences/knownWords");
        options.put("preferredDefsPath", "preferences/preferredDefs");
        
        options.put("windowWidth", "1280");
        options.put("maxHeight", "720");
        options.put("defWidth", "250");
        
        options.put("splitLines", "true");
        options.put("reflowToFit", "false");
        options.put("defsShowUpwards", "false");
        options.put("expectedLineCount", "4");
        options.put("knownFuriMode", "mouseover");
        options.put("unknownFuriMode", "always");

        options.put("showOnNewLine", "true");
        options.put("takeFocus", "true");
        
        
        options.put("commentOnExport", "true");
        options.put("commentOnExportLine", "true");
        options.put("exportMarksKnown", "false");
        options.put("showAllKanji", "false");
        options.put("reduceSave", "true");
        options.put("hideOnOtherText", "false");
        options.put("showDefOnMouseover", "false");
        options.put("addKanjiAsDef", "true");
        options.put("startInTray", "false");
        options.put("hideDefOnMouseLeave", "true");
        options.put("timeStampFormat", "dd-MM-yyyy_hh-mm-ss");
        options.put("exportImage", "true");
        options.put("lineExportPath", "savedLines.csv");
        options.put("screenshotExportPath", "screenshots/");
        options.put("fullscreenScreenshot", "false");


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
        if(!defaults.options.containsKey(tag))throw new IllegalArgumentException("Unknown option tag '" + tag + "'");
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
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          getFontAA(tag)?RenderingHints.VALUE_TEXT_ANTIALIAS_ON:RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
    public void getFont(Graphics2D g, String tag)
    {
        g.setFont(getFont(tag));
        getFontAA(g, tag);
    }
    public File getFile(String tag)
    {
        return new File(getOption(tag));
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

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        super.clone();
        Options c = new Options();
        c.options = (HashMap<String, String>)options.clone();
        c.file = file;
        return c;
    }

    /**
     * Get the file storing this configuration
     * @return
     */
    public File getFile()
    {
        return file;
    }
    
}
