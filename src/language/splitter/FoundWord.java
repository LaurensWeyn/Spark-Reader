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
package language.splitter;

import language.deconjugator.DeconRule;
import language.deconjugator.ValidWord;
import language.dictionary.Dictionary;
import language.dictionary.Epwing.EPWINGDefinition;
import language.dictionary.Japanese;
import main.Main;
import ui.UI;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import static main.Main.options;

/**
 * Holds a word from the text and definitions that match it
 * @author Laurens Weyn
 */
public class FoundWord
{
    private final String text;//text to display
    private List<FoundDef> definitions;//known meanings
    private int startX;//start point in sentence (for rendering)
    
    private int currentDef = 0;//current definition to render
    
    private boolean showDef = false;
    private boolean mouseover;

    private final boolean hasKanji;
    private boolean hasOpened = false;

    public FoundWord(char text, List<FoundDef> definitions, int startX)
    {
        this(text + "", definitions, startX);
    }
    public FoundWord(String text, List<FoundDef> definitions, int startX)
    {
        this.text = text;
        hasKanji = Japanese.hasKanji(text);
        this.definitions = definitions;
        this.startX = startX;

        if(definitions != null)definitions.sort(null);
    }
    public FoundWord(String text, List<FoundDef> definitions)
    {
        this.text = text;
        hasKanji = Japanese.hasKanji(text);
        this.definitions = definitions;
        this.startX = 0;

        if(definitions != null)definitions.sort(null);
    }
    public FoundWord(char text, int startX)
    {
        this(text + "", startX);
    }
    public FoundWord(String text, int startX)
    {
        this.text = text;
        hasKanji = Japanese.hasKanji(text);
        definitions = null;
        this.startX = startX;
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
        this.startX = 0;
    }
    public void addDefinition(FoundDef def)
    {
        if(definitions == null)definitions = new ArrayList<>();
        definitions.add(def);
    }
    public void sortDefs()
    {
        if(definitions != null)definitions.sort(null);
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
    
    
    ArrayList<Integer> cachedWidths = null;
    int rememberedAdvancementWidth = 0;
    public int getCachedWidth(int numchars)
    {
        if(numchars < cachedWidths.size()) return cachedWidths.get(numchars);
        else return rememberedAdvancementWidth;
    }
    public int getAdvancementWidth(Graphics2D g)
    {
        options.getFont(g, "textFont");
        Rectangle2D rect = g.getFontMetrics().getStringBounds(text, g);
        
        // Java doesn't give a reasonable way to get the character at a given physical distance into a string so this has to be done manually and it's awful
        int newAdvancementWidth = (int)Math.round(rect.getWidth());
        if(newAdvancementWidth != rememberedAdvancementWidth)
        {
            cachedWidths = new ArrayList<>();
            cachedWidths.add(0);
            for(int i = 1; i < text.length(); i++)
                cachedWidths.add((int)Math.round(g.getFontMetrics().getStringBounds(text.substring(0,i), g).getWidth()));
        }
        rememberedAdvancementWidth = newAdvancementWidth;
        return rememberedAdvancementWidth;
    }
    
    public void renderClear(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, options.getOptionInt("windowWidth"), options.getOptionInt("maxHeight"));//render only over window
        options.getFont(g, "textFont");
        
        int bgStart = xStart + xOff;
        int bgEnd = bgStart + getAdvancementWidth(g);
        
        g.clearRect(bgStart, yOff + UI.textStartY, bgEnd-bgStart, g.getFontMetrics().getHeight());//remove background
        
    }
    public void renderBackground(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, options.getOptionInt("windowWidth"), options.getOptionInt("maxHeight"));//render only over window
        int startPos = xStart + xOff;
        int bgEnd = startPos + getAdvancementWidth(g);
        
        boolean known = isKnown();
        if(options.getOptionBool("unparsedWordsAltColor")) known |= (getDefinitionCount()==0);//TODO see if we can move this to isKnown
        
        Color bgColor;
        
        if(showDef)
            bgColor = options.getColor("clickedTextBackCol");
        else if(known)
            bgColor = options.getColor("knownTextBackCol");
        else if(Main.wantToLearn.isWanted(this))
            bgColor = options.getColor("wantTextBackCol");
        else
            bgColor = options.getColor("textBackCol");
        
        float textBackVar = 2.0f;
        try
        {
            textBackVar = Float.valueOf(options.getOption("textBackVariable").trim());
        }
        catch (NumberFormatException e)
        { /* */ }
        
        // for the Shape outline;-based text rendering mode 
        boolean aaEnabled = options.getFontAA("textFont");
        if(aaEnabled)
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        }
        else
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        }
        
        if(options.getOption("textBackMode").equals("outline"))
        {
            Shape outline = g.getFont().createGlyphVector(g.getFontRenderContext(), text).getOutline(startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
            // We need it to be not 100% transparent to allow the word to be clicked.
            Color fakeBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 1);
            g.setColor(fakeBgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());
            
            
            // Render it
            g.setColor(bgColor);
            g.setStroke(new BasicStroke(textBackVar*2.0f, CAP_ROUND, JOIN_ROUND));
            g.draw(outline);
        }
        else if(options.getOption("textBackMode").equals("dropshadow"))
        {
            // We need it to be not 100% transparent to allow the word to be clicked
            Color fakeBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 1);
            g.setColor(fakeBgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());
            
            // Now draw the dropshadow text
            g.setColor(bgColor);
            g.drawString(text, startPos + textBackVar, yOff + UI.textStartY + textBackVar + g.getFontMetrics().getMaxAscent());
        }
        else
        {
            g.setColor(bgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());//set to new color
        }
    }
    
    public void render(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, options.getOptionInt("windowWidth"), options.getOptionInt("maxHeight"));//render only over window
        int startPos = xStart + xOff;
        
        boolean known = isKnown();
        
        int width = getAdvancementWidth(g);
        
        g.setColor((known ? options.getColor("knownTextCol") : options.getColor("textCol")));
        if(!options.getOptionBool("textFontUnhinted"))
            g.drawString(text, startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
        else
        {
            Shape outline = g.getFont().createGlyphVector(g.getFontRenderContext(), text).getOutline(startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
            g.fill(outline);
        }
        
        if(showDef && !hasOpened)
        {
            attachEpwingDefinitions(Main.dict);//load these in only when needed
            sortDefs();
            hasOpened = true;
        }

        //find furigana
        if(definitions == null)return;//don't bother rendering furigana/defs if we don't know it
        String furiText = "";
        if(showDef)
        {
            if(!UI.showMenubar)furiText = (currentDef + 1) + "/" + definitions.size();
        }
        else if(showFurigana(known))
        {
            furiText = definitions.get(currentDef).getFurigana();
            if(!options.getOption("furiMode").equals("original"))
            {
                //convert dictionary form to displayed form
                for(DeconRule rule : definitions.get(currentDef).getFoundForm().getProcess())
                {
                    furiText = rule.conjugate(furiText);
                }
            }
            if(options.getOption("furiMode").equals("stripKana"))
            {
                furiText = Japanese.stripOkurigana(definitions.get(currentDef).getFoundForm().getOriginalWord(), furiText);
            }
        }
        //render furigana

        options.getFont(g, "furiFont");
        g.setColor(options.getColor("furiCol"));
        int furiX = startPos + width/2 - g.getFontMetrics().stringWidth(furiText)/2;
        if(furiX < 0 &&Main.ui.xOffset == 0)furiX = 0;//ensure it's visible if scrolled to front
        g.drawString(furiText, furiX, UI.furiganaStartY + g.getFontMetrics().getAscent() + yOff);
        
        //not effected by Y offset
        if(showDef)
        {
            g.setClip(null);//render this anywhere
            options.getFont(g, "defFont");
            int y = UI.defStartY + g.getFontMetrics().getAscent();
            int defPosition = startPos;
            if(options.getOptionBool("defConstrainPosition"))
                defPosition = Math.max(0, Math.min(startPos, options.getOptionInt("windowWidth")-options.getOptionInt("defWidth")));
            
            definitions.get(currentDef).render(g, defPosition, Math.max(width, options.getOptionInt("defWidth")), y);
        }
        
        return;
    }
    private boolean showFurigana(boolean known)
    {
        if(!hasKanji)return false;//no point
        if(UI.showMenubar)return false;//furigana disabled when menubar visible

        switch(options.getOption(known?"knownFuriMode":"unknownFuriMode"))
        {
            case "always":return true;
            case "mouseover":return mouseover;
            case "never":return false;

            default:return false;
        }
    }
    public void toggleWindow()
    {
        showDef(!showDef);
    }
    public void showDef(boolean mode)
    {
        showDef = mode;
        if(definitions == null)return;//no point if there's no definition
        if(!mode && Main.options.getOptionBool("resetDefScroll"))
        {
            definitions.get(currentDef).resetScroll();
        }
    }
    public boolean inBounds(int xPos)
    {
        return xPos >= startX && xPos < startX + getLength();
    }
    public String getText()
    {
        return text;
    }
    public int getLength()
    {
        return text.length();
    }

    @Override
    public String toString()
    {
        if(definitions == null)return text;
        else return "" + definitions.get(currentDef);
    }
    public boolean isKnown()
    {
        return Main.known.isKnown(this);
    }
    public boolean isShowingDef()
    {
        return showDef;
    }
    public FoundDef getCurrentDef()
    {
        if(definitions == null)return null;
        return definitions.get(currentDef);
    }
    public void scrollDown()
    {
        if(definitions == null)return;
        currentDef = Math.min(currentDef + 1, definitions.size() - 1);
    }
    public void scrollUp()
    {
        if(definitions == null)return;
        currentDef = Math.max(currentDef - 1, 0);
    }
    
    public void resetScroll()
    {
        currentDef = 0;
    }
    
    public List<FoundDef> getFoundDefs()
    {
        return definitions;
    }

    public int startX()
    {
        return startX;
    }
    public int endX()
    {
        return startX + getLength();
    }

    public boolean isShowingFirstDef()
    {
        return currentDef == 0;
    }

    public void setMouseover(boolean mouseover) {
        this.mouseover = mouseover;

        if(!isKnown() && options.getOptionBool("showDefOnMouseover"))
        {
            showDef = mouseover;
        }
    }

    public boolean updateOnMouse()
    {
        if(!hasKanji)return false;

        return ( isKnown() && options.getOption("knownFuriMode").equals("mouseover"))
            || (!isKnown() && options.getOption("unknownFuriMode").equals("mouseover"));
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    private void attachEpwingDefinitions(Dictionary dict)
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
