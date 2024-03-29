package ui.menubar;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MenubarItem
{
    private String name;
    private JPopupMenu menu = new JPopupMenu();
    private int startPos = -1, endPos = -1;


    public MenubarItem(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    int getStartPos()
    {
        return startPos;
    }
    int getEndPos()
    {
        return endPos;
    }

    void setStartAndEndPos(int startPos, int endPos)
    {
        this.startPos = startPos;
        this.endPos = endPos;
    }



    public JPopupMenu getMenu()
    {
        return menu;
    }

    public void addMenuItem(Action a, String name)
    {
        addMenuItem(a, true, name);
    }

    public void addMenuItem(Action a, boolean enabled, String name)
    {
        JMenuItem item = new JMenuItem(a);
        item.setEnabled(enabled);
        item.setName(name);
        menu.add(item);
    }

    public void addMenuItem(JMenuItem item)
    {
        menu.add(item);
    }

    public void addSpacer()
    {
        menu.add(new JSeparator());
    }

    public void hide()
    {
        menu.setVisible(false);
    }
}
