package language.dictionary;

/**
 * Created by Laurens on 2/19/2017.
 */
public class DefSource
{
    private int priority;
    private String name;

    public DefSource(int priority, String name)
    {
        this.priority = priority;
        this.name = name;
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
