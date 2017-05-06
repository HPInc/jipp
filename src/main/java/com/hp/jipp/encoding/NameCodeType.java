package com.hp.jipp.encoding;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.List;

import javax.annotation.Nonnull;

public class NameCodeType<T extends NameCode> extends AttributeType<T> {

    /** Constructs a new NameCodeType based on the supplied encoder and name */
    public static <T extends NameCode> NameCodeType<T> typeOf(NameCodeEncoder<T> encoder, String name) {
        return new NameCodeType<>(encoder, name);
    }

    private final Function<Integer, T> toNameCode = new Function<Integer, T>() {
        @Override
        public T apply(@Nonnull Integer input) {
            return ((NameCodeEncoder<T>) getEncoder()).get(input);
        }
    };

    public NameCodeType(NameCodeEncoder<T> encoder, String name) {
        super(encoder, Tag.EnumValue, name);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public Optional<Attribute<T>> of(Attribute<?> attribute) {
        if (attribute.getValueTag() != Tag.EnumValue) return Optional.absent();
        return Optional.of(of(Lists.transform((List<Integer>) attribute.getValues(), toNameCode)));
    }
}
