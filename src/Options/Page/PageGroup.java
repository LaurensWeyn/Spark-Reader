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
package Options.Page;

import java.awt.Component;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Laurens Weyn
 */
public class PageGroup implements Page
{
    private String name, info;
    ArrayList<Page> pages;
    public PageGroup(String name, String info)
    {
        this.name = name;
        this.info = info;
        pages = new ArrayList<>();
    }
    public void add(Page page)
    {
        pages.add(page);
    }
    public ArrayList<Page> getPages()
    {
        return pages;
    }
    
    @Override
    public JComponent getComponent()
    {
        JPanel frame = new JPanel();
        frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));
        for(String line:info.split("\n"))
        {
            frame.add(new JLabel(line));
        }
        //frame.add(new JLabel("(double-click folder to see options)"));
        return frame;
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public void update()
    {
        for(Page page:pages)
        {
            page.update();
        }
    }
    
    
}
