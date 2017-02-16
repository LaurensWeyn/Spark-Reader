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
package ui;

/**
 * Utility for flowing text with variable character widths
 * @author Laurens Weyn
 */
public class TextStream
{
    private String text;
    private int pos;

    public TextStream(String text)
    {
        this.text = text;
        pos = 0;
    }
    public String nextWord()
    {
        if(pos >= text.length())return null;
        String buffer = "";
        while(true)
        {
            buffer += text.charAt(pos++);
            
            if(pos >= text.length() || !noWrap(text.charAt(pos)) || text.charAt(pos) == ' ')
            {
                if(buffer.equals(" ") == false)return buffer;
            }
        }
    }
    public boolean isDone()
    {
        return pos >= text.length();
    }
    private boolean noWrap(char c)
    {
        return c < 0x0600;//English characters
    }
    
}
