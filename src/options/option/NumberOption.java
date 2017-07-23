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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Laurens Weyn
 */
public class NumberOption extends UIOption
{
    JSpinner spinner;
    JPanel panel;
    public NumberOption(String tag, String name, String tip, SpinnerModel model)
    {
        super(tag, name, tip);

        spinner = new JSpinner(model);
        spinner.setValue(Integer.parseInt(getValue()));
        JLabel label = new JLabel(" " + name);
        label.setToolTipText(tip);
        spinner.addChangeListener(new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e)
            {
                setValue(spinner.getValue().toString());
            }
        });
        spinner.setToolTipText(tip);
        //spinner.setMaximumSize(label.getPreferredSize());
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(spinner);
        panel.add(label);
        ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().setColumns(5);
        spinner.setMaximumSize(spinner.getPreferredSize());
    }
    public NumberOption(String tag, String name, String tip)
    {
        this(tag, name, tip, makeModel(NumberPreset.resolution));
    }
    public NumberOption(String tag, String name, String tip, NumberPreset preset)
    {
        this(tag, name, tip, makeModel(preset));
    }
    private static SpinnerNumberModel makeModel(NumberPreset preset)
    {
        switch(preset)
        {
            case posNeg: return new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
            case posOnly:return new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
            case resolution:return new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 10);
        }
        throw new IllegalArgumentException("Unsupported preset " + preset);
    }
    public enum NumberPreset
    {
        resolution,
        posNeg,
        posOnly,
    }
    @Override
    public JComponent getComponent()
    {
        return panel;
    }

    @Override
    public void update()
    {
        spinner.setValue(Integer.parseInt(getValue()));
    }
    
}
