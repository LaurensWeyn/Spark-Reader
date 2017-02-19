package hooker;

import com.sun.jna.Pointer;

/**
 * Created by Laurens on 2/18/2017.
 */
public class APITests
{
    public static void main(String[] args)throws Exception
    {
        System.out.println(KernelController.findAllWindows(true));
        Thread.sleep(1000);
        Pointer window = KernelController.getFocusedWindow();
        System.out.println("Focused window: " + KernelController.getWindowName(window));
        while(true)
        {
            System.out.println(KernelController.getWindowArea(window));
            Thread.sleep(100);
        }
    }


}
