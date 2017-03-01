package freerunningapps.veggietizer.model;

import freerunningapps.veggietizer.model.util.Formatter;
import junit.framework.TestCase;

/**
 * @author Lukas Gebhard <freerunningapps@gmail.com>
 */
public class DateParserTest extends TestCase {
    public void testFormat() throws Exception {
        assertEquals(Formatter.format(0, "ml", "l", Formatter.KILO, 2), "0 ml");
        assertEquals(Formatter.format(9, "ml", "l", Formatter.KILO, 3), "9 ml");
        assertEquals(Formatter.format(9, "ml", "l", Formatter.KILO, 2), "10 ml");
        assertEquals(Formatter.format(9, "ml", "l", Formatter.KILO, -1), "9 ml");
        assertEquals(Formatter.format(6789789, "ml", "l", Formatter.KILO, 2), "6.789,79 l");
        assertEquals(Formatter.format(6789789.0f, "ml", "l", Formatter.KILO, 2), "6.789,79 l");
        assertEquals(Formatter.format(123.4567f, "dm2", "m2", Formatter.CENTI, 1), "1,2 m2");
    }

}