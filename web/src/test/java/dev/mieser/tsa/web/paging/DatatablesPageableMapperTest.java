package dev.mieser.tsa.web.paging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import dev.mieser.tsa.web.dto.datatable.Column;
import dev.mieser.tsa.web.dto.datatable.DatatablesPagingRequest;
import dev.mieser.tsa.web.dto.datatable.Order;
import dev.mieser.tsa.web.dto.datatable.SortDirection;

class DatatablesPageableMapperTest {

    private final DatatablesPageableMapper testSubject = new DatatablesPageableMapper();

    @Test
    void applyReturnsUnsortedPageableWhenNoOrderSpecified() {
        // given
        DatatablesPagingRequest request = DatatablesPagingRequest.builder()
            .start(25)
            .length(50)
            .columns(List.of(new Column("name", "data")))
            .build();

        // when
        Pageable mappedPageable = testSubject.apply(request);

        // then
        assertThat(mappedPageable).isEqualTo(new OffsetBasedPageable(25, 50, Sort.unsorted()));
    }

    @Test
    void applyReturnsPageableWithExpectedSorting() {
        // given
        DatatablesPagingRequest request = DatatablesPagingRequest.builder()
            .start(10)
            .length(25)
            .columns(List.of(new Column("first-name", "first-data"), new Column("second-name", "second-data")))
            .order(List.of(new Order(0, SortDirection.ASC), new Order(1, SortDirection.DESC)))
            .build();

        // when
        Pageable mappedPageable = testSubject.apply(request);

        // then
        Sort expectedSort = Sort.by(List.of(Sort.Order.asc("first-data"), (Sort.Order.desc("second-data"))));

        assertThat(mappedPageable).isEqualTo(new OffsetBasedPageable(10, 25, expectedSort));
    }

}
