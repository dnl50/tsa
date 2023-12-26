package dev.mieser.tsa.rest.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import dev.mieser.tsa.persistence.api.Sort;
import dev.mieser.tsa.persistence.api.SortDirection;

/**
 * Converts between {@link Sort} instances and their string representation.
 *
 * @implNote Not implemented as a JAX-RS {@link jakarta.ws.rs.ext.ParamConverter}, because that makes things more
 * complicated, since the standard {@link jakarta.validation.constraints.Pattern} bean validation annotation can not be
 * used on the resource method.
 */
@ApplicationScoped
public class SortQueryParamConverter {

    public static final String PATTERN = "^(?<attributeName>[\\w.]+),(?<direction>asc|desc)$";

    private final Pattern compiledPattern = Pattern.compile(PATTERN, Pattern.CASE_INSENSITIVE);

    public Sort fromString(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        Matcher matcher = compiledPattern.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("The value is not properly formatted.");
        }

        SortDirection direction = EnumUtils.getEnumIgnoreCase(SortDirection.class, matcher.group("direction"));
        String attributeName = matcher.group("attributeName");
        return new Sort(direction, attributeName);
    }

}
