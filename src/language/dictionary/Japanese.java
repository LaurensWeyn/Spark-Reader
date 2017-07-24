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
    //disallow instances of class
    private Japanese(){}

    //reference: http://www.rikai.com/library/kanjitables/kanji_codes.unicode.shtml

    public static boolean isDigit(char c)
    {
        return (c >= '0' && c <= '9') || (c >= '０' && c <= '９');
    }
    public static boolean isGrammar(char c)
    {
        return c == '\n' || c == '。' || c == '…' || c == '？' || c == '　';
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
        return isKana(c) || isKanji(c) || c == '々';
    }
    public static boolean hasKanji(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(isKanji(text.charAt(i)))return true;
        }
        return false;
    }
    public static boolean isKanji(char c)
    {
        return (0x4e00 <= c && c <= 0x9faf) || (0x3400 <= c && c <= 0x4dbf);
    }
    public static boolean hasKana(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(isKana(text.charAt(i)))return true;
        }
        return false;
    }
    public static boolean hasOnlyKana(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(!isKana(text.charAt(i)))return false;
        }
        return true;
    }
    public static boolean hasOnlyKatakana(String text)
    {
        for (int i = 0; i < text.length(); i++)
        {
            if(!isKatakana(text.charAt(i)))return false;
        }
        return true;
    }
    public static boolean isKana(char c)
    {
        return (0x3040 <= c && c <= 0x30ff) || c == '○';
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
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if(isHiragana(c))
            {
                output.append(c);
            }
            else if(isKatakana(c))
            {
                output.append((char) (c - (0x30a0 - 0x3040)));//shift
            }
            else if(!stripOthers) output.append(c);//Kanji and others intentionally removed from output if needed (for reading extraction)
        }
        return output.toString();
    }
    public static String toKatakana(String input, boolean stripOthers)
    {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if(isHiragana(c))
            {
                output.append((char) (c + (0x30a0 - 0x3040)));//shift
            }
            else if(isKatakana(c))
            {
                output.append(c);
            }
            else if(!stripOthers) output.append(c);//Kanji and others intentionally removed from output if needed (for reading extraction)
        }
        return output.toString();
    }

    /**
     * Removes kana before and after a word. Used for Furigana rendering.
     * @param kanji the word in Kanji
     * @param kana the word in Kana
     * @return the kanji word, minus matching kana at the start and end.
     */
    public static String stripOkurigana(String kanji, String kana)
    {
        if(kanji == null)return null;
        if(kana == null || kana.equals(""))return "";
        int end = 0;
        int start = 0;
        try
        {
            while(kanji.substring(kanji.length() - end - 1, kanji.length())
                    .equals(kana.substring(kana.length() - end - 1, kana.length())))
                end++;
            while(kanji.substring(0, start + 1)
                    .equals(kana.substring(0, start + 1)))
                start++;

            return kana.substring(start, kana.length() - end);
        }catch(StringIndexOutOfBoundsException ignored)
        {
            return "";
        }
    }


    /**
     * Converts latin characters to their full width equivalent. Other characters (Japanese etc.) are left as is.<br>
     * Also removes carriage returns if present.
     * @param input text to convert
     * @return full width version of input
     */
    public static String toFullWidth(String input)
    {
        StringBuilder output = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if(c >= 'a' && c <= 'z')
            {
                output.append((char)(c + ('ａ' - 'a')));
            }else if(c >= 'A' && c <= 'Z')
            {
                output.append((char)(c + ('Ａ' - 'A')));
            }else if(c >= '0' && c <= '9')
            {
                output.append((char)(c + ('０' - '0')));
            }
            else if(c == ' ' || c == '\t')output.append('　');//full width space
            else if(c != '\r')output.append(c);

        }
        return output.toString();
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
