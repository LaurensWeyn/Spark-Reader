package tests.dictionary;

import language.deconjugator.ValidWord;
import language.dictionary.Definition;
import language.dictionary.JMDict.Spelling;
import language.dictionary.Japanese;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class JMDictTest extends DictionaryTest
{

    @Test
    public void testNonsenseSearch()
    {
        assertNull(dict.find("あああああああああああ"));
    }

    @Test
    public void testBasicProperties()
    {
        List<Definition> defs = dict.find("在る");
        assertEquals(1, defs.size());
        Definition def = defs.get(0);
        //def.
        //TODO actually test stuff here
    }

    @Test
    public void testAshitaFurigana()
    {
        ValidWord word = new ValidWord("明日");
        List<Definition> defs = dict.find(word.getWord());
        assertEquals(1, defs.size());
        Definition def = defs.get(0);
        assertEquals("あした", def.getFurigana(word));
    }

    @Test
    public void testDictionaryStructure()
    {
        int single = 0;
        int total = 0;
        for(Object value:dict.getInternalLookup().values())
        {
            if(value instanceof Definition)single++;
            else
            {
                List valueList = (List)value;
                if(valueList.size() <= 1) Assert.fail("Empty list/list of 1 item should not be present in dictionary data structure");
                for(Object subValue:valueList)
                {
                    assertNotNull("Null object found in JMDict dictionary", subValue);
                }
            }
            total++;
        }
        double ratio = (double)single / (double)total;
        System.out.println("singletons: " + single + " / " + total);
        System.out.println("Singleton ratio: " + ratio);
        assertTrue(ratio > 0);
    }
}
