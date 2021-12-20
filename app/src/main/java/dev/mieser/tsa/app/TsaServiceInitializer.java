package dev.mieser.tsa.app;

import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Spring {@link InitializingBean} to initialize the {@link TimeStampAuthority} and {@link TimeStampValidator}.
 */
@Component
@RequiredArgsConstructor
public class TsaServiceInitializer implements InitializingBean {

    private final TimeStampAuthority timeStampAuthority;

    private final TimeStampValidator timeStampValidator;

    @Override
    public void afterPropertiesSet() {
        timeStampAuthority.initialize();
        timeStampValidator.initialize();
    }

}
