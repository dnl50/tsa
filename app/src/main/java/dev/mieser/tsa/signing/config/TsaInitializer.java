package dev.mieser.tsa.signing.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import lombok.RequiredArgsConstructor;

import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
@RequiredArgsConstructor
public class TsaInitializer {

    private final TimeStampAuthority timeStampAuthority;

    private final TimeStampValidator timeStampValidator;

    void onStartup(@Observes StartupEvent startupEvent) {
        timeStampAuthority.initialize();
        timeStampValidator.initialize();
    }

}
