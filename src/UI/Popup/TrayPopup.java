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
import Language.Splitter.FoundWord;
import Options.OptionsUI;
import UI.UI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 *
 * @author Laurens Weyn
 */
public class TrayPopup extends JPopupMenu
{

    JMenuItem restore;
    JMenuItem close;
    JMenuItem settings;

    int x, y;
    UI ui;
    private Component invoker;
    public TrayPopup(UI ui, Component invoker)
    {
        this.ui = ui;
        this.invoker = invoker;
        close = new JMenuItem(new AbstractAction("Exit")
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
        restore = new JMenuItem(new AbstractAction("Restore")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                UI.hidden = false;
                ui.tray.hideTray();
                ui.render();
            }
        });
        settings = new JMenuItem(new AbstractAction("Settings")
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
        //show(this, x, y);
        setLocation(x, y);
        setInvoker(this);
        setVisible(true);
    }
}
