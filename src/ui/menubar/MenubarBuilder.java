package ui.menubar;

import hooker.ClipboardHook;
import main.Main;
import network.Client;
import network.Host;
import options.OptionsUI;
import ui.UI;
import ui.WindowHookUI;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import static main.Main.currPage;
import static main.Main.options;
import static main.Main.ui;

/**
 * Holds the code that builds the options in the menu bar.
 */
public class MenubarBuilder
{
    private MenubarBuilder(){}

    public static Menubar buildMenu()
    {
        Menubar menubar = new Menubar();
        menubar.addItem(buildFileMenu());
        menubar.addItem(buildEditMenu());
        menubar.addItem(buildConnectMenu());
        menubar.addItem(buildHelpMenu());
        return menubar;
    }
    private static MenubarItem buildFileMenu()
    {
        MenubarItem item = new MenubarItem("File");
        item.addMenuItem(new AbstractAction("Import known words")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                importPrompt();
            }
        }, "Import");
        item.addMenuItem(new AbstractAction("Options")
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
        }, "Options");
        item.addMenuItem(new AbstractAction("Exit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.exit();
            }
        }, "Exit");
        return item;
    }

    private static MenubarItem buildEditMenu()
    {
        MenubarItem item = new MenubarItem("Edit");
        item.addMenuItem(new AbstractAction("Copy line")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ClipboardHook.setClipboard(currPage.getText());
            }
        }, "CopyLine");
        item.addSpacer();
        item.addMenuItem(new AbstractAction("Add new word")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        }, false, "NewWord");
        item.addMenuItem(new AbstractAction("Edit dictionary")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        }, false, "EditDict");
        return item;
    }

    private static MenubarItem buildConnectMenu()
    {
        MenubarItem item = new MenubarItem("Connect");

        item.addMenuItem(new AbstractAction("Stick to window")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JMenuItem thisItem = (JMenuItem)ui.menubar.getMenuItem(item.getName(), "Stick").getComponent();
                if(UI.stickToWindow == null)
                {
                    new WindowHookUI();//enable
                    thisItem.setText("Stop sticking to window");//rename option
                }
                else
                {
                    UI.stickToWindow = null;//disable
                    thisItem.setText("Stick to window");//rename option
                }
            }
        }, "Stick");

        item.addSpacer();
        item.addMenuItem(buildSourceSubMenu());
        item.addMenuItem(buildMPSubMenu());
        return item;
    }

    private static MenubarItem buildHelpMenu()
    {
        MenubarItem item = new MenubarItem("Help");
        item.addMenuItem(new AbstractAction("Statistics")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(Main.getParentFrame(), Main.persist.toString(), "Statistics", JOptionPane.PLAIN_MESSAGE);
            }
        }, "Stats");
        item.addMenuItem(new AbstractAction("View on GitHub")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Desktop.getDesktop().browse(new URI("https://github.com/thatdude624/Spark-Reader"));
                }catch(IOException | URISyntaxException err)
                {
                    err.printStackTrace();
                }
            }
        }, "Git");
        item.addMenuItem(new AbstractAction("About")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JOptionPane.showMessageDialog(Main.getParentFrame(), Main.ABOUT, "About Spark Reader", JOptionPane.PLAIN_MESSAGE);
            }
        }, "About");
        return item;
    }

    private static JMenu buildMPSubMenu()
    {
        JMenuItem mpHost = new JMenuItem(new AbstractAction("Host")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String oldIgnoreState = options.getOption("hideOnOtherText");
                options.setOption("hideOnOtherText", "false");
                String portStr = JOptionPane.showInputDialog(Main.getParentFrame(), "Enter the port to use (leave blank for default port, 11037)");
                int port = 11037;
                try
                {
                    if(portStr.length() > 0)port = Integer.parseInt(portStr);
                }catch(NumberFormatException ignored){}

                Main.mpManager = new Host(port);
                Main.mpThread = new Thread(Main.mpManager);
                Main.mpThread.start();
                JOptionPane.showMessageDialog(Main.getParentFrame(), "Server running. Other users with Spark Reader can now connect to your IP.\nIf you want people to connect outside of your LAN, please port forward port " + port);
                options.setOption("hideOnOtherText", oldIgnoreState);
            }
        });
        JMenuItem mpJoin = new JMenuItem(new AbstractAction("Join")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String oldIgnoreState = options.getOption("hideOnOtherText");
                options.setOption("hideOnOtherText", "false");
                String addr = JOptionPane.showInputDialog(Main.getParentFrame(), "Please enter the IP address of the host");
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
                    JOptionPane.showMessageDialog(Main.getParentFrame(), "Error connecting to host: " + ex, "Error", JOptionPane.ERROR_MESSAGE);
                }
                options.setOption("hideOnOtherText", oldIgnoreState);
            }
        });
        JMenuItem mpDisconnect = new JMenuItem(new AbstractAction("Disconnect")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(Main.mpManager != null)
                {
                    Main.mpManager.running = false;
                }
                Main.mpThread = null;
                Main.ui.render();//remove MP text from screen on disconnect
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
        
        
        
        JMenu mp = new JMenu("Multiplayer");
        mp.add(mpHost);
        mp.add(mpJoin);
        mp.add(mpDisconnect);
        return mp;
    }

    private static JMenu buildSourceSubMenu()
    {
        JRadioButtonMenuItem clipboard = new JRadioButtonMenuItem(new AbstractAction("Clipboard")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        JRadioButtonMenuItem mp = new JRadioButtonMenuItem(new AbstractAction("Multiplayer host")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        JRadioButtonMenuItem memory = new JRadioButtonMenuItem(new AbstractAction("Memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        JRadioButtonMenuItem subs = new JRadioButtonMenuItem(new AbstractAction("Subtitle file")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        clipboard.setSelected(true);
        mp.setEnabled(false);
        memory.setEnabled(false);
        subs.setEnabled(false);
        JMenu source = new JMenu("Text source");
        source.add(clipboard);
        source.add(mp);
        source.add(memory);
        source.add(subs);
        return source;
    }



    //other


    public static void importPrompt()
    {
        JFrame parent = Main.getParentFrame();
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

        Main.ui.render();//redraw known words on current text
    }
}
