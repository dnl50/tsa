package dev.mieser.tsa.integration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.api.TimeStampAuthority;

@ExtendWith(MockitoExtension.class)
class IssueTimeStampServiceImplTest {

    private final TimeStampAuthority timeStampAuthorityMock;

    private final TspResponseDataRepository responseDataRepositoryMock;

    private final IssueTimeStampServiceImpl testSubject;

    IssueTimeStampServiceImplTest(@Mock TimeStampAuthority timeStampAuthorityMock,
        @Mock TspResponseDataRepository responseDataRepositoryMock) {
        this.timeStampAuthorityMock = timeStampAuthorityMock;
        this.responseDataRepositoryMock = responseDataRepositoryMock;

        this.testSubject = new IssueTimeStampServiceImpl(timeStampAuthorityMock, responseDataRepositoryMock);
    }

    @Test
    void signTimestampRequestIssuesAndSavesResponse() {
        // given
        InputStream tspRequestInputStream = new ByteArrayInputStream("TSP request".getBytes(UTF_8));
        TimestampResponseData issuedResponse = TimestampResponseData.builder().build();
        TimestampResponseData savedResponse = TimestampResponseData.builder().build();

        given(timeStampAuthorityMock.signRequest(tspRequestInputStream)).willReturn(savedResponse);
        given(responseDataRepositoryMock.save(issuedResponse)).willReturn(savedResponse);

        // when
        TimestampResponseData actualResponse = testSubject.signTimestampRequest(tspRequestInputStream);

        // then
        assertThat(actualResponse).isSameAs(savedResponse);
    }

}
