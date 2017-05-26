package com.hp.jipp.client;

import com.google.auto.value.AutoValue;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.model.Attributes;

import java.net.URI;
import java.util.UUID;

@AutoValue
public abstract class Printer {

    public static Printer of(UUID uuid, URI uri, AttributeGroup group) {
        return new AutoValue_Printer(uuid, uri, group);
    }

    /**
     * The UUID by which this printer is known
     */
    public abstract UUID getUuid();

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

    /** Return the printer's "info" field or a blank string */
    public String getInfo() {
        String info = getAttributes().getValue(Attributes.PrinterInfo);
        return info == null ? "" : info;
    }

    @Override
    public String toString() {
        String info = getInfo();
        return "Printer{uri=" + getUri() + (info.isEmpty() ? "" : " info=" + info) + "}";
    }
}
