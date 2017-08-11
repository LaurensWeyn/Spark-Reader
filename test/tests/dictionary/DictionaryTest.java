package tests.dictionary;

import language.dictionary.DefSource;
import language.dictionary.Dictionary;
import language.dictionary.JMDict.JMParser;
import org.junit.Assert;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
/**
 * Provides a dictionary for use in test functions.<br>
 * This dictionary should be treated as READ ONLY - tests that need to test writing changes should load a dictionary independently.<br>
 * This dictionary is shared across tests for efficiency.
 */
public abstract class DictionaryTest
{
    private static Dictionary globalDict = null;

    protected Dictionary dict;
    @Before
    public void loadDict()
    {
        dict = getDict();
    }


    protected static Dictionary getDict()
    {
        if(globalDict == null)
        {
            try
            {
                globalDict = new Dictionary();
                JMParser.parseJMDict(new File("dictionaries/JMdict"), globalDict, DefSource.getSource("edict"));
            }catch(IOException err)
            {
                Assert.fail("Cannot load test data: " + err);
            }
        }
        return globalDict;
    }
}
