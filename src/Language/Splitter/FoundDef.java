/* 
 * Copyright (C) 2016 Laurens Weyn
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
package Language.Splitter;

import Language.Dictionary.DefTag;
import Language.Dictionary.Definition;
import Language.Deconjugator.ValidWord;
import Language.Dictionary.Japanese;
import Language.Dictionary.Kanji;
import UI.TextStream;
import UI.UI;
import static UI.UI.options;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Set;

/**
 * Holds and renders a found definition of a FoundWord
 * @author laure
 */
public class FoundDef implements Comparable<FoundDef>
{
    private final ValidWord foundForm;
    private final Definition foundDef;
    
    private int defLines = 0;
    private int startLine = 0;
    
    
    public FoundDef(ValidWord foundForm, Definition foundDef)
    {
        this.foundForm = foundForm;
        this.foundDef = foundDef;
    }
    
    
    public void render(Graphics g, int xPos, int maxWidth, int y)
    {
        g.setColor(new Color(0,0,0,1));
        g.fillRect(xPos, y, maxWidth, 1);//let mouse move thorugh 1 pixel space
        y++;//slight spacer
        
        defLines = 0;//will be recounted
        
        //output original form if processed
        if(!foundForm.getProcess().equals(""))y = renderText(g, options.getColor("defReadingCol"), options.getColor("defBackCol"), xPos, y, foundForm.toString(), maxWidth);
        
        //output tags
        String tagList = "";
        for(DefTag tag:foundDef.getTags())
        {
            tagList += tag.name() + " ";
        }
        y = renderText(g, options.getColor("defTagCol"), options.getColor("defBackCol"), xPos, y, tagList.trim(), maxWidth);
        
        String[] readings = foundDef.getReadings();
        if(options.getOptionBool("showAllKanji"))readings = foundDef.getSpellings();
        for(String reading:readings)
        {
            //output readings if not in this form already
            if(!reading.equals(foundForm.getWord()))y = renderText(g, options.getColor("defReadingCol"), options.getColor("defBackCol"), xPos, y, reading, maxWidth);
        }
        for (int i = 0; i < foundForm.getWord().length(); i++)
        {
            //output Kanji if known
            char c = foundForm.getWord().charAt(i);
            String lookup = Kanji.lookup(c);
            if(lookup != null)
            {
                y = renderText(g, options.getColor("defKanjiCol"), options.getColor("defBackCol"), xPos, y, c + "【" + lookup + "】", maxWidth);
            }
        }
        for(String def:foundDef.getMeaning())
        {
            //output non-empty definitions
            if(!def.equals("") && !def.equals("(P)"))y = renderText(g, options.getColor("defCol"), options.getColor("defBackCol"), xPos, y, def, maxWidth);
        }
        
        capturePoint = 0;//disable for next iteration
    }
    
    private int capturePoint = 0;
    private String capture = "";
    /**
     * 0=disable, -1=all, y=line
     * @param pos 
     */
    public void setCapturePoint(int pos)
    {
        capturePoint = pos;
        capture = "";
    }
    public String getCapture()
    {
        return capture;
    }
    
    private int renderText(Graphics g, Color fore, Color back, int x, int y, String text, int width)
    {
        defLines++;
        if(startLine > defLines)return y;//don't render here yet
        int startY = y;
        FontMetrics font = g.getFontMetrics();
        TextStream stream = new TextStream(text);
        String line = "";
        while(!stream.isDone())
        {
            String nextBit = stream.nextWord();
            if(font.stringWidth(line + nextBit) > width)//too much for this line, wrap over
            {
                g.setColor(back);
                g.fillRect(x, y - font.getAscent(), width, font.getHeight());
                g.setColor(fore);
                g.drawString(line, x, y);
                y += font.getHeight();
                line = nextBit.trim();//put word on new line
            }
            else
            {
                line += nextBit;
            }
        }
        //draw last line
        g.setColor(back);
        g.fillRect(x, y - font.getAscent(), width, font.getHeight() - 1);//leave out 1 pixel seperator
        g.setColor(new Color(0,0,0,1));
        g.fillRect(x, y - font.getAscent() + font.getHeight() - 1, width, 1);//add a dim line here so scrolling still works
        g.setColor(fore);
        g.drawString(line, x, y);
        y += font.getHeight();
        
        //capture if in this
        if(capturePoint == -1 || (capturePoint > startY - font.getHeight() + font.getDescent()
            && capturePoint <= y - font.getHeight() + font.getDescent()))
        {
            //TODO allow export with HTML color info perhaps?
            if(capture.equals(""))
            {
                capture = text;
            }
            else
            {
                capture += "\n" + text;
            }
        }
        
        return y;
    }
    public void scrollDown()
    {
        startLine = Math.min(startLine + 1, defLines - 1);
    }
    public void scrollUp()
    {
        startLine = Math.max(startLine - 1, 0);
    }
    @Override
    public String toString()
    {
        return foundForm + ": " + foundDef;
    }
    public String getFurigana()
    {
        return foundDef.getFurigana();
    }
    public String getDictForm()
    {
        return foundForm.getWord();
    }

    public Definition getDefinition()
    {
        return foundDef;
    }
    
    public int getScore()
    {
        int score = 0;
        
        if(foundDef.getSourceNum() == 1)score += 100;//prefer user added defs
        if(UI.prefDef.isPreferred(foundDef))score += 1000;//HIGHLY favour definitions the user preferred
        
        Set<DefTag> tags = foundDef.getTags();
        if(tags.contains(DefTag.obs) || tags.contains(DefTag.obsc) || tags.contains(DefTag.rare) ||tags.contains(DefTag.arch))score -= 50;//obscure penalty
        
        if(tags.contains(DefTag.uk) && !Japanese.isKana(foundForm.getWord()))score -= 10;//usually kana without kana
        if(tags.contains(DefTag.uK) && Japanese.isKana(foundForm.getWord()))score -= 10;//usually Kanji, only kana
        
        if(foundForm.getProcess().equals(""))score += 5;//prefer words/phrases instead of deviations
        if(tags.contains(DefTag.suf) || tags.contains(DefTag.pref))score -= 3;//suf/prefixes _usually_ caught with the whole word
        score -= foundDef.getSpellings().length;//-1 for every spelling; more likely it's coincidence
        
        //TODO: only disfavour counters not attached to numbers!
        if(tags.contains(DefTag.ctr))score -= 10;
        //TODO: join numbers and counter words!
        //System.out.println("score for " + foundDef + " is " + score);
        return score;
    }

    @Override
    public int compareTo(FoundDef o)
    {
        return o.getScore() - getScore();
    }
    
    
}
