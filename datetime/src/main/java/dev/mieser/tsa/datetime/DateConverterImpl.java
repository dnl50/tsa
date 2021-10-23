package dev.mieser.tsa.datetime;

import dev.mieser.tsa.datetime.api.DateConverter;
import lombok.RequiredArgsConstructor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

@RequiredArgsConstructor
public class DateConverterImpl implements DateConverter {

    private final ZoneId zoneId;

    @Override
    public ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(), zoneId);
    }

}
