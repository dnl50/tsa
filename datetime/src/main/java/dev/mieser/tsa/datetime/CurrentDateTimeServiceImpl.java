package dev.mieser.tsa.datetime;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Date;

@RequiredArgsConstructor
public class CurrentDateTimeServiceImpl implements CurrentDateTimeService {

    private final Clock clock;

    @Override
    public Date now() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return Date.from(now.toInstant());
    }

}
