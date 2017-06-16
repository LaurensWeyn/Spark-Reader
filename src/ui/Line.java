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
import main.Main;

import java.awt.*;
import java.util.*;
import java.util.List;

import static ui.UI.*;

/**
 * Holds a line of Japanese text
 * @author Laurens Weyn
 */
public class Line
{
    private SortedSet<Integer> markers;
    private List<FoundWord> words;

    public Line(SortedSet<Integer> markers, ArrayList<FoundWord> words)
    {
        this.markers = markers;
        this.words = words;
    }
    public Line(List<FoundWord> words)
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
    
    List<Integer> wordLocations = null; // must be stored in ascending order
    public FoundWord getWordAt(int x)
    {
        if(wordLocations == null) return null;
        
        for(int i = 1; i < wordLocations.size(); i++)
        {
            if(wordLocations.get(i) > x && i-1 < words.size())
                return words.get(i-1);
        }
        return null;
    }
    public int getCharAt(int x)
    {
        if(wordLocations == null) return 0;
        
        for(int i = 1; i < wordLocations.size(); i++)
        {
            if(wordLocations.get(i) > x && i-1 < words.size())
            {
                int startpoint = wordLocations.get(i-1);
                FoundWord word = words.get(i-1);
                if(word.getLength() == 0) return word.startX();
                for(int j = 0; j < word.getLength(); j++)
                {
                    if(word.getCachedWidth(j)/2+word.getCachedWidth(j+1)/2+startpoint > x) return word.startX() + j; 
                }
                return word.endX();
            }
        }
        return 0;
    }
    public int render(Graphics2D g, int xOff, int yOff)
    {
        //find markers
        Set<Integer> splitPoints = new HashSet<>();
        for(FoundWord word:words)
        {
            splitPoints.add(word.startX());
        }
        g.setClip(0, 0, Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("maxHeight"));//render only over window
        //render markers
        int length = calcLength();
        
        
        /*
        for (int i = 1; i < length; i++) // starting at 1: don't draw markers at the very beginning of the line 
        {
            if(markers.contains(i) || splitPoints.contains(i))//only draw on actual points
            {
                g.setColor(markers.contains(i)? Main.options.getColor("markerCol"): Main.options.getColor("noMarkerCol"));

                g.clearRect(xOff + i * mainFontSize - 1, yOff + textStartY, 2, UI.textHeight);
                g.fillRect (xOff + i * mainFontSize - 1, yOff + textStartY, 2, UI.textHeight);//TODO make markers variable size
            }
        }
        */

        //render words in three stages to make sure any overlapping text overlaps exactly the way it should (i.e. so clearing doesn't cut off text)
        Main.options.getFont("textFont");

        int lastX = 0;
        int characters = 0;
        for(FoundWord word:words)
        {
            // do this first to put it under the text
            int width = word.getAdvancementWidth(g);
            characters += word.getText().length();
            if(markers.contains(characters) || splitPoints.contains(characters))//only draw on actual points
            {
                g.setColor(markers.contains(characters)? Main.options.getColor("markerCol"): Main.options.getColor("noMarkerCol"));

                g.clearRect(xOff + lastX + width, yOff + textStartY, 2, UI.textHeight);
                g.fillRect (xOff + lastX + width, yOff + textStartY, 2, UI.textHeight);//TODO make marker size configurable
            }
            word.renderClear(g, lastX, xOff, yOff);
            lastX = width + lastX + 2;
        }
        lastX = 0;
        for(FoundWord word:words)
        {
            int width = word.getAdvancementWidth(g);
            word.renderBackground(g, lastX, xOff, yOff);
            lastX = width + lastX + 2;
        }
        
        wordLocations = new ArrayList<>();
        wordLocations.add(0);
        lastX = 0;
        for(FoundWord word:words)
        {
            int width = word.getAdvancementWidth(g);
            word.render(g, lastX, xOff, yOff);
            lastX = width + lastX + 2;
            wordLocations.add(lastX);
        }
        return lastX;
    }

    @Override
    public String toString()
    {
        StringBuilder text = new StringBuilder();
        for(FoundWord word:words)
        {
            text.append(word.getText());
        }
        return text.toString();
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

    public List<FoundWord> getWords()
    {
        return words;
    }

    public void addWord(FoundWord word)
    {
        words.add(word);
    }

    public void setWords(List<FoundWord> words)
    {
        this.words = words;
    }

    public void setMarkers(SortedSet<Integer> markers)
    {
        this.markers = markers;
    }
    
    
}
