package dev.mieser.tsa.web.filter;

import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

/**
 * Servlet {@link Filter} to verify that HTTP servlet requests received on a specific port are HTTP {@code POST}
 * requests with the content type {@code application/timestamp-query}. If the port matches, but the HTTP request is not
 * a {@code POST} request with content type {@code application/timestamp-query}, the response status is set to
 * {@code 405} (Method not allowed)/{@code 415} (Unsupported Media Type).
 */
@Slf4j
@RequiredArgsConstructor
public class TimeStampProtocolFilter implements Filter {

    /**
     * The media type of Time Stamp Protocol HTTP requests.
     */
    private static final MediaType TIME_STAMP_QUERY_MEDIA_TYPE = MediaType.parseMediaType("application/timestamp-query");

    /**
     * The name of the HTTP method of {@code POST} requests.
     */
    private static final String HTTP_POST_METHOD = "POST";

    /**
     * The TCP port which accepts Time Stamp Protocol requests.
     */
    private final int tspHandlerPort;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
        FilterChain chain) throws IOException, ServletException {

        if (isRequestToTspHandlerPort(request)) {
            if (!isPostRequest(request)) {
                log.info("HTTP request with unsupported method '{}' received on TSP handler port '{}'.",
                    ((HttpServletRequest) request).getMethod(), tspHandlerPort);
                ((HttpServletResponse) response).setStatus(METHOD_NOT_ALLOWED.value());
                return;
            } else if (!isTimeStampQueryRequest(request)) {
                log.info("HTTP post request with illegal content type '{}' received on TSP handler port '{}'.",
                    request.getContentType(), tspHandlerPort);
                ((HttpServletResponse) response).setStatus(UNSUPPORTED_MEDIA_TYPE.value());
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * @param request
     *     The servlet request to check, not {@code null}.
     * @return {@code true}, iff the content type is {@code application/timestamp-query}.
     */
    private boolean isTimeStampQueryRequest(ServletRequest request) {
        MediaType requestMediaType = StringUtils.isBlank(request.getContentType()) ? null
            : MediaType.parseMediaType(request.getContentType());

        return requestMediaType != null && TIME_STAMP_QUERY_MEDIA_TYPE.isCompatibleWith(requestMediaType);
    }

    /**
     * @param request
     *     The HTTP request to check, not {@code null}.
     * @return {@code true}, iff the request is a {@value HTTP_POST_METHOD} request.
     */
    private boolean isPostRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        return HTTP_POST_METHOD.equals(httpRequest.getMethod());
    }

    /**
     * @param request
     *     The servlet request to check, not {@code null}.
     * @return {@code true}, iff the servlet request was received on the configured TSP handler port.
     */
    private boolean isRequestToTspHandlerPort(ServletRequest request) {
        return request.getServerPort() == tspHandlerPort;
    }

}
