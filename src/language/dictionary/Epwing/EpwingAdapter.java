/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package language.dictionary.Epwing;

import fuku.eb4j.EBException;
import fuku.eb4j.SubAppendix;
import fuku.eb4j.SubBook;
import fuku.eb4j.hook.HookAdapter;
import fuku.eb4j.util.ByteUtil;
import fuku.eb4j.util.HexUtil;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Modified DefaultHook from eb4j. translated/removed some useless
 * documentation, changes for Spark Reader def output
 */
public class EpwingAdapter extends HookAdapter<String[]>
{

    private int maxLines;
    private int lineNum = 0;

    List<String> defLines = new ArrayList<>();

    /**
     * half/full width character mode
     */
    private boolean narrow = false;

    /**
     * current line buffer
     */
    private StringBuilder lineBuffer = new StringBuilder(2048);

    /**
     * Ignore rest of line
     */
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

    public EpwingAdapter(SubBook sub, int maxLines, Set<Character> blackList)
    {
        super();
        appendix = sub.getSubAppendix();
        this.maxLines = maxLines;
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
     * @return true if more input is possible
     */
    @Override
    public boolean isMoreInput()
    {
        return lineNum < maxLines;
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
        if(narrow)
        {
            if(appendix != null)
            {
                try
                {
                    str = appendix.getNarrowFontAlt(code);
                }
                catch(EBException ignored)
                {
                }
            }
            if(StringUtils.isBlank(str))
            {
                switch(code)
                {
                    case 0xA235:
                        str = "ā";
                        break;
                    case 0xA14B:
                        str = "é";
                        break;
                    case 0xA236:
                        str = "ē";
                        break;
                    case 0xA226:
                        str = "ï";
                        break;
                    case 0xA237:
                        str = "ī";
                        break;
                    case 0xA238:
                        str = "ō";
                        break;
                    case 0xA239:
                        str = "ū";
                        break;

                    case 0xA568:
                        str = "〚";
                        break;
                    case 0xA569:
                        str = "〛";
                        break;

                    case 0xA135:
                    case 0xA136:
                        str = "";//ignored
                        break;
                    default:
                        System.out.println("Epwing adapter: unknown half width character " + HexUtil.toHexString(code));
                        str = "?";
                        break;
                }
            }
        }
        else
        {
            if(appendix != null)
            {
                try
                {
                    str = appendix.getWideFontAlt(code);
                }catch(EBException ignored){}
            }
            if(StringUtils.isBlank(str))
            {
                switch(code)
                {
                    case 0xB65E:
                    case 0xB571:
                        str = "▶";//all basically solid arrows
                        break;
                    case 0xB661:
                        str = "▷";
                        break;
                    case 0xB667:
                        str =  "Romaji: ";//'romaji' symbol
                        break;
                    case 0xB23D://weird start quote that's never closed
                    case 0xB66B://weird half-book thing
                    case 0xB66C://weird half-book thing as well
                        str = "";//ignore these
                        break;
                    default:
                        System.out.println("Epwing adapter: unknown full width character " + HexUtil.toHexString(code));
                        str = "?";
                        break;
                }
            }
        }
        addText(str);
    }
    private void addText(String text)
    {
        if(text.length() == 0)return;
        
        if(!ignoreLine && lineBuffer.length() == 0 && (blackList != null && blackList.contains(text.charAt(0))))
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
