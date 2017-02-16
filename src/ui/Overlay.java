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
package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author laure
 */
public class Overlay
{
    private JFrame frame;
    private BufferedImage front;
    private BufferedImage back;
    private ImageIcon display;
    public Overlay(int width, int height)
    {
        frame = new JFrame("Spark Reader overlay");
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setAlwaysOnTop(true);
        try
        {
            frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("/ui/icon.gif")));
        } catch (IOException ex)
        {
            System.out.println("error loading icon: " + ex);
        }
        // Without this, the window is draggable from any non transparent
        // point, including points  inside textboxes.
        frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);

        front = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        //back = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        display = new ImageIcon(front);
        frame.getContentPane().add(new JLabel(display));
        
        //frame.getContentPane().add(new JTextField("text field south"), java.awt.BorderLayout.SOUTH);
        frame.setSize(width, height);
        frame.setFocusableWindowState(UI.options.getOptionBool("takeFocus"));//set the focus mode
        //frame.pack();
        frame.setVisible(true);
        frame.setFocusable(false);
        
        //frame.repaint();
    }
    public JFrame getFrame()
    {
        return frame;
    }
    public void refresh()
    {
        //swap buffers
        //*
        //BufferedImage temp = front;
        //front = back;
        //back = temp;
        //display front buffer
        //display.setImage(front);
        //clear back buffer
        //Graphics2D f = back.createGraphics();
        //f.setBackground(new Color(0,0,0,0));
        //f.clearRect(0, 0, back.getWidth(), back.getHeight());
        //*/
        //update
        frame.repaint();
        
         
    }
    public Graphics2D getGraphics()
    {
        Graphics2D f = front.createGraphics();
        f.setBackground(new Color(0,0,0,0));
        f.clearRect(0, 0, front.getWidth(), front.getHeight());
        return front.createGraphics();
    }
}
