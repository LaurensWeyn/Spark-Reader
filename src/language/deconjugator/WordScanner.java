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

/**
 * Produces all possible deconjugations of a word for lookup in dictionaries
 * @author Laurens Weyn
 */
public class WordScanner
{
    private ArrayList<ValidWord> matches;
    private String word;

    private static ArrayList<DeconRule> ruleList;
    private static void init()
    {
        if(ruleList != null)return;
        ruleList = new ArrayList<>();

        ruleList.add(word ->
        {
            String hiragana = Japanese.toHiragana(word.getWord(), false);
            if(!word.getWord().equals(hiragana))
            {
                return new ValidWord(hiragana, "hiragana");
            }
            else
            {
                return null;
            }
        });
        
        // collapse these at will because they're dumb and long
        ruleList.add(new StdRule("なければいけない", "", "must", DefTag.stem_a, DefTag.adj_i));
        ruleList.add(new StdRule("なければならない", "", "must", DefTag.stem_a, DefTag.adj_i));
        ruleList.add(new StdRule("なくてはいけない", "", "must", DefTag.stem_a, DefTag.adj_i));
        ruleList.add(new StdRule("なくてはならない", "", "must", DefTag.stem_a, DefTag.adj_i));
        ruleList.add(new StdRule("なければいけない", "", "must", DefTag.stem_ku, DefTag.adj_i));
        ruleList.add(new StdRule("なければならない", "", "must", DefTag.stem_ku, DefTag.adj_i));
        ruleList.add(new StdRule("なくてはいけない", "", "must", DefTag.stem_ku, DefTag.adj_i));
        ruleList.add(new StdRule("なくてはならない", "", "must", DefTag.stem_ku, DefTag.adj_i));
        
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
        
        // たら, the generic conditional
        // verbs
        ruleList.add(new StdRule("たら", "", "conditional", DefTag.stem_ren_less, DefTag.uninflectable));
        ruleList.add(new StdRule("たらば", "", "formal conditional", DefTag.stem_ren_less, DefTag.uninflectable));
        // i-adjectives
        ruleList.add(new StdRule("だら", "", "conditional", DefTag.stem_ren_less_v, DefTag.uninflectable));
        ruleList.add(new StdRule("だらば", "", "formal conditional", DefTag.stem_ren_less_v, DefTag.uninflectable));
        
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
        
        // passive (godan)
        // pattern: areru
        
        // potential (godan)
        // pattern: eru
        
        // passive-potential and potential (ichidan)
        // passive-potential: rareru; potential: reru
        
        // nasai
        // technically an i-adjective, but again, letting the deconjugator use it like that would cause more problems than it's worth
        ruleList.add(new StdRule("なさい",         "", "kind request", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("な",         "", "casual kind request", DefTag.stem_ren, DefTag.uninflectable));
        
        // ます inflects, but does so entirely irregularly.
        ruleList.add(new StdRule("ます",         "", "polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ません",       "", "negative polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ました",       "", "past polite", DefTag.stem_ren, DefTag.uninflectable));
        ruleList.add(new StdRule("ませんでした", "", "past negative polite", DefTag.stem_ren, DefTag.uninflectable));
        
        // part-of-speech roles
        ruleList.add(new StdRule("な", "", "adjective", DefTag.adj_na));
        ruleList.add(new StdRule("の", "", "adjective", DefTag.adj_no));
        
        // i-adjective stems
        ruleList.add(new StdRule("く", "い", "(adverb)", DefTag.adj_i, DefTag.stem_ku));
        ruleList.add(new StdRule("か", "い", "ka stem", DefTag.adj_i, DefTag.stem_ka));
        ruleList.add(new StdRule("け", "い", "ke stem", DefTag.adj_i, DefTag.stem_ke));
        ruleList.add(new StdRule("さ", "い", "noun form", DefTag.adj_i, DefTag.n));
        
        // negative
        // verbs
        ruleList.add(new StdRule("ない", "", "negative", DefTag.stem_a, DefTag.adj_i));
        // i-adjectives
        ruleList.add(new StdRule("ない", "", "negative", DefTag.stem_ku, DefTag.adj_i));
        
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
        ruleList.add(new StdRule("え", "う", "(izenkei)", DefTag.v5u_s, DefTag.stem_a));
        
        // negative stem (and stem of some conjunctions)
        ruleList.add(new StdRule("か", "く", "(mizenkei)", DefTag.v5k, DefTag.stem_a));
        ruleList.add(new StdRule("さ", "す", "(mizenkei)", DefTag.v5s, DefTag.stem_a));
        ruleList.add(new StdRule("た", "つ", "(mizenkei)", DefTag.v5t, DefTag.stem_a));
        ruleList.add(new StdRule("わ", "う", "(mizenkei)", DefTag.v5u, DefTag.stem_a));
        ruleList.add(new StdRule("ら", "る", "(mizenkei)", DefTag.v5r, DefTag.stem_a));
        ruleList.add(new StdRule("が", "ぐ", "(mizenkei)", DefTag.v5g, DefTag.stem_a));
        ruleList.add(new StdRule("ば", "ぶ", "(mizenkei)", DefTag.v5b, DefTag.stem_a));
        ruleList.add(new StdRule("な", "ぬ", "(mizenkei)", DefTag.v5n, DefTag.stem_a));
        ruleList.add(new StdRule("ま", "む", "(mizenkei)", DefTag.v5m, DefTag.stem_a));
        // fixme: having multiple conjugations with the same form can make the ui display bogus extra definitions
        ruleList.add(new StdRule(""  , "る", "(mizenkei)", DefTag.v1,  DefTag.stem_a));
        // marginal categories
        ruleList.add(new StdRule("わ", "う", "(mizenkei)", DefTag.v5u_s, DefTag.stem_a));
        
        // past stem
        ruleList.add(new StdRule("い", "く", "(other infinitive)", DefTag.v5k, DefTag.stem_ren_less));
        ruleList.add(new StdRule("し", "す", "(other infinitive)", DefTag.v5s, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "つ", "(other infinitive)", DefTag.v5t, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "う", "(other infinitive)", DefTag.v5u, DefTag.stem_ren_less));
        ruleList.add(new StdRule("っ", "る", "(other infinitive)", DefTag.v5r, DefTag.stem_ren_less));
        ruleList.add(new StdRule("い", "ぐ", "(other infinitive)", DefTag.v5g, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "ぶ", "(other infinitive)", DefTag.v5b, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "ぬ", "(other infinitive)", DefTag.v5n, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule("ん", "む", "(other infinitive)", DefTag.v5m, DefTag.stem_ren_less_v));
        ruleList.add(new StdRule(""  , "る", "(other infinitive)", DefTag.v1,  DefTag.stem_ren_less));
        // marginal categories
        ruleList.add(new StdRule("う", "う", "(other infinitive)", DefTag.v5u_s, DefTag.stem_ren_less));
        
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
    }
    
    public WordScanner(String word)
    {
        init();
        
        matches = new ArrayList<>();
        this.word = word;
        matches.add(new ValidWord(word, ""));//add initial undeconjugated word
        // start from the top of the list when we have a successful deconjugation
        int fully_covered_matches = 0;
        int iters = 0;
        while(true)
        {
            int temp = matches.size();
            int number_of_new_matches = test_rules(fully_covered_matches, word);
            
            // the safeguards in process() should be enough, but in case they're not, or they break...
            if(iters > 32)
            {
                System.out.println("bailing out from deconjugation");
                break;
            }
            
            iters++;
            
            if(number_of_new_matches > 0)
                fully_covered_matches += number_of_new_matches;
            else
                break;
        }
    }
    
    private int test_rules(int starting_match, String word)
    {
        //if(starting_match > 3) return false;
        int new_matches = 0;
        //attempt all deconjugation rules in order
        for(DeconRule rule:ruleList)
        {
            //check if any of our possible matches can be deconjugated by this rule
            new_matches += test_matches(starting_match, rule);
        }
        return new_matches;
    }
    
    private int test_matches(int starting_match, DeconRule rule)
    {
        int size = matches.size();//don't scan matches added during iteration
        int good_matches = 0;
        if(starting_match >= size) return 0; // shouldn't happen but just in case it does we should also save the cpu
        for(int i = starting_match; i < size; i++)
        {
            ValidWord gotten = rule.process(matches.get(i));
            if(gotten != null)
            {
                matches.add(gotten);
                good_matches += 1;
            }
        }
        return good_matches;
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
        for(ValidWord vw: new WordScanner("分かりません").getMatches())
        {
            System.out.println(vw.toString() + " " + vw.getNeededTags());
        }
    }
    
}
