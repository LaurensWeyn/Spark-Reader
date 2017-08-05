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

import main.Utils;

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
public class Options
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

        options.put("textFontUnhinted", "false");
        options.put("textFont", "Meiryo, A, 30");
        options.put("furiFont", "MS Gothic,, 12");
        options.put("defFont", "Meiryo,, 15");

        options.put("markerCol", "255, 255, 0, 200");
        options.put("noMarkerCol", "50, 50, 50, 150");
        options.put("furiCol", "0, 255, 255, 255");
        options.put("furiBackCol", "0, 0, 0, 128");
        options.put("windowBackCol", "0, 0, 0, 175");

        options.put("textBackMode", "normal");
        options.put("textBackVariable", "2");
        options.put("textBackCol", "0, 0, 0, 200");
        options.put("knownTextBackCol", "0, 0, 100, 200");
        options.put("clickedTextBackCol", "0, 100, 0, 200");
        options.put("textCol", "255, 255, 255, 255");
        options.put("knownTextCol", "255, 255, 255, 255");

        options.put("defReadingCol", "0, 255, 255, 255");
        options.put("defKanjiCol", "255, 255, 255, 255");
        options.put("defTagCol", "255, 255, 255, 255");
        options.put("defCol", "255, 255, 0, 255");
        options.put("defBackCol", "0, 0, 0, 128");
        

        options.put("dictionaryPath", "dictionaries/");
        options.put("customSourcePriority", "1");
        options.put("edictSourcePriority", "0");
        options.put("epwingSourcePriority", "-1");
        options.put("kanjideckSourcePriority", "-2");

        options.put("epwingStartBlacklist", "・");
        options.put("epwingBlacklistMinLines", "10");

        options.put("ankiExportPath", "forAnki.csv");
        options.put("knownWordsPath", "preferences/knownWords");
        options.put("preferredDefsPath", "preferences/preferredDefs");
        options.put("blacklistDefsPath", "preferences/blacklistDefs");
        
        options.put("persistPath", "preferences/persist.dat");

        options.put("windowWidth", "1280");
        options.put("maxHeight", "720");
        options.put("defWidth", "250");
        options.put("useNativeUI", "false");

        options.put("splitLines", "true");
        options.put("reflowToFit", "false");
        options.put("defsShowUpwards", "false");
        options.put("expectedLineCount", "4");
        options.put("knownFuriMode", "mouseover");
        options.put("unknownFuriMode", "always");

        options.put("showOnNewLine", "true");
        options.put("takeFocus", "false");

        options.put("enableKnown", "true");
        options.put("knownKanaLength", "0");
        options.put("knowKatakana", "false");
        options.put("enableWantList", "false");
        //options.put("exportUnmarksWant", "true");
        options.put("wantTextBackCol", "100, 0, 100, 200");

        options.put("commentOnExport", "true");
        options.put("commentOnExportLine", "true");
        options.put("exportMarksKnown", "false");
        options.put("showAllKanji", "false");
        options.put("reduceSave", "true");
        options.put("hideOnOtherText", "false");
        options.put("showDefOnMouseover", "false");
        options.put("resetDefScroll", "false");
        options.put("addKanjiAsDef", "true");
        options.put("startInTray", "false");
        options.put("hideDefOnMouseLeave", "true");
        options.put("timeStampFormat", "dd-MM-yyyy_hh-mm-ss");
        options.put("exportImage", "true");
        options.put("lineExportPath", "savedLines.csv");
        options.put("exportDisplay", "external");
        options.put("screenshotExportPath", "screenshots/");
        options.put("fullscreenScreenshot", "false");
        options.put("splitterMode", "full");
        options.put("deconMode", "recursive");
        options.put("furiMode", "original");
        options.put("unparsedWordsAltColor", "false");

        options.put("hookKeyboard", "false");
        options.put("forwardKeys", "false");
        options.put("showDefID", "false");
        options.put("defConstrainPosition", "true");//TODO UI doesn't work properly if this is set to false
        options.put("uiThrottleMilliseconds", "100");
        
        options.put("ankiExportEdictID", "false");
        options.put("ankiExportFreqData", "false");

        options.put("menubarOptionSpacing", "7");

        options.put("showReadingFreqs", "false");
    }
    public Options(File file)throws IOException
    {
        this();//start with defaults
        this.file = file;
        if(file.exists())
        {
            load();//load existing one
        }
        else
        {
            save();//make a new one
        }
    }
    public void save()throws IOException
    {
        Set<String> keysLeft = new HashSet<>(defaults.options.keySet());
        Set<String> keysDone = new HashSet<>();//attempt to stop duplicate settings
        StringBuilder output = new StringBuilder();
        if(file.exists())
        {
            BufferedReader br = Utils.UTF8Reader(file);
            String line = br.readLine();
            while(line != null)
            {
                String bits[] = line.split("#")[0].split("=");//remove all after hash (comments), split between =
                if(bits.length == 2)//if this line has an option
                {
                    String key = bits[0].trim();
                    if(keysDone.contains(key))
                    {
                        line = br.readLine();
                        continue;//skip this one, it's a duplicate
                    }
                    if(options.containsKey(key))
                    {
                        output.append(key).append(" = ").append(options.get(key)).append("\n");//replace with new value
                        keysLeft.remove(key);
                        keysDone.add(key);
                    }
                    else output.append(line).append("\n");//unknown option, keep it in I guess
                }
                else output.append(line).append("\n");//keep comments and such the way it is
                line = br.readLine();
            }
            br.close();
        }
        //now overwrite the file
        file.getParentFile().mkdirs();//ensure the folder it's supposed to be in exists
        Writer fr = new OutputStreamWriter(new FileOutputStream(file, false), Charset.forName("UTF-8"));
        fr.append(output.toString().trim()).append("\n");
        //add all leftover keys (or all keys if file does not exist)
        for(String key:keysLeft)
        {
            System.out.println("appending leftover option " + key);
            fr.append(key).append(" = ").append(defaults.options.get(key)).append("\n");
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
        int mods = Font.PLAIN;
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
                    //Anti-Aliasing isn't applied to Font object, applied separately
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
        BufferedReader br = Utils.UTF8Reader(file);
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

    /**
     * Get the file storing this configuration
     * @return
     */
    public File getFile()
    {
        return file;
    }
    
}
