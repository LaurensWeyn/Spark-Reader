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
package Language.Dictionary;

import java.util.Set;

/**
 *
 * @author Laurens Weyn
 */
public abstract class Definition
{
    public abstract String getFurigana();
    public abstract int getID();

    public abstract int getSourceNum();
    public abstract String[] getSpellings();
    public abstract String[] getMeaning();
    
    public abstract String getMeaningLine();
    
    public Set<DefTag> getTags()
    {
        return null;
    }

    public String getTagLine()
    {
        Set<DefTag> tags = getTags();
        if(tags == null)return null;

        String tagList = "";
        for(DefTag tag:tags)
        {
            tagList += tag.name() + " ";
        }
        return tagList.trim();
    }
}
