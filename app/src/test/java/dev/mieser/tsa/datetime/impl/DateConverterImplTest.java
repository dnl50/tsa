package dev.mieser.tsa.datetime.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

class DateConverterImplTest {

    private final DateConverterImpl testSubject = new DateConverterImpl();

    @Test
    void convertsDateWithSystemDefaultZone() {
        // given
        ZonedDateTime expectedZonedDateTime = LocalDateTime.parse("2023-06-21T20:39:32").atZone(ZoneId.systemDefault());
        Date givenDate = Date.from(expectedZonedDateTime.toInstant());

        // when
        ZonedDateTime actual = testSubject.toZonedDateTime(givenDate);

        // then
        assertThat(actual).isEqualTo(expectedZonedDateTime);
    }

}
