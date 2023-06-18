package dev.mieser.tsa.persistence.api;

import lombok.NonNull;

/**
 * @param direction
 *     The direction the returned are
 * @param attributeName
 *     The name of the attribute used to sort the elements. Nested Fields are referenced by chaining the field using a
 *     dot as a separator (e.g. {@code parent.child}).
 */
public record Sort(@NonNull SortDirection direction, @NonNull String attributeName) {
}
