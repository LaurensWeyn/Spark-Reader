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
            FileInputStream is = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            int count = 0;
            while(br.readLine() != null)count++;
            return count;
        }catch(IOException ignore)
        {
            return 0;
        }

    }
}
