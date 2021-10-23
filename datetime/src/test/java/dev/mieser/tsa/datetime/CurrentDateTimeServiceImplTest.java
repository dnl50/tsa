package dev.mieser.tsa.datetime;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentDateTimeServiceImplTest {

    @Test
    void nowReadsTimeFromClock() {
        // given
        ZonedDateTime now = ZonedDateTime.parse("2021-11-13T17:46:51Z");
        Clock fixedClock = Clock.fixed(now.toInstant(), ZoneId.of("UTC"));

        var testSubject = new CurrentDateTimeServiceImpl(fixedClock);

        // when
        Date nowAsDate = testSubject.now();

        // then
        assertThat(ZonedDateTime.ofInstant(nowAsDate.toInstant(), ZoneId.of("UTC"))).isEqualTo(now);
    }

}
