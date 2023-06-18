package dev.mieser.tsa.datetime.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.datetime.api.DateConverter;

@RequiredArgsConstructor
public class DateConverterImpl implements DateConverter {

    private final ZoneId zoneId;

    public ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

}
