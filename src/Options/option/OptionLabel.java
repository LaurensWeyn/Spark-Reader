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
package options.option;

import javax.swing.*;

/**
 *
 * @author Laurens Weyn
 */
public class OptionLabel extends UIOption
{
    JLabel label;
    public OptionLabel(String name, String tip)
    {
        super("", name, tip);
        label = new JLabel("<html><br><h3>" + name);
        label.setToolTipText(tip);
    }

    @Override
    public JComponent getComponent()
    {
        return label;
    }

    @Override
    public void update()
    {
        
    }
    
}
