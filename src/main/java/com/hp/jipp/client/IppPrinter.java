package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.Packet;
import com.hp.jipp.encoding.Tag;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.util.Nullable;

import java.io.IOException;
import java.net.URI;

@AutoValue
public abstract class IppPrinter {

    public static IppPrinter of(URI uri) {
        return new AutoValue_IppPrinter(uri, null);
    }

    /**
     * The URI at which this printer is available
     */
    public abstract URI getUri();

    /**
     * The most recently retrieved attribute group ({@link com.hp.jipp.encoding.Tag#PrinterAttributes}), if any
     */
    @Nullable
    public abstract AttributeGroup getAttributes();

    /** Return a matching object containing the specified attributes */
    IppPrinter withResponse(Packet response) throws IOException {
        Optional<AttributeGroup> attributes = response.getAttributeGroup(Tag.PrinterAttributes);
        if (!attributes.isPresent()) throw new IOException("No printer attributes from " + getUri());
        if (Objects.equal(attributes, Optional.fromNullable(getAttributes()))) return this;
        return new AutoValue_IppPrinter(getUri(), attributes.get());
    }

    @Override
    public String toString() {
        AttributeGroup group = getAttributes();
        Optional<String> info = group == null ? Optional.<String>absent() : group.getValue(Attributes.PrinterInfo);
        return "IppPrinter{url=" + getUri() +
                (info.isPresent() ? "name=" + info.get() : "") + "}";
    }
}
