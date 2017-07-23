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

import main.Main;
import options.OptionsUI;
import ui.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

/**
 * Right-click menu associated with the tray icon
 * @author Laurens Weyn
 */
public class TrayPopup extends JPopupMenu
{

    private JMenuItem restore;
    private JMenuItem close;
    private JMenuItem settings;

    int x, y;
    UI ui;
    public TrayPopup(UI ui)
    {
        this.ui = ui;
        close = new JMenuItem(new AbstractAction("Exit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.exit();
            }
        });
        restore = new JMenuItem(new AbstractAction("Restore")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                ui.restore();
            }
        });
        settings = new JMenuItem(new AbstractAction("Settings")
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
        add(settings);
        add(restore);
        add(new Separator());
        add(close);

        addPopupMenuListener(new IgnoreExitListener());
    }
    public void show(int x, int y)
    {
        this.x = x;
        this.y = y;
        setLocation(x, y);
        setInvoker(this);
        setVisible(true);
    }
}
