package dev.mieser.tsa.web.filter;

import static jakarta.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static jakarta.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeStampProtocolFilterTest {

    private static final int TSP_HANDLER_PORT = 1337;

    private final TimeStampProtocolFilter testSubject = new TimeStampProtocolFilter(TSP_HANDLER_PORT);

    @Test
    void callsFilterChainWhenRequestIsNotSentToTspHandlerPort(@Mock ServletRequest requestMock,
        @Mock ServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT + 1);

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).should().doFilter(requestMock, responseMock);
        then(responseMock).shouldHaveNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "application/timestamp-query",
        "application/timestamp-query; charset=UTF-8"
    })
    void callsFilterChainWhenPostRequestIsSentToTspHandlerPortAndIsTspRequest(String contentType,
        @Mock HttpServletRequest requestMock, @Mock ServletResponse responseMock,
        @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getContentType()).willReturn(contentType);
        given(requestMock.getMethod()).willReturn("POST");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).should().doFilter(requestMock, responseMock);
        then(responseMock).shouldHaveNoInteractions();
    }

    @Test
    void setsStatusCodeToUnsupportedMediaTypeWhenPostRequestHasBlankContentType(@Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getMethod()).willReturn("POST");
        given(requestMock.getContentType()).willReturn("\t");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @Test
    void setsStatusCodeToUnsupportedMediaTypeWhenPostRequestHasWrongContentType(@Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getMethod()).willReturn("POST");
        given(requestMock.getContentType()).willReturn("text/html");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(SC_UNSUPPORTED_MEDIA_TYPE);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "GET",
        "PUT",
        "DELETE"
    })
    void setsStatusCodeWhenRequestIsNotAPostRequest(String requestMethod, @Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getMethod()).willReturn(requestMethod);

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(SC_METHOD_NOT_ALLOWED);
    }

}
