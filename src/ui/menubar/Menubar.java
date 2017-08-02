package ui.menubar;

import main.Main;
import ui.UI;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;

/**
 * Manages the menu bar on the main ui, for quick access to settings.
 */
public class Menubar
{
    ArrayList<MenubarItem> items = new ArrayList<>();
    MenubarItem selectedItem = null;
    public static MenubarItem ignoreNextClick = null;

    public void addItem(MenubarItem item)
    {
        item.getMenu().addPopupMenuListener(new PopupMenuListener()
        {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e){}
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
            {
                popupMenuCanceled(e);
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e)
            {
                if(selectedItem != null)
                {
                    ignoreNextClick = selectedItem;//if they chose to click on the menubar when closing the popup, don't open it again
                    selectedItem = null;
                    UI.tempIgnoreMouseExit = false;
                    Main.ui.render();
                }
            }
        });
        items.add(item);
    }

    public void render(Graphics2D g)
    {
        int x = 0;
        int itemSpacing = Main.options.getOptionInt("menubarOptionSpacing");
        Main.options.getFont(g, "furiFont");

        for(MenubarItem item:items)
        {
            g.setColor(Main.options.getColor(item == selectedItem? "clickedTextBackCol":"furiBackCol"));
            int width = item.getEndPos() - x;
            if(item.getEndPos() == -1)
            {
                width = g.getFontMetrics().stringWidth(item.getName()) + itemSpacing * 2;
                item.setStartAndEndPos(x, x + width);
            }
            g.fillRect(x, UI.furiganaStartY, width, UI.furiHeight);

            g.setColor(Main.options.getColor("furiCol"));
            g.drawString(item.getName(), x + itemSpacing, UI.furiganaStartY + g.getFontMetrics().getAscent());
            x += width;
        }
        //render the remainder of the bar
        g.setColor(Main.options.getColor("furiBackCol"));
        g.fillRect(x, UI.furiganaStartY, UI.currentWidth - x, UI.furiHeight);

        g.setColor(Main.options.getColor("furiCol"));
        //log progress
        int currentLine = Main.log.linePos(Main.currPage.getText());
        int logSize = Main.log.getSize() - 1;//don't count the current line as part of the 'backlog'
        String logProgress = currentLine + "/" + logSize;
        if(currentLine > 0)
        {
            g.drawString(logProgress, UI.minimiseStartX - g.getFontMetrics().stringWidth(logProgress), UI.furiganaStartY + g.getFontMetrics().getAscent());
        }
    }

    public void processClick(Point point)
    {

        if(point.y > UI.textStartY)return;
        for(MenubarItem item:items)
        {
            if(item.getEndPos() >= point.x && item.getStartPos() <= point.x)
            {
                if(ignoreNextClick == item)//ignore this one
                {
                    ignoreNextClick = null;
                    return;
                }

                if(selectedItem == item)selectedItem = null;
                else selectedItem = item;
                Main.ui.render();//render selected option
                //display menu
                break;
            }
        }
        if(selectedItem != null)
        {
            UI.tempIgnoreMouseExit = true;
            selectedItem.getMenu().show(Main.getParentFrame(), selectedItem.getStartPos(), UI.textStartY);
        }
    }


    /**
     * Gets a menu item by name. Used for updating elements after the menubar has been built.
     * @param menuName the name of the menu/category
     * @param elementName the name of the element in that menu
     * @return the component with the given name, or null if not found
     */
    public MenuElement getMenuItem(String menuName, String elementName)
    {
        for(MenubarItem menu:items)
        {
            if(menu.getName().equals(menuName))
            {
                return getSubMenuItem(menu.getMenu(), elementName);
            }
        }
        return null;
    }

    /**
     * Searches for a menu item by name. Recursively checks sub-menus.
     * @param menu the menu to search
     * @param elementName the element to search for
     * @return the element if found in this menu, null if not found
     */
    private MenuElement getSubMenuItem(MenuElement menu, String elementName)
    {
        if(elementName.equals(menu.getComponent().getName()))return menu;
        if(menu.getSubElements() != null)
        {
            for(MenuElement subItem:menu.getSubElements())
            {
                MenuElement found = getSubMenuItem(subItem, elementName);
                if(found != null)return found;
            }
        }
        return null;
    }

}
