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

package language.deconjugator;

import language.dictionary.DefTag;
import language.dictionary.Japanese;

import java.util.ArrayList;

public class WordScannerOld extends WordScanner implements WordScanner.SubScanner
{
    public void subInit()
    {
        if(ruleList != null)return;
        ruleList = new ArrayList<>();


        //Hiragana->Katakana: words are often written in katakana for emphasis but won't be found in EDICT in that form
        ruleList.add(word ->
        {
            String hiragana = Japanese.toHiragana(word.getWord(), false);
            if(!word.getWord().equals(hiragana))
            {
                return new ValidWord(hiragana, "hiragana");
            }
            else return null;
        });


        //Decensor: simple, but actually works well enough with a lot of 'censored' words
        ruleList.add(l_word ->
        {
            if(l_word.getWord().contains("○"))
            {
                return new ValidWord(l_word.getWord().replace('○', 'っ'), (l_word.getProcess() + " " + "censored").trim());
            }
            else return null;
        });
        ruleList.add(l_word ->
        {
            if(l_word.getWord().contains("○"))
            {
                return new ValidWord(l_word.getWord().replace('○', 'ん'), (l_word.getProcess() + " " + "censored").trim());
            }
            else return null;
        });

        //entire Japanese deconjugation lookup table
        //see http://www.wikiwand.com/en/Japanese_verb_conjugation

        //conditional/past conditional (ra) (+ba for formal) (adds on to past, must come before)
        ruleList.add(new StdRule("ったら", "った", "conditional", DefTag.v5u));
        ruleList.add(new StdRule("ったらば", "った", "conditional-formal", DefTag.v5u));
        ruleList.add(new StdRule("いたら", "いた", "conditional", DefTag.v5k));
        ruleList.add(new StdRule("いたらば", "いた", "conditional-formal", DefTag.v5k));
        ruleList.add(new StdRule("いだら", "いだ", "conditional", DefTag.v5g));
        ruleList.add(new StdRule("いだらば", "いだ", "conditional-formal", DefTag.v5g));
        ruleList.add(new StdRule("したら", "した", "conditional", DefTag.v5s));
        ruleList.add(new StdRule("したらば", "した", "conditional-formal", DefTag.v5s));
        ruleList.add(new StdRule("ったら", "った", "conditional", DefTag.v5t));
        ruleList.add(new StdRule("ったらば", "った", "conditional-formal", DefTag.v5t));
        ruleList.add(new StdRule("んだら", "んだ", "conditional", DefTag.v5b));
        ruleList.add(new StdRule("んだらば", "んだ", "conditional-formal", DefTag.v5b));
        ruleList.add(new StdRule("んだら", "んだ", "conditional", DefTag.v5n));
        ruleList.add(new StdRule("んだらば", "んだ", "conditional-formal", DefTag.v5n));
        ruleList.add(new StdRule("んだら", "んだ", "conditional", DefTag.v5m));
        ruleList.add(new StdRule("んだらば", "んだ", "conditional-formal", DefTag.v5m));
        ruleList.add(new StdRule("ったら", "った", "conditional", DefTag.v5r));
        ruleList.add(new StdRule("ったらば", "った", "conditional-formal", DefTag.v5r));

        ruleList.add(new StdRule("かったら", "かった", "conditional", DefTag.adj_i));//TODO does this work with i adjectives?
        ruleList.add(new StdRule("かったらば", "かった", "conditional-formal", DefTag.adj_i));//TODO does this work with i adjectives?
        ruleList.add(new StdRule("たら", "た", "conditional", DefTag.v1));
        ruleList.add(new StdRule("たらば", "た", "conditional-formal", DefTag.v1));

        //potential (can do verb, can combine with past)
        //further conjugates like v1
        ruleList.add(new StdRule("える", "う", "potential", DefTag.v5u, DefTag.v1));
        ruleList.add(new StdRule("ける", "く", "potential", DefTag.v5k, DefTag.v1));
        ruleList.add(new StdRule("げる", "ぐ", "potential", DefTag.v5g, DefTag.v1));
        ruleList.add(new StdRule("せる", "す", "potential", DefTag.v5s, DefTag.v1));
        ruleList.add(new StdRule("てる", "つ", "potential", DefTag.v5t, DefTag.v1));
        ruleList.add(new StdRule("べる", "ぶ", "potential", DefTag.v5b, DefTag.v1));
        ruleList.add(new StdRule("ねる", "ぬ", "potential", DefTag.v5n, DefTag.v1));
        ruleList.add(new StdRule("める", "む", "potential", DefTag.v5m, DefTag.v1));
        ruleList.add(new StdRule("れる", "る", "potential", DefTag.v5r, DefTag.v1));

        //past->dict
        ruleList.add(new StdRule("った", "う", "past", DefTag.v5u));
        ruleList.add(new StdRule("いた", "く", "past", DefTag.v5k));
        ruleList.add(new StdRule("いだ", "ぐ", "past", DefTag.v5g));
        ruleList.add(new StdRule("した", "す", "past", DefTag.v5s));
        ruleList.add(new StdRule("った", "つ", "past", DefTag.v5t));
        ruleList.add(new StdRule("んだ", "ぶ", "past", DefTag.v5b));
        ruleList.add(new StdRule("んだ", "ぬ", "past", DefTag.v5n));
        ruleList.add(new StdRule("んだ", "む", "past", DefTag.v5m));
        ruleList.add(new StdRule("った", "る", "past", DefTag.v5r));

        ruleList.add(new StdRule("かった", "い", "past", DefTag.adj_i));
        ruleList.add(new StdRule("た", "る", "past", DefTag.v1));

        //te->dict
        ruleList.add(new StdRule("って", "う", "て form", DefTag.v5u));
        ruleList.add(new StdRule("いて", "く", "て form", DefTag.v5k));
        ruleList.add(new StdRule("いで", "ぐ", "て form", DefTag.v5g));
        ruleList.add(new StdRule("して", "す", "て form", DefTag.v5s));
        ruleList.add(new StdRule("って", "つ", "て form", DefTag.v5t));
        ruleList.add(new StdRule("んで", "ぶ", "て form", DefTag.v5b));
        ruleList.add(new StdRule("んで", "ぬ", "て form", DefTag.v5n));
        ruleList.add(new StdRule("んで", "む", "て form", DefTag.v5m));
        ruleList.add(new StdRule("って", "る", "て form", DefTag.v5r));

        ruleList.add(new StdRule("くて", "い", "て form", DefTag.adj_i));
        ruleList.add(new StdRule("て", "る", "て form", DefTag.v1));

        //neg->dict (te form must be done first)
        //further conjugates like i adjective
        ruleList.add(new StdRule("わない", "う", "negative", DefTag.v5u, DefTag.adj_i));
        ruleList.add(new StdRule("かない", "く", "negative", DefTag.v5k, DefTag.adj_i));
        ruleList.add(new StdRule("がない", "ぐ", "negative", DefTag.v5g, DefTag.adj_i));
        ruleList.add(new StdRule("さない", "す", "negative", DefTag.v5s, DefTag.adj_i));
        ruleList.add(new StdRule("たない", "つ", "negative", DefTag.v5t, DefTag.adj_i));
        ruleList.add(new StdRule("ばない", "ぶ", "negative", DefTag.v5b, DefTag.adj_i));
        ruleList.add(new StdRule("なない", "ぬ", "negative", DefTag.v5n, DefTag.adj_i));
        ruleList.add(new StdRule("まない", "む", "negative", DefTag.v5m, DefTag.adj_i));
        ruleList.add(new StdRule("らない", "る", "negative", DefTag.v5r, DefTag.adj_i));

        ruleList.add(new StdRule("くない", "い", "negative", DefTag.adj_i));
        ruleList.add(new StdRule("ない", "る", "negative", DefTag.v1, DefTag.adj_i));

        //masu/tai/etc removal (handled after past so that still conjugates, before i stem)
        //TODO make these only work with verbs (not with gatai!)
        ruleList.add(new StdRule("ます", "", "polite"));
        ruleList.add(new StdRule("ません", "", "negative polite"));
        ruleList.add(new StdRule("たい", "", "want"));
        ruleList.add(new StdRule("なさい", "", "command"));

        //i stem (polite/tai/etc)
        ruleList.add(new StdRule("い", "う", "i stem", DefTag.v5u));
        ruleList.add(new StdRule("き", "く", "i stem", DefTag.v5k));
        ruleList.add(new StdRule("ぎ", "ぐ", "i stem", DefTag.v5g));
        ruleList.add(new StdRule("し", "す", "I stem", DefTag.v5s));//note: capital I to stop removal of v5s tag
        ruleList.add(new StdRule("ち", "つ", "i stem", DefTag.v5t));
        ruleList.add(new StdRule("に", "ぬ", "i stem", DefTag.v5n));
        ruleList.add(new StdRule("び", "ぶ", "i stem", DefTag.v5b));
        ruleList.add(new StdRule("み", "む", "i stem", DefTag.v5m));
        ruleList.add(new StdRule("り", "る", "i stem", DefTag.v5r));

        ruleList.add(new StdRule("", "る", "i stem", DefTag.v1));
        //adjective stems moved to bottom to avoid conflict with provisional-conditional

        //adjective conjugations
        ruleList.add(new StdRule("く", "い", "adverb", DefTag.adj_i));
        ruleList.add(new StdRule("な", "", "adjective", DefTag.adj_na));
        ruleList.add(new StdRule("の", "", "adjective", DefTag.adj_no));

        //potential was here

        //not for adjectives
        ruleList.add(new StdRule("られる", "る", "potential", DefTag.v1));//normal
        ruleList.add(new StdRule("らる", "る", "potential", DefTag.v1));//coloquial

        //passive
        //further conjugates like v1
        ruleList.add(new StdRule("われる", "う", "passive", DefTag.v5u, DefTag.v1));
        ruleList.add(new StdRule("かれる", "く", "passive", DefTag.v5k, DefTag.v1));
        ruleList.add(new StdRule("がれる", "ぐ", "passive", DefTag.v5g, DefTag.v1));
        ruleList.add(new StdRule("される", "す", "passive", DefTag.v5s, DefTag.v1));
        ruleList.add(new StdRule("たてる", "つ", "passive", DefTag.v5t, DefTag.v1));
        ruleList.add(new StdRule("ばれる", "ぶ", "passive", DefTag.v5b, DefTag.v1));
        ruleList.add(new StdRule("なれる", "ぬ", "passive", DefTag.v5n, DefTag.v1));
        ruleList.add(new StdRule("まれる", "む", "passive", DefTag.v5m, DefTag.v1));
        ruleList.add(new StdRule("られる", "る", "passive", DefTag.v5r, DefTag.v1));

        //not for adjectives
        ruleList.add(new StdRule("られる", "る", "passive", DefTag.v1));


        //causative

        //causative passive (colloquial version, add polite later?)


        //-eba form (provisional conditional)
        ruleList.add(new StdRule("えば", "う", "provisional-conditional", DefTag.v5u));
        ruleList.add(new StdRule("けば", "く", "provisional-conditional", DefTag.v5k));
        ruleList.add(new StdRule("げば", "ぐ", "provisional-conditional", DefTag.v5g));
        ruleList.add(new StdRule("せば", "す", "provisional-conditional", DefTag.v5s));
        ruleList.add(new StdRule("てば", "つ", "provisional-conditional", DefTag.v5t));
        ruleList.add(new StdRule("べば", "ぶ", "provisional-conditional", DefTag.v5b));
        ruleList.add(new StdRule("ねば", "ぬ", "provisional-conditional", DefTag.v5n));
        ruleList.add(new StdRule("めば", "む", "provisional-conditional", DefTag.v5m));
        ruleList.add(new StdRule("れば", "る", "provisional-conditional", DefTag.v5r));
        ruleList.add(new StdRule("れば", "る", "provisional-conditional", DefTag.v5r_i));

        ruleList.add(new StdRule("くない", "い", "provisional-conditional", DefTag.adj_i));
        ruleList.add(new StdRule("なければ", "ない", "provisional-conditional", DefTag.aux_adj));
        ruleList.add(new StdRule("れば", "る", "provisional-conditional", DefTag.v1));


        //imperative (for orders)
        ruleList.add(new StdRule("え", "う", "imperative", DefTag.v5u));
        ruleList.add(new StdRule("け", "く", "imperative", DefTag.v5k));
        ruleList.add(new StdRule("げ", "ぐ", "imperative", DefTag.v5g));
        ruleList.add(new StdRule("せ", "す", "imperative", DefTag.v5s));
        ruleList.add(new StdRule("て", "つ", "imperative", DefTag.v5t));
        ruleList.add(new StdRule("べ", "ぶ", "imperative", DefTag.v5b));
        ruleList.add(new StdRule("ね", "ぬ", "imperative", DefTag.v5n));
        ruleList.add(new StdRule("め", "む", "imperative", DefTag.v5m));
        ruleList.add(new StdRule("れ", "る", "imperative", DefTag.v5r));
        ruleList.add(new StdRule("れ", "る", "imperative", DefTag.v5r_i));

        //not for i-adj, 4 exist for v1
        ruleList.add(new StdRule("いろ", "いる", "imperative", DefTag.v1));
        ruleList.add(new StdRule("いよ", "いる", "imperative", DefTag.v1));
        ruleList.add(new StdRule("えろ", "える", "imperative", DefTag.v1));
        ruleList.add(new StdRule("えよ", "える", "imperative", DefTag.v1));

        //volitional (let's)
        ruleList.add(new StdRule("おう", "う", "volitional", DefTag.v5u));
        ruleList.add(new StdRule("こう", "く", "volitional", DefTag.v5k));
        ruleList.add(new StdRule("ごう", "ぐ", "volitional", DefTag.v5g));
        ruleList.add(new StdRule("そう", "す", "volitional", DefTag.v5s));
        ruleList.add(new StdRule("とう", "つ", "volitional", DefTag.v5t));
        ruleList.add(new StdRule("ぼう", "ぶ", "volitional", DefTag.v5b));
        ruleList.add(new StdRule("のう", "ぬ", "volitional", DefTag.v5n));
        ruleList.add(new StdRule("もう", "む", "volitional", DefTag.v5m));
        ruleList.add(new StdRule("ろう", "る", "volitional", DefTag.v5r));

        ruleList.add(new StdRule("かろう", "い", "volitional", DefTag.adj_i));
        ruleList.add(new StdRule("よう", "る", "volitional", DefTag.v1));


        ruleList.add(new StdRule("", "い", "adj 'stem'", DefTag.adj_i));//Try match stem anyways, needed for things like '頼もしげに'
        //no stem for adjectives, but -sou sort-of uses a stem
        ruleList.add(new StdRule("そう", "い", "-sou", DefTag.adj_i));
    }
    private  void test_rules()
    {
        //attempt all deconjugation rules in order

        // Have to iterate on rules outside of matches because this deconjugator is non-recursive
        for(DeconRule rule:ruleList)
        {
            int size = matches.size();//don't scan matches added during iteration
            if(size == 0) return;
            for(int i = 0; i < size; i++)
            {
                //check if any of our possible matches can be deconjugated by this rule
                ValidWord gotten = rule.process(matches.get(i));
                if(gotten != null)
                {
                    matches.add(gotten);
                }
            }
        }
    }
    public void scanWord(String word)
    {
        matches.add(new ValidWord(word, ""));//add initial unmodified word
        test_rules();
    }
}
