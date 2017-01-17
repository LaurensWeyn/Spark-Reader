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

import Options.Options;
import Options.Page.Page;
import java.util.HashMap;

/**
 *
 * @author Laurens Weyn
 */
public abstract class UIOption implements Page
{
    protected static Options options;
    protected String tag, name, tip;

    public UIOption(String tag, String name, String tip)
    {
        this.tag = tag;
        this.name = name;
        this.tip = tip;
    }

    
    
    public static void setTable(Options table)
    {
        UIOption.options = table;
    }
    protected String getValue()
    {
        return options.getOption(tag);
    }
    protected void setValue(String value)
    {
        options.setOption(tag, value);
    }

    public static Options getOptions()
    {
        return options;
    }
    
}
