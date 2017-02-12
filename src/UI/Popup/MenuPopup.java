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
package UI.Popup;

import Hooker.ClipboardHook;
import Hooker.MemoryHook;
import Multiplayer.Client;
import Multiplayer.Host;
import Options.OptionsUI;
import UI.UI;

import static UI.UI.hidden;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;

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
                UI.mpManager = new Host();
                UI.mpThread = new Thread(UI.mpManager);
                UI.mpThread.start();
                JOptionPane.showMessageDialog(ui.disp.getFrame(), "Server running. Other users with Spark Reader can now connect to your IP.\nIf you want people to connect outside of your LAN, please port forward port 11037");
            }
        });
        mpJoin = new JMenuItem(new AbstractAction("Join")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String addr = JOptionPane.showInputDialog(ui.disp.getFrame(), "Please enter the IP address of the host");
                try
                {
                    Socket s = new Socket(addr, 11037);
                    UI.mpManager = new Client(s);
                    UI.mpThread = new Thread(UI.mpManager);
                    UI.mpThread.start();
                } catch (IOException ex)
                {
                    JOptionPane.showMessageDialog(ui.disp.getFrame(), "Error connecting to host: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        mpDisconnect = new JMenuItem(new AbstractAction("Disconnect")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(UI.mpManager != null)
                {
                    UI.mpManager.running = false;
                }
                UI.mpThread = null;
                ui.render();//remove MP text from screen on disconnect
            }
        });
        if(UI.mpThread == null)
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
                try
                {
                    UI.known.save();
                    UI.prefDef.save();
                }catch(IOException err)
                {
                    JOptionPane.showMessageDialog(ui.disp.getFrame(), "Error while saving changes");
                    err.printStackTrace();
                }
                ui.disp.getFrame().setVisible(false);
                System.exit(0);
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
                    OptionsUI.showOptions(UI.options);
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
                    UI.hook = newHook;
                }
            }
        });
        memoryHookStop = new JMenuItem(new AbstractAction("Stop memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.hook = new ClipboardHook();
            }
        });
        memoryHookRefine = new JMenuItem((new AbstractAction("Rescan memory")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MemoryHook hook = (MemoryHook)UI.hook;
                hook.refine();
            }
        }));
        add(settings);
        /*if(UI.hook instanceof MemoryHook)
        {
            add(memoryHookRefine);
            add(memoryHookStop);
        }
        else add(memoryHookStart);*/
        add(imprt);
        add(mp);
        add(minimise);
        add(exit);
    }
    public void show()
    {
        show(ui.disp.getFrame(), UI.buttonStartX, UI.textStartY);
    }
    public void show(MouseEvent e)
    {
        show(ui.disp.getFrame(), e.getX(), e.getY());
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
                UI.known.importCsv(chosen.getAbsoluteFile(), "\t");
                JOptionPane.showMessageDialog(parent, "Import successful!");
            }catch(Exception e)
            {
                JOptionPane.showMessageDialog(parent, "Error while importing: " + e);
            }
        }
        
        ui.render();//redraw known words on current text
    }
    
}
