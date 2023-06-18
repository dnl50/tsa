package dev.mieser.tsa.datetime.api;

import java.util.Date;

/**
 * Interface abstraction of a service returning the current date and time.
 */
public interface CurrentDateService {

    /**
     * @return The current date.
     */
    Date now();

}
