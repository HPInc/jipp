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
import java.util.List;

@AutoValue
public abstract class IppPrinter {

    public static IppPrinter of(List<URI> uris) {
        return new AutoValue_IppPrinter(uris, null);
    }

    public static IppPrinter of(List<URI> uris, AttributeGroup group) {
        return new AutoValue_IppPrinter(uris, group);
    }

    /**
     * The URI at which this printer is available
     */
    public abstract List<URI> getUris();

    /**
     * The most recently retrieved attribute group ({@link com.hp.jipp.encoding.Tag#PrinterAttributes}), if any
     */
    @Nullable
    public abstract AttributeGroup getAttributes();

    @Override
    public String toString() {
        AttributeGroup group = getAttributes();
        Optional<String> info = group == null ? Optional.<String>absent() : group.getValue(Attributes.PrinterInfo);
        return "IppPrinter{uris=" + getUris() +
                (info.isPresent() ? "name=" + info.get() : "") + "}";
    }
}
