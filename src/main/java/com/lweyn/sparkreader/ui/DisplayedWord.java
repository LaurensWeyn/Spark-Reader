package com.lweyn.sparkreader.ui;

import com.lweyn.sparkreader.Main;
import com.lweyn.sparkreader.language.deconjugator.DeconRule;
import com.lweyn.sparkreader.language.dictionary.Japanese;
import com.lweyn.sparkreader.language.splitter.FoundDef;
import com.lweyn.sparkreader.language.splitter.FoundWord;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;

/**
 * Displays a FoundWord from the language engine.
 * @see com.lweyn.sparkreader.language.splitter.FoundWord
 */
public class DisplayedWord
{
    private FoundWord word;

    private int startX;//start point in sentence (for rendering)

    private int currentDef = 0;//current definition to render

    private boolean showDef = false;
    private boolean mouseover;
    
    private boolean hasOpened = false;

    public DisplayedWord(FoundWord word)
    {
        this.word = word;
        startX = 0;
    }

    public DisplayedWord(FoundWord word, int startX)
    {
        this.word = word;
        this.startX = startX;
    }

    public FoundWord getFoundWord()
    {
        return word;
    }

    private ArrayList<Integer> cachedWidths = null;
    private int rememberedAdvancementWidth = 0;
    public int getCachedWidth(int numchars)
    {
        if(numchars < cachedWidths.size()) return cachedWidths.get(numchars);
        else return rememberedAdvancementWidth;
    }

    public int getAdvancementWidth(Graphics2D g)
    {
        Main.options.getFont(g, "textFont");
        Rectangle2D rect = g.getFontMetrics().getStringBounds(word.getText(), g);

        // Java doesn't give a reasonable way to get the character at a given physical distance into a string so this has to be done manually and it's awful
        int newAdvancementWidth = (int)Math.round(rect.getWidth());
        if(newAdvancementWidth != rememberedAdvancementWidth)
        {
            cachedWidths = new ArrayList<>();
            cachedWidths.add(0);
            for(int i = 1; i < word.getText().length(); i++)
                cachedWidths.add((int)Math.round(g.getFontMetrics().getStringBounds(word.getText().substring(0,i), g).getWidth()));
        }
        rememberedAdvancementWidth = newAdvancementWidth;
        return rememberedAdvancementWidth;
    }

    public int getTextLength()
    {
        return word.getTextLength();
    }

    public String getText()
    {
        return word.getText();
    }



    public void renderClear(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("maxHeight"));//render only over window
        Main.options.getFont(g, "textFont");

        int bgStart = xStart + xOff;
        int bgEnd = bgStart + getAdvancementWidth(g);

        g.clearRect(bgStart, yOff + UI.textStartY, bgEnd-bgStart, g.getFontMetrics().getHeight());//remove background

    }
    public void renderBackground(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("maxHeight"));//render only over window
        int startPos = xStart + xOff;
        int bgEnd = startPos + getAdvancementWidth(g);

        boolean known = word.isKnown();
        if(Main.options.getOptionBool("unparsedWordsAltColor")) known |= (word.getDefinitionCount()==0);//TODO see if we can move this to isKnown

        Color bgColor;

        if(showDef)
            bgColor = Main.options.getColor("clickedTextBackCol");
        else if(known)
            bgColor = Main.options.getColor("knownTextBackCol");
        else if(Main.wantToLearn.isWanted(word))
            bgColor = Main.options.getColor("wantTextBackCol");
        else
            bgColor = Main.options.getColor("textBackCol");

        float textBackVar = 2.0f;
        try
        {
            textBackVar = Float.valueOf(Main.options.getOption("textBackVariable").trim());
        }
        catch (NumberFormatException e)
        { /* */ }

        // for the Shape outline;-based text rendering mode 
        boolean aaEnabled = Main.options.getFontAA("textFont");
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

        if(Main.options.getOption("textBackMode").equals("outline"))
        {
            Shape outline = g.getFont().createGlyphVector(g.getFontRenderContext(), word.getText()).getOutline(startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
            // We need it to be not 100% transparent to allow the word to be clicked.
            Color fakeBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 1);
            g.setColor(fakeBgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());


            // Render it
            g.setColor(bgColor);
            g.setStroke(new BasicStroke(textBackVar*2.0f, CAP_ROUND, JOIN_ROUND));
            g.draw(outline);
        }
        else if(Main.options.getOption("textBackMode").equals("dropshadow"))
        {
            // We need it to be not 100% transparent to allow the word to be clicked
            Color fakeBgColor = new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), 1);
            g.setColor(fakeBgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());

            // Now draw the dropshadow text
            g.setColor(bgColor);
            g.drawString(word.getText(), startPos + textBackVar, yOff + UI.textStartY + textBackVar + g.getFontMetrics().getMaxAscent());
        }
        else
        {
            g.setColor(bgColor);
            g.fillRect(startPos, yOff + UI.textStartY, bgEnd-startPos, g.getFontMetrics().getHeight());//set to new color
        }
    }

    public void render(Graphics2D g, int xStart, int xOff, int yOff)
    {
        g.setClip(0, 0, Main.options.getOptionInt("windowWidth"), Main.options.getOptionInt("maxHeight"));//render only over window
        int startPos = xStart + xOff;

        boolean known = word.isKnown();

        int width = getAdvancementWidth(g);

        g.setColor((known ? Main.options.getColor("knownTextCol") : Main.options.getColor("textCol")));
        if(!Main.options.getOptionBool("textFontUnhinted"))
            g.drawString(word.getText(), startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
        else
        {
            Shape outline = g.getFont().createGlyphVector(g.getFontRenderContext(), word.getText()).getOutline(startPos, yOff + UI.textStartY + g.getFontMetrics().getMaxAscent());
            g.fill(outline);
        }

        if(showDef && !hasOpened)
        {
            word.attachEpwingDefinitions(Main.dict);//load these in only when needed
            word.sortDefs();
            hasOpened = true;
        }

        //find furigana
        if(!word.hasDefinitions())return;//don't bother rendering furigana/defs if we don't know it
        String furiText = "";
        if(showDef)
        {
            if(!UI.showMenubar)furiText = (currentDef + 1) + "/" + word.getDefinitionCount();
        }
        else if(showFurigana(known))
        {
            furiText = word.getFoundDef(currentDef).getFurigana();
            if(!Main.options.getOption("furiMode").equals("original"))
            {
                //convert dictionary form to displayed form
                for(DeconRule rule : word.getFoundDef(currentDef).getFoundForm().getProcess())
                {
                    furiText = rule.conjugate(furiText);
                }
            }
            if(Main.options.getOption("furiMode").equals("stripKana"))
            {
                furiText = Japanese.stripOkurigana(word.getFoundDef(currentDef).getFoundForm().getOriginalWord(), furiText);
            }
        }
        //render furigana

        Main.options.getFont(g, "furiFont");
        g.setColor(Main.options.getColor("furiCol"));
        int furiX = startPos + width/2 - g.getFontMetrics().stringWidth(furiText)/2;
        if(furiX < 0 &&Main.ui.xOffset == 0)furiX = 0;//ensure it's visible if scrolled to front
        g.drawString(furiText, furiX, UI.furiganaStartY + g.getFontMetrics().getAscent() + yOff);

        //not effected by Y offset
        if(showDef)
        {
            g.setClip(null);//render this anywhere
            Main.options.getFont(g, "defFont");
            int y = UI.defStartY + g.getFontMetrics().getAscent();
            int defPosition = startPos;
            if(Main.options.getOptionBool("defConstrainPosition"))
                defPosition = Math.max(0, Math.min(startPos, Main.options.getOptionInt("windowWidth")- Main.options.getOptionInt("defWidth")));

            word.getFoundDef(currentDef).render(g, defPosition, Math.max(width, Main.options.getOptionInt("defWidth")), y);
        }

        return;
    }

    private boolean showFurigana(boolean known)
    {
        if(!word.hasKanji())return false;//no point
        if(UI.showMenubar)return false;//furigana disabled when menubar visible

        switch(Main.options.getOption(known?"knownFuriMode":"unknownFuriMode"))
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
        if(!word.hasDefinitions())
            return;//no point if there's no definition
        if(!mode && Main.options.getOptionBool("resetDefScroll"))
        {
            word.getFoundDef(currentDef).resetScroll();
        }
    }

    public boolean inBounds(int xPos)
    {
        return xPos >= startX && xPos < startX + word.getTextLength();
    }


    public FoundDef getCurrentDef()
    {
        if(!word.hasDefinitions())
            return null;
        return word.getFoundDef(currentDef);
    }
    public void scrollDown()
    {
        if(!word.hasDefinitions())
            return;
        currentDef = Math.min(currentDef + 1, word.getDefinitionCount() - 1);
    }
    public void scrollUp()
    {
        if(!word.hasDefinitions())
            return;
        currentDef = Math.max(currentDef - 1, 0);
    }

    public void resetScroll()
    {
        currentDef = 0;
    }

    public int startX()
    {
        return startX;
    }
    public int endX()
    {
        return startX + word.getTextLength();
    }

    public boolean isShowingFirstDef()
    {
        return currentDef == 0;
    }

    public void setMouseover(boolean mouseover) {
        this.mouseover = mouseover;

        if(!word.isKnown() && Main.options.getOptionBool("showDefOnMouseover"))
        {
            showDef = mouseover;
        }
    }

    public boolean isShowingDef()
    {
        return showDef;
    }



    public boolean updateOnMouse()
    {
        if(!word.hasKanji())return false;

        return ( word.isKnown() && Main.options.getOption("knownFuriMode").equals("mouseover"))
                || (!word.isKnown() && Main.options.getOption("unknownFuriMode").equals("mouseover"));
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    @Override
    public String toString()
    {
        if(!word.hasDefinitions() || word.getDefinitionCount() == 0)return word.getText();
        else return word.getText() + "[" + getCurrentDef() + "]";
    }
}
