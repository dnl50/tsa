package dev.mieser.tsa.web;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.TspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = TimeStampAuthorityController.class)
class TimeStampAuthorityControllerTest {

    private static final String QUERY_CONTENT_TYPE = "application/timestamp-query";

    private static final String REPLY_CONTENT_TYPE = "application/timestamp-reply";

    private final MockMvc mockMvc;

    @MockBean
    private IssueTimeStampService issueTimeStampServiceMock;

    @Autowired
    TimeStampAuthorityControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void doesNotAcceptOtherContentTypes() throws Exception {
        // given / when / then
        mockMvc.perform(post("/").contentType(APPLICATION_JSON))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void setsExpectedResponseContentType() throws Exception {
        // given
        byte[] requestContent = "TSP request".getBytes(UTF_8);
        byte[] responseContent = "TSP response".getBytes(UTF_8);

        TimestampResponseData issuedResponse = TimestampResponseData.builder()
                .asnEncoded(responseContent)
                .build();

        given(issueTimeStampServiceMock.signTimestampRequest(any())).willReturn(issuedResponse);

        // when / then
        mockMvc.perform(post("/")
                        .content(requestContent)
                        .contentType(QUERY_CONTENT_TYPE)
                        .accept(REPLY_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(header().stringValues(CONTENT_TYPE, REPLY_CONTENT_TYPE));
    }

    @Test
    void returnsAsnEncodedTspResponseInBody() throws Exception {
        // given
        byte[] requestContent = "TSP request".getBytes(UTF_8);
        byte[] responseContent = "TSP response".getBytes(UTF_8);

        TimestampResponseData issuedResponse = TimestampResponseData.builder()
                .asnEncoded(responseContent)
                .build();

        given(issueTimeStampServiceMock.signTimestampRequest(any())).willAnswer(invocation -> {
            InputStream actualStream = invocation.getArgument(0);
            byte[] actualContent = IOUtils.toByteArray(actualStream);
            return Arrays.equals(actualContent, requestContent) ? issuedResponse : null;
        });

        // when / then
        mockMvc.perform(post("/")
                        .content(requestContent)
                        .contentType(QUERY_CONTENT_TYPE)
                        .accept(REPLY_CONTENT_TYPE))
                .andExpect(status().isOk())
                .andExpect(content().bytes(responseContent));
    }

    @MethodSource("exceptionToStatusCodeProvider")
    @ParameterizedTest
    void mapsExceptionToExpectedStatusCode(RuntimeException exception, HttpStatus expectedStatus) throws Exception {
        // given
        byte[] requestContent = "TSP request".getBytes(UTF_8);

        given(issueTimeStampServiceMock.signTimestampRequest(any())).willThrow(exception);

        // when / then
        mockMvc.perform(post("/")
                        .content(requestContent)
                        .contentType(QUERY_CONTENT_TYPE)
                        .accept(REPLY_CONTENT_TYPE))
                .andExpect(status().is(expectedStatus.value()));
    }

    static Stream<Arguments> exceptionToStatusCodeProvider() {
        return Stream.of(
                arguments(new InvalidTspRequestException("Test", new IllegalStateException()), BAD_REQUEST),
                arguments(new UnknownHashAlgorithmException("Test"), BAD_REQUEST),
                arguments(new TspResponseException("Test", new IllegalStateException()), INTERNAL_SERVER_ERROR)
        );
    }

}
