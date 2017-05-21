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
package options.page;

import options.option.UIOption;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;

/**
 *
 * @author Laurens Weyn
 */
public class OptionPage implements Page
{
    private ArrayList<UIOption> options;
    private final JPanel frame;
    private String name;
    
    public OptionPage(String name)
    {
        options = new ArrayList<>();
        frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));
        frame.setBorder(new EmptyBorder(3, 3, 3, 3));
        //frame.setLayout(new FlowLayout(FlowLayout.LEFT));
        frame.add(new JLabel(name));
        this.name = name;
    }
    public void add(UIOption option)
    {
        options.add(option);
        JComponent c = option.getComponent();
        c.setAlignmentX(0);
        frame.add(c);
    }
    
    @Override
    public JComponent getComponent()
    {
        return frame;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public void update()
    {
        for(UIOption option:options)
        {
            option.update();
        }
    }
    
}
