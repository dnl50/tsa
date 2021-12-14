package dev.mieser.tsa.app;

import dev.mieser.tsa.signing.api.TimeStampAuthority;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * {@link InitializingBean} to {@link TimeStampAuthority#initialize() initialize} the {@link TimeStampAuthority}.
 */
@Component
@RequiredArgsConstructor
public class TsaInitializer implements InitializingBean {

    private final TimeStampAuthority timeStampAuthority;

    @Override
    public void afterPropertiesSet() {
        timeStampAuthority.initialize();
    }

}
