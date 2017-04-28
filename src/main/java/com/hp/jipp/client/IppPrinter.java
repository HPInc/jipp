package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;

import java.net.URI;
import java.util.List;

@AutoValue
public abstract class IppPrinter {

    public static IppPrinter of(List<URI> uris) {
        return new AutoValue_IppPrinter(uris, AttributeGroup.empty());
    }

    public static IppPrinter of(List<URI> uris, AttributeGroup group) {
        return new AutoValue_IppPrinter(uris, group);
    }

    /**
     * The URI at which this printer is available
     */
    public abstract List<URI> getUris();

    /**
     * The most recently retrieved attribute group ({@link com.hp.jipp.encoding.Tag#PrinterAttributes}) or
     * an empty attribute group.
     */
    public abstract AttributeGroup getAttributes();

    @Override
    public String toString() {
        Optional<String> info = getAttributes().getValue(Attributes.PrinterInfo);
        return "IppPrinter{uris=" + getUris() +
                (info.isPresent() ? ", name=" + info.get() : "") + "}";
    }
}
