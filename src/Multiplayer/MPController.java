package Multiplayer;

import UI.UI;

/**
 * Created by Laurens on 2/12/2017.
 */
public abstract class MPController implements Runnable
{
    /**
     * Value of byte used to check if connection is still available.
     * Ignored by packet reader
     */
    public static final int ALIVE_CODE = 5;//ASCII ENQ (Enquiry)
    public boolean running = true;

    public abstract String getStatusText();

    protected static String currentLine()
    {
        return UI.log.mostRecent().replace("\n", "\\n");
    }
    protected static int positionOf(String line)
    {
        return UI.log.linePos(line.replace("\\n","\n"));
    }
}
