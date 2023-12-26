package dev.mieser.tsa.signing.impl.testutil;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.function.Supplier;

import lombok.Setter;

import dev.mieser.tsa.datetime.api.CurrentDateService;

/**
 * {@link CurrentDateService} stub to make tests more concise.
 */
@Setter
public class CurrentDateServiceStub implements CurrentDateService {

    private Supplier<ZonedDateTime> currentDateSupplier = ZonedDateTime::now;

    @Override
    public Date now() {
        return Date.from(currentDateSupplier.get().toInstant());
    }

}
