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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 *
 * @author Laurens Weyn
 */
public class RadioOption extends UIOption
{
    JPanel mainPanel;
    ArrayList<ValueHandler> radioButtons;

    public RadioOption(String tag, String configText, String name, String tip)
    {
        super(tag, name, tip);
        String configs[] = configText.split(";");
        //create main panel
        //mainPanel = new JPanel();
        //mainPanel.setLayout(new BorderLayout());
        //mainPanel.setToolTipText(tip);

        mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(name));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setToolTipText(tip);
        radioButtons = new ArrayList<>();
        for(String config:configs)
        {
            String bits[] = config.split("=");//tag = Human Readable Name
            radioButtons.add(new ValueHandler(bits[1].trim(), bits[0].trim()));
        }

        update();//select the right one to begin

        for(ValueHandler vh:radioButtons)
        {
            mainPanel.add(vh.getComponent());
        }

        //mainPanel.add(new JLabel(tip), BorderLayout.NORTH);
        //mainPanel.add(preview, BorderLayout.EAST);
        //mainPanel.add(colPanel, BorderLayout.WEST);


        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, mainPanel.getMinimumSize().height));
    }
    @Override
    public void update()
    {
        String value = getValue();
        for(ValueHandler vh:radioButtons)
        {
            vh.setValue(vh.getTagValue().equals(value));
        }
    }

    @Override
    public JComponent getComponent()
    {
        return mainPanel;
    }

    private class ValueHandler
    {
        JRadioButton button;
        
        private String tagValue;
        public ValueHandler(String name, String tagValue)
        {
            this.tagValue = tagValue;
            button = new JRadioButton(new AbstractAction()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    for(ValueHandler vh:radioButtons)
                    {
                        vh.setValue(false);//all to false
                    }
                    setValue(true);//except this one

                    options.setOption(tag, tagValue);//write it to our options table
                }
            });
            button.setText(name);
            button.setToolTipText(tip);
        }

        public Component getComponent()
        {
            return button;
        }

        public boolean getValue()
        {
            return button.isSelected();
        }
        public void setValue(boolean value)
        {
            button.setSelected(value);
        }
        public String getTagValue()
        {
            return tagValue;
        }
    }
    private interface ValUpdater
    {
        public void valChange(int newVal);
    }
    
    
}
