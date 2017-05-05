package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.util.Hook;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Associates a specific tag and name such that an attribute can be safely created or retrieved from a group
 */
public class AttributeType<T> {

    private final Attribute.BaseEncoder<T> encoder;
    private final Tag tag;
    private final String name;

    public AttributeType(Attribute.BaseEncoder<T> encoder, Tag tag, String name) {
        if (!(encoder.valid(tag) || Hook.is(Attribute.HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw new BuildError("Invalid tag " + tag + " for encoder " + encoder);
        }
        this.encoder = encoder;
        this.tag = tag;
        this.name = name;
    }

    /** Create an attribute of this attribute type with supplied values */
    @SafeVarargs
    public final Attribute<T> of(T... values) {
        return getEncoder().builder(getTag()).setValues(values).setName(getName()).build();
    }

    /** Create an attribute of this attribute type with supplied values */
    public final Attribute<T> of(List<T> values) {
        return getEncoder().builder(getTag()).setValues(values).setName(getName()).build();
    }

    public Attribute.BaseEncoder<T> getEncoder() {
        return encoder;
    }

    public Tag getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    /** Return true if the attribute has a matching encoder */
    boolean isValid(Attribute<?> attribute) {
        return attribute.getEncoder().equals(getEncoder());
    }

    /** If possible, convert the supplied attribute into an attribute of this type. */
    public Optional<Attribute<T>> from(Attribute<?> attribute) {
        return Optional.absent();
    }

    /** Return all accessible static members of the specified class which are AttributeType objects */
    public static List<AttributeType<?>> staticMembers(Class<?> cls) {
        ImmutableList.Builder<AttributeType<?>> members = new ImmutableList.Builder<>();
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers())) continue;

            Object object;
            try {
                object = field.get(null);
            } catch (IllegalAccessException ignored) {
                object = null;
            }
            if (object instanceof AttributeType<?>) {
                members.add((AttributeType<?>) object);
            }
        }
        return members.build();
    }
}
