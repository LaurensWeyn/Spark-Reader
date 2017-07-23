package options;

import language.splitter.FoundDef;
import language.splitter.FoundWord;
import main.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
/**
 * Keeps track of a list of words the user wants to learn
 */
public class WantToLearn
{
    Known knownWords;
    HashSet<String> table;

    public WantToLearn(Known knownWords)
    {
        this.knownWords = knownWords;
        table = new HashSet<>();
    }

    /**
     * Adds all words in a file as words to learn
     * @param file the file to load
     * @throws IOException for file errors
     */
    public void addFile(File file)throws IOException
    {
        //Basically written to work with Thierry Bézecourt's JLPT files (along with simple user files)
        BufferedReader br = Utils.UTF8Reader(file);
        String line = br.readLine();
        int loadCount = 0;
        while(line != null)
        {
            if(line.length() > 0 && line.charAt(0) != '#')
            {
                for(String bit:line.split("[ \t]"))
                {
                    if(!knownWords.hasEntryFor(bit))
                    {
                        table.add(bit);
                        loadCount++;
                    }
                }
            }
            line = br.readLine();
        }
        System.out.println("Loaded " + loadCount + " target words from " + file.getName());
    }


    public boolean isWanted(FoundWord word)
    {
        if(word.getFoundDefs() == null)return false;

        for(FoundDef def:word.getFoundDefs())
        {
            if(table.contains(def.getDictForm()))
            {
                return true;
            }
        }
        return false;
    }
}
