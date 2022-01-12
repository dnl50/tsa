package dev.mieser.tsa.web.paging;

import lombok.EqualsAndHashCode;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * {@link Pageable} Implementation which uses a predefined offset.
 */
@EqualsAndHashCode
public class OffsetBasedPageable implements Pageable {

    private final long offset;

    private final int pageSize;

    private final Sort sort;

    /**
     * @param offset
     *     The number of items to skip. Must be greater than or equal to zero.
     * @param pageSize
     *     The number of items on each page. Must be greater than or equal to one.
     * @param sort
     *     The sorting to user. May be {@code null}.
     */
    public OffsetBasedPageable(long offset, int pageSize, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset cannot be less than zero.");
        } else if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than or equal to one.");
        }

        this.offset = offset;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    @Override
    public int getPageNumber() {
        return (int) (offset / pageSize);
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetBasedPageable(offset + pageSize, pageSize, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetBasedPageable(offset - pageSize, pageSize, sort) : first();
    }

    @Override
    public Pageable first() {
        return new OffsetBasedPageable(0, pageSize, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetBasedPageable((long) pageSize * pageNumber, pageSize, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset >= pageSize;
    }

}
