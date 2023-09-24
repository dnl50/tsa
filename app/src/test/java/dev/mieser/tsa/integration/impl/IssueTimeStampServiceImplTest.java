package dev.mieser.tsa.integration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.TimeStampListener;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;

@ExtendWith(MockitoExtension.class)
class IssueTimeStampServiceImplTest {

    @Mock
    private TimeStampAuthority timeStampAuthorityMock;

    @Mock
    private TspResponseDataRepository tspResponseDataRepositoryMock;

    private IssueTimeStampServiceImpl testSubject;

    @BeforeEach
    void setUp() {
        testSubject = new IssueTimeStampServiceImpl(timeStampAuthorityMock, tspResponseDataRepositoryMock);
    }

    @Test
    void signTimestampRequestReturnsSavedResponse(@Mock TimeStampResponseData generatedResponseMock,
        @Mock TimeStampResponseData savedResponseMock) throws Exception {
        // given
        var inputStream = new ByteArrayInputStream("asn-encoded-request".getBytes());

        given(timeStampAuthorityMock.signRequest(inputStream)).willReturn(generatedResponseMock);
        given(tspResponseDataRepositoryMock.save(generatedResponseMock)).willReturn(savedResponseMock);

        // when
        var actualResponse = testSubject.signTimestampRequest(inputStream);

        // then
        assertThat(actualResponse).isSameAs(savedResponseMock);
    }

    @Test
    void notifiesRegisteredListeners(@Mock TimeStampListener listenerMock, @Mock TimeStampResponseData generatedResponseMock,
        @Mock TimeStampResponseData savedResponseMock) throws Exception {
        // given
        var inputStream = new ByteArrayInputStream("asn-encoded-request".getBytes());

        given(timeStampAuthorityMock.signRequest(inputStream)).willReturn(generatedResponseMock);
        given(tspResponseDataRepositoryMock.save(generatedResponseMock)).willReturn(savedResponseMock);

        testSubject.registerListener(listenerMock);

        // when
        testSubject.signTimestampRequest(inputStream);

        // then
        then(listenerMock).should().onResponse(savedResponseMock);
    }

}
