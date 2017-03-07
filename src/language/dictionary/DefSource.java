package language.dictionary;

import main.Main;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages sources of definitions
 * Created by Laurens on 2/19/2017.
 */
public class DefSource
{
    private int priority;
    private String name;
    private List<Definition> definitions;

    private static List<DefSource> sources = new LinkedList<>();

    /**
     * Gets a source to attach definitions to
     * @param sourceName the name of the source to look for
     * @return the DefSource with that name, generated if not found
     */
    public static DefSource getSource(String sourceName)
    {
        for(DefSource source:sources)
        {
            if(source.getName().equals(sourceName))return source;
        }
        //reach here, source does not yet exist, build it

        //editable sources should store used definitions
        boolean storeDefs = sourceName.toLowerCase().contains("user") || sourceName.toLowerCase().contains("custom");
        int priority = 0;
        try
        {
            priority = Main.options.getOptionInt(sourceName.toLowerCase().replace(" ", "") + "SourcePriority");
        }catch(IllegalArgumentException | NullPointerException ignored)
        {
            System.out.println("WARN: source priority for " + sourceName + " not known, assuming 0");
        }
        DefSource newSource = new DefSource(priority, sourceName, storeDefs);
        sources.add(newSource);
        return newSource;
    }

    private DefSource(int priority, String name, boolean storeDefinitions)
    {
        this.priority = priority;
        this.name = name;
        if(storeDefinitions)definitions = new ArrayList<>();
    }

    public void attach(Definition definition)
    {
        if(definitions != null)definitions.add(definition);
    }

    /**
     * Get all definitions assigned to this DefSource
     * @return a list of definitions, or null if this is not tracked by this DefSource
     */
    public List<Definition> getDefinitions()
    {
        return definitions;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
