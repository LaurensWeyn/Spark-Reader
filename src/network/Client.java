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

import main.Main;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Manages a client to a Spark Reader MP server
 * @author Laurens Weyn
 */
public class Client extends MPController
{
    private final Socket socket;
    private int position = 0;
    private final static Charset encoding = Charset.forName("UTF-8");
    private String lastText;

    public Client(Socket socket)
    {
        this.socket = socket;
        lastText = currentLine();
    }

    @Override
    public void run()
    {
        try
        {
            PacketReader in = new PacketReader(new InputStreamReader(socket.getInputStream(), encoding));
            OutputStream out = socket.getOutputStream();
            out.write(("C\t" + currentLine() + "\n").getBytes(encoding));//start by telling server where we are
            while(running)
            {
                String bits[] = in.getPacket();
                if(bits != null)switch(bits[0])
                {
                    case "U"://send C (what's your text?)
                        out.write(("C\t" + currentLine() + "\n").getBytes(encoding));
                        break;
                    case "C"://text is now [arg] for me, send R
                        {
                            int pos = positionOf(bits[1]);
                            out.write(("R\t" + pos + "\n").getBytes(encoding));
                            //client is behind (in our logs)
                            if(pos >= 0)
                            {
                                position = -pos;
                            }
                        }
                        break;
                    case "R"://that line is at pos [arg] for me
                        {
                            int pos = Integer.parseInt(bits[1]);
                            //client is ahead (in our logs)
                            if(pos >= 0)position = pos;
                            else//line unknown, request line (client behind)
                            {
                                out.write("U\n".getBytes(encoding));
                            }
                        }
                        break;
                    case "V"://protocol version request (pseudo future proofing)
                        out.write("v 1.0\n".getBytes(encoding));
                        break;
                }
                
                //check for updates on our text
                String text = currentLine();
                if(!text.equals(lastText))
                {
                    out.write(("C\t" + text + "\n").getBytes(encoding));
                    lastText = text;
                    position = Integer.MIN_VALUE;//unknown until response
                }
                //check if server is still connected (will throw Exception if disconnected)
                out.write(ALIVE_CODE);
                //wait a bit before we run again
                try
                {
                    Thread.sleep(300);
                }catch(InterruptedException e){}
                
            }
            out.close();
            in.close();
        }catch(IOException e)
        {
            //disconnect from server
            JOptionPane.showMessageDialog(Main.ui.disp.getFrame(), "Disconnected from server:\n" + e);
        }
        running = false;
    }

    @Override
    public String getStatusText()
    {
        if(position == Integer.MIN_VALUE)return "Waiting for server";
        else if(position == 0)return "in sync";
        if(position < 0)return (position * -1) + " lines ahead";
        else return position + " lines behind";
    }
}
