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
package ui.popup;

import hooker.ClipboardHook;
import hooker.MemoryHook;
import hooker.WindowHook;
import main.Main;
import multiplayer.Client;
import multiplayer.Host;
import options.OptionsUI;
import ui.UI;
import ui.WindowHookUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import static main.Main.options;

/**
 *
 * @author Laurens Weyn
 */
public class MenuPopup extends JPopupMenu
{
    UI ui;
    
    JMenuItem exit, imprt, settings, reloadDict, minimise;
    JMenu mp;
    JMenuItem mpHost, mpJoin, mpDisconnect;
    JMenuItem  memoryHookStart, memoryHookStop, memoryHookRefine;
    public MenuPopup(UI ui)
    {
        this.ui = ui;
        ///////////////////////
        //multiplayer submenu//
        ///////////////////////
        mp = new JMenu("Multiplayer");
        mpHost = new JMenuItem(new AbstractAction("Host")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String oldIgnoreState = options.getOption("hideOnOtherText");
                options.setOption("hideOnOtherText", "false");
                String portStr = JOptionPane.showInputDialog(ui.disp.getFrame(), "Enter the port to use (leave blank for default port, 11037)");
                int port = 11037;
                try
                {
                    if(portStr.length() > 0)port = Integer.parseInt(portStr);
                }catch(NumberFormatException ignored){}

                Main.mpManager = new Host(port);
                Main.mpThread = new Thread(Main.mpManager);
                Main.mpThread.start();
                JOptionPane.showMessageDialog(ui.disp.getFrame(), "Server running. Other users with Spark Reader can now connect to your IP.\nIf you want people to connect outside of your LAN, please port forward port " + port);
                options.setOption("hideOnOtherText", oldIgnoreState);
            }
        });
        mpJoin = new JMenuItem(new AbstractAction("Join")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String oldIgnoreState = options.getOption("hideOnOtherText");
                options.setOption("hideOnOtherText", "false");
                String addr = JOptionPane.showInputDialog(ui.disp.getFrame(), "Please enter the IP address of the host");
                try
                {
                    String port = "11037";
                    String bits[] = addr.split(":");
                    if(bits.length == 2)
                    {
                        addr = bits[0];
                        port = bits[1];
                    }
                    Socket s = new Socket(addr, Integer.parseInt(port));
                    Main.mpManager = new Client(s);
                    Main.mpThread = new Thread(Main.mpManager);
                    Main.mpThread.start();
                } catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(ui.disp.getFrame(), "Error connecting to host: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
                options.setOption("hideOnOtherText", oldIgnoreState);
            }
        });
        mpDisconnect = new JMenuItem(new AbstractAction("Disconnect")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Main.mpManager != null)
                {
                    Main.mpManager.running = false;
                }
                Main.mpThread = null;
                ui.render();//remove MP text from screen on disconnect
            }
        });
        if(Main.mpThread == null)
        {
            mpDisconnect.setEnabled(false);
        }
        else
        {
            mpJoin.setEnabled(false);
            mpHost.setEnabled(false);
        }
        mp.add(mpHost);
        mp.add(mpJoin);
        mp.add(mpDisconnect);
        
        ///////////////////
        //main menu items//
        ///////////////////
        exit = new JMenuItem(new AbstractAction("Exit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.exit();
            }
        });
        minimise = new JMenuItem(new AbstractAction("Minimise to tray")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.hidden = true;
                ui.tray.showTray();
                ui.render();
            }
        });
        minimise.setSelected(UI.hidden);
        
        imprt = new JMenuItem(new AbstractAction("Import known words")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                importPrompt();
            }
        });
        settings = new JMenuItem(new AbstractAction("Edit settings")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    OptionsUI.showOptions(Main.options);
                }catch(IOException err)
                {
                    JOptionPane.showMessageDialog(ui.disp.getFrame(), "Error editing configuration: " + e);
                }
            }
        });

        memoryHookStart = new JMenuItem(new AbstractAction("Start memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MemoryHook newHook = new MemoryHook();
                if(newHook.isRunning())
                {
                    Main.hook = newHook;
                }
            }
        });
        memoryHookStop = new JMenuItem(new AbstractAction("Stop memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.hook = new ClipboardHook();
            }
        });
        memoryHookRefine = new JMenuItem((new AbstractAction("Rescan memory")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MemoryHook hook = (MemoryHook) Main.hook;
                hook.refine();
            }
        }));
        JMenuItem windowHook = new JMenuItem(new AbstractAction("Stick to window")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new WindowHookUI();
            }
        });
        JMenuItem windowUnHook = new JMenuItem(new AbstractAction("Stop sticking to window")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.stickToWindow = null;
            }
        });
        
        add(settings);
        /*if(UI.hook instanceof MemoryHook)
        {
            add(memoryHookRefine);
            add(memoryHookStop);
        }
        else add(memoryHookStart);*/
        
        if(WindowHook.hook != null && UI.stickToWindow == null)
            add(windowHook);
        else if(UI.stickToWindow != null)
            add(windowUnHook);
        
        add(imprt);
        add(mp);
        add(minimise);
        add(exit);
    }

    public void display()
    {
        show(ui.disp.getFrame(), UI.buttonStartX, UI.textStartY);
    }

    public void display(Point pos)
    {
        show(ui.disp.getFrame(), pos.x, pos.y);
    }
    public void importPrompt()
    {
        JFrame parent = ui.disp.getFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Word list (.csv; .txt)", "txt", "csv"));
        fileChooser.setAcceptAllFileFilterUsed(false);
        if(fileChooser.showDialog(parent, "Import") == JFileChooser.APPROVE_OPTION)
        {
            File chosen = fileChooser.getSelectedFile();
            try
            {
                Main.known.importCsv(chosen.getAbsoluteFile(), "\t");
                JOptionPane.showMessageDialog(parent, "Import successful!");
            }catch(Exception e)
            {
                JOptionPane.showMessageDialog(parent, "Error while importing: " + e);
            }
        }
        
        ui.render();//redraw known words on current text
    }
    
}
