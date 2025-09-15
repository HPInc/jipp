// © Copyright 2018 - 2020 HP Development Company, L.P.
// SPDX-License-Identifier: MIT

package com.hp.jipp.encoding;

import com.hp.jipp.model.Types;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;

import static com.hp.jipp.encoding.Cycler.cycle;
import static org.junit.Assert.assertEquals;

public class DateTimeTypeTest {

    @Test
    public void now() throws IOException {
        Calendar calendar = Calendar.getInstance();
        // Chop to nearest 100 millis because that's the only resolution we support
        calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND) / 100 * 100);
        // Set a timezone without a daylight savings bit because that really can't be encoded either
        calendar.setTimeZone(TimeZone.getTimeZone("GMT-8000"));

        Attribute<Calendar> dateTime = cycle(Types.jobHoldUntilTime, Types.jobHoldUntilTime.of(calendar));
        assertEquals(calendar.getTime(), dateTime.getValue().getTime());
    }
}
