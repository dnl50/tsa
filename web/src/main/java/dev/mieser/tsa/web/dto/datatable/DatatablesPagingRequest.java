package dev.mieser.tsa.web.dto.datatable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * DTO encapsulating part of the request parameters sent by <a href="https://datatables.net">Datatables</a> AJAX requests. Searching is not supported.
 *
 * @see DatatablesPage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatatablesPagingRequest {

    /**
     * A draw counter used by Datatables. The same value must be included in the response.
     */
    private int draw;

    /**
     * The zero-based index of the first item to include.
     */
    @Min(0)
    private int start;

    /**
     * The number of items to include.
     */
    @Positive
    private int length;

    /**
     * The table columns to sort by.
     */
    private List<@Valid Order> order;

    /**
     * The columns of the table.
     */
    private List<@Valid Column> columns;

}
