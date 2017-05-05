package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;

/**
 * Describes a printing resolution
 *
 * @see <a href="https://tools.ietf.org/html/rfc2911#section-4.1.15">RFC2911 Section 4.1.15</a>
 */
@AutoValue
public abstract class Resolution {

    public static Resolution of(int x, int y, Unit unit) {
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
        public static final Unit DotsPerInch = of("dpi", 3);
        public static final Unit DotsPerCentimeter = of("dpcm", 4);

        public static Unit of(String name, int code) {
            return new AutoValue_Resolution_Unit(name, code);
        }

        /** The encoder for converting integers to Operation objects */
        public static final NameCodeType.Encoder<Unit> ENCODER = NameCodeType.encoder(
                "unit", ImmutableSet.of(
                        DotsPerInch, DotsPerCentimeter
                ), new NameCode.Factory<Resolution.Unit>() {
                    @Override
                    public Resolution.Unit of(String name, int code) {
                        return Resolution.Unit.of(name, code);
                    }
                });
    }

    @Override
    public String toString() {
        return getX() + "x" + getY() + " " + getUnit().getName();
    }
}
