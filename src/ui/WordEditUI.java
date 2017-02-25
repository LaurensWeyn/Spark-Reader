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
package ui;

import language.dictionary.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Used to edit words in the user dictionary
 * Created by Laurens on 2/25/2017.
 */
public class WordEditUI
{
    private JFrame frame;
    private JPanel mainPanel, topPanel, buttonPanel;
    private JLabel spellingLabel, tagLabel, defTextLabel;
    private JComboBox<WordOption> tagSelect;
    private JTextField spelling, reading, advancedTags;
    private JTextArea defText;
    private JButton saveButton, cancelButton;


    private static final String NO_SPELLING = "Spelling";
    private static final String NO_READING = "Reading (Kanji words only)";
    private static final String NO_TAGS = "Extra tags (advanced)";

    private Definition definition;

    public WordEditUI(UserDefinition toEdit)
    {
        definition = toEdit;
        buildUI(String.join(", ", toEdit.getSpellings()), String.join(", ", toEdit.getReadings()), "");
        defText.setText(toEdit.getMeaningRaw().replace('/','\n'));
        //spelling.setText();
        buildFrame("Editing " + toEdit.getSpellings()[0]);
    }

    public WordEditUI()
    {
        buildUI("", "", "");
        definition = new UserDefinition(DefSource.getSource("Custom"));
        buildFrame("Adding new word");
    }

    private void buildUI(String spellingText, String readingText, String tagText)
    {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        //upper section
        topPanel = new JPanel();
        LayoutManager manager = new BoxLayout(topPanel, BoxLayout.Y_AXIS);
        topPanel.setLayout(manager);

        spellingLabel = new JLabel("Spelling and reading:");
        spellingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(spellingLabel);

        spelling = new JTextField(spellingText);
        reading = new JTextField(readingText);
        spelling.setAlignmentX(Component.LEFT_ALIGNMENT);
        reading.setAlignmentX(Component.LEFT_ALIGNMENT);
        spelling.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e){}
            @Override
            public void keyPressed(KeyEvent e){}
            @Override
            public void keyReleased(KeyEvent e)
            {
                reading.setEnabled(Japanese.hasKanji(spelling.getText()));
            }
        });
        topPanel.add(new GhostText(spelling, NO_SPELLING).getTextfield());
        topPanel.add(new GhostText(reading, NO_READING).getTextfield());


        tagLabel = new JLabel("<html><br>Word type:");
        tagLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(tagLabel);
        tagSelect = new JComboBox<>(WordOption.values());
        tagSelect.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(tagSelect);
        advancedTags = new JTextField(tagText);
        advancedTags.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(new GhostText(advancedTags, NO_TAGS).getTextfield());

        defTextLabel = new JLabel("<html><br>Definition text:");
        defTextLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(defTextLabel);



        buttonPanel = new JPanel(new BorderLayout());
        saveButton = new JButton(new AbstractAction("Save")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

            }
        });
        cancelButton = new JButton(new AbstractAction("Cancel")
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {

            }
        });
        buttonPanel.add(saveButton, BorderLayout.EAST);
        buttonPanel.add(cancelButton, BorderLayout.WEST);
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0));

        defText = new JTextArea();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(defText, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

    }


    private void buildFrame(String title)
    {
        frame = new JFrame(title);
        frame.setContentPane(mainPanel);
        frame.setSize(300, 400);
        frame.setVisible(true);

        reading.setEnabled(Japanese.hasKanji(spelling.getText()));
    }

    private enum WordOption
    {
        noun(DefTag.n, "Noun"),
        v1(DefTag.v1),
        v5("Godan verb"),
        adj_i(DefTag.adj_i, "i adjective"),
        none("Other (specify below)");

        DefTag tag;
        String displayName;

        WordOption(String displayName)
        {
            tag = null;
            this.displayName = displayName;
        }
        WordOption(DefTag tag)
        {
            this.tag = tag;
            this.displayName = tag.toString();
        }
        WordOption(DefTag tag, String displayName)
        {
            this.tag = tag;
            this.displayName = displayName;
        }

        public DefTag getTag()
        {
            return tag;
        }

        @Override
        public String toString()
        {
            return displayName;
        }
    }

    public static void main(String[] args)
    {
        WordEditUI weui = new WordEditUI();
    }
}
