package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/**
 * Describes a printing resolution
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-4.1.15">RFC2911 Section 4.1.15</a>
 */
@AutoValue
abstract public class Resolution {

    public static Resolution create(int x, int y, Unit unit) {
        return new AutoValue_Resolution(x, y, unit);
    }

    /** Return the cross-feed (or X) resolution */
    public abstract int getCrossFeedResolution();

    /** Return the feed (or Y) resolution */
    public abstract int getFeedResolution();

    /** Return the unit of measurement for either feed resolution */
    public abstract Unit getUnit();

    public int getX() {
        return getCrossFeedResolution();
    }

    public int getY() {
        return getFeedResolution();
    }

    @AutoValue
    public abstract static class Unit extends NameCode {
        public final static Unit DotsPerInch = create("dpi", 3);
        public final static Unit DotsPerCentimeter = create("dpcm", 4);

        public static Unit create(String name, int code) {
            return new AutoValue_Resolution_Unit(name, code);
        }

        /** The encoder for converting integers to Operation objects */
        public final static EnumType.Encoder<Resolution.Unit> ENCODER = EnumType.encoder(
                "operation-id", ImmutableSet.of(
                        DotsPerInch, DotsPerCentimeter
                ), new NameCode.Factory<Resolution.Unit>() {
                    @Override
                    public Resolution.Unit create(String name, int code) {
                        return Resolution.Unit.create(name, code);
                    }
                });
    }

    @Override
    public String toString() {
        return getX() + "x" + getY() + getUnit().getName();
    }
}
