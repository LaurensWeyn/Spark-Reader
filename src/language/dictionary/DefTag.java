package language.dictionary;

import java.util.Collection;

/**
 *
 * @author laure
 */
public enum DefTag
{
    //codes:
    //0=part of speech
    //1=term type
    //2=dialect
    //////////////////
    //part of speech//
    //////////////////
    
    // custom (internal to deconjugator)
    // verbs
    stem_e(-1, "izenkei stem"), // izenkei, as-if-so
    stem_o(-1, "volitional stem"), // tentative form
    stem_a(-1, "stem ending in a"), // the "a" stem of godan verbs
    stem_mizenkei(-1, "mizenkei stem"), // same as stem_a, but parted in deconjugator for simplicity
    stem_te(-1, "te form"), // te form; comes from stem_i_reduced plus te/de
    stem_ren(-1, "renyoukei (masu stem)"), // verb renyoukei; the masu stem infinitive
    stem_ren_less(-1, "reduced renyoukei (past stem)"), // phonologically reduced masu stem infinitive
    stem_ren_less_v(-1, "reduced renyoukei (past stem) (voiced)"), // voiced
    form_volition(-1, "volitional form"), // based on the mizenkei
    // i-adj
    // i-adjectives have two renyoukei-like infinitives, but only this one is useful as a tag
    stem_ku(-1, "adjective ku form"), // e.g. nakute
    stem_ka(-1, "adjective ka form"), // e.g. nakatta
    stem_ke(-1, "adjective ke form"), // e.g. nakereba
    stem_must_first_half(-1, "first half of a 'must' form"), // to reduce the number of rules in WordScanner
    stem_te_defective(-1, "adjective te form (defective syntax)"), // adjectives have a defective te form that does not work the same morphologically as the verbal one; for example なくてください is wrong, but ないでください is fine.
    // dummy tag for organization purposes (might do something later, or might get removed)
    uninflectable(-1, "uninflectable"), // does not further inflect
    stem_adj_base(-1, "adjective stem"),
    
    // non-custom
    adj_i(0, "adjective (keiyoushi)", 0),
    adj_na(0, "adjectival nouns or quasi_adjectives (keiyodoshi)", 1),
    adj_no(0, "nouns which may take the genitive case particle `no'", 2),
    adj_pn(0, "pre_noun adjectival (rentaishi)"),
    adj_t(0, "`taru' adjective"),
    adj_f(0, "noun or verb acting prenominally (other than the above)"),
    
    adj(0, "former adjective classification (being removed)"),
    adv(0, "adverb (fukushi)"),
    adv_n(0, "adverbial noun"),
    adv_to(0, "adverb taking the `to' particle", 3),
    aux(0, "auxiliary", 4),
    aux_v(0, "auxiliary verb"),
    aux_adj(0, "auxiliary adjective", 5),
    conj(0, "conjunction"),
    ctr(0, "counter"),
    exp(0, "Expressions (phrases, clauses, etc.)"),
    INT(0, "interjection (kandoushi)"),
    iv(0, "irregular verb"),
    n(0, "noun (common) (futsuumeishi)", 6),
    n_pr(0, "proper noun"),
    n_adv(0, "adverbial noun (fukushitekimeishi)"),
    n_pref(0, "noun, used as a prefix"),
    n_suf(0, "noun, used as a suffix"),
    n_t(0, "noun (temporal) (jisoumeishi)"),
    num(0, "numeric"),
    pn(0, "pronoun"),
    pref(0, "prefix", 7),
    prt(0, "particle"),
    suf(0, "suffix", 8),

    v1(0, "Ichidan verb", 9),
    v5b(0, "Godan verb with `bu' ending", 10),
    v5g(0, "Godan verb with `gu' ending", 11),
    v5k(0, "Godan verb with `ku' ending", 12),
    v5m(0, "Godan verb with `mu' ending", 13),
    v5n(0, "Godan verb with `nu' ending", 14),
    v5r(0, "Godan verb with `ru' ending", 15),
    v5s(0, "Godan verb with `su' ending", 16),
    v5t(0, "Godan verb with `tsu' ending", 17),
    v5u(0, "Godan verb with `u' ending", 18),
    v5z(0, "Godan verb with `zu' ending", 19),
    
    v2a_s(0, "Nidan verb with 'u' ending (archaic)"),
    v4h(0, "Yodan verb with `hu/fu' ending (archaic)"),
    v4r(0, "Yodan verb with `ru' ending (archaic)", 20),
    v5(0, "Godan verb (not completely classified)"),
    v5aru(0, "Godan verb - -aru special class", 21),
    v5k_s(0, "Godan verb - iku/yuku special class", 22),
    v5r_i(0, "Godan verb with `ru' ending (irregular verb)", 23),
    v5u_s(0, "Godan verb with `u' ending (special class)", 24),
    v5uru(0, "Godan verb - uru old class verb (old form of Eru)"),
    
    vz(0, "Ichidan verb - zuru verb - (alternative form of -jiru verbs)"),
    
    vi(0, "intransitive verb"),
    vk(0, "kuru verb - special class", 25),
    vn(0, "irregular nu verb"),
    vs(0, "noun or participle which takes the aux. verb suru"),
    vs_c(0, "su verb - precursor to the modern suru"),
    vs_i(0, "suru verb - irregular", 26),
    vs_s(0, "suru verb - special class"),
    vt(0, "transitive verb"),
    
    //////////////
    //term types//
    //////////////
    Buddh(1, "Buddhist term"),
    MA(1, "martial arts term"),
    comp(1, "computer terminology"),
    food(1, "food term"),
    geom(1, "geometry term"),
    gram(1, "grammatical term"),
    joc(1, "jocular, humorous term"),
    sports(1, "sports term"),
    law(1, "law, etc. term"),
    geol(1, "geology, etc. term"),
    proverb(1, "proverb"),
    yoji(1, "yojijukugo"),
    intr(1, "interjection (kandoushi)"),
    finc(1, "finance term"),
    med(1, "medicine, etc. term"),
    biol(1, "biology term"),
    chem(1, "chemistry term"),
    astron(1, "astronomy, etc. term"),
    bot(1, "botany term"),
    baseb(1, "baseball term"),
    ling(1, "linguistics terminology"),
    math(1, "mathematics"),
    mil(1, "military"),
    physics(1, "physics terminology"),
    X(2, "rude or X_rated term"),
    abbr(2, "abbreviation"),
    arch(2, "archaism"),
    ateji(2, "ateji (phonetic) reading"),
    chn(2, "children's language"),
    col(2, "colloquialism"),
    derog(2, "derogatory term"),
    eK(2, "exclusively kanji"),
    ek(2, "exclusively kana"),
    fam(2, "familiar language"),
    fem(2, "female term or language"),
    gikun(2, "gikun (meaning) reading"),
    hon(2, "honorific or respectful (sonkeigo) language"),
    hum(2, "humble (kenjougo) language"),
    ik(2, "word containing irregular kana usage"),
    iK(2, "word containing irregular kanji usage"),
    id(2, "idiomatic expression"),
    io(2, "irregular okurigana usage"),
    m_sl(2, "manga slang"),
    male(2, "male term or language"),
    male_sl(2, "male slang"),
    oK(2, "word containing out_dated kanji"),
    obs(2, "obsolete term"),
    obsc(2, "obscure term"),
    ok(2, "out_dated or obsolete kana usage"),
    on_mim(2, "onomatopoeic or mimetic word"),
    poet(2, "poetical term"),
    pol(2, "polite (teineigo) language"),
    rare(2, "rare"),
    sens(2, "sensitive word"),
    sl(2, "slang"),
    uK(2, "word usually written using kanji alone"),
    uk(2, "word usually written using kana alone"),
    vulg(2, "vulgar expression or word"),
    kyb(3, "Kyoto-ben"),
    osb(3, "Osaka-ben"),
    ksb(3, "Kansai-ben"),
    ktb(3, "Kantou-ben"),
    tsb(3, "Tosa-ben"),
    thb(3, "Touhoku-ben"),
    tsug(3, "Tsugaru-ben"),
    kyu(3, "Kyuushuu-ben"),
    rkb(3, "Ryuukyuu-ben"),
    
    p(4, ""), P(4, "");
    private int group;
    private String name;
    private int shortBit = -1;
    DefTag(int group, String name)
    {
        this.group = group;
        this.name = name;
    }
    DefTag(int group, String name, int bitNum)
    {
        this.group = group;
        this.name = name;
        this.shortBit = 1 << bitNum;
    }
    public String toString()
    {
        return name;
    }
    public int getGroup()
    {
        return group;
    }
    public static DefTag toTag(String text)
    {
        //System.out.println("tagging " + text);
        switch(text)
        {
            case "int":return intr;
        }
        try
        {
            return valueOf(text.toLowerCase().replace('-', '_'));
        }catch(IllegalArgumentException e)
        {
            return null;
        }
    }

    /**
     * Converts a DefTag collection to a single primitive value for quick comparison operations.
     * Tags not relevant for the short code are ignored.
     * @param tags the tags to include
     * @return the tags encoded in binary
     */
    public static long toQuickTag(Collection<DefTag> tags)
    {
        if(tags == null)return 0;
        long out = 0;
        for(DefTag tag:tags)
        {
            if(tag.shortBit != -1)
                out |= tag.shortBit;
        }
        return out;
    }
    
}
