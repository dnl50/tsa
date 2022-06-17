package dev.mieser.tsa.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import dev.mieser.tsa.web.ApplicationVersionService;

/**
 * @implNote Import the actual {@link ApplicationVersionService} so the {@link MockBean} has the same bean name. The
 * bean name is used in the footer template.
 */
@Import(ApplicationVersionService.class)
@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @MockBean
    private ApplicationVersionService applicationVersionServiceMock;

    private final MockMvc mockMvc;

    @Autowired
    HistoryControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void rendersExpectedView() throws Exception {
        // given / when / then
        mockMvc.perform(MockMvcRequestBuilders.get("/web/history").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andExpect(view().name("history"));
    }

    @Test
    void footerContainsExpectedVersion() throws Exception {
        // given
        given(applicationVersionServiceMock.getApplicationVersion()).willReturn("1.2.3");

        // when / then
        String renderedHtml = mockMvc.perform(MockMvcRequestBuilders.get("/web/history").accept(MediaType.TEXT_HTML))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String actualVersion = Jsoup.parse(renderedHtml).select("body footer span").text();

        assertThat(actualVersion).isEqualTo("Version 1.2.3");
    }

}
