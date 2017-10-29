/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package tests;

import org.junit.Test;
import static org.junit.Assert.*;
import static language.dictionary.Japanese.*;
/**
 *
 * @author Laurens Weyn
 */
public class JapaneseTest
{
    //these tests look stupid, but I've actually caught 2 errors from mistyping the hex codes.
    
    @Test
    public void testKanaDetect()
    {
        assertEquals("hiragana test", isHiragana('は'), true);
        assertEquals("hiragana test", isHiragana('ぼ'), true);
        assertEquals("hiragana test", isHiragana('を'), true);
        assertEquals("hiragana test", isHiragana('シ'), false);
        assertEquals("hiragana test", isHiragana('ヲ'), false);
        assertEquals("hiragana test", isHiragana('レ'), false);
        
        assertEquals("katakana test", isKatakana('は'), false);
        assertEquals("katakana test", isKatakana('ぼ'), false);
        assertEquals("katakana test", isKatakana('を'), false);
        assertEquals("katakana test", isKatakana('シ'), true);
        assertEquals("katakana test", isKatakana('ヲ'), true);
        assertEquals("katakana test", isKatakana('レ'), true);
    }
    
    @Test
    public void testKanjiDetect()
    {
        assertEquals("Kanji detect test", true, isKanji('何'));
        assertEquals("Kanji detect test", false, isKanji('Q'));
        assertEquals("Kanji detect test", false, isKanji('し'));
        assertEquals("Kanji detect test - Korean", false, isKanji('쾌'));
    }
    
    @Test
    public void testJapaneseDetect()
    {
        assertEquals("Japanese detect test", true, isJapanese('し'));
        assertEquals("Japanese detect test", true, isJapanese('赤'));
        assertEquals("Japanese detect test", true, isJapanese('ヵ'));
        assertEquals("Japanese detect test", false, isJapanese('A'));
        assertEquals("Japanese detect test - String", false, isJapanese("This is a test."));
        assertEquals("Japanese detect test - String", true, isJapanese("This is a test。"));
        assertEquals("Japanese detect test - String", true, isJapanese("これはテストです"));
        assertEquals("Japanese detect test - String", true, isJapanese("Some 漢字 mixed in English"));
        assertEquals("Japanese detect test - Other Unicode", false, isJapanese("하이드 의외로 유쾌해서 좋다ㅋㄱㅋ"));
        
    }
    
    @Test
    public void testKanaConvertion()
    {
        assertEquals("Hiragana to Katakana", "きょう", toHiragana("キョウ", false));
        assertEquals("Hiragana to Katakana", "きょう", toHiragana("キョウ", true));
        assertEquals("Hiragana to Katakana, stripping", "きょう", toHiragana("キョウ today", true));
        assertEquals("Hiragana to Katakana, stripping", "きょう today", toHiragana("キョウ today", false));
    }

    @Test
    public void testFullWidthConversion()
    {
        assertEquals("ａｅｓｔｈｅｔｉｃ", toFullWidth("aesthetic"));
        assertEquals("ＡＥＳＴＨＥＴＩＣ", toFullWidth("AESTHETIC"));
        assertEquals("１２３４５６７８９０", toFullWidth("1234567890"));
        assertEquals("ａｂｃ.　あいう。ｘｙｚ.１２３漢字", toFullWidth("abc. あいう。xyz.123漢字"));
    }

    @Test
    public void stripOkuriTest()
    {
        assertEquals("かんじ", stripOkurigana("漢字", "かんじ"));
        assertEquals("", stripOkurigana("ひらがな", "ひらがな"));
        assertEquals("あお", stripOkurigana("青い", "あおい"));
        assertEquals("あまのがわ", stripOkurigana("天の川", "あまのがわ"));
        assertEquals("にち", stripOkurigana("あ日い", "あにちい"));
        assertEquals("にち", stripOkurigana("日", "にち"));
        assertEquals("", stripOkurigana("", "にち"));
        assertEquals("", stripOkurigana("日", ""));
    }

    @Test
    public void onlyRomanTest()
    {
        assertTrue(isOnlyRoman("hello"));
        assertTrue(isOnlyRoman("World"));
        assertFalse(isOnlyRoman("ignore space"));
        assertFalse(isOnlyRoman("日"));
        assertFalse(isOnlyRoman("にち"));
    }
}
