package com.hp.jipp.encoding;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;

import javax.annotation.Nonnull;

/** A string, possibly encoded with language */
@AutoValue
public abstract class LangString {
    static final Function<LangString, String> ToStringFunc = new Function<LangString, String>() {
        @Override
        public String apply(@Nonnull LangString input) {
            return input.getString();
        }
    };

    static final Function<String, LangString> FromStringFunc = new Function<String, LangString>() {
        @Override
        public LangString apply(@Nonnull String input) {
            return of(input);
        }
    };

    public static LangString of(String string, String language) {
        return new AutoValue_LangString(string, Optional.of(language));
    }

    public static LangString of(String string) {
        return new AutoValue_LangString(string, Optional.<String>absent());
    }

    public abstract String getString();

    public abstract Optional<String> getLang();

    @Override
    public String toString() {
        Optional<String> lang = getLang();
        return "\"" + getString() + "\" of " + (lang.isPresent() ? lang.get() : "?");
    }
}
