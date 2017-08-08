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
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Laurens Weyn
 */
public class Host extends MPController
{
    private ConcurrentHashMap<Integer, Integer> clientPositions = new ConcurrentHashMap<>();
    private int port;

    public Host(int port)
    {
        this.port = port;
    }
    public Host()
    {
        this(11037);
    }

    @Override
    public void run()
    {

        //This thread will wait for and take in clients
        try(ServerSocket listener = new ServerSocket(port))
        {
            int clientNumber = 1;
            while (running)
            {
                new ClientManager(this, listener.accept(), clientNumber++).start();
            }
        }
        catch(IOException e)
        {
            Component parent = null;
            if(Main.ui != null)parent = Main.ui.disp.getFrame();

            if(parent != null)JOptionPane.showMessageDialog(parent, "Error receiving clients:\n" + e);
            else e.printStackTrace();//headless
        }
        running = false;
    }
    void updateClient(int clientID, int position)
    {
        clientPositions.put(clientID, position);
    }
    void removeClient(int clientID)
    {
        clientPositions.remove(clientID);
    }

    @Override
    public String getStatusText()
    {
        if(clientPositions.size() == 1)//2 player: show friend's status
        {
            int pos = clientPositions.values().iterator().next();
            if(pos == Integer.MIN_VALUE)return "unknown client position";
            else if(pos > 0)return "client " + pos + " lines ahead";
            else if(pos < 0)return "client " + (-pos) + " lines behind";
            else return "client in sync";
        }
        else//one host, many players: show status summary
        {
            int ahead = 0;
            int behind = 0;
            int here = 0;
            int lost = 0;
            for (Integer i:clientPositions.values())
            {
                if(i == Integer.MIN_VALUE) lost++;
                else if(i > 0) ahead++;
                else if(i < 0) behind++;
                else here++;
            }
            String out = "";
            if(lost > 0)
            {
                out += lost + " unknown";
            }
            if(ahead > 0)
            {
                if(!out.equals(""))out += ", ";
                out += ahead + " ahead";
            }
            if(behind > 0)
            {
                if(!out.equals(""))out += ", ";
                out += behind + " behind";
            }
            if(here > 0)
            {
                if(!out.equals(""))out += ", ";
                out += here + " in sync";
            }
            if(out.equals(""))
            {
                out = "Waiting for clients..";
            }
            return out + ".";
        }
    }
}
