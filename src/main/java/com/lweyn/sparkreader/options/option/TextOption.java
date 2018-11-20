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
package com.lweyn.sparkreader.options.option;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author Laurens Weyn
 */
public class TextOption extends UIOption
{
    private JTextField textField;
    private JPanel panel;
    public TextOption(String tag, String name, String tip)
    {
        this(tag, name, tip, 0);
    }
    public TextOption(String tag, String name, String tip, int size)
    {
        super(tag, name, tip);

        textField = new JTextField(getValue());

        JLabel label = new JLabel();
        if(size == 0)label.setText(name + ":");
        else label.setText(" " + name);
        label.setToolTipText(tip);

        textField.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e){}
            @Override
            public void keyPressed(KeyEvent e){}
            @Override
            public void keyReleased(KeyEvent e)
            {
                setValue(textField.getText());
            }
        });
        textField.setToolTipText(tip);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        textField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel = new JPanel();
        if(size == 0)
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.add(label);
            panel.add(textField);
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, textField.getPreferredSize().height));
        }
        else
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
            panel.add(textField);
            panel.add(label);
            textField.setColumns(size);
            textField.setMaximumSize(textField.getPreferredSize());
        }
    }
    @Override
    public JComponent getComponent()
    {
        return panel;
    }

    @Override
    public void update()
    {
        textField.setText(getValue());
    }
    
}
