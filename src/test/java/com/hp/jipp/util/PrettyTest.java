package com.hp.jipp.util;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

import java.util.Arrays;

public class PrettyTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private Pretty.Printer printer;

    @Test
    public void converage() {
        // Create a useless object for coverage purposes
        new Pretty();
    }

    @Test
    public void oneLine() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 60);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test { XXX, YYY, ZZZ }", printer.print());
    }

    @Test
    public void multiLine() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 5);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test {\n  XXX,\n  YYY,\n  ZZZ }", printer.print());
    }

    @Test
    public void inner() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 60);
        printer.addAll(Arrays.asList("XXX", "YYY", "ZZZ"));
        printer.open(Pretty.ARRAY, "");
        printer.add(4, 5, 6);
        printer.close();
        assertEquals("Test { XXX, YYY, ZZZ, [ 4, 5, 6 ] }", printer.print());
    }

    @Test
    public void innerMulti() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 16);
        printer.add("XXX", "YYY", "ZZZ");
        printer.open(Pretty.ARRAY);
        printer.add(4, 5, 6);
        assertEquals("Test {\n  XXX,\n  YYY,\n  ZZZ,\n  [ 4, 5, 6 ] }", printer.print());
    }

    @Test
    public void compact() {
        //12345678901234567
        //Test { XXX, YYY,
        //  ZZZ}

        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 17);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test { XXX, YYY,\n  ZZZ }", printer.print());
    }

    @Test
    public void innerCompact() {
        //12345678901234567890
        //Test {
        //  Test { 1, 2, 3, 4,
        //    5 } }
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 20);
        printer.open(Pretty.OBJECT, "Test");
        printer.add(1, 2, 3, 4, 5);
        assertEquals("Test {\n  Test { 1, 2, 3, 4,\n    5 } }", printer.print());
    }

    @Test
    public void addToBuilt() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 16);
        printer.print();

        exception.expect(IllegalArgumentException.class);
        printer.add(5);
    }

    @Test
    public void unclosedClose() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 16);

        exception.expect(IllegalArgumentException.class);
        printer.close();
    }

    @Test
    public void closeAfterBuild() {
        printer = Pretty.printer("Test", Pretty.OBJECT, "  ", 16);
        printer.print();
        exception.expect(IllegalArgumentException.class);
        printer.close();
    }

    @Test
    public void keyValue() {
        printer = Pretty.printer("Test:", Pretty.KEY_VALUE, "  ", 30);
        printer.add("thing");
        assertEquals("Test: thing", printer.print());
    }
}
