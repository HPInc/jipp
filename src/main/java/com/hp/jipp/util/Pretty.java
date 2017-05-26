package com.hp.jipp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

/**
 * Simplifies the process of printing structured data
 */
public final class Pretty {
    private static final String NEWLINE = "\n";

    /** A style for arrays, e.g. "Me [ A, B, C ]" */
    public static final Style ARRAY = Style.of("[", "]", ",", " ");

    /** A style for objects, e.g. "Me { A, B, C }" */
    public static final Style OBJECT = Style.of("{", "}", ",", " ");

    /** A style for key/value pairs e.g. "Me A/B/C". Works best when there is only one value. */
    public static final Style KEY_VALUE = Style.of(" ", "", "/", "");

    /** A style used internally for the root group */
    private static final Style SILENT = Style.of("", "", ",", "");

    /** Style used for delimiting members of a pretty-printed group */
    public static final class Style {
        private final String opener;
        private final String closer;
        private final String separator;
        private final String spacer;

        private Style(String opener, String closer, String separator, String spacer) {
            this.opener = opener;
            this.closer = closer;
            this.separator = separator;
            this.spacer = spacer;
        }

        public static Style of(String opener, String closer, String separator, String spacer) {
            return new Style(opener, closer, separator, spacer);
        }
    }

    /** A group of objects currently being pretty-printed */
    private static final class Group {
        private final Style style;

        private final String prefix;

        private final int maxWidth;

        private final List<Object> items = new ArrayList<>();

        private Group(Style style, String prefix, int maxWidth) {
            this.style = style;
            this.prefix = prefix;
            this.maxWidth = maxWidth;
        }

        private int width() {
            int result = 0;
            if (!prefix.isEmpty()) {
                result += prefix.length() + style.spacer.length();
            }
            result += style.opener.length() + style.spacer.length();
            for (Object item : items) {
                result += item.toString().length();
            }
            result += (items.size() - 1) * style.separator.length() + style.spacer.length();
            result += style.spacer.length() + style.closer.length();
            return result;
        }

        private String compressed() {
            StringBuilder out = new StringBuilder();
            if (!prefix.isEmpty()) {
                out.append(prefix);
                out.append(style.spacer);
            }
            out.append(style.opener);
            out.append(style.spacer);
            out.append(Strings.join(style.separator + style.spacer, items));
            out.append(style.spacer);
            out.append(style.closer);
            return out.toString();
        }

        private String expanded(int startPos, String indent) {
            StringBuilder out = new StringBuilder();
            if (!prefix.isEmpty()) {
                out.append(prefix);
                out.append(style.spacer);
            }
            out.append(style.opener);

            // If all contained items are relatively short, use a semi-expanded form
            boolean compact = true;
            for (Object item : items) {
                if (item.toString().length() > maxWidth / 3) {
                    compact = false;
                    break;
                }
            }

            if (compact) {
                out.append(style.spacer);
                int curWidth = startPos + out.length();
                for (int i = 0; i < items.size(); i++) {
                    String itemString = items.get(i).toString();
                    if (i > 0) {
                        int expectedWidth = style.separator.length() + style.spacer.length() + itemString.length();
                        if (curWidth + expectedWidth > maxWidth) {
                            out.append(style.separator);
                            out.append(NEWLINE);
                            out.append(indent);
                            out.append(itemString);
                            curWidth = indent.length() + itemString.length();
                        } else {
                            out.append(style.separator);
                            out.append(style.spacer);
                            out.append(itemString);
                            curWidth += style.separator.length();
                            curWidth += style.spacer.length();
                            curWidth += itemString.length();
                        }
                    } else {
                        out.append(itemString);
                        curWidth += itemString.length();
                    }
                }
            } else {
                out.append(NEWLINE);
                out.append(indent);
                out.append(Strings.join(style.separator + NEWLINE + indent, items));
            }
            out.append(style.spacer);
            out.append(style.closer);
            return out.toString();
        }
    }

    /** An object that knows how to pretty-print itself */
    public interface Printable {
        /** Add a representation of self to the printer */
        void print(Printer printer);
    }

    /**
     * Return a new {@link Printer} object
     *
     * @param prefix prefix to appear before items
     * @param style display for items added
     * @param indent a single level of indent
     * @param maxWidth maximum width of a line before forcing grouped items each onto their own line
     */
    public static Printer printer(String prefix, Style style, String indent, int maxWidth) {
        return new Printer(prefix, style, indent, maxWidth);
    }

    /** Used to construct pretty-printed output of structured data */
    public static final class Printer {
        private final int mMaxWidth;

        private final String mIndent;

        private final Stack<Group> mGroups = new Stack<>();

        private Printer(String prefix, Style style, String indent, int maxWidth) {
            mIndent = indent;
            mMaxWidth = maxWidth;
            // Push a root group
            mGroups.push(new Group(SILENT, "", mMaxWidth));
            // Push the user's initial group (closed by print())
            mGroups.push(new Group(style, prefix, mMaxWidth));
        }

        /**
         * Open a new pretty-printed group of the specified style and prefix. Any items added will be appended to this
         * group, until {@link #close()} is called.
         */
        public Printer open(Style style, String prefix) {
            mGroups.push(new Group(style, prefix, mMaxWidth));
            return this;
        }

        /**
         * Open a new pretty-printed group of the specified style. Any items added will be appended to this group,
         * until {@link #close()} is called.
         */
        public Printer open(Style style) {
            return open(style, "");
        }

        /**
         * Closes the current group, falling back to the parent group. Any items added will appear in the previously
         * opened group.
         */
        public Printer close() {
            if (mGroups.size() < 3) throw new IllegalArgumentException("nothing open to close");
            if (mGroups.size() < 2) throw new IllegalArgumentException("close after print");
            innerClose();
            return this;
        }

        private void innerClose() {
            Group closed = mGroups.pop();
            int startPos = mGroups.size() * (mIndent.length() - 1);
            if (startPos + closed.width() < mMaxWidth) {
                innerAdd(closed.compressed());
            } else {
                innerAdd(closed.expanded(startPos, new String(new char[mGroups.size()]).replace("\0", mIndent)));
            }
        }

        /**
         * Add items to the current group
         */
        public <T> Printer addAll(Collection<T> items) {
            if (mGroups.size() < 2) throw new IllegalArgumentException("print already called");
            for (T item : items) {
                innerAdd(item);
            }
            return this;
        }

        /**
         * Add items to the current group
         */
        @SafeVarargs
        @SuppressWarnings("PMD.UnnecessaryFinalModifier") // SafeVarargs doesn't know class is final
        public final <T> Printer add(T... items) {
            if (mGroups.size() < 2) throw new IllegalArgumentException("print already called");
            for (T item : items) {
                innerAdd(item);
            }
            return this;
        }

        private <T> void innerAdd(T item) {
            Group group = mGroups.peek();
            if (item instanceof Printable) {
                ((Printable) item).print(this);
            } else {
                group.items.add(item);
            }
        }

        /**
         * Closes all open groups and builds a result. After making this call, no more items or groups can be added
         */
        public String print() {
            while (mGroups.size() > 1) innerClose();
            Group all = mGroups.peek();
            return all.items.get(0).toString();
        }
    }
}
