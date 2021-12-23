package dev.mieser.tsa.web.paging;

import dev.mieser.tsa.web.dto.datatable.Column;
import dev.mieser.tsa.web.dto.datatable.DatatablesPagingRequest;
import dev.mieser.tsa.web.dto.datatable.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper {@link Function} to map {@link DatatablesPagingRequest}s to Spring Data {@link Pageable}s.
 */
@Component
public class DatatablesPageableMapper implements Function<DatatablesPagingRequest, Pageable> {

    @Override
    public Pageable apply(DatatablesPagingRequest pagingRequest) {
        return new OffsetBasedPageable(pagingRequest.getStart(), pagingRequest.getLength(), mapSorting(pagingRequest));
    }

    private Sort mapSorting(DatatablesPagingRequest pagingRequest) {
        List<Order> orders = pagingRequest.getOrder();
        if (orders == null || orders.isEmpty()) {
            return Sort.unsorted();
        }

        List<Column> columns = pagingRequest.getColumns();
        List<Sort.Order> pageableOrder = orders.stream()
                .map(order -> mapOrder(columns, order))
                .collect(Collectors.toList());

        return Sort.by(pageableOrder);
    }

    private Sort.Order mapOrder(List<Column> columns, Order order) {
        Column column = columns.get(order.getColumn());
        String sortProperty = column.getData();

        return switch (order.getDir()) {
            case ASC -> Sort.Order.asc(sortProperty);
            case DESC -> Sort.Order.desc(sortProperty);
        };
    }

}
