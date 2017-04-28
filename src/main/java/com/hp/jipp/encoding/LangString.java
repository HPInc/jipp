package com.hp.jipp.encoding;


import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.base.Optional;

/** A string, possibly encoded with language */
@AutoValue
public abstract class LangString {
    static Function<LangString, String> ToStringFunc = new Function<LangString, String>() {
        @Override
        public String apply(LangString input) {
            return input.getString();
        }
    };

    static Function<String, LangString> FromStringFunc = new Function<String, LangString>() {
        @Override
        public LangString apply(String input) {
            return of(input);
        }
    };

    public static LangString of(String string, String language) {
        return new AutoValue_LangString(string, Optional.of(language));
    }

    public static LangString of(String string) {
        return new AutoValue_LangString(string, Optional.<String>absent());
    }

    abstract public String getString();
    abstract public Optional<String> getLang();

    @Override
    public String toString() { return "\"" + getString() + "\" of " +
            (getLang().isPresent() ? getLang().get() : "?");
    }
}
