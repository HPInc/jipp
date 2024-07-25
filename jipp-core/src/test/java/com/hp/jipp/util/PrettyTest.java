// Copyright 2017 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.util;

import java.util.Arrays;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class PrettyTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private PrettyPrinter printer;

    @Test
    public void oneLine() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 60);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test { XXX, YYY, ZZZ }", printer.print());
    }

    @Test
    public void multiLine() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 5);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test {\n  XXX,\n  YYY,\n  ZZZ }", printer.print());
    }

    @Test
    public void inner() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 60);
        printer.addAll(Arrays.asList("XXX", "YYY", "ZZZ"));
        printer.open(PrettyPrinter.ARRAY, "");
        printer.add(4, 5, 6);
        printer.close();
        assertEquals("Test { XXX, YYY, ZZZ, [ 4, 5, 6 ] }", printer.print());
    }

    @Test
    public void innerMulti() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 16);
        printer.add("XXX", "YYY", "ZZZ");
        printer.open(PrettyPrinter.ARRAY);
        printer.add(4, 5, 6);
        assertEquals("Test {\n  XXX,\n  YYY,\n  ZZZ,\n  [ 4, 5, 6 ] }", printer.print());
    }

    class NamedPrintable<T> implements PrettyPrintable {
        String name;
        T value;
        NamedPrintable(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public void print(@NotNull PrettyPrinter printer) {
            printer.open(PrettyPrinter.SILENT, name + " = ");
            printer.add(value);
        }
    }

    @Test
    public void namedInner() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 16);
        printer.open(PrettyPrinter.OBJECT, "AAA");
        printer.add(new NamedPrintable<>("BBB", 5));
        assertEquals("Test {\n  AAA {\n    BBB = 5 } }", printer.print());
    }

    @Test
    public void compact() {
        //12345678901234567
        //Test { XXX, YYY,
        //  ZZZ}

        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 17);
        printer.add("XXX", "YYY", "ZZZ");
        assertEquals("Test { XXX, YYY,\n  ZZZ }", printer.print());
    }

    @Test
    public void innerCompact() {
        //12345678901234567890
        //Test {
        //  Test { 1, 2, 3, 4,
        //    5 } }
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 20);
        printer.open(PrettyPrinter.OBJECT, "Test");
        printer.add(1, 2, 3, 4, 5);
        assertEquals("Test {\n  Test { 1, 2, 3, 4,\n    5 } }", printer.print());
    }

    @Test
    public void addToBuilt() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 16);
        printer.print();

        exception.expect(IllegalArgumentException.class);
        printer.add(5);
    }

    @Test
    public void unclosedClose() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 16);

        exception.expect(IllegalArgumentException.class);
        printer.close();
    }

    @Test
    public void closeAfterBuild() {
        printer = new PrettyPrinter("Test", PrettyPrinter.OBJECT, "  ", 16);
        printer.print();
        exception.expect(IllegalArgumentException.class);
        printer.close();
    }

    @Test
    public void keyValue() {
        printer = new PrettyPrinter("Test:", PrettyPrinter.KEY_VALUE, "  ", 30);
        printer.add("thing");
        assertEquals("Test: thing", printer.print());
    }
}
