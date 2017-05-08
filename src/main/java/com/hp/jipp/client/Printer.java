package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.google.common.base.Optional;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;
import com.hp.jipp.model.MediaSize;

import java.net.URI;
import java.util.List;

@AutoValue
public abstract class Printer {

    public static Printer of(URI uri, AttributeGroup group) {
        return new AutoValue_Printer(uri, group);
    }

    /**
     * The URI at which this printer is available
     */
    public abstract URI getUri();

    /**
     * The most recently retrieved attribute group ({@link com.hp.jipp.encoding.Tag#PrinterAttributes}) if any
     */
    public abstract AttributeGroup getAttributes();

    // List<MediaSize> getMediaSizesSupported();

    // List<MediaSize> getMediaSizeReady();

    @Override
    public String toString() {
        Optional<String> info = getAttributes().getValue(Attributes.PrinterInfo);
        return "Printer{uri=" + getUri() +
                (info.isPresent() ? ", name=" + info.get() : "") + "}";
    }
}
