/*
 * By Laurens Weyn
 * All rights reserved and stuff.
 * Not my fault if anything blows up.
 */
package Tests;

import Hooker.Log;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Laurens Weyn
 */
public class LogTest
{
    
    public LogTest()
    {
    }

    @Test
    public void testLinePointer()
    {
        Log l = new Log(4);
        l.addLine("A");
        l.addLine("B");
        l.addLine("C");
        assertEquals("first back", "B", l.back());
        assertEquals("last element", "A", l.back());
        assertEquals("fixed after last", "A", l.back());
        l.addLine("D");
        assertEquals("back to first after adding", "C", l.back());
        assertEquals("first forward", "D", l.forward());
        assertEquals("fixed after first", "D", l.forward());
        //go back to last element
        l.back();
        l.back();
        l.back();
        l.back();
        assertEquals("last element present", "A", l.back());
        l.addLine("E");
        l.back();
        l.back();
        l.back();
        l.back();
        l.back();
        assertEquals("last element removed from overflow", "B", l.back());
    }
    @Test
    public void testLogPosition()
    {
        Log l = new Log(4);
        l.addLine("A");
        l.addLine("B");
        l.addLine("C");
        assertEquals("first element is 0", 0, l.linePos("A"));
        assertEquals("next element is 1", 1, l.linePos("B"));
        l.addLine("D");
        assertEquals("none-existant elements are -1", -1, l.linePos("Q"));
        assertEquals("last element is 3", 3, l.linePos("D"));
        
    }
}
