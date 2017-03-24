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

// With contributions Copyright 2017 Alexander Nadeau

package language.deconjugator;

import language.dictionary.DefTag;
import language.dictionary.Japanese;

import java.util.ArrayList;

/**
 * Created by wareya on 2017/03/23.
 */
public class WordScannerNew extends WordScanner implements SubScanner
{
    public void subinit()
    {
        if(ruleList != null)return;
        ruleList = new ArrayList<>();

        /*
        // handle "must" in a single block because it's dumb and long
        // todo: add a type of rule that allows A/B matches in conjugated ending
        // todo: or allow conjugation display to substitute sequences "process" text for others
        // todo: (so that this isn't needed to make "must" look like "must"
        ruleList.add(new StdRule("いけない", "", "must", DefTag.stem_must_first_half, DefTag.adj_i));
        ruleList.add(new StdRule("いけぬ", "", "must", DefTag.stem_must_first_half, DefTag.adj_i));
        ruleList.add(new StdRule("ならない", "", "must", DefTag.stem_must_first_half, DefTag.adj_i));
        ruleList.add(new StdRule("ならぬ", "", "must", DefTag.stem_must_first_half, DefTag.adj_i));
        ruleList.add(new StdRule("ねば", "", "(negative condition)", DefTag.stem_a, DefTag.stem_must_first_half));
        ruleList.add(new StdRule("ねば", "", "(negative condition)", DefTag.stem_ku, DefTag.stem_must_first_half));
        ruleList.add(new StdRule("なければ", "", "(negative condition)", DefTag.stem_a, DefTag.stem_must_first_half));
        ruleList.add(new StdRule("なければ", "", "(negative condition)", DefTag.stem_ku, DefTag.stem_must_first_half));
        ruleList.add(new StdRule("なくては", "", "(negative condition)", DefTag.stem_a, DefTag.stem_must_first_half));
        ruleList.add(new StdRule("なくては", "", "(negative condition)", DefTag.stem_ku, DefTag.stem_must_first_half));
        // fixme: can this use たら instead of ば as well? are certain combinations forbidden?
        // fixme: なかったら? ないと?
        // fixme: なりません? いけません?
        */

        ruleList.add(new StdRule("たい", "", "want", DefTag.stem_ren, DefTag.adj_i));
        ruleList.add(new StdRule("ください", "", "polite request", DefTag.stem_te, DefTag.adj_i));

        // te form
        // verbs
        ruleList.add(new StdRule("で", "", "(te form)", DefTag.stem_ren_less_v, DefTag.stem_te));
        ruleList.add(new StdRule("て", "", "(te form)", DefTag.stem_ren_less, DefTag.stem_te));
        ruleList.add(new StdRule("て", "", "(te form)", DefTag.stem_ren, DefTag.stem_te)); // formal but real
        // i-adjectives
        // i-adjectives have two te forms. One works well with auxiliary verbs (and thus deconjugation), and the other does not.
        ruleList.add(new StdRule("で", "", "(te form)", DefTag.adj_i, DefTag.stem_te));
        ruleList.add(new StdRule("て", "", "(te form)", DefTag.stem_ku, DefTag.stem_te_defective));

        // todo: add better names for these later
        // Should be restricted to verbs
        ruleList.add(new StdRule("いる", "", "teiru", DefTag.stem_te, DefTag.v1));
        // -- common colloquial form drops the い entirely
        ruleList.add(new StdRule("る", "", "teru", DefTag.stem_te, DefTag.v1)); // this causes so many problems...
        // Not sure if these should be restricted to verbs but probably
        ruleList.add(new StdRule("いく", "", "teiku", DefTag.stem_te, DefTag.v5k_s));
        ruleList.add(new StdRule("くる", "", "tekuru", DefTag.stem_te, DefTag.vk));
        // Should form differently on adjectives than verbs
        ruleList.add(new StdRule("ある", "", "tearu", DefTag.stem_te, DefTag.v5aru));

        // たら, the generic conditional
        // verbs
        ruleList.add(new StdRule("たら", "", "conditional", DefTag.stem_ren_less, DefTag.uninflectable));
        ruleList.add(new StdRule("たらば", "", "formal conditional", DefTag.stem_ren_less, DefTag.uninflectable));
        // (voiced)
        ruleList.add(new StdRule("だら", "", "conditional", DefTag.stem_ren_less_v, DefTag.uninflectable));
        ruleList.add(new StdRule("だらば", "", "formal conditional", DefTag.stem_ren_less_v, DefTag.uninflectable));
        // i-adjectives
        ruleList.add(new StdRule("ったら", "", "conditional", DefTag.stem_ka, DefTag.uninflectable));
        ruleList.add(new StdRule("ったらば", "", "formal conditional", DefTag.stem_ka, DefTag.uninflectable));

        // ば, the provisional conditional
        // verbs
        ruleList.add(new StdRule("ば", "", "provisional conditional", DefTag.stem_e, DefTag.uninflectable));
        // i-adjectives
        ruleList.add(new StdRule("れば", "", "provisional conditional", DefTag.stem_ke, DefTag.uninflectable));

        // past
        // verbs
        ruleList.add(new StdRule("だ", "", "past", DefTag.stem_ren_less_v, DefTag.uninflectable));
        ruleList.add(new StdRule("た", "", "past", DefTag.stem_ren_less, DefTag.uninflectable));
        // i-adjectives
        ruleList.add(new StdRule("った", "", "past", DefTag.stem_ka, DefTag.uninflectable));

        // たり is its own morpheme, not た+り, and semgmenters (like kuromoji) should make たり an entire segment, so we have to deconjugate たり (it's also the right thing to do)
        // * etymology: てあり; as in てある
        ruleList.add(new StdRule("だり", "", "~tari", DefTag.stem_ren_less_v, DefTag.uninflectable));
        ruleList.add(new StdRule("たり", "", "~tari", DefTag.stem_ren_less, DefTag.uninflectable));
        // i-adjectives
        ruleList.add(new StdRule("ったり", "", "~tari", DefTag.stem_ka, DefTag.uninflectable));


        // passive (godan)
        ruleList.add(new StdRule("れる", "", "passive", DefTag.stem_a, DefTag.v1)); // ichidan cannot conjugate to "stem_a"

        // passive-potential (ichidan)
        ruleList.add(new StdRule("られる", "る", "potential/passive nexus", DefTag.v1, DefTag.v1));

        // potential
        // pattern is the same for ichidan and godan verbs; the ichidan one is PROscribed, but still real.
        ruleList.add(new StdRule("る", "", "potential", DefTag.stem_e, DefTag.v1));

        // causative
        ruleList.add(new StdRule("させる", "る", "causative", DefTag.v1, DefTag.v1));
        ruleList.add(new StdRule("せる", "", "causative", DefTag.stem_a, DefTag.v1)); // ichidan cannot conjugate to "stem_a"
        // spoken language -- this also covers the "short causative passive" indirectly
        // Only allowed on non-す godan verbs.
        ruleList.add(new ContextRule("す", "", "short causative", DefTag.stem_a, DefTag.v5s,(rule, word) -> {
            if(word.getWord().equals("")) return false;
            if(!word.getWord().endsWith(rule.ending)) return false;
            String base = word.getWord().substring(0, word.getWord().length() - rule.ending.length());
            if(base.endsWith("さ")) return false;
            else return true;
        }
        ));
        // nasai
        // technically an i-adjective, but again, letting the deconjugator use it like that would cause more problems than it's worth
        ruleList.add(new StdRule("なさい", "", "kind request", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("な", "", "casual kind request", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ながら", "", "while", DefTag.stem_ren, DefTag.uninflectable));

        // ます inflects, but does so entirely irregularly.
        ruleList.add(new StdRule("ます", "", "polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ません", "", "negative polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ました", "", "past polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ませんでした", "", "past negative polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ましょう", "", "polite volitional", DefTag.stem_ren, DefTag.uninflectable));

        // part-of-speech roles
        ruleList.add(new StdRule("に", "", "adverb", DefTag.adj_na));
        ruleList.add(new StdRule("な", "", "attributive", DefTag.adj_na));
        ruleList.add(new StdRule("の", "", "attributive", DefTag.adj_no));
        ruleList.add(new StdRule("と", "", "attributive", DefTag.adv_to));

        // i-adjective stems
        ruleList.add(new StdRule("く", "い", "(adverb)", DefTag.adj_i, DefTag.stem_ku));
        ruleList.add(new StdRule("か", "い", "(ka stem)", DefTag.adj_i, DefTag.stem_ka));
        ruleList.add(new StdRule("け", "い", "(ke stem)", DefTag.adj_i, DefTag.stem_ke));
        ruleList.add(new StdRule("さ", "い", "noun form", DefTag.adj_i, DefTag.n));
        // also applies to verbs
        ruleList.add(new StdRule("すぎる", "い", "excess", DefTag.adj_i, DefTag.v1));
        ruleList.add(new StdRule("そう", "い", "seemingness", DefTag.adj_i, DefTag.adj_na));
        ruleList.add(new StdRule("がる", "い", "~garu", DefTag.adj_i, DefTag.v5r));
        ruleList.add(new StdRule("", "い", "(stem)", DefTag.adj_i, DefTag.uninflectable));

        // negative
        // verbs
        ruleList.add(new StdRule("ない", "", "negative", DefTag.stem_mizenkei, DefTag.adj_i));
        ruleList.add(new StdRule("ず", "", "adverbial negative", DefTag.stem_mizenkei, DefTag.uninflectable)); // archaically, not adverbiall, but in modern japanese, almost always adverbial
        ruleList.add(new StdRule("ずに", "", "without doing so", DefTag.stem_mizenkei, DefTag.uninflectable)); // exactly the same meaning, despite the difference in label
        // i-adjectives
        ruleList.add(new StdRule("ない", "", "negative", DefTag.stem_ku, DefTag.adj_i));

        // fixme: having multiple conjugations with the same form makes the ui display bogus extra definitions
        ruleList.add(new StdRule("", "", "(mizenkei)", DefTag.stem_a, DefTag.stem_mizenkei));
        ruleList.add(new StdRule("", "る", "(mizenkei)", DefTag.v1, DefTag.stem_mizenkei));

        // potential stem (and stem of some conjunctions)
        ruleList.add(new StdRule("け", "く", "(izenkei)", DefTag.v5k, DefTag.stem_e));
        ruleList.add(new StdRule("せ", "す", "(izenkei)", DefTag.v5s, DefTag.stem_e));
        ruleList.add(new StdRule("て", "つ", "(izenkei)", DefTag.v5t, DefTag.stem_e));
        ruleList.add(new StdRule("え", "う", "(izenkei)", DefTag.v5u, DefTag.stem_e));
        ruleList.add(new StdRule("れ", "る", "(izenkei)", DefTag.v5r, DefTag.stem_e));
        ruleList.add(new StdRule("げ", "ぐ", "(izenkei)", DefTag.v5g, DefTag.stem_e));
        ruleList.add(new StdRule("べ", "ぶ", "(izenkei)", DefTag.v5b, DefTag.stem_e));
        ruleList.add(new StdRule("ね", "ぬ", "(izenkei)", DefTag.v5n, DefTag.stem_e));
        ruleList.add(new StdRule("め", "む", "(izenkei)", DefTag.v5m, DefTag.stem_e));
        ruleList.add(new StdRule("れ", "る", "(izenkei)", DefTag.v1,  DefTag.stem_e)); // not a copy/paste mistake
        // marginal categories
        ruleList.add(new StdRule("え", "う", "(izenkei)", DefTag.v5u_s, DefTag.stem_e));
        ruleList.add(new StdRule("け", "く", "(izenkei)", DefTag.v5k_s, DefTag.stem_e));

        // "a" stem used by godan verbs
        ruleList.add(new StdRule("か", "く", "('a' stem)", DefTag.v5k, DefTag.stem_a));
        ruleList.add(new StdRule("さ", "す", "('a' stem)", DefTag.v5s, DefTag.stem_a));
        ruleList.add(new StdRule("た", "つ", "('a' stem)", DefTag.v5t, DefTag.stem_a));
        ruleList.add(new StdRule("わ", "う", "('a' stem)", DefTag.v5u, DefTag.stem_a));
        ruleList.add(new StdRule("ら", "る", "('a' stem)", DefTag.v5r, DefTag.stem_a));
        ruleList.add(new StdRule("が", "ぐ", "('a' stem)", DefTag.v5g, DefTag.stem_a));
        ruleList.add(new StdRule("ば", "ぶ", "('a' stem)", DefTag.v5b, DefTag.stem_a));
        ruleList.add(new StdRule("な", "ぬ", "('a' stem)", DefTag.v5n, DefTag.stem_a));
        ruleList.add(new StdRule("ま", "む", "('a' stem)", DefTag.v5m, DefTag.stem_a));
        // marginal categories
        ruleList.add(new StdRule("わ", "う", "('a' stem)", DefTag.v5u_s, DefTag.stem_a));
        ruleList.add(new StdRule("か", "く", "('a' stem)", DefTag.v5k_s, DefTag.stem_a));

        // past stem
        ruleList.add(new StdRule("い", "く", "(unstressed infinitive)", DefTag.v5k, DefTag.stem_ren_less));
        ruleList.add(new StdRule("し", "す", "(unstressed infinitive)", DefTag.v5s, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "つ", "(unstressed infinitive)", DefTag.v5t, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "う", "(unstressed infinitive)", DefTag.v5u, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "る", "(unstressed infinitive)", DefTag.v5r, DefTag.stem_ren_less));
        ruleList.add(new StdRule("い", "ぐ", "(unstressed infinitive)", DefTag.v5g, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "ぶ", "(unstressed infinitive)", DefTag.v5b, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "ぬ", "(unstressed infinitive)", DefTag.v5n, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "む", "(unstressed infinitive)", DefTag.v5m, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule(""  , "る", "(unstressed infinitive)", DefTag.v1,  DefTag.stem_ren_less));
        // marginal categories
        ruleList.add(new StdRule("う", "う", "(unstressed infinitive)", DefTag.v5u_s, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "く", "(unstressed infinitive)", DefTag.v5k_s, DefTag.stem_ren_less));

        // masu stem
        ruleList.add(new StdRule("き", "く", "(infinitive)", DefTag.v5k, DefTag.stem_ren));
        ruleList.add(new StdRule("し", "す", "(infinitive)", DefTag.v5s, DefTag.stem_ren));
        ruleList.add(new StdRule("ち", "つ", "(infinitive)", DefTag.v5t, DefTag.stem_ren));
        ruleList.add(new StdRule("い", "う", "(infinitive)", DefTag.v5u, DefTag.stem_ren));
        ruleList.add(new StdRule("り", "る", "(infinitive)", DefTag.v5r, DefTag.stem_ren));
        ruleList.add(new StdRule("ぎ", "ぐ", "(infinitive)", DefTag.v5g, DefTag.stem_ren));
        ruleList.add(new StdRule("び", "ぶ", "(infinitive)", DefTag.v5b, DefTag.stem_ren));
        ruleList.add(new StdRule("に", "ぬ", "(infinitive)", DefTag.v5n, DefTag.stem_ren));
        ruleList.add(new StdRule("み", "む", "(infinitive)", DefTag.v5m, DefTag.stem_ren));
        ruleList.add(new StdRule(""  , "る", "(infinitive)", DefTag.v1,  DefTag.stem_ren));
        // marginal categories
        ruleList.add(new StdRule("い", "う", "(infinitive)", DefTag.v5u_s, DefTag.stem_ren));
        ruleList.add(new StdRule("き", "く", "(infinitive)", DefTag.v5k_s, DefTag.stem_ren));

        // volitional stem
        ruleList.add(new StdRule("こう", "く", "volitional", DefTag.v5k, DefTag.stem_ren_less));
        ruleList.add(new StdRule("そう", "す", "volitional", DefTag.v5s, DefTag.stem_ren_less));
        ruleList.add(new StdRule("とう", "つ", "volitional", DefTag.v5t, DefTag.stem_ren_less));
        ruleList.add(new StdRule("おう", "う", "volitional", DefTag.v5u, DefTag.stem_ren_less));
        ruleList.add(new StdRule("ろう", "る", "volitional", DefTag.v5r, DefTag.stem_ren_less));
        ruleList.add(new StdRule("ごう", "ぐ", "volitional", DefTag.v5g, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ぼう", "ぶ", "volitional", DefTag.v5b, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("のう", "ぬ", "volitional", DefTag.v5n, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("もう", "む", "volitional", DefTag.v5m, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("よう"  , "る", "volitional", DefTag.v1,  DefTag.stem_ren_less));
        // marginal categories
        ruleList.add(new StdRule("おう", "う", "volitional", DefTag.v5u_s, DefTag.stem_ren_less));
        ruleList.add(new StdRule("こう", "く", "volitional", DefTag.v5k_s, DefTag.stem_ren_less));

        // irregulars
        ruleList.add(new StdRule("し", "する", "(infinitive)", DefTag.vs_i, DefTag.stem_ren));
        ruleList.add(new StdRule("し", "する", "(unstressed infinitive)", DefTag.vs_i, DefTag.stem_ren_less));
        ruleList.add(new StdRule("し", "する", "(mizenkei)", DefTag.vs_i, DefTag.stem_mizenkei)); // actually irregular itself but this will do for now
        ruleList.add(new StdRule("すれ", "する", "(izenkei)", DefTag.vs_i, DefTag.stem_e));
        ruleList.add(new StdRule("しろ", "する", "imperative", DefTag.vs_i, DefTag.uninflectable));
        ruleList.add(new StdRule("せよ", "する", "imperative", DefTag.vs_i, DefTag.uninflectable));

        ruleList.add(new StdRule("き", "くる", "(infinitive)", DefTag.vk, DefTag.stem_ren));
        ruleList.add(new StdRule("き", "くる", "(unstressed infinitive)", DefTag.vk, DefTag.stem_ren_less));
        ruleList.add(new StdRule("こ", "くる", "(mizenkei)", DefTag.vk, DefTag.stem_mizenkei));
        ruleList.add(new StdRule("くれ", "くる", "(izenkei)", DefTag.vk, DefTag.stem_e));
        ruleList.add(new StdRule("こい", "くる", "imperative", DefTag.vk, DefTag.uninflectable));

        ruleList.add(new StdRule("来", "来る", "(infinitive)", DefTag.vk, DefTag.stem_ren));
        ruleList.add(new StdRule("来", "来る", "(unstressed infinitive)", DefTag.vk, DefTag.stem_ren_less));
        ruleList.add(new StdRule("来", "来る", "(mizenkei)", DefTag.vk, DefTag.stem_mizenkei));
        ruleList.add(new StdRule("来れ", "来る", "(izenkei)", DefTag.vk, DefTag.stem_e));
        ruleList.add(new StdRule("来い", "来る", "imperative", DefTag.vk, DefTag.uninflectable));

        ruleList.add(new StdRule("あり", "ある", "(infinitive)", DefTag.v5r_i, DefTag.stem_ren));
        ruleList.add(new StdRule("あっ", "ある", "(unstressed infinitive)", DefTag.v5r_i, DefTag.stem_ren_less));
        //ruleList.add(new StdRule("", "ある", "(mizenkei)", DefTag.v5r_i, DefTag.stem_mizenkei)); // not used
        ruleList.add(new StdRule("あれ", "ある", "(izenkei)", DefTag.v5r_i, DefTag.stem_e));
        // ruleList.add(new StdRule("あれ", "ある", "imperative", DefTag.v5r_i, DefTag.uninflectable)); // rare and conflicts with あれ "that"
        
        ruleList.add(new StdRule("ろ", "る", "imperative", DefTag.v1, DefTag.uninflectable));
    }
    // nasty subroutine: make functional? how much overhead does passing data structures have in java?
    public void ScanWord(String word)
    {
        matches.add(new ValidWord(word, ""));//add initial unmodified word
        // convert to kana and add that too if it's not already in hiragana
        String hiragana = Japanese.toHiragana(word, false);
        if(!word.equals(hiragana))
            matches.add(new ValidWord(hiragana, ""));

        // start from the top of the list when we have a successful deconjugation
        int fully_covered_matches = 0;
        int iters = 0;
        while(true)
        {
            int matches_before_testing = matches.size();
            int number_of_new_matches = test_rules(fully_covered_matches);

            // the safeguards in process() should be enough, but in case they're not, or they break...
            if(iters > 24)
            {
                System.out.println("bailing out from deconjugation");
                System.out.println("conjugation tags: " + matches.get(matches.size()-1).getConjugationTags());
                System.out.println("conjugation path: " + matches.get(matches.size()-1).getProcess());
                System.out.println("original: " +  matches.get(matches.size()-1).getOriginalWord());
                System.out.println("iteration " + Integer.toString(iters));
                break;
            }
            //System.out.println("Matches: " + matches.size());
            //System.out.println("New matches: " + number_of_new_matches);

            iters++;

            if(number_of_new_matches > 0)
                fully_covered_matches = matches_before_testing;
            else
                break;
        }
    }
}
