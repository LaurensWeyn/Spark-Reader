package main;

import javax.swing.*;
import java.awt.*;

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
}
