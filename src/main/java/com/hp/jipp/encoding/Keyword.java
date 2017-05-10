package com.hp.jipp.encoding;

/**
 * A known sequence of characters.
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-4.1.3">RFC2911 Section 4.1.3</a>
 */
public abstract class Keyword {
    public abstract String getName();

    /** A factory for Keyword objects */
    public interface Factory<T extends Keyword> {
        T of(String name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
