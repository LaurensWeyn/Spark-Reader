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
import java.awt.event.ActionEvent;

/**
 *
 * @author Laurens Weyn
 */
public class ToggleOption extends UIOption
{
    private boolean invert;
    private JCheckBox checkbox;
    public ToggleOption(String tag, String name, String tip)
    {
        super(tag, name, tip);
        invert = false;
        
        checkbox = new JCheckBox();
                
        checkbox.setAction(new AbstractAction(name){
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean val = checkbox.isSelected();
                if(invert)val = !val;
                setValue(val? "true":"false");
            }
        });
        checkbox.setToolTipText(tip);
        update();
    }
    public ToggleOption(String tag, String name, String tip, boolean invert)
    {
        this(tag, name, tip);
        this.invert = invert;
    }
    @Override
    public JComponent getComponent()
    {
        return checkbox;
    }

    @Override
    public void update()
    {
        checkbox.setSelected(invert? !options.getOptionBool(tag):options.getOptionBool(tag));
    }
    
}
