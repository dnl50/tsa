package dev.mieser.tsa.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    private final MockMvc mockMvc;

    @Autowired
    IndexControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void redirectsToHistoryPage() throws Exception {
        // given / when / then
        mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
            .andExpect(redirectedUrl("/web/history"));
    }

}
