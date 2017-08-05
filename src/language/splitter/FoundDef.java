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

import language.deconjugator.ValidWord;
import language.dictionary.*;
import language.dictionary.JMDict.Sense;
import language.dictionary.JMDict.Spelling;
import main.Main;
import ui.TextBlockRenderer;

import java.awt.*;
import java.util.Set;

import static main.Main.options;

/**
 * Holds and renders a found definition of a FoundWord.
 * @author Laurens Weyn
 */
public class FoundDef implements Comparable<FoundDef>
{
    private final ValidWord foundForm;
    private final Definition foundDef;
    

    private int score = Integer.MIN_VALUE;

    TextBlockRenderer defText;

    public FoundDef(ValidWord foundForm, Definition foundDef)
    {
        this.foundForm = foundForm;
        this.foundDef = foundDef;
    }

    private void generateText(int width)
    {
        defText = new TextBlockRenderer(options.getOptionBool("defsShowUpwards"), width);
        //output source name TODO custom color for this
        defText.addText(foundDef.getSource().getName(), options.getColor("defReadingCol"), options.getColor("defBackCol"));

        //output original form if processed
        if(foundForm.getProcess().size() != 0)
        {
            defText.addText(foundForm.toString(), options.getColor("defReadingCol"), options.getColor("defBackCol"));
        }

        //output tags
        defText.addText(foundDef.getTagLine(), options.getColor("defTagCol"), options.getColor("defBackCol"));
        //output ID (debug)
        if(options.getOptionBool("showDefID"))
            defText.addText(String.valueOf(foundDef.getID()), options.getColor("defTagCol"), options.getColor("defBackCol"));
        //frequency data
        FrequencySink.FreqData freqdata = FrequencySink.get(this);
        if(freqdata != null)
            defText.addText(freqdata.toString(), options.getColor("defTagCol"), options.getColor("defBackCol"));
        
        for(Spelling reading:foundDef.getSpellings(foundForm))
        {
            if(reading.isKanji() && !options.getOptionBool("showAllKanji"))continue; // note: showAllKanji currently broken
            //output readings if not in this form already
            if(!reading.getText().equals(foundForm.getWord()))
            {
                String freqString = "";
                if(options.getOptionBool("showReadingFreqs"))
                {
                    FrequencySink.FreqData specificFreqdata = FrequencySink.get(this, reading.getText());
                    if(specificFreqdata != null)
                        freqString = " " + specificFreqdata.toString();
                }
                defText.addText(reading.getText() + freqString, options.getColor("defReadingCol"), options.getColor("defBackCol"));
            }
        }
        if(!(foundDef instanceof KanjiDefinition))
        {
            for (int i = 0; i < foundForm.getWord().length(); i++)
            {
                //output Kanji if known
                char c = foundForm.getWord().charAt(i);
                String lookup = Kanji.lookup(c);
                if (lookup != null)
                {
                    defText.addText(c + "【" + lookup + "】", options.getColor("defKanjiCol"), options.getColor("defBackCol"));
                }
            }
        }
        for(Sense def:foundDef.getMeanings(foundForm))
        {
            for(String defLine:def.getMeaningLines())
            {
                defText.addText(defLine, options.getColor("defCol"), options.getColor("defBackCol"));
            }
        }
        resetScroll();
    }
    
    public void render(Graphics g, int xPos, int maxWidth, int y)
    {
        if(defText == null)generateText(maxWidth);
        defText.render(g, xPos, y);
    }

    //passthrough methods for TextBlockRenderer
    
    public void setCapturePoint(int pos)
    {
        defText.setCapturePoint(pos);
    }
    public String getCapture()
    {
        return defText.getCapturedText();
    }

    public void scrollDown()
    {
        defText.scrollDown();
    }
    public void scrollUp()
    {
        defText.scrollUp();
    }
    public void resetScroll()
    {
        if(defText == null)return;
        //TODO if upwards, scroll such that the top is visible
        defText.setScrollPosition(0);
    }
    @Override
    public String toString()
    {
        return foundForm + ": " + foundDef;
    }
    public String getFurigana()
    {
        return foundDef.getFurigana(foundForm);
    }
    public String getDictForm()
    {
        return foundForm.getWord();
    }

    public Definition getDefinition()
    {
        return foundDef;
    }

    private int genScore()
    {
        int score = 0;

        score += foundDef.getSource().getPriority() * 100;
        if (Main.prefDef.isPreferred(foundDef)) score += 1000;//HIGHLY favour definitions the user preferred

        Set<DefTag> tags = foundDef.getTags(foundForm);
        if (tags != null)
        {
            if (tags.contains(DefTag.obs) || tags.contains(DefTag.obsc) || tags.contains(DefTag.rare) || tags.contains(DefTag.arch))
                score -= 50;//obscure penalty
            if(tags.contains(DefTag.p) || tags.contains(DefTag.P))
                score += 50;//'common in newspapers etc.' bonus

            if (tags.contains(DefTag.uk) && !Japanese.hasKana(foundForm.getWord())) score -= 10;//usually kana without kana
            if (tags.contains(DefTag.uK) && Japanese.hasOnlyKana(foundForm.getWord())) score -= 10;//usually Kanji, only kana

            if (tags.contains(DefTag.suf) || tags.contains(DefTag.pref))
                score -= 3;//suf/prefixes _usually_ caught with the whole word
            //TODO: only disfavour counters not attached to numbers!
            if (tags.contains(DefTag.ctr)) score -= 10;
        }
        score -= foundDef.getSpellings().length;//-1 for every spelling; more likely it's coincidence
        if (foundForm.getNumConjugations() == 0) score += 5 + 50;//prefer words/phrases instead of deviations
        //'common' words are often marked as such while a conjugated form of a common word would not.
        //To avoid this, the above has an increased score (since word must be common enough to have it conjugated)
        //FIXME if the above causes extra side effects.

        //System.out.println("score for " + foundDef + " is " + score);
        return score;
    }

    /**
     * Get the score of this definition for this word. Higher scored definitions are considered more relevant.
     * @return This definition's score
     */
    public int getScore()
    {
        if(score == Integer.MIN_VALUE)score = genScore();
        return score;
    }

    @Override
    public int compareTo(FoundDef o)
    {
        return o.getScore() - getScore();
    }


    public ValidWord getFoundForm()
    {
        return foundForm;
    }

    public void resetScore()
    {
        score = Integer.MIN_VALUE;
    }
}
