package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;

import java.net.URI;

@AutoValue
public abstract class IppPrinter {

    public static IppPrinter of(URI uri, AttributeGroup group) {
        return new AutoValue_IppPrinter(uri, group);
    }

    /**
     * The URI at which this printer is available
     */
    public abstract URI getUri();

    /**
     * The most recently retrieved attribute group ({@link com.hp.jipp.encoding.Tag#PrinterAttributes}) if any
     */
    public abstract AttributeGroup getAttributes();

    @Override
    public String toString() {
        Optional<String> info = getAttributes().getValue(Attributes.PrinterInfo);
        return "IppPrinter{uri=" + getUri() +
                (info.isPresent() ? ", name=" + info.get() : "") + "}";
    }
}
