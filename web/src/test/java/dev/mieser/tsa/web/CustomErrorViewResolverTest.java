package dev.mieser.tsa.web;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

@ExtendWith(MockitoExtension.class)
class CustomErrorViewResolverTest {

    private final CustomErrorViewResolver testSubject = new CustomErrorViewResolver();

    @Test
    void resolveErrorViewThrowsExceptionWhenStatusNotPresent(@Mock HttpServletRequest servletRequestMock) {
        // given
        Map<String, Object> emptyModel = emptyMap();

        // when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> testSubject.resolveErrorView(servletRequestMock, HttpStatus.FORBIDDEN, emptyModel))
            .withMessage("HTTP Status Code not present in model.");
    }

    @Test
    void resolveErrorViewReturnsExpectedModelAndView(@Mock HttpServletRequest servletRequestMock) {
        // given
        Map<String, Object> model = Map.of("status", 200);

        // when
        ModelAndView actualModelAndView = testSubject.resolveErrorView(servletRequestMock, HttpStatus.BAD_REQUEST, model);

        // then
        assertSoftly(softly -> {
            softly.assertThat(actualModelAndView.getModel()).isEqualTo(model);
            softly.assertThat(actualModelAndView.getViewName()).isEqualTo("error");
        });
    }

}
