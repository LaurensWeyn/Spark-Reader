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
package Options.Option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Laurens Weyn
 */
public class FontOption extends UIOption
{
    Font font;
    Updater updater;
    boolean AA;
    
    JPanel mainPanel = new JPanel();
    JComboBox<String> cmbFontName = new JComboBox<>();
    JLabel example = new JLabel("ひらがな、漢字、カタカナ, Romaji");
    JSpinner size = new JSpinner(new SpinnerNumberModel(0, 0, 500, 1));
    JLabel nameLabel = new JLabel();
    
    JCheckBox chkBold, chkItalic, chkAA;
    public FontOption(String tag, String name, String tip)
    {
        super(tag, name, tip);
        
        updater = new Updater();
        
        String tags = options.getOption(tag).split(",")[1].toUpperCase();

        
        font = options.getFont(tag);
        nameLabel.setText(name);
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder(name));
        //mainPanel.setToolTipText(tip);
        
        JPanel settings = new JPanel(new GridLayout(3, 2));
        chkBold = new JCheckBox(new AbstractAction("Bold")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updater.update();
            }
        });
        chkItalic = new JCheckBox(new AbstractAction("Italic")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updater.update();
            }
        });
        chkAA = new JCheckBox(new AbstractAction("Anti-Aliasing")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updater.update();
            }
        });
        chkAA.setToolTipText("<html>(not shown in preview)<br>Enabling this makes the text smoother on the UI, but also less 'crisp'.<br>I reccomend turning this on with large font sizes.");
        
        for(String font: GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
        {
            cmbFontName.addItem(font);//TODO override the model to make this easier?
        }
        chkAA.setSelected(tags.contains("A"));
        chkBold.setSelected(tags.contains("B"));
        chkItalic.setSelected(tags.contains("I"));
        
        cmbFontName.setEditable(true);
        cmbFontName.setSelectedItem(font.getName());
        cmbFontName.setToolTipText("Not all fonts support Unicode or work with Spark Reader!");
        size.addChangeListener(updater);
        cmbFontName.addActionListener(updater);
        size.setValue(font.getSize());
        size.setMaximumSize(size.getPreferredSize());
        
        //settings.add(new JLabel("Font name/size"));
        settings.add(new JLabel(tip));
        settings.add(chkBold);
        settings.add(cmbFontName);
        settings.add(chkItalic);
        settings.add(size);
        settings.add(chkAA);
        
        //mainPanel.add(new JLabel(tip), BorderLayout.NORTH);
        mainPanel.add(settings, BorderLayout.NORTH);
        mainPanel.add(example , BorderLayout.SOUTH);
        
        
        
        
        example.setFont(font);
        
        
        //mainPanel.setPreferredSize(new Dimension(mainPanel.getWidth(),12));
        
        
    }
    
    
    @Override
    public JComponent getComponent()
    {
        mainPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, (int)mainPanel.getPreferredSize().getHeight()));//resize
        return mainPanel;
    }

    @Override
    public void update()
    {
        String tags = options.getOption(tag).split(",")[1].toUpperCase();
        font = options.getFont(tag);
        example.setFont(font);
        AA = tags.contains("A");
        chkAA.setSelected(AA);
        chkBold.setSelected(tags.contains("B"));
        chkItalic.setSelected(tags.contains("I"));
    }
    private class Updater implements ChangeListener, ActionListener
    {

        @Override
        public void stateChanged(ChangeEvent e)
        {
            update();
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            update();            
        }
        
        public void update()
        {
            System.out.println("update");
            int style = 0;
            if(chkBold.isSelected())  style |= Font.BOLD;
            if(chkItalic.isSelected())style |= Font.ITALIC;
            
            AA = chkAA.isSelected();
            font = new Font((String)cmbFontName.getSelectedItem(), style, (Integer)size.getValue());
            
            example.setFont(font);
            
            String styleTags = "";
            if(AA)styleTags += "A";
            if(chkBold.isSelected())styleTags += "B";
            if(chkItalic.isSelected())styleTags += "B";
            
            setValue(cmbFontName.getSelectedItem() + ", " + styleTags + ", " + size.getValue());
        }
    }
}
