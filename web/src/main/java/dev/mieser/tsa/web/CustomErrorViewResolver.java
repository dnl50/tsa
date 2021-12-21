package dev.mieser.tsa.web;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Spring Boot {@link ErrorViewResolver} which uses the same error view for all HTTP status codes.
 */
@Component
public class CustomErrorViewResolver implements ErrorViewResolver {

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        validateRequiredModelFieldsArePresent(model);
        return new ModelAndView("error", model);
    }

    /**
     * @param model The model to validate, not {@code null}.
     */
    private void validateRequiredModelFieldsArePresent(Map<String, Object> model) {
        if (model.get("status") == null) {
            throw new IllegalArgumentException("HTTP Status Code not present in model.");
        }
    }

}
