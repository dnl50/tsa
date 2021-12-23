package dev.mieser.tsa.web.controller;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.web.dto.datatable.DatatablesPage;
import dev.mieser.tsa.web.dto.datatable.DatatablesPagingRequest;
import dev.mieser.tsa.web.paging.DatatablesPageableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * REST API Endpoint for querying responses sent by this TSA. The API is compatible with <a href="https://datatables.net">Datatables</a> AJAX requests.
 *
 * @see HistoryController
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/history", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class HistoryRestController {

    private final QueryTimeStampResponseService queryTimeStampResponseService;

    private final DatatablesPageableMapper datatablesPageableMapper;

    @PostMapping
    public DatatablesPage<TimestampResponseData> history(@Valid @RequestBody DatatablesPagingRequest request) {
        Page<TimestampResponseData> page = queryTimeStampResponseService.findAll(datatablesPageableMapper.apply(request));

        return DatatablesPage.<TimestampResponseData>builder()
                .draw(request.getDraw())
                .recordsTotal(page.getTotalElements())
                .recordsFiltered(page.getTotalElements())
                .data(page.getContent())
                .build();
    }

}
