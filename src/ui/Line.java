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

import language.splitter.FoundWord;

import java.awt.*;
import java.util.*;

import static ui.UI.*;

/**
 *
 * @author Laurens Weyn
 */
public class Line
{
    SortedSet<Integer> markers;
    ArrayList<FoundWord> words;

    public Line(SortedSet<Integer> markers, ArrayList<FoundWord> words)
    {
        this.markers = markers;
        this.words = words;
    }
    public Line(ArrayList<FoundWord> words)
    {
        markers = new TreeSet<>();
        this.words = words;
    }
    public Line()
    {
        markers = new TreeSet<>();
        words = new ArrayList<>();
    }
    public void resetSelection()
    {
        for(FoundWord word:words)
        {
            word.showDef(false);
        }
    }
    public int render(Graphics2D g, int xOff, int yOff)
    {
        //find markers
        Set<Integer> splitPoints = new HashSet<>();
        for(FoundWord word:words)
        {
            splitPoints.add(word.startX());
        }
        g.setClip(0, 0, UI.options.getOptionInt("windowWidth"), UI.options.getOptionInt("maxHeight"));//render only over window
        //render markers
        for (int i = 0; i < text.length(); i++)
        {
            if(markers.contains(i) || splitPoints.contains(i))//only draw on actual points
            {
                g.setColor(markers.contains(i)?options.getColor("markerCol"):options.getColor("noMarkerCol"));

                g.clearRect(xOff + i * mainFontSize - 1, yOff + textStartY, 2, UI.textHeight);
                g.fillRect (xOff + i * mainFontSize - 1, yOff + textStartY, 2, UI.textHeight);//TODO make markers variable size
            }
        }

        //render words
        options.getFont("textFont");

        int lastX = 0;
        for(FoundWord word:words)
        {
            word.render(g, xOff, yOff);
            lastX = word.endX() * mainFontSize + xOff;
        }
        return lastX;
    }

    @Override
    public String toString()
    {
        String text = "";
        for(FoundWord word:words)
        {
            text += word.getText();
        }
        return text;
    }

    public int calcLength()
    {
        int charLength = 0;
        for(FoundWord word:words)
        {
            charLength += word.getLength();
        }
        return charLength;
    }

    public SortedSet<Integer> getMarkers()
    {
        return markers;
    }

    public ArrayList<FoundWord> getWords()
    {
        return words;
    }

    public void addWord(FoundWord word)
    {
        words.add(word);
    }

    public void setWords(ArrayList<FoundWord> words)
    {
        this.words = words;
    }

    public void setMarkers(SortedSet<Integer> markers)
    {
        this.markers = markers;
    }
    
    
}
