package dev.mieser.tsa.web.dto.datatable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO encapsulating parts of the column information included in a <a href="https://datatables.net">Datatables</a> AJAX
 * request.
 *
 * @see DatatablesPagingRequest
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column {

    /**
     * The column's name.
     */
    private String name;

    /**
     * The column's data source.
     */
    private String data;

}
