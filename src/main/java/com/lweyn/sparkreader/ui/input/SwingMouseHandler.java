package com.lweyn.sparkreader.ui.input;

import com.lweyn.sparkreader.ui.UI;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.event.*;

import static com.lweyn.sparkreader.ui.UI.*;

/**
 * Abstracts implementation details of recognising various 'gestures' from the Swing MouseListeners and forwards it to the MouseHandler it extends.
 */
public class SwingMouseHandler extends MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener
{
    private static Logger logger = Logger.getLogger(SwingMouseHandler.class);

    private boolean lMouseClick = false;
    private boolean movingWindow = false;
    private Point dragReference;
    private long lastClickTime = 0;

    private static final long MAX_CLICK_DELAY = 100;
    private static final long MIN_DRAG_DIST = 100;

    public SwingMouseHandler(UI ui)
    {
        super(ui);
    }

    @Override
    public void addListeners()
    {
        ui.disp.getFrame().addMouseListener(this);
        ui.disp.getFrame().addMouseMotionListener(this);
        ui.disp.getFrame().addMouseWheelListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        long clickTime = System.currentTimeMillis();
        if(clickTime - lastClickTime < MAX_CLICK_DELAY)
        {
            logger.info("Stopped double click event");
            return;//[attempt to]stop accidental double click
        }
        lastClickTime = clickTime;

        if(e.getButton() == 1) movingWindow = false;

        if(e.getButton() == 1 && lMouseClick)//if left click (and wasn't drag)
        {
            leftClick(e.getPoint());
            lMouseClick = false;
        }
        else if(e.getButton() == 2)//middle click: place marker
        {
            middleClick(e.getPoint());
        }
        else if(e.getButton() == 3)//right click: extra option menu
        {
            rightClick(e.getPoint());
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        dragReference = e.getPoint();
        if(e.getButton() == 1)
        {
            lMouseClick = true;
            if(e.getY() >= furiganaStartY && e.getY() <= textStartY)//only furigana bar draggable
            {
                movingWindow = true;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

        double dist = dragReference.distanceSq(e.getPoint());
        if((dist != 0 || movingWindow) && dist < MIN_DRAG_DIST)//only moved a little
        {
            if(e.getButton() == 1)
                lMouseClick = true;
            movingWindow = false;
            mouseClicked(e);//pass this over as a click
        }
        else if (!movingWindow)//long drag, not on Furigana bar
        {
            dragComplete(dragReference, e.getPoint());
        }
        movingWindow = false;
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        //detected and handled by 'mouseMoved'
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        mouseExit();
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        if(movingWindow)
        {
            Point moveTo = e.getLocationOnScreen();
            moveTo.translate(-dragReference.x, -dragReference.y);
            ui.disp.getFrame().setLocation(moveTo);
            lMouseClick = false;//no longer a click
        }
        else if(dragReference.distanceSq(e.getPoint()) >= MIN_DRAG_DIST)
        {
            mouseDrag(dragReference, e.getPoint());
        }
    }


    @Override
    public void mouseMoved(MouseEvent e)
    {
        mouseMove(e.getPoint());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        if(hidden)
            return;
        mouseScroll(e.getWheelRotation(), e.getPoint());
    }
}
