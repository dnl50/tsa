package dev.mieser.tsa.web.filter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

/**
 * Servlet {@link Filter} to verify that HTTP servlet requests received on a specific port are HTTP {@code POST}
 * requests with the content type {@code application/timestamp-query}. If the port matches, but the HTTP request is not
 * a {@code POST} request with content type {@code application/timestamp-query}, the response status is set to
 * {@code 415} (Unsupported Media Type).
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

        if (isRequestToTspHandlerPort(request) && !isTimeStampQueryRequest(request)) {
            log.info("HTTP request with illegal content type '{}' received on TSP handler port '{}'.", request.getContentType(),
                tspHandlerPort);

            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(UNSUPPORTED_MEDIA_TYPE.value());
            httpResponse.setCharacterEncoding(UTF_8.name());
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * @param request
     *     The HTTP servlet request to check, not {@code null}.
     * @return {@code true}, iff the request is a {@value HTTP_POST_METHOD} request with the content type
     * {@code application/timestamp-query}.
     */
    private boolean isTimeStampQueryRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        MediaType requestMediaType = StringUtils.isBlank(request.getContentType()) ? null
            : MediaType.parseMediaType(request.getContentType());

        return requestMediaType != null && TIME_STAMP_QUERY_MEDIA_TYPE.isCompatibleWith(requestMediaType)
            && HTTP_POST_METHOD.equals(httpRequest.getMethod());
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
