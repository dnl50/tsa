package dev.mieser.tsa.integration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.TimeStampValidator;

@ExtendWith(MockitoExtension.class)
class ValidateTimeStampResponseServiceImplTest {

    @Mock
    private TimeStampValidator timeStampValidatorMock;

    private ValidateTimeStampResponseServiceImpl testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new ValidateTimeStampResponseServiceImpl(timeStampValidatorMock);
    }

    @Test
    void validateTimeStampResponseDelegates() throws Exception {
        // given
        var inputStream = new ByteArrayInputStream("asn-encoded-response".getBytes());
        var validationResult = TimeStampValidationResult.builder().build();

        given(timeStampValidatorMock.validateResponse(inputStream)).willReturn(validationResult);

        // when
        TimeStampValidationResult result = testSubject.validateTimeStampResponse(inputStream);

        // then
        assertThat(result).isEqualTo(validationResult);
    }

}
