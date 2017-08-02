package options.page;

import language.dictionary.*;
import main.Main;
import ui.WordEditUI;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;

/**
 * OptionPage to allow editing user definitions
 * Created by Laurens on 2/25/2017.
 * TODO move to different window?
 */
public class UserDefPage implements Page
{

    private JPanel mainPanel, buttonPanel;
    private JList<UserDefinition> definitionList;
    private DefListModel listModel;
    private JScrollPane defScroll;
    private DefSource source;
    public UserDefPage(DefSource source)
    {
        this.source = source;
        listModel = new DefListModel(source);
        definitionList = new JList<>(listModel);
        defScroll = new JScrollPane(definitionList);
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton(new AbstractAction("Add")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new WordEditUI();
            }
        });
        JButton editButton = new JButton(new AbstractAction("Edit")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                new WordEditUI(definitionList.getSelectedValue());
            }
        });
        JButton deleteButton = new JButton(new AbstractAction("Delete")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                for(UserDefinition definition:definitionList.getSelectedValuesList())
                {
                    Main.dict.removeDefinition(definition);
                    source.getDefinitions().remove(definition);
                    //TODO save these changes
                    listModel.update();
                }
            }
        });
        JButton saveButton = new JButton(new AbstractAction("Save changes")
        {
            @Override
            public void actionPerformed(ActionEvent act)
            {
                StringBuilder userDefs = new StringBuilder();
                userDefs.append("\n");
                for(Definition definition:source.getDefinitions())
                {
                    UserDefinition def = (UserDefinition)definition;
                    userDefs.append(String.join(";",def.getSpellingsRaw()));
                    userDefs.append(" ["); 
                    userDefs.append(String.join(";",def.getReadings()));
                    userDefs.append("] /");
                    for(DefTag tag:def.getTags())
                    {
                        if(tag == null) continue;
                        userDefs.append("(");
                        userDefs.append(tag.name());
                        userDefs.append(")");
                    }
                    userDefs.append(def.getMeaningRaw().replace("\n", "/"));
                    userDefs.append("/EntL");
                    userDefs.append(def.getID());
                    userDefs.append("/\n");
                }
                try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(Dictionary.userdictFilename), "UTF-8"))
                {
                    out.write(userDefs.toString());
                }
                catch (IOException e)
                { /* */ }
            }
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(saveButton);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(defScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
    }
    @Override
    public JComponent getComponent()
    {
        return mainPanel;
    }

    @Override
    public void update()
    {
        //TODO unload and reload user dictionary
    }

    private class DefListModel implements ListModel<UserDefinition>
    {
        DefSource source;
        DefListModel(DefSource source)
        {
            this.source = source;
        }
        @Override
        public int getSize()
        {
            return source.getDefinitions().size();
        }

        @Override
        public UserDefinition getElementAt(int index)
        {
            return (UserDefinition)source.getDefinitions().get(index);
        }
        ArrayList<ListDataListener> listeners = new ArrayList<>();
        @Override
        public void addListDataListener(ListDataListener l)
        {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l)
        {
            listeners.remove(l);
        }

        public void update()
        {
            //TODO don't be lazy on listDataEvent here
            ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            for(ListDataListener listener:listeners)
            {
                listener.contentsChanged(event);
            }
        }
    }

    @Override
    public String toString()
    {
        return /*"edit " + */source.getName().toLowerCase() + " dictionary";
    }
}
