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

import ui.popup.TrayPopup;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Tray icon controller
 * @author Laurens Weyn
 */
public class Tray
{
    private boolean showing = false;
    private UI parent;
    TrayIcon icon;

    public Tray(UI parent)
    {
        this.parent = parent;
        try
        {
            icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream("/ui/icon.gif")));
            icon.addActionListener(new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    UI.hidden = false;
                    parent.render();
                    hideTray();
                }
            });
            icon.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                    if(e.getButton() == MouseEvent.BUTTON3)//right click
                    {
                        new TrayPopup(parent, null).show(e.getX(), e.getY());
                    }
                    else if(e.getButton() == MouseEvent.BUTTON1)//left click
                    {
                        UI.hidden = false;
                        parent.render();
                        hideTray();
                    }
                }
            });
        }catch (IOException ex)
        {
            System.out.println("error loading icon: " + ex);
        }
    }

    public boolean isShowing()
    {
        return showing;
    }
    
    public void showTray()
    {
        if(showing || !SystemTray.isSupported())return;
        SystemTray tray = SystemTray.getSystemTray();
        try
        {
            tray.add(icon);
        }catch (AWTException ex)
        {
            System.out.println("error displaying icon: " + ex);
        } 
        
        
        showing = true;
    }
    public void hideTray()
    {
        if(!showing || !SystemTray.isSupported())return;
        SystemTray tray = SystemTray.getSystemTray();
        
        tray.remove(icon);
        
        showing = false;        
    }
}
