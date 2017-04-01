package ui;

import java.awt.*;
import java.util.Deque;
import java.util.LinkedList;

import static main.Main.options;

/**
 * Renders blocks of text on the overlay. Used for rendering definitions.
 * Allows for utilities like text capture and scrolling.
 * Created by Laurens on 3/3/2017.
 */
public class TextBlockRenderer
{
    private final boolean displayUpwards;
    private final int width;

    private int defLines;
    private int startLine = 0;

    private int capturePoint;
    private String capture;

    private Deque<TextBlock> blocks;

    public TextBlockRenderer(boolean displayUpwards, int width)
    {
        this.displayUpwards = displayUpwards;
        this.width = width;
        blocks = new LinkedList<>();
    }

    /**
     * Add a block of text
     * @param text the text to add
     * @param fore the color of the text
     * @param back the background colour of this text block
     */
    public void addText(String text, Color fore, Color back)
    {
        addText(new TextBlock(text, fore, back));
    }

    private void addText(TextBlock text)
    {
        if(displayUpwards) blocks.addFirst(text);
        else blocks.addLast(text);
    }

    /**
     * Render this set of text blocks, updating the capture in the process.
     * @param g The graphics to render at
     * @param x The starting X position (will take up X to X + width)
     * @param y the starting Y position
     */
    public void render(Graphics g, int x, int y)
    {
        defLines = 0;//will be recounted
        capture = "";//will be recaptured
        g.setColor(new Color(0,0,0,1));
        g.fillRect(x, y - g.getFontMetrics().getAscent() + (options.getOptionBool("defsShowUpwards")?1:0), width, 1);//let mouse move through 1 pixel space
        y++;//slight spacer
        if(!options.getOptionBool("defsShowUpwards"))y -= g.getFontMetrics().getHeight();

        for(TextBlock block: blocks)
        {
            y = renderLine(block, g, x, y);
        }
    }

    /**
     * Get the rendered width of this text block renderer
     * @return the width
     */
    public int getWidth()
    {
        return width;
    }

    /**
     * Sets the y coordinate for capturing text from this definition.
     * You can also set this to -1 to capture all text. Resets after render.
     * @param y the y-coordinate the mouse was at
     */
    public void setCapturePoint(int y)
    {
        this.capturePoint = y;
    }

    /**
     * Get text captured after setting the capture point and calling render().
     * @return The text set by the capture point
     */
    public String getCapturedText()
    {
        return capture;
    }

    private int renderLine(TextBlock text, Graphics g, int x, int y)
    {
        if(text.getText() == null)return y;//don't render null text
        int startY = y;
        FontMetrics font = g.getFontMetrics();
        TextStream stream = new TextStream(text.getText());
        StringBuilder line = new StringBuilder();
        Deque<String> lines = new LinkedList<>();
        while(!stream.isDone())
        {
            String nextBit = stream.nextWord();
            if(font.stringWidth(line + nextBit) > width)//too much for this line, wrap over
            {
                lines.add(line.toString());//add line for rendering
                line = new StringBuilder(nextBit.trim());//put word on new line
            }
            else
            {
                line.append(nextBit);
            }
        }
        if(!line.toString().equals(""))lines.add(line.toString());//add last line
        //draw lines
        while(!lines.isEmpty())
        {
            if(displayUpwards) line = new StringBuilder(lines.pollLast());
            else line = new StringBuilder(lines.pollFirst());
            //draw line
            defLines++;
            if(startLine <= defLines)
            {
                if(displayUpwards)y -= font.getHeight();
                if(!displayUpwards) y += font.getHeight();

                //print background
                g.setColor(text.getBackCol());
                g.fillRect(x, y - font.getAscent(), width, font.getHeight());

                //print text
                g.setColor(text.getTextCol());
                g.drawString(line.toString(), x, y);

            }
        }
        //'gap' between def lines
        g.setColor(new Color(0, 0, 0, 1));
        g.clearRect(x, y - font.getAscent() + (displayUpwards?0:font.getHeight() - 1), width,1);//clear this last line
        g.fillRect (x, y - font.getAscent() + (displayUpwards?0:font.getHeight() - 1), width,1);//add a dim line here so scrolling still works

        //capture if in this
        if(capturePoint <= -1 || (displayUpwards?
                (capturePoint <= startY - font.getHeight() + font.getDescent() && capturePoint > y - font.getHeight() + font.getDescent()):
                (capturePoint > startY + font.getDescent() && capturePoint <= y + font.getDescent())))
        {
            //TODO allow export with HTML color info perhaps?
            if(capture.equals(""))
            {
                capture = text.getText();
            }
            else
            {
                capture += "\n" + text;
            }
        }

        return y;
    }

    /**
     * Get the number of lines this definition has in total.
     * Only valid after having been rendered
     * @return the number of lines, -1 if we haven't rendered yet
     */
    public int getDefLines()
    {
        return defLines;
    }

    /**
     * Sets the position in the text that we are scrolled at.
     * Can be between 0 and the total number of lines.
     * @param pos position to scroll to.
     */
    public void setScrollPosition(int pos)
    {
        startLine = pos;
        if(startLine < 0)startLine = 0;
        else if(startLine > defLines)startLine = defLines;
    }

    /**
     * Find where we are currently scrolled
     * @return the line scrolled to
     */
    public int getScrollPosition()
    {
        return startLine;
    }

    /**
     * Scroll one line down if possible
     */
    public void scrollDown()
    {
        startLine = Math.min(startLine + 1, defLines - 2);
    }

    /**
     * Scroll one line up if possible
     */
    public void scrollUp()
    {
        startLine = Math.max(startLine - 1, 0);
    }

    /**
     * Class holding a "block" of definition text
     * Created by Laurens on 3/3/2017.
     */
    private static class TextBlock
    {
        private final String text;
        private final Color textCol, backCol;

        public TextBlock(String text, Color textCol, Color backCol)
        {
            this.text = text;
            this.textCol = textCol;
            this.backCol = backCol;
        }

        public String getText()
        {
            return text;
        }

        public Color getTextCol()
        {
            return textCol;
        }

        public Color getBackCol()
        {
            return backCol;
        }

        @Override
        public String toString()
        {
            return text;
        }
    }
}
