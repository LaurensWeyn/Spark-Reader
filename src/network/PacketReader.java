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
package network;

import java.io.IOException;
import java.io.Reader;

/**
 * Reads "CSV packets". Each packet is terminated by a new line, and has tab separated parameters.
 * The first parameter is the command code
 * @author Laurens Weyn
 */
public class PacketReader
{
    private Reader input;
    private StringBuilder buffer;


    public PacketReader(Reader input)
    {
        this.input = input;
        buffer = new StringBuilder();
    }

    /**
     * Gets a packet from the stream
     * @return an array of strings containing the parameters for this packet, or null if no new packet is available
     * @throws IOException if the underling Reader fails
     */
    public String[] getPacket()throws IOException
    {
        while(input.ready())//read all new input
        {
            char c = (char)input.read();
            if(c == '\n')
            {
                String output = buffer.toString();
                buffer = new StringBuilder();
                System.out.println("recieved packet " + output);
                return output.split("\t");
            }
            else if(c != MPController.ALIVE_CODE)buffer.append(c);
        }
        return null;//no packet yet
    }

    /**
     * Closes the associated Reader
     * @throws IOException if the Reader fails
     */
    public void close()throws IOException
    {
        input.close();
    }
}
