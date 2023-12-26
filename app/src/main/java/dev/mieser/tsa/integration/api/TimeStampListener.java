package dev.mieser.tsa.integration.api;

import dev.mieser.tsa.domain.TimeStampResponseData;

@FunctionalInterface
public interface TimeStampListener {

    void onResponse(TimeStampResponseData response);

}
