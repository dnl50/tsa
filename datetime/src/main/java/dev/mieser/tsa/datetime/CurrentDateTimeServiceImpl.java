package dev.mieser.tsa.datetime;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;

import lombok.RequiredArgsConstructor;
import dev.mieser.tsa.datetime.api.CurrentDateTimeService;

@RequiredArgsConstructor
public class CurrentDateTimeServiceImpl implements CurrentDateTimeService {

    private final Clock clock;

    @Override
    public Date now() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return Date.from(now.toInstant());
    }

}
