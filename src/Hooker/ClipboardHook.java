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
package Hooker;

import Language.Dictionary.Japanese;
import UI.UI;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 *
 * @author Laurens Weyn
 */
public class ClipboardHook implements Hook
{
    private static String lastClip = "";
    public static boolean ignoreNextLine = false;

    public ClipboardHook()
    {
        lastClip = getClipboard();
        if(Japanese.isJapanese(lastClip))
        {
            lastClip = "";//never mind, update anyway (don't immediately hide on English text)
        }
        if(UI.options.getOptionBool("startInTray"))
        {
            lastClip = "";//allow immediately hiding on English text
        }
    }
    //to call every 100ms or so
    public String check()
    {
        String clip = getClipboard();
        if(!lastClip.contains(clip))//new line (not word lookup or something)
        {
            lastClip = clip;
            System.out.println("clipboard updated to " + clip);
            boolean isJapanese = Japanese.isJapaneseWriting(clip);
            if(isJapanese && !ignoreNextLine)
            {
                return clip;
            }
            if(!isJapanese && UI.options.getOptionBool("hideOnOtherText"))
            {
                UI.hidden = true;
                if(UI.instance != null)
                {
                    UI.instance.tray.showTray();
                    UI.instance.render();
                }
            }
            ignoreNextLine = false;//line passed
        }
        return null;//nothing new
    }
    public static String getClipboard()
    {
        String clipboard = "";
        try
        {
            clipboard = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        }catch(IllegalStateException ex)//happens sometimes, pretty normal
        {
            try
            {
                Thread.sleep(1);
            } catch (InterruptedException ignored){}

            return getClipboard();//try again later xD
        }
        catch(UnsupportedFlavorException ex)
        {
            //ignore these, it happens sometimes, trying later doesn't help
        } catch(IOException ex)
        {
            System.out.println("clipboard IO error: " + ex);
        }
        //System.out.println("clipboard: " + clipboard);
        return clipboard;
    }
    public static void setClipboard(String text)
    {
        if(!lastClip.contains(text))
        {
            ignoreNextLine = true;//don't include this change or else we'll trigger
        }
        setClipBoardAndUpdate(text);//update avoided
    }

    public static void setClipBoardAndUpdate(String text)
    {
        try
        {
            StringSelection selection = new StringSelection(text);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
        }catch(IllegalStateException e)
        {
            try
            {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}

            setClipBoardAndUpdate(text);//try again later
        }
    }
}
