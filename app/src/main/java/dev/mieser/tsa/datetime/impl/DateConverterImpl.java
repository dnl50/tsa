package dev.mieser.tsa.datetime.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import dev.mieser.tsa.datetime.api.DateConverter;

public class DateConverterImpl implements DateConverter {

    public ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

}
