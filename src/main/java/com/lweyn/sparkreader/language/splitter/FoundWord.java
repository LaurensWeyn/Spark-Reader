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
package com.lweyn.sparkreader.language.splitter;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.language.deconjugator.DeconRule;
import com.lweyn.sparkreader.language.deconjugator.ValidWord;
import com.lweyn.sparkreader.language.dictionary.Dictionary;
import com.lweyn.sparkreader.language.dictionary.Epwing.EPWINGDefinition;
import com.lweyn.sparkreader.language.dictionary.Japanese;
import com.lweyn.sparkreader.ui.UI;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;

/**
 * Holds a word from the text and definitions that match it
 * @author Laurens Weyn
 */
public class FoundWord
{
    private final String text;//text to display
    private List<FoundDef> definitions;//known meanings
    private final boolean hasKanji;

    public FoundWord(String text, List<FoundDef> definitions)
    {
        this.text = text;
        hasKanji = Japanese.hasKanji(text);
        this.definitions = definitions;

        if(definitions != null)definitions.sort(null);
    }

    public FoundWord(char text)
    {
        this(text + "");
    }

    public FoundWord(String text)
    {
        this.text = text;
        hasKanji = Japanese.hasKanji(text);
        definitions = null;
    }

    public void addDefinition(FoundDef def)
    {
        if(definitions == null)
            definitions = new ArrayList<>();
        definitions.add(def);
    }

    public void sortDefs()
    {
        if(definitions != null)
            definitions.sort(null);
    }

    public void resortDefs()
    {
        if(definitions != null)
        {
            for(FoundDef def:definitions) def.resetScore();
            sortDefs();
        }
    }

    public int getDefinitionCount()
    {
        if(definitions == null)return 0;
        return definitions.size();
    }

    public boolean hasKanji()
    {
        return hasKanji;
    }



    public String getText()
    {
        return text;
    }

    public int getTextLength()
    {
        return text.length();
    }

    @Override
    public String toString()
    {
        if(definitions == null || definitions.size() == 0)return text;
        else return "" + definitions.get(0);
    }

    public boolean isKnown()
    {
        return Main.known.isKnown(this);
    }


    
    public List<FoundDef> getFoundDefs()
    {
        return definitions;
    }


    public boolean hasDefinitions()
    {
        return definitions != null;
    }

    public FoundDef getFoundDef(int index)
    {
        return definitions.get(index);
    }


    public void attachEpwingDefinitions(Dictionary dict)
    {
        Set<String> alreadyQueried = new HashSet<>();
        //for each known valid reading, find a dictionary equivalent
        if(definitions != null)for(int i = 0; i < definitions.size(); i++)
        {
            FoundDef foundDef = definitions.get(i);
            String query = foundDef.getDictForm();
            if(alreadyQueried.contains(query))continue;
            alreadyQueried.add(query);
            List<EPWINGDefinition> extraDefs = dict.findEpwing(foundDef.getDictForm());
            for(EPWINGDefinition extraDef:extraDefs)
            {
                extraDef.setTags(foundDef.getDefinition().getTags(foundDef.getFoundForm()));
                addDefinition(new FoundDef(foundDef.getFoundForm(), extraDef));
            }
        }

        //find plain form word as well
        if(!alreadyQueried.contains(text))
        {
            List<EPWINGDefinition> extraDefs = dict.findEpwing(text);
            for(EPWINGDefinition extraDef:extraDefs)
            {
                addDefinition(new FoundDef(new ValidWord(text), extraDef));
            }
        }
        //TODO search for other forms (kana etc.)
    }
}
