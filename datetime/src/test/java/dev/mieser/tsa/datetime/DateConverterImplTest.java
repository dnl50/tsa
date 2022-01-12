package dev.mieser.tsa.datetime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

class DateConverterImplTest {

    private final DateConverterImpl testSubject = new DateConverterImpl(ZoneId.of("UTC"));

    @Test
    void toZonedDateTimeReturnsNullWhenDateIsNull() {
        // given / when
        ZonedDateTime convertedZonedDateTime = testSubject.toZonedDateTime(null);

        // then
        assertThat(convertedZonedDateTime).isNull();
    }

    @Test
    void toZonedDateTimeReturnsExpectedZoneDateTime() {
        // given
        ZonedDateTime zonedDateTime = ZonedDateTime.parse("2021-11-13T17:43:30Z");
        Date date = Date.from(zonedDateTime.toInstant());

        // when
        ZonedDateTime convertedZonedDateTime = testSubject.toZonedDateTime(date);

        // then
        assertThat(convertedZonedDateTime).isEqualTo(zonedDateTime);
    }

}
