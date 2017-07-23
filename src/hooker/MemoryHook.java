/*
 * Copyright (C) 2017 Laurens Weyn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hooker;

import com.sun.jna.Pointer;
import main.Main;
import ui.LineSelectUI;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Laurens on 2/9/2017.
 */
public class MemoryHook implements Hook
{

    private static final String START_MESSAGE = "Welcome to the memory text hook wizard."
            + "\nPlease enter the process ID (pid) of the game you want to hook. You can get this pid from the task manager."
            + "\nNote that this only works on Windows and when running this Spark Reader as Administrator";

    private static final String FAILED_MESSAGE = "Failed to find pid."
            + "\nPlease ensure that you are running Spark Reader as Administrator and that the pid was entered correctly";

    Pointer process;
    LineFinder lineFinder;
    private boolean resolved;
    public MemoryHook()
    {
        Component parent = null;
        if(Main.ui != null)parent = Main.ui.disp.getFrame();
        String response = JOptionPane.showInputDialog(parent, START_MESSAGE);
        if(response == null)return;
        process = null;
        try
        {
            int pid = Integer.parseInt(response);

            Kernel32 kernel32 = KernelController.getKernel32();

            process = kernel32.OpenProcess(KernelController.PROCESS_VM_READ|KernelController.PROCESS_VM_WRITE|KernelController.PROCESS_VM_OPERATION, true, pid);
        }catch(NumberFormatException ignored) {}

        if(process == null)
        {
            JOptionPane.showMessageDialog(parent, FAILED_MESSAGE);
            return;
        }

        lineFinder = new LineFinder(process);
        resolved = false;
        LineSelectUI.requestSelection(lineFinder, () -> resolved = true);
    }

    /**
     * Checks whether the hook was sucessfully started or not
     * @return false if cancelled by user or error, true otherwise
     */
    public boolean isRunning()
    {
        return process != null;
    }

    private String lastLine = "";

    public String check()
    {
        if(resolved)
        {

            String line = lineFinder.scan();
            if(line == null)
            {
                System.out.println("multiple found, resolving");
                resolved = false;
                LineSelectUI.requestSelection(lineFinder, () -> resolved = true);
            }
            else
            {
                if(!line.equals(lastLine))
                {
                    lastLine = line;
                    System.out.println("line updated to " + line);
                    return line;
                }
            }
        }
        return null;
    }

    /**
     * When false, the MemoryHook is waiting for user input to resolve the correct memory address
     * @return true if the hook is active, false if waiting for user
     */
    public boolean isResolved()
    {
        return resolved;
    }

    public void refine()
    {

    }

    //Old UI code reference:
    /*
    memoryHookStart = new JMenuItem(new AbstractAction("Start memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MemoryHook newHook = new MemoryHook();
                if(newHook.isRunning())
                {
                    Main.hook = newHook;
                }
            }
        });
        memoryHookStop = new JMenuItem(new AbstractAction("Stop memory hook")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                Main.hook = new ClipboardHook();
            }
        });
        memoryHookRefine = new JMenuItem((new AbstractAction("Rescan memory")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                MemoryHook hook = (MemoryHook) Main.hook;
                hook.refine();
            }
        }));
     */
}
