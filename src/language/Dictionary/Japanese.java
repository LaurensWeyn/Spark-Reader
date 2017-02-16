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
package language.dictionary;

import java.util.ArrayList;

/**
 * Contains various methods for dealing with Japanese text and characters
 * @author Laurens Weyn
 */
public class Japanese
{


    //reference: http://www.rikai.com/library/kanjitables/kanji_codes.unicode.shtml

    public static boolean isDigit(char c)
    {
        return (c >= '0' && c <= '9') || (c >= '０' && c <= '９');
    }
    public static boolean isGrammar(char c)
    {
        return c == '\n' || c == '\r' || c == '\t' || c == '。' || c == '…' || c == '？' || c == '　';
    }
    public static boolean isJapanese(String text)
    {
        //search for any Japanese characters
        for (int i = 0; i < text.length(); i++)
        {
            if(isJapanese(text.charAt(i)))return true;
        }
        //none found
        return false;
    }
    public static boolean isJapaneseWriting(String text)
    {
        //search for any Japanese characters
        for (int i = 0; i < text.length(); i++)
        {
            if(isJapaneseWriting(text.charAt(i)))return true;
        }
        //none found
        return false;
    }
    public static boolean isJapanese(char c)
    {
        return (0x3000 <= c && c <= 0x30ff) || isKanji(c) || isGrammar(c);
    }
    public static boolean isJapaneseWriting(char c)
    {
        return isKana(c) || isKanji(c);
    }
    public static boolean hasKanji(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(isKanji(text.charAt(i)))return true;
        }
        return false;
    }
    public static boolean hasKana(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(isKana(text.charAt(i)))return true;
        }
        return false;
    }
    public static boolean isKanji(char c)
    {
        return (0x4e00 <= c && c <= 0x9faf) || (0x3400 <= c && c <= 0x4dbf);
    }
    public static boolean isKana(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(isKana(text.charAt(i)))return true;
        }
        return false;
    }
    public static boolean isKana(char c)
    {
        return 0x3040 <= c && c <= 0x30ff;
    }
    public static boolean isHiragana(char c)
    {
        return 0x3040 <= c && c <= 0x309f;
    }
    public static boolean isKatakana(char c)
    {
        return 0x30a0 <= c && c <= 0x30ff;
    }
    public static String toHiragana(String input, boolean stripOthers)
    {
        String output = "";
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if(isHiragana(c))
            {
                output += c;
            }
            else if(isKatakana(c))
            {
                output += (char)(c - (0x30a0 - 0x3040));//shift
            }
            else if(stripOthers == false)output += c;//Kanji and others intentionally removed from output (for reading extraction)
        }
        return output;
    }

    public static String[] splitJapaneseWriting(String text)
    {
        ArrayList<String> strings = new ArrayList<>();
        int start = 0;
        int pos = 0;
        while(pos != text.length())
        {
            if(isJapaneseWriting(text.charAt(pos)))
            {
                pos++;
            }
            else
            {
                if(start != pos)strings.add(text.substring(start, pos));
                pos++;
                start = pos;
            }
        }
        return strings.toArray(new String[strings.size()]);
    }
}
