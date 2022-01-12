package dev.mieser.tsa.app;

import static org.mockito.BDDMockito.then;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;

@ExtendWith(MockitoExtension.class)
class TsaServiceInitializerTest {

    private final TimeStampAuthority timeStampAuthorityMock;

    private final TimeStampValidator timeStampValidatorMock;

    private final TsaServiceInitializer testSubject;

    TsaServiceInitializerTest(@Mock TimeStampAuthority timeStampAuthorityMock, @Mock TimeStampValidator timeStampValidatorMock) {
        this.timeStampAuthorityMock = timeStampAuthorityMock;
        this.timeStampValidatorMock = timeStampValidatorMock;
        this.testSubject = new TsaServiceInitializer(timeStampAuthorityMock, timeStampValidatorMock);
    }

    @Test
    void afterPropertiesSetInitializesTimeStampAuthority() {
        // given // when
        testSubject.afterPropertiesSet();

        // then
        then(timeStampAuthorityMock).should().initialize();
    }

    @Test
    void afterPropertiesSetInitializesTimeStampValidator() {
        // given // when
        testSubject.afterPropertiesSet();

        // then
        then(timeStampValidatorMock).should().initialize();
    }

}
