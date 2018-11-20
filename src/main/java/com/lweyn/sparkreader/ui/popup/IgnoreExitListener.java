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
package com.lweyn.sparkreader.ui.popup;

import com.lweyn.sparkreader.ui.UI;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 *  Used by popup windows to stop focus loss when the mouse is moved over to them.
 *  Sends status to UI.tempIgnoreMouseExit
 * @author Laurens Weyn
 */
public class IgnoreExitListener implements PopupMenuListener
{
    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e)
    {
        UI.tempIgnoreMouseExit = true;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
    {
        UI.tempIgnoreMouseExit = false;                
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e)
    {
        UI.tempIgnoreMouseExit = false;
    }
}
