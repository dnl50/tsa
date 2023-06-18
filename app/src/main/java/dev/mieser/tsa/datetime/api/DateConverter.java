package dev.mieser.tsa.datetime.api;

import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Converts between legacy {@link Date}s and the Java Time API introduced in Java 8.
 */
public interface DateConverter {

    /**
     * @param date
     *     The date to convert.
     * @return The {@link ZonedDateTime} representation of the specified date.
     */
    ZonedDateTime toZonedDateTime(Date date);

}
