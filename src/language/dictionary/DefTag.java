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
    adj_i(0, "adjective (keiyoushi)", 0, "i-adjective"),
    adj_ix(0, "adjective (keiyoushi) - yoi/ii class", 0, "i-adj"),
    adj_na(0, "adjectival nouns or quasi_adjectives (keiyodoshi)", 1, "na-adj"),
    adj_no(0, "nouns which may take the genitive case particle `no'", 2, "no-adj"),
    adj_pn(0, "pre_noun adjectival (rentaishi)"),
    adj_t(0, "'taru' adjective"),
    adj_f(0, "noun or verb acting prenominally (other than the above)"),
    
    adj(0, "former adjective classification (being removed)"),
    adv(0, "adverb (fukushi)", "adverb"),
    adv_n(0, "adverbial noun"),
    adv_to(0, "adverb taking the 'to' particle", 3, "to-adj"),
    aux(0, "auxiliary", 4),
    aux_v(0, "auxiliary verb"),
    aux_adj(0, "auxiliary adjective", 5),
    conj(0, "conjunction"),
    ctr(0, "counter"),
    exp(0, "Expressions (phrases, clauses, etc.)"),
    INT(0, "interjection (kandoushi)"),
    iv(0, "irregular verb"),
    n(0, "noun (common) (futsuumeishi)", 6, "noun"),
    n_pr(0, "proper noun"),
    n_adv(0, "adverbial noun (fukushitekimeishi)"),
    n_pref(0, "noun, used as a prefix", "prefix"),
    n_suf(0, "noun, used as a suffix", "suffix"),
    n_t(0, "noun (temporal) (jisoumeishi)", "temp. noun"),
    num(0, "numeric"),
    pn(0, "pronoun"),
    pref(0, "prefix", 7),
    prt(0, "particle"),
    suf(0, "suffix", 8),

    v1(0, "Ichidan verb", 9, "Ichidan"),
    v5b(0, "Godan verb with `bu' ending", 10, "Godan"),
    v5g(0, "Godan verb with `gu' ending", 11, "Godan"),
    v5k(0, "Godan verb with `ku' ending", 12, "Godan"),
    v5m(0, "Godan verb with `mu' ending", 13, "Godan"),
    v5n(0, "Godan verb with `nu' ending", 14, "Godan"),
    v5r(0, "Godan verb with `ru' ending", 15, "Godan"),
    v5s(0, "Godan verb with `su' ending", 16, "Godan"),
    v5t(0, "Godan verb with `tsu' ending", 17, "Godan"),
    v5u(0, "Godan verb with `u' ending", 18, "Godan"),
    v5z(0, "Godan verb with `zu' ending", 19, "Godan"),
    
    v2a_s(0, "Nidan verb with 'u' ending (archaic)", "Nidan"),
    v4h(0, "Yodan verb with `hu/fu' ending (archaic)", "Yodan"),
    v4r(0, "Yodan verb with `ru' ending (archaic)", 20, "Yodan"),
    v5(0, "Godan verb (not completely classified)", "Godan special"),
    v5aru(0, "Godan verb - -aru special class", 21, "Godan special"),
    v5k_s(0, "Godan verb - iku/yuku special class", 22, "Godan special"),
    v5r_i(0, "Godan verb with `ru' ending (irregular verb)", 23, "Godan special"),
    v5u_s(0, "Godan verb with `u' ending (special class)", 24, "Godan special"),
    v5uru(0, "Godan verb - uru old class verb (old form of Eru)", "Godan special"),
    
    vz(0, "Ichidan verb - zuru verb - (alternative form of -jiru verbs)"),
    
    vi(0, "intransitive verb", "intrans"),
    vk(0, "kuru verb - special class", 25, "kuru verb"),
    vn(0, "irregular nu verb"),
    vs(0, "noun or participle which takes the aux. verb suru", "suru verb"),
    vr(0, "irregular ru verb, plain form ends with -ri", "ru verb"),
    vs_c(0, "su verb - precursor to the modern suru", "su verb"),
    vs_i(0, "suru verb - irregular", 26, "suru special"),
    vs_s(0, "suru verb - special class", "suru special"),
    vt(0, "transitive verb", "trans"),

    unc(0, "unclassified"),
    other(0, "not classified"),

    //////////////
    //term types//
    //////////////
    Buddh(1, "Buddhist term"),
    MA(1, "martial arts term"),
    sumo(1, "sumo term"),
    shogi(1, "shogi term"),
    music(1, "music term"),
    Shinto(1, "Shinto term"),
    mahj(1, "mahjong term", "mahjong"),
    archit(1, "architecture term"),
    anat(1, "anatomical term", "anatomical"),
    bus(1, "business term", "business"),
    econ(1, "economics term", "economics"),
    zool(1, "zoology term", "zoology"),
    engr(1, "engineering term", "engineering"),
    comp(1, "computer terminology", "computer"),
    food(1, "food term", "food term"),
    geom(1, "geometry term", "geometry"),
    gram(1, "grammatical term", "grammar term"),
    joc(1, "jocular, humorous term"),
    sports(1, "sports term", "sports term"),
    law(1, "law, etc. term", "law"),
    geol(1, "geology, etc. term", "geology"),
    proverb(1, "proverb"),
    yoji(1, "yojijukugo"),
    intr(1, "interjection (kandoushi)"),
    finc(1, "finance term", "finance"),
    med(1, "medicine, etc. term"),
    biol(1, "biology term", "biology"),
    chem(1, "chemistry term"),
    astron(1, "astronomy, etc. term", "astronomy"),
    bot(1, "botany term", "botany"),
    baseb(1, "baseball term", "baseball"),
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
    ok(2, "out-dated or obsolete kana usage", "outdated Kana"),
    oik(2, "old or irregular kana form", "old/irregular Kana"),
    on_mim(2, "onomatopoeic or mimetic word", "onomatopoeia"),
    poet(2, "poetical term", "poetical"),
    pol(2, "polite (teineigo) language", "polite"),
    rare(2, "rare"),
    sens(2, "sensitive word", "sensitive"),
    sl(2, "slang", "slang"),
    uK(2, "word usually written using kanji alone", "usu. Kanji"),
    uk(2, "word usually written using kana alone", "usu. Kana"),
    vulg(2, "vulgar expression or word"),
    kyb(3, "Kyoto-ben", "Kyoto-ben"),
    osb(3, "Osaka-ben", "Osaka-ben"),
    ksb(3, "Kansai-ben", "Kansai-ben"),
    ktb(3, "Kantou-ben", "Kantou-ben"),
    tsb(3, "Tosa-ben", "Tosa-ben"),
    thb(3, "Touhoku-ben", "Touhoku-ben"),
    tsug(3, "Touhoku-ben", "Touhoku-ben"),
    kyu(3, "Kyuushuu-ben", "Kyuushuu-ben"),
    rkb(3, "Ryuukyuu-ben", "Ryuukyuu-ben"),
    hob(3, "Ryuukyuu-ben", "Ryuukyuu-ben"),

    p(4, ""), P(4, "");
    private int group;
    private String name;
    private String shortName;
    private int shortBit = -1;
    DefTag(int group, String name)
    {
        this.group = group;
        this.name = name;
        this.shortName = null;
    }
    DefTag(int group, String name, String shortName)
    {
        this(group, name);
        this.shortName = shortName;
    }
    DefTag(int group, String name, int bitNum)
    {
        this.group = group;
        this.name = name;
        this.shortBit = 1 << bitNum;
    }
    DefTag(int group, String name, int bitNum, String shortName)
    {
        this(group, name, bitNum);
        this.shortName = shortName;
    }
    public String toString()
    {
        return shortName == null? name():shortName;
    }
    public String getFullName()
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
            return valueOf(text.replace('-', '_'));
        }catch(IllegalArgumentException e)
        {
            //System.out.println("WARN: unknown tag " + text);
            return other;//tag used for rare archaic entries I can't be bothered to add since we don't deal with them (yet)
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
