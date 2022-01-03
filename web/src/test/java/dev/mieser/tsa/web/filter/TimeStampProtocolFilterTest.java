package dev.mieser.tsa.web.filter;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        given(requestMock.getServerPort()).willReturn(12345);

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).should().doFilter(requestMock, responseMock);
        then(responseMock).shouldHaveNoInteractions();
    }

    @Test
    void callsFilterChainWhenRequestIsSentToTspHandlerPortAndIsTspRequest(@Mock HttpServletRequest requestMock,
        @Mock ServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getContentType()).willReturn("application/timestamp-query; charset=UTF-8");
        given(requestMock.getMethod()).willReturn("POST");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).should().doFilter(requestMock, responseMock);
        then(responseMock).shouldHaveNoInteractions();
    }

    @Test
    void setsStatusCodeWhenRequestHasNoContentType(@Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(415);
    }

    @Test
    void setsStatusCodeWhenRequestHasWrongContentType(@Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getContentType()).willReturn("text/html");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(415);
    }

    @Test
    void setsStatusCodeWhenRequestIsNotAPostRequest(@Mock HttpServletRequest requestMock,
        @Mock HttpServletResponse responseMock, @Mock FilterChain filterChainMock) throws Exception {
        // given
        given(requestMock.getServerPort()).willReturn(TSP_HANDLER_PORT);
        given(requestMock.getContentType()).willReturn("application/timestamp-query");
        given(requestMock.getMethod()).willReturn("GET");

        // when
        testSubject.doFilter(requestMock, responseMock, filterChainMock);

        // then
        then(filterChainMock).shouldHaveNoInteractions();
        then(responseMock).should().setStatus(415);
    }

}
