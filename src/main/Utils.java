package main;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;

/**
 * Global utility methods that are too small to need their own class
 * Created by Laurens on 3/21/2017.
 */
public class Utils
{

    /**
     * Centers a window on the user's main monitor
     * @param window the window frame to center
     */
    public static void centerWindow(Window window)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(screenSize.width / 2 - window.getWidth() / 2,
                screenSize.height / 2 - window.getHeight() / 2);
    }

    /**
     * A really silly utility to count the number of lines in a file.
     * @param file the file to count lines in
     * @return the number of lines the file has, 0 if it does not exist
     */
    public static int countLines(File file)
    {
        try
        {
            BufferedReader br = Utils.UTF8Reader(file);
            int count = 0;
            while(br.readLine() != null)count++;
            return count;
        }catch(IOException ignore)
        {
            return 0;
        }

    }

    /**
     * Convenience function to read files in UTF-8
     * @param file the file to read
     * @return a BufferedReader set to read the given file as UTF-8
     * @throws IOException if file isn't found
     */
    public static BufferedReader UTF8Reader(File file)throws IOException
    {
        FileInputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
        return new BufferedReader(isr);
    }
}
