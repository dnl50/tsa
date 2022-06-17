package dev.mieser.tsa.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Locale;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.web.ApplicationVersionService;
import dev.mieser.tsa.web.dto.TimeStampResponseDto;

@WebMvcTest(ValidateController.class)
@Import(ApplicationVersionService.class)
class ValidateControllerTest {

    @MockBean
    private ValidateTimeStampResponseService validateTimeStampResponseServiceMock;

    private final MockMvc mockMvc;

    @Autowired
    ValidateControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void canRenderValidationPageWithEmptyModel() throws Exception {
        // given / when / then
        mockMvc.perform(get("/web/validate"))
            .andExpect(status().isOk())
            .andExpect(view().name("validate"))
            .andExpect(model().attribute("response", new TimeStampResponseDto()));
    }

    @Test
    void rendersValidationResultPageWhenValidResponseIsEntered() throws Exception {
        // given
        String base64EncodedTspResponse = "dGVzdA==";
        var validationResult = TimeStampValidationResult.builder().build();

        given(validateTimeStampResponseServiceMock.validateTimeStampResponse(base64EncodedTspResponse))
            .willReturn(validationResult);

        // when / then
        mockMvc.perform(post("/web/validate")
            .locale(Locale.ENGLISH)
            .accept(MediaType.TEXT_HTML)
            .content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .flashAttr("response", new TimeStampResponseDto(base64EncodedTspResponse)))
            .andExpect(status().isOk())
            .andExpect(view().name("validation-result"))
            .andExpect(model().attribute("validationResult", validationResult));
    }

    @Test
    void showsErrorWhenInputIsNotBase64Encoded() throws Exception {
        // given
        var illegalInput = new TimeStampResponseDto("ü");

        // when / then
        String renderedHtml = mockMvc.perform(post("/web/validate")
            .locale(Locale.ENGLISH)
            .accept(MediaType.TEXT_HTML)
            .content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .flashAttr("response", illegalInput))
            .andExpect(status().isOk())
            .andExpect(view().name("validate"))
            .andExpect(model().attribute("response", illegalInput))
            .andReturn().getResponse().getContentAsString();

        String warningAlertText = Jsoup.parse(renderedHtml).select(".alert-warning").text();

        assertThat(warningAlertText).isEqualTo("Not a valid Base64 String.");
    }

    @Test
    void showsErrorWhenInvalidTspResponseIsEntered() throws Exception {
        // given
        String base64EncodedTspResponse = "dGVzdA==";

        given(validateTimeStampResponseServiceMock.validateTimeStampResponse(base64EncodedTspResponse))
            .willThrow(new InvalidTspResponseException("Error!1!!"));

        // when / then
        String renderedHtml = mockMvc.perform(post("/web/validate")
            .locale(Locale.ENGLISH)
            .accept(MediaType.TEXT_HTML)
            .content(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .flashAttr("response", new TimeStampResponseDto(base64EncodedTspResponse)))
            .andExpect(status().isBadRequest())
            .andExpect(view().name("validate"))
            .andExpect(model().attribute("response", new TimeStampResponseDto()))
            .andReturn().getResponse().getContentAsString();

        String dangerAlertText = Jsoup.parse(renderedHtml).select(".alert-danger").text();

        assertThat(dangerAlertText).isEqualTo("The input is not a valid time stamp response according to RFC 3161/RFC 5816.");
    }

}
