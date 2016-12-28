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
package Language.Deconjugator;

import Language.Dictionary.DefTag;
import Language.Dictionary.Japanese;
import java.util.ArrayList;

/**
 * Produces all possible deconjugations of a word for lookup in dictionaries
 * @author Laurens Weyn
 */
public class WordScanner
{
    private ArrayList<ValidWord> matches;
    private String word;
    
    public WordScanner(String word)
    {
        
        //see http://www.wikiwand.com/en/Japanese_verb_conjugation
        matches = new ArrayList<>();
        this.word = word;
        
        matches.add(new ValidWord(word, ""));//dictionary form
        String hiragana = Japanese.toHiragana(word, false);
        if(word.equals(hiragana) == false)matches.add(new ValidWord(hiragana, "hiragana"));
        //past->dict
        //*
        test(new DeconRule("った", "う", "past", DefTag.v5u));
        test(new DeconRule("いた", "く", "past", DefTag.v5k));
        test(new DeconRule("いだ", "ぐ", "past", DefTag.v5g));
        test(new DeconRule("した", "す", "past", DefTag.v5s));
        test(new DeconRule("った", "つ", "past", DefTag.v5t));
        test(new DeconRule("んだ", "ぶ", "past", DefTag.v5b));
        test(new DeconRule("んだ", "ぬ", "past", DefTag.v5n));
        test(new DeconRule("んだ", "む", "past", DefTag.v5m));
        test(new DeconRule("った", "る", "past", DefTag.v5r));
        
        test(new DeconRule("かった", "い", "past", DefTag.adj_i));
        test(new DeconRule("た", "る", "past", DefTag.v1));
                
        //te->dict
        test(new DeconRule("って", "う", "て form", DefTag.v5u));
        test(new DeconRule("いて", "く", "て form", DefTag.v5k));
        test(new DeconRule("いで", "ぐ", "て form", DefTag.v5g));
        test(new DeconRule("して", "す", "て form", DefTag.v5s));
        test(new DeconRule("って", "つ", "て form", DefTag.v5t));
        test(new DeconRule("んで", "ぶ", "て form", DefTag.v5b));
        test(new DeconRule("んで", "ぬ", "て form", DefTag.v5n));
        test(new DeconRule("んで", "む", "て form", DefTag.v5m));
        test(new DeconRule("って", "る", "て form", DefTag.v5r));
        
        test(new DeconRule("くて", "い", "て form", DefTag.adj_i));
        test(new DeconRule("て", "る", "て form", DefTag.v1));
        
        //neg->dict (te form must be done first)
        test(new DeconRule("わない", "う", "negative", DefTag.v5u));
        test(new DeconRule("かない", "く", "negative", DefTag.v5k));
        test(new DeconRule("がない", "ぐ", "negative", DefTag.v5g));
        test(new DeconRule("さない", "す", "negative", DefTag.v5s));
        test(new DeconRule("たない", "つ", "negative", DefTag.v5t));
        test(new DeconRule("ばない", "ぶ", "negative", DefTag.v5b));
        test(new DeconRule("なない", "ぬ", "negative", DefTag.v5n));
        test(new DeconRule("まない", "む", "negative", DefTag.v5m));
        test(new DeconRule("らない", "る", "negative", DefTag.v5r));
        
        test(new DeconRule("くない", "い", "negative", DefTag.adj_i));
        test(new DeconRule("ない", "る", "negative", DefTag.v1));
        
        //masu/tai/etc removal (handled after past so that still conjugates, before i stem)
        //TODO make these only work with verbs (not with gatai!)
        test(new DeconRule("ます", "", "polite"));
        test(new DeconRule("たい", "", "want"));
        test(new DeconRule("なさい", "", "command"));
        
        //i stem (polite/tai/etc)
        test(new DeconRule("い", "う", "i stem", DefTag.v5u));
        test(new DeconRule("き", "く", "i stem", DefTag.v5k));
        test(new DeconRule("ぎ", "ぐ", "i stem", DefTag.v5g));
        test(new DeconRule("し", "す", "I stem", DefTag.v5s));//note: capital I to stop removal of v5s tag
        test(new DeconRule("ち", "つ", "i stem", DefTag.v5t));
        test(new DeconRule("に", "ぬ", "i stem", DefTag.v5n));
        test(new DeconRule("び", "ぶ", "i stem", DefTag.v5b));
        test(new DeconRule("み", "む", "i stem", DefTag.v5m));
        test(new DeconRule("り", "る", "i stem", DefTag.v5r));
        
        //no stem for adjectives, but -sou sort-of uses a stem
        test(new DeconRule("そう", "い", "-sou", DefTag.adj_i));
        test(new DeconRule("", "る", "i stem", DefTag.v1));
        
        //potential (can do verb)
        test(new DeconRule("える", "う", "potential", DefTag.v5u));
        test(new DeconRule("ける", "く", "potential", DefTag.v5k));
        test(new DeconRule("げる", "ぐ", "potential", DefTag.v5g));
        test(new DeconRule("せる", "す", "potential", DefTag.v5s));
        test(new DeconRule("てる", "つ", "potential", DefTag.v5t));
        test(new DeconRule("べる", "ぶ", "potential", DefTag.v5b));
        test(new DeconRule("ねる", "ぬ", "potential", DefTag.v5n));
        test(new DeconRule("める", "む", "potential", DefTag.v5m));
        test(new DeconRule("れる", "る", "potential", DefTag.v5r));
        
        //not for adjectives
        test(new DeconRule("られる", "る", "potential", DefTag.v1));//normal
        test(new DeconRule("らる", "る", "potential", DefTag.v1));//coloquial
        
        //passive
        test(new DeconRule("われる", "う", "passive", DefTag.v5u));
        test(new DeconRule("かれる", "く", "passive", DefTag.v5k));
        test(new DeconRule("がれる", "ぐ", "passive", DefTag.v5g));
        test(new DeconRule("させる", "す", "passive", DefTag.v5s));
        test(new DeconRule("たてる", "つ", "passive", DefTag.v5t));
        test(new DeconRule("ばれる", "ぶ", "passive", DefTag.v5b));
        test(new DeconRule("なれる", "ぬ", "passive", DefTag.v5n));
        test(new DeconRule("まれる", "む", "passive", DefTag.v5m));
        test(new DeconRule("られる", "る", "passive", DefTag.v5r));
        
        //not for adjectives
        test(new DeconRule("られる", "る", "passive", DefTag.v1));
        
        
        //causative
        
        //causative passive (colloquial version, add polite later?)
        
        
        //-eba form (provisional conditional)
        test(new DeconRule("えば", "う", "provisional-conditional", DefTag.v5u));
        test(new DeconRule("けば", "く", "provisional-conditional", DefTag.v5k));
        test(new DeconRule("げば", "ぐ", "provisional-conditional", DefTag.v5g));
        test(new DeconRule("せば", "す", "provisional-conditional", DefTag.v5s));
        test(new DeconRule("てば", "つ", "provisional-conditional", DefTag.v5t));
        test(new DeconRule("べば", "ぶ", "provisional-conditional", DefTag.v5b));
        test(new DeconRule("ねば", "ぬ", "provisional-conditional", DefTag.v5n));
        test(new DeconRule("めば", "む", "provisional-conditional", DefTag.v5m));
        test(new DeconRule("れば", "る", "provisional-conditional", DefTag.v5r));
        test(new DeconRule("れば", "る", "provisional-conditional", DefTag.v5r_i));
        
        test(new DeconRule("くない", "い", "provisional-conditional", DefTag.adj_i));
        test(new DeconRule("なければ", "ない", "provisional-conditional", DefTag.aux_adj));
        test(new DeconRule("れば", "る", "provisional-conditional", DefTag.v1));
        
        //conditional/past conditional (ra) (+ba for formal) (adds on to past)
        test(new DeconRule("ったら", "った", "conditional", DefTag.v5u));
        test(new DeconRule("ったらば", "った", "conditional-formal", DefTag.v5u));
        test(new DeconRule("いたら", "いた", "conditional", DefTag.v5k));
        test(new DeconRule("いたらば", "いた", "conditional-formal", DefTag.v5k));
        test(new DeconRule("いだら", "いだ", "conditional", DefTag.v5g));
        test(new DeconRule("いだらば", "いだ", "conditional-formal", DefTag.v5g));
        test(new DeconRule("したら", "した", "conditional", DefTag.v5s));
        test(new DeconRule("したらば", "した", "conditional-formal", DefTag.v5s));
        test(new DeconRule("ったら", "った", "conditional", DefTag.v5t));
        test(new DeconRule("ったらば", "った", "conditional-formal", DefTag.v5t));
        test(new DeconRule("んだら", "んだ", "conditional", DefTag.v5b));
        test(new DeconRule("んだらば", "んだ", "conditional-formal", DefTag.v5b));
        test(new DeconRule("んだら", "んだ", "conditional", DefTag.v5n));
        test(new DeconRule("んだらば", "んだ", "conditional-formal", DefTag.v5n));
        test(new DeconRule("んだら", "んだ", "conditional", DefTag.v5m));
        test(new DeconRule("んだらば", "んだ", "conditional-formal", DefTag.v5m));
        test(new DeconRule("ったら", "った", "conditional", DefTag.v5r));
        test(new DeconRule("ったらば", "った", "conditional-formal", DefTag.v5r));
        
        test(new DeconRule("かったら", "かった", "conditional", DefTag.adj_i));//TODO does this work with i adjectives?
        test(new DeconRule("かったらば", "かった", "conditional-formal", DefTag.adj_i));//TODO does this work with i adjectives?
        test(new DeconRule("たら", "た", "conditional", DefTag.v1));
        test(new DeconRule("たらば", "た", "conditional-formal", DefTag.v1));
        
        //imperative (for orders)
        test(new DeconRule("え", "う", "imperative", DefTag.v5u));
        test(new DeconRule("け", "く", "imperative", DefTag.v5k));
        test(new DeconRule("げ", "ぐ", "imperative", DefTag.v5g));
        test(new DeconRule("せ", "す", "imperative", DefTag.v5s));
        test(new DeconRule("て", "つ", "imperative", DefTag.v5t));
        test(new DeconRule("べ", "ぶ", "imperative", DefTag.v5b));
        test(new DeconRule("ね", "ぬ", "imperative", DefTag.v5n));
        test(new DeconRule("め", "む", "imperative", DefTag.v5m));
        test(new DeconRule("れ", "る", "imperative", DefTag.v5r));
        test(new DeconRule("れ", "る", "imperative", DefTag.v5r_i));
        
        //not for i-adj, 4 exist for v1
        test(new DeconRule("いろ", "いる", "imperative", DefTag.v1));
        test(new DeconRule("いよ", "いる", "imperative", DefTag.v1));
        test(new DeconRule("えろ", "える", "imperative", DefTag.v1));
        test(new DeconRule("えよ", "える", "imperative", DefTag.v1));
        
        //volitional (let's)
        test(new DeconRule("おう", "う", "volitional", DefTag.v5u));
        test(new DeconRule("こう", "く", "volitional", DefTag.v5k));
        test(new DeconRule("ごう", "ぐ", "volitional", DefTag.v5g));
        test(new DeconRule("そう", "す", "volitional", DefTag.v5s));
        test(new DeconRule("とう", "つ", "volitional", DefTag.v5t));
        test(new DeconRule("ぼう", "ぶ", "volitional", DefTag.v5b));
        test(new DeconRule("のう", "ぬ", "volitional", DefTag.v5n));
        test(new DeconRule("もう", "む", "volitional", DefTag.v5m));
        test(new DeconRule("ろう", "る", "volitional", DefTag.v5r));
        
        test(new DeconRule("かろう", "い", "volitional", DefTag.adj_i));
        test(new DeconRule("よう", "る", "volitional", DefTag.v1));
        
    }
    
    
    private void add(ValidWord word)
    {
        if(word != null)matches.add(word);
    }
    private void test(DeconRule rule)
    {
        int size = matches.size();//don't scan ones added by this rule
        for (int i = 0; i < size; i++)
        {
           add(rule.process(matches.get(i)));
        }
    }

    public ArrayList<ValidWord> getMatches()
    {
        return matches;
    }

    public String getWord()
    {
        return word;
    }
    
    public static void main(String[] args)
    {
        System.out.println();
        for(ValidWord vw: new WordScanner("絞られて").getMatches())
        {
            System.out.println(vw.toString() + " " + vw.getNeededTags());
        }
    }
    
}
