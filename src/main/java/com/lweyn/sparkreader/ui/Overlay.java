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
package com.lweyn.sparkreader.ui;

import com.lweyn.sparkreader.Main;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Transparent UI overlay window
 * @author Laurens Weyn
 */
public class Overlay
{
    private static Logger logger = Logger.getLogger(Overlay.class);

    private JFrame frame;
    private BufferedImage front;

    private CustomPanel display;

    public Overlay(int width, int height)
    {
        frame = new JFrame("Spark Reader overlay");
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setAlwaysOnTop(true);
        try
        {
            frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("/com/lweyn/sparkreader/ui/icon.gif")));
        } catch (IOException ex)
        {
            logger.error("error loading icon: " + ex);
        }
        // Without this, the window is draggable from any non transparent
        // point, including points  inside textboxes.
        frame.getRootPane().putClientProperty("apple.awt.draggableWindowBackground", false);

        setSize(width, height);//build components with given size

        frame.setFocusableWindowState(Main.options.getOptionBool("takeFocus"));//set the focus mode
        frame.setAutoRequestFocus(false); // prevent auto focus when takeFocus is disabled when using setVisible
        //frame.pack();
        frame.setVisible(true);
        frame.setFocusable(false);
        
        //frame.repaint();
    }
    public void setSize(int width, int height)
    {
        front = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        display = new CustomPanel();
        frame.getContentPane().removeAll();
        frame.getContentPane().add(display);
        frame.setSize(width, height);
    }

    public JFrame getFrame()
    {
        return frame;
    }

    public void refresh()
    {
        frame.repaint();
    }

    public Graphics2D getGraphics()
    {
        Graphics2D f = front.createGraphics();
        f.setBackground(new Color(0,0,0,0));
        f.clearRect(0, 0, front.getWidth(), front.getHeight());
        return front.createGraphics();
    }

    private static double foundScale = 1.0;
    private Overlay superThis = this;

    private class CustomPanel extends JPanel
    {
        @Override
        protected void paintComponent(Graphics g1)
        {
            Graphics2D g = (Graphics2D)g1;
            AffineTransform t = g.getTransform();
            double scaling = t.getScaleX();
            if(scaling != foundScale)
            {
                foundScale = scaling;
                logger.info("Scaling set to " + scaling);
                //compensate for non-1.0 DPI settings
                superThis.setSize((int)(frame.getWidth() * scaling), (int)(frame.getHeight() * scaling));
            }
            t.setToScale(1, 1);
            g.setTransform(t);

            g.drawImage(front, 0, 0, null);
        }
    }

    public static double getFoundScale()
    {
        return foundScale;
    }

    public static int scaleToReal(int val)
    {
        return (int)(val / foundScale);
    }

    public static double getFoundScaleInverse()
    {
        return 1.0 / foundScale;
    }
}
