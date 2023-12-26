package dev.mieser.tsa.signing.impl.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.api.DateConverter;

// TODO: Test überdenken/kopieren

@ExtendWith(MockitoExtension.class)
class TimeStampValidationResultMapperTest {

    @Mock
    private DateConverter dateConverterMock;

    private TimeStampValidationResultMapper testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TimeStampValidationResultMapper(dateConverterMock);
    }

}
