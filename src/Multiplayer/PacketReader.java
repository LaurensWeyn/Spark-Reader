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
package Multiplayer;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 *
 * @author Laurens Weyn
 */
public class PacketReader
{
    private Reader input;
    private String buffer;
    public PacketReader(Reader input)
    {
        this.input = input;
    }
    public String[] getPacket()throws IOException
    {
        while(input.ready())//read all new input
        {
            char c = (char)input.read();
            if(c == '\n')
            {
                String output = buffer;
                buffer = "";
                return output.split("\t");
            }
            else buffer += c;
        }
        return null;//no packet yet
    }
    public void close()throws IOException
    {
        input.close();
    }
}
