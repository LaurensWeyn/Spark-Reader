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
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Laurens Weyn
 */
public class ColourOption extends UIOption
{
    Color colour;
    JLabel preview;
    JPanel mainPanel, colPanel;
    ArrayList<ValueHandler> values;

    static final int height = 150;
    
    private boolean internalChange = false;
    public ColourOption(String tag, String name, String tip)
    {
        super(tag, name, tip);
        colour = options.getColor(tag);
        
        //create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(name));
        //mainPanel.setToolTipText(tip);
        
        //create colours
        colPanel = new JPanel();
        colPanel.setLayout(new BoxLayout(colPanel, BoxLayout.Y_AXIS));
        values = new ArrayList<>();
        values.add(new ValueHandler("R", colour.getRed(), new ValUpdater()
        {
            @Override
            public void valChange(int newVal)
            {
                colour = new Color(newVal, colour.getGreen(), colour.getBlue(), colour.getAlpha());
                updateInternal();
            }
        }));
        values.add(new ValueHandler("G", colour.getGreen(), new ValUpdater()
        {
            @Override
            public void valChange(int newVal)
            {
                colour = new Color(colour.getRed(), newVal, colour.getBlue(), colour.getAlpha());
                updateInternal();
            }
        }));
        values.add(new ValueHandler("B", colour.getBlue(), new ValUpdater()
        {
            @Override
            public void valChange(int newVal)
            {
                colour = new Color(colour.getRed(), colour.getGreen(), newVal, colour.getAlpha());
                updateInternal();
            }
        }));
        values.add(new ValueHandler("A", colour.getAlpha(), new ValUpdater()
        {
            @Override
            public void valChange(int newVal)
            {
                colour = new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), newVal);
                updateInternal();
            }
        }));
        for(ValueHandler val:values)
        {
            colPanel.add(val.getPanel());
        }
        
        preview = new JLabel(genIcon(colour));
        
        
        mainPanel.add(new JLabel(tip), BorderLayout.NORTH);
        mainPanel.add(preview, BorderLayout.EAST);
        mainPanel.add(colPanel, BorderLayout.WEST);
        
        
        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
    }
    private void updateInternal()
    {
        setValue(colour.getRed() + ", " + colour.getGreen() + ", " + colour.getBlue() + ", " + colour.getAlpha());
        preview.setIcon(genIcon(colour));
    }
    @Override
    public void update()
    {
        colour = options.getColor(tag);
        values.get(0).setValue(colour.getRed());
        values.get(1).setValue(colour.getGreen());
        values.get(2).setValue(colour.getBlue());
        values.get(3).setValue(colour.getAlpha());
        preview.setIcon(genIcon(colour));        
    }

    @Override
    public JComponent getComponent()
    {
        
        return mainPanel;
    }
    private static Icon genIcon(Color color)
    {
        int boxWidth = 16;
        int boxHeight = height - 25;
        int squareSize = 8;
        Color neg = new Color(255, 255, 255);//white
        Color pos = new Color(200, 200, 200);//gray
        BufferedImage image = new BufferedImage(boxWidth, boxHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        //generate checkerboard
        int y = 0;
        while(y < boxHeight)
        {
            int x = 0;
            while(x < boxWidth)
            {
                graphics.setColor(((y + x) / squareSize) % 2 == 0? pos:neg);
                graphics.fillRect(x, y, squareSize, squareSize);
                x += squareSize;
            }
            y += squareSize;
        }
        //add main color
        graphics.setColor(color);
        graphics.fillRect(0, 0, boxWidth, boxHeight);
        //graphics.setColor(Color.BLACK);
        //graphics.drawRect(0, 0, boxWidth-1, boxHeight-1);
        
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
    private static Color findTextColor(Color color)
    {
        //Y (color) value
        double y = (299 * color.getRed() + 587 * color.getGreen() + 114 * color.getBlue()) / 1000;
        //compensate for transparency (assume background is white)
        y *= (color.getAlpha() / 255);
        return y >= 128 ? Color.black : Color.white;
    }
    private class ValueHandler
    {
        String name;
        int value;
        ValUpdater updater;
        
        JSlider slider;
        JSpinner spinner;
        JPanel panel;
        JLabel label;
        
        private boolean internalChange = false;
        
        public ValueHandler(String name, int initialValue, ValUpdater updater)
        {
            this.name = name;
            this.value = initialValue;
            this.updater = updater;
            
            panel = new JPanel(new BorderLayout());
            label = new JLabel(name + ":");
            spinner = new JSpinner(new SpinnerNumberModel(value, 0, 255, 1));
            slider = new JSlider(0, 255, value);
            
            panel.add(label, BorderLayout.WEST);
            panel.add(slider, BorderLayout.CENTER);
            panel.add(spinner, BorderLayout.EAST);
            
            spinner.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    if(internalChange)return;
                    internalChange = true;
                    value = (Integer)spinner.getValue();
                    slider.setValue(value);
                    updater.valChange(value);
                    internalChange = false;
                }
            });
            
            slider.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent e)
                {
                    if(internalChange)return;
                    internalChange = true;
                    value = slider.getValue();
                    spinner.setValue(value);
                    updater.valChange(value);
                    internalChange = false;
                }
            });
        }

        public JPanel getPanel()
        {
            return panel;
        }

        public int getValue()
        {
            return value;
        }
        public void setValue(int value)
        {
            this.value = value;
            spinner.setValue(value);
            slider.setValue(value);
        }
    }
    private interface ValUpdater
    {
        public void valChange(int newVal);
    }
    
    
}
