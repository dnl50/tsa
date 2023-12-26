package dev.mieser.tsa.datetime.impl;

import java.util.Date;

import dev.mieser.tsa.datetime.api.CurrentDateService;

/**
 * Abstraction layer for the current date.
 */
public class CurrentDateServiceImpl implements CurrentDateService {

    /**
     * @return The current date.
     */
    public Date now() {
        return new Date();
    }

}
