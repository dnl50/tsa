package dev.mieser.tsa.persistence.api;

/**
 * @param pageNumber
 *     The number of the Page to return. Must be greater than or equal to {@code 1}).
 * @param size
 *     The size of the page. Must be greater than or equal to {@code 1}.
 * @param sort
 *     The property to sort after.
 */
public record PageRequest(int pageNumber, int size, Sort sort) {

}
