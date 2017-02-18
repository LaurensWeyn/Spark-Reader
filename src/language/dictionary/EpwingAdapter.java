/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package language.dictionary;

import fuku.eb4j.EBException;
import fuku.eb4j.SubAppendix;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.HookAdapter;
import fuku.eb4j.util.ByteUtil;
import fuku.eb4j.util.HexUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Set;

/**
 * Modified DefaultHook from eb4j. translated/removed some useless
 * documentation, changes for Spark Reader def output
 */
public class EpwingAdapter extends HookAdapter<String[]>
{

    private int maxLine;
    ArrayList<String> defLines;
    /**
     * half/full width character mode
     */
    private boolean narrow = false;
    private int lineNum = 0;

    /**
     * current line buffer
     */
    private StringBuilder lineBuffer = new StringBuilder(2048);
    private boolean ignoreLine = false;

    private SubAppendix appendix = null;
    private Set<Character> blackList;
    public EpwingAdapter(SubBook sub)
    {
        this(sub, 500, null);
    }
    public EpwingAdapter(SubBook sub, Set<Character> blackList)
    {
        this(sub, 500, blackList);
    }

    public EpwingAdapter(SubBook sub, int maxLine, Set<Character> blackList)
    {
        super();
        appendix = sub.getSubAppendix();
        this.maxLine = maxLine;
        this.blackList = blackList;
    }

    /**
     * reinitialize all
     */
    @Override
    public void clear()
    {
        lineBuffer.delete(0, lineBuffer.length());
        defLines.clear();
        narrow = false;
        lineNum = 0;
    }

    @Override
    public String[] getObject()
    {
        return defLines.toArray(new String[defLines.size()]);
    }

    /**
     * Returns whether we can take more input.
     * @return 
     */
    @Override
    public boolean isMoreInput()
    {
        if (lineNum >= maxLine)
        {
            return false;
        }
        return true;
    }

    /**
     * append text.
     */
    @Override
    public void append(String str)
    {
        if (narrow)
        {
            str = ByteUtil.wideToNarrow(str);
        }
        addText(str);
    }

    /**
     * deal with foreign character codes not in UTF-16
     */
    @Override
    public void append(int code)
    {
        String str = null;
        if (narrow)
        {
            if (appendix != null)
            {
                try
                {
                    str = appendix.getNarrowFontAlt(code);
                } catch (EBException e)
                {
                }
            }
            if (StringUtils.isBlank(str))
            {
                //str = "[GAIJI=n" + HexUtil.toHexString(code) + "]";
                str = "?";
            }
        } else
        {
            if (appendix != null)
            {
                try
                {
                    str = appendix.getWideFontAlt(code);
                } catch (EBException e)
                {
                }
            }
            if (StringUtils.isBlank(str))
            {
                switch(code)
                {
                    case 0xB65E:
                        str = "▶";
                        break;
                    case 0xB667:
                        str =  "Romaji: ";
                        break;
                    default:
                        str = "[UNKNOWN CHAR 0x" + HexUtil.toHexString(code) + "]";
                        break;
                }
            }
        }
        addText(str);
    }
    private void addText(String text)
    {
        if(text.length() == 0)return;
        
        if(ignoreLine == false && lineBuffer.length() == 0 &&
          (blackList != null && blackList.contains(text.charAt(0))))
        {
            ignoreLine = true;
        }
        
        if(!ignoreLine)lineBuffer.append(text);
    }

    @Override
    public void beginNarrow()
    {
        narrow = true;
    }

    @Override
    public void endNarrow()
    {
        narrow = false;
    }

    /**
     * Start of new line, append buffer
     *
     */
    @Override
    public void newLine()
    {
        if(!ignoreLine)
        {
            defLines.add(lineBuffer.toString());
            lineBuffer.delete(0, lineBuffer.length());
        }
        
        ignoreLine = false;
        lineNum++;
    }
}
