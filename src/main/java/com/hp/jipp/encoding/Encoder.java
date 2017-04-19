package com.hp.jipp.encoding;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.hp.jipp.Hook;
import com.hp.jipp.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Reads/writes attributes and values according to the attribute's type.
 */
abstract class Encoder<T> {
    /** Read a single value from the input stream */
    abstract T readValue(DataInputStream in, Tag valueTag) throws IOException;

    /** Write a single value to the output stream */
    abstract void writeValue(DataOutputStream out, T value) throws IOException;

    /** Return true if this tag can be handled by this encoder */
    abstract boolean valid(Tag valueTag);

    /** Reads a two-byte length field, asserting that it is of a specific length */
    void expectLength(DataInputStream in, int length) throws IOException {
        int readLength = in.readShort();
        if (readLength != length) {
            throw new ParseError("expected " + length + " but got " + readLength);
        }
    }

    /**
     * Return a new Attribute builder for the specified valueTag (assumes a valid valueTag).
     * @param valueTag value-tag for attributes that can be built for the returned builder.
     *                 Throws if not a known tag for this encoder.
     */
    Attribute.Builder<T> builder(Tag valueTag) {
        if (!(valid(valueTag) || Hook.is(Attribute.HOOK_ALLOW_BUILD_INVALID_TAGS))) {
            throw new BuildError(valueTag.toString() + " is not a valid tag for " + this);
        }
        return Attribute.builder(this, valueTag);
    }

    /** Read an attribute and its values from the data stream */
    Attribute<T> read(DataInputStream in, Tag valueTag) throws IOException {
        Attribute.Builder<T> builder = builder(valueTag)
                .setName(new String(readValueBytes(in), Util.UTF8));

        // Read first value...there always has to be one, right?
        ImmutableList.Builder<T> valueBuilder = new ImmutableList.Builder<>();
        valueBuilder.add(readValue(in, valueTag));

        Optional<T> value;
        while ((value = readAdditionalValue(in, valueTag)).isPresent()) {
            valueBuilder.add(value.get());
        }
        builder.setValues(valueBuilder.build());
        return builder.build();
    }

    /** Read a single additional value into the builder, returning true if more */
    private Optional<T> readAdditionalValue(DataInputStream in, Tag valueTag) throws IOException {
        if (in.available() < 3) return Optional.absent();
        in.mark(3);
        if (Tag.read(in) == valueTag) {
            int nameLength = in.readShort();
            if (nameLength == 0) {
                return Optional.of(readValue(in, valueTag));
            }
        }
        // Failed to read an additional value so back up and quit.
        in.reset();
        return Optional.absent();
    }

    /** Write a length-value tuple */
    void writeValueBytes(DataOutputStream out, byte[] bytes) throws IOException {
        out.writeShort(bytes.length);
        out.write(bytes);
    }

    /** Read and return value bytes from a length-value pair */
    byte[] readValueBytes(DataInputStream in) throws IOException {
        int valueLength = in.readShort();
        byte valueBytes[] = new byte[valueLength];
        if (valueLength != in.read(valueBytes)) throw new ParseError("Value too short");
        return valueBytes;
    }

    /** Skip (discard) a length-value pair */
    void skipValueBytes(DataInputStream in) throws IOException {
        int valueLength = in.readShort();
        if (valueLength != in.skip(valueLength)) throw new ParseError("Value too short");
    }

}
