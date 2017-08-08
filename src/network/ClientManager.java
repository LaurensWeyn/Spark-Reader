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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * Manages clients connecting to a MP server
 * @author Laurens Weyn
 */
public class ClientManager extends Thread
{
    private final Socket socket;
    private final Host host;
    private final int clientNum;
    private final static Charset encoding = Charset.forName("UTF-8");
    private String lastText;
    public ClientManager(Host host, Socket socket, int clientNum)
    {
        this.socket = socket;
        this.host = host;
        this.clientNum = clientNum;
        lastText = Main.currPage.getText();
    }
    
    @Override
    public void run()
    {
        try
        {
            PacketReader in = new PacketReader(new InputStreamReader(socket.getInputStream(), encoding));
            OutputStream out = socket.getOutputStream();
            out.write(("C\t" + MPController.currentLine()+ "\n").getBytes(encoding));//start by telling client where we are
            while(host.running)
            {
                String bits[] = in.getPacket();
                if(bits != null)switch(bits[0])
                {
                    case "U"://send C (what's your text?)
                        out.write(("C\t" + MPController.currentLine() + "\n").getBytes(encoding));
                        break;
                    case "C"://text is now [arg] for me, send R
                        {
                            int pos = MPController.positionOf(bits[1]);
                            out.write(("R\t" + pos + "\n").getBytes(encoding));
                            //client is behind (in our logs)
                            if(pos >= 0)
                            {
                                host.updateClient(clientNum, -pos);
                            }
                        }
                        break;
                    case "R"://that line is at pos [arg] for me
                        {
                            int pos = Integer.parseInt(bits[1]);
                            //client is ahead (in our logs)
                            if(pos >= 0)host.updateClient(clientNum, pos);
                            else//line unknown, request line (client ahead)
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
                String text = MPController.currentLine();
                if(!text.equals(lastText))
                {
                    out.write(("C\t" + text + "\n").getBytes(encoding));
                    lastText = text;
                    host.updateClient(clientNum, Integer.MIN_VALUE);//unknown until response
                }
                //check if client is still connected (will throw Exception if disconnected)
                out.write(MPController.ALIVE_CODE);
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
            //client disconnect, drop it from the list
            System.out.println(clientNum + " disconnected: " + e);
        }

        host.removeClient(clientNum);
    }
}
