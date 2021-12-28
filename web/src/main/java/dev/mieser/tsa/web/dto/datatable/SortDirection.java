package dev.mieser.tsa.web.dto.datatable;

import static java.lang.String.format;

import java.util.EnumSet;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The sort directions
 */
public enum SortDirection {

    ASC,

    DESC;

    /**
     * Jackson factory method to map the lower-case names of Datatables to enum constants.
     *
     * @param direction
     *     The name of the enum constant to return.
     * @return The corresponding enum constant or {@code null}, when the specified direction is {@code null}.
     * @throws IllegalArgumentException
     *     When no enum constant was found.
     */
    @JsonCreator
    public static SortDirection fromDirection(String direction) {
        if (direction == null) {
            return null;
        }

        return EnumSet.allOf(SortDirection.class).stream()
            .filter(sortDirection -> sortDirection.name().equalsIgnoreCase(direction))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(format("No direction constant found for '%s'.", direction)));
    }

}
