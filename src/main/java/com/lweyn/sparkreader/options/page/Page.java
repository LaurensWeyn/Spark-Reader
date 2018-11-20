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
package com.lweyn.sparkreader.options.page;

import javax.swing.*;

/**
 *
 * @author Laurens Weyn
 */
public interface Page
{
    /**
     * Get the UI component representing this Page
     * @return the UI component
     */
    JComponent getComponent();

    /**
     * Request that this Page and all of its children reload their settings from the Options map.
     */
    void update();
}
