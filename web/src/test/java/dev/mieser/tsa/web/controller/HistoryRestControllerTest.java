package dev.mieser.tsa.web.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.web.dto.datatable.Column;
import dev.mieser.tsa.web.dto.datatable.DatatablesPagingRequest;
import dev.mieser.tsa.web.paging.DatatablesPageableMapper;

@WebMvcTest(HistoryRestController.class)
class HistoryRestControllerTest {

    @MockBean
    private QueryTimeStampResponseService queryTimeStampResponseServiceMock;

    @MockBean
    private DatatablesPageableMapper datatablesPageableMapperMock;

    private final MockMvc mockMvc;

    @Autowired
    HistoryRestControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void returnsExpectedPageOfResponses() throws Exception {
        // given
        ObjectMapper objectMapper = new ObjectMapper();

        TimeStampResponseData timeStampResponse = TimeStampResponseData.builder()
            .serialNumber(BigInteger.valueOf(8L))
            .build();
        DatatablesPagingRequest pagingRequest = DatatablesPagingRequest.builder()
            .draw(12)
            .start(0)
            .length(1)
            .columns(List.of(new Column("name", "data")))
            .build();
        Pageable mappedPageable = PageRequest.of(0, 1);
        Page<TimeStampResponseData> page = new PageImpl<>(List.of(timeStampResponse), mappedPageable, 1337);

        given(datatablesPageableMapperMock.apply(pagingRequest)).willReturn(mappedPageable);
        given(queryTimeStampResponseServiceMock.findAll(mappedPageable)).willReturn(page);

        // when / then
        mockMvc.perform(post("/api/history")
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(pagingRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.draw").value("12"))
            .andExpect(jsonPath("$.recordsTotal").value("1337"))
            .andExpect(jsonPath("$.recordsFiltered").value("1337"))
            .andExpect(jsonPath("$.data.length()").value("1"))
            .andExpect(jsonPath("$.data[0].serialNumber").value("8"));
    }

}
