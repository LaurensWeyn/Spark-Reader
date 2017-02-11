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
package UI;

import Hooker.LineFinder;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Laurens Weyn
 */
public class LineSelectUI implements ListSelectionListener
{
    private final JFrame frame;
    
    private final JPanel mainPanel;
    private final JScrollPane matchScroll;
    private final JList<String> matchList;
    
    private final JPanel optionPanel;
    private final JLabel statusLabel;
    private final JTextField searchBox;

    private final JPanel buttonPanel;
    private final JButton btnThreadControl;
    private final JButton btnSelect;
    HashMap<String, ArrayList<Integer>> lineMapping;
    
    private State state;
    private final LineFinder finder;
    private final Runnable listener;
    
    private final static String DEFAULT_TEXT = "Please select what matches the game text below";
    public static void requestSelection(LineFinder finder, Runnable listener)
    {
        new Thread(() -> new LineSelectUI(finder, listener)).start();
    }
    private LineSelectUI(LineFinder finder, Runnable listener)
    {
        this.finder = finder;
        this.listener = listener;
        if(finder.hasAddresses())state = State.resolveConflict;
        else state = State.scanAllStopped;
        
        mainPanel = new JPanel(new BorderLayout());
        
        //options
        optionPanel = new JPanel();
        LayoutManager manager = new BoxLayout(optionPanel, BoxLayout.Y_AXIS);
        optionPanel.setLayout(manager);
        
        searchBox = new JTextField();
        statusLabel = new JLabel(DEFAULT_TEXT);
        optionPanel.add(statusLabel);
        //optionPanel.add(searchBox);
        mainPanel.add(optionPanel, BorderLayout.NORTH);
        
        //main list
        matchList = new JList<>(new LineModel(finder));
        matchList.addListSelectionListener(this);
        matchScroll = new JScrollPane(matchList);
        mainPanel.add(matchScroll, BorderLayout.CENTER);
        
        //lower buttons
        buttonPanel = new JPanel(new BorderLayout());
        btnThreadControl = new JButton(new AbstractAction("start scanning")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onThreadControl();
            }
        });
        if(state == State.resolveConflict)onThreadControl();//scan right now
        
        
        btnSelect = new JButton(new AbstractAction("select")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                onSelect();
            }
        });
        btnSelect.setEnabled(false);
        buttonPanel.add(btnSelect, BorderLayout.EAST);
        buttonPanel.add(btnThreadControl, BorderLayout.WEST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        frame = new JFrame("Find text thread");
        frame.setContentPane(mainPanel);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setSize(400,500);
        frame.setVisible(true);
    }
    private void onSelect()
    {
        finder.setValidText(matchList.getSelectedValue());//selection made
        frame.setVisible(false);//UI not needed
         new Thread(listener).start();//notify we're done
    }
    private void onThreadControl()
    {
        switch(state)
        {
            case scanAll:
                state = State.scanAllStopped;
                finder.setRunSearch(false);
                btnThreadControl.setText("Restart scan");
                ((LineModel)matchList.getModel()).update();//sometimes doesn't render correctly, force re-render
                break;
            case scanAllStopped:
                state = State.scanAll;
                finder.reset();
                matchList.setModel(new LineModel(finder));
                new Thread(finder).start();
                btnThreadControl.setText("stop scanning");
                break;
            case resolveConflict:
                btnThreadControl.setText("rescan");
                matchList.setModel(new LineModel(finder));
                finder.scan();
                break;
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        String select = matchList.getSelectedValue();
        btnSelect.setEnabled(select != null && state != State.scanAll);
        if(select != null && state != State.scanAll)
        {
            statusLabel.setText("Line has " + finder.getLineMapping(select).size() + " occurances");
        }
        else
        {
            statusLabel.setText(DEFAULT_TEXT);
        }
        
    }

    
    private enum State
    {
        scanAll,
        scanAllStopped,
        resolveConflict,
    }
    static class LineModel implements ListModel<String>
    {
        List<String> matches;
        public LineModel(LineFinder finder)
        {
            matches = new ArrayList<>();
            finder.addListener(new LineFinder.LineListener()
            {
                @Override
                public void newLine(String line)
                {
                    matches.add(line);
                    ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, matches.size() - 1, matches.size() - 1);
                    for(ListDataListener ldl:listeners)
                    {
                        ldl.intervalAdded(event);
                    }
                }
            });
        }
        @Override
        public int getSize()
        {
            return matches.size();
        }

        @Override
        public String getElementAt(int index)
        {
            return matches.get(index);
        }

        ArrayList<ListDataListener> listeners = new ArrayList<>();
        public void update()
        {
            ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
            for(ListDataListener ldl:listeners)
            {
                ldl.contentsChanged(event);
            }
        }
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

    }
    
}
