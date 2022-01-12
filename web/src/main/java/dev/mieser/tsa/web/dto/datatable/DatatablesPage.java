package dev.mieser.tsa.web.dto.datatable;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <a href="https://datatables.net">Datatables</a> paging response.
 *
 * @param <T>
 *     The type of the data.
 * @see DatatablesPagingRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatatablesPage<T> {

    /**
     * The draw counter included in the paging request.
     */
    private int draw;

    /**
     * The total number of records in the database.
     */
    private long recordsTotal;

    /**
     * The number of records after filtering.
     */
    private long recordsFiltered;

    /**
     * The page entries.
     */
    private List<T> data;

}
