package com.hp.jipp.encoding;

import org.junit.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.*;

public class DateTimeTypeTest {
    private DateTimeType type = new DateTimeType("date-time");

    @Test
    public void now() throws IOException {
        Calendar calendar = Calendar.getInstance();
        // Chop to nearest 100 millis because that's the only resolution we support
        calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND) / 100 * 100);
        // Set a timezone without a daylight savings bit because that really can't be encoded either
        calendar.setTimeZone(TimeZone.getTimeZone("GMT-8000"));

        Attribute<Calendar> dateTime = cycle(type, type.of(calendar));
        assertEquals(calendar.getTime(), dateTime.getValue().getTime());
    }
}
