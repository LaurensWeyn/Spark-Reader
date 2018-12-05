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
import com.lweyn.sparkreader.language.splitter.FoundWord;

import java.awt.*;
import java.util.*;
import java.util.List;

import static com.lweyn.sparkreader.ui.UI.*;

/**
 * Holds a line of Japanese text
 * @author Laurens Weyn
 */
public class Line
{
    private SortedSet<Integer> markers;
    private List<FoundWord> foundWords;
    private List<DisplayedWord> displayedWords;

    public Line(SortedSet<Integer> markers, ArrayList<FoundWord> words)
    {
        setWords(words);
        this.markers = markers;
    }
    public Line(List<FoundWord> words)
    {
        markers = new TreeSet<>();
        setWords(words);
    }
    public Line()
    {
        markers = new TreeSet<>();
        displayedWords = new ArrayList<>();
        foundWords = new ArrayList<>();
    }
    public void resetSelection()
    {
        for(DisplayedWord word: displayedWords)
        {
            word.showDef(false);
        }
    }
    //TODO check and document this later
    List<Integer> wordLocations = null; // must be stored in ascending order
    public DisplayedWord getWordAt(int x)
    {
        if(wordLocations == null) return null;
        
        for(int i = 1; i < wordLocations.size(); i++)
        {
            if(wordLocations.get(i) > x && i-1 < displayedWords.size())
                return displayedWords.get(i-1);
        }
        return null;
    }
    public int getCharAt(int x)
    {
        if(wordLocations == null) return 0;
        
        for(int i = 1; i < wordLocations.size(); i++)
        {
            if(wordLocations.get(i) > x && i-1 < displayedWords.size())
            {
                int startpoint = wordLocations.get(i-1);
                DisplayedWord word = displayedWords.get(i-1);
                if(word.getTextLength() == 0) return word.startX();
                for(int j = 0; j < word.getTextLength(); j++)
                {
                    if(word.getCachedWidth(j) / 2 + word.getCachedWidth(j + 1) / 2 + startpoint > x) return word.startX() + j;
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
        for(DisplayedWord word: displayedWords)
        {
            splitPoints.add(word.startX());
        }
        g.setClip(0, 0, Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("maxHeight"));//render only over window

        //render displayedWords in three stages to make sure any overlapping text overlaps exactly the way it should (i.e. so clearing doesn't cut off text)
        Main.options.getFont("textFont");

        int lastX = 0;
        int characters = 0;
        for(DisplayedWord word : displayedWords)
        {
            // render markers first to put them under the text
            int width = word.getAdvancementWidth(g);
            characters += word.getTextLength();
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
        for(DisplayedWord word: displayedWords)
        {
            int width = word.getAdvancementWidth(g);
            word.renderBackground(g, lastX, xOff, yOff);
            lastX = width + lastX + 2;
        }
        
        wordLocations = new ArrayList<>();
        wordLocations.add(0);
        lastX = 0;
        for(DisplayedWord word: displayedWords)
        {
            int width = word.getAdvancementWidth(g);
            word.render(g, lastX, xOff, yOff);
            lastX = width + lastX + 2;
            wordLocations.add(lastX+xOff);
        }
        return lastX;
    }

    @Override
    public String toString()
    {
        StringBuilder text = new StringBuilder();
        for(DisplayedWord word: displayedWords)
        {
            text.append(word.getText());
        }
        return text.toString();
    }

    public int calcCharLength()
    {
        int charLength = 0;
        for(DisplayedWord word: displayedWords)
        {
            charLength += word.getTextLength();
        }
        return charLength;
    }

    public SortedSet<Integer> getMarkers()
    {
        return markers;
    }

    public List<DisplayedWord> getDisplayedWords()
    {
        return displayedWords;
    }

    public void addWord(DisplayedWord word)
    {
        foundWords.add(word.getFoundWord());
        displayedWords.add(word);
    }

    public void setWords(List<FoundWord> words)
    {
        this.foundWords = words;
        this.displayedWords = new ArrayList<>();
        for(FoundWord word:words)
        {
            displayedWords.add(new DisplayedWord(word));
        }
        recalcPositions(displayedWords);
    }

    public static void recalcPositions(List<DisplayedWord> words)
    {
        int x = 0;
        for(DisplayedWord word:words)
        {
            word.setStartX(x);
            x = word.endX();
        }
    }

    public void setMarkers(SortedSet<Integer> markers)
    {
        this.markers = markers;
    }
    
    
}
