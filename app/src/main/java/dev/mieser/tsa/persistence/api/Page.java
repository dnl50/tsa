package dev.mieser.tsa.persistence.api;

import java.util.List;

/**
 * @param size
 *     The size of this page.
 * @param pageNumber
 *     The number of this page starting at 1.
 * @param totalPages
 *     The total number of pages when using the specified size.
 * @param totalElements
 *     The total number of elements across all pages.
 * @param content
 *     The elements of this page.
 */
public record Page<T>(int size, int pageNumber, long totalPages, long totalElements, List<T> content) {

}
