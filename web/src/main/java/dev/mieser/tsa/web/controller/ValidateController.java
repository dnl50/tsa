package dev.mieser.tsa.web.controller;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.Map;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.web.dto.TimeStampResponseDto;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/web/validate", produces = MediaType.TEXT_HTML_VALUE)
public class ValidateController {

    private final ValidateTimeStampResponseService validateTimeStampResponseService;

    @GetMapping
    public String validateResponse(@ModelAttribute("response") TimeStampResponseDto response) {
        return "validate";
    }

    @PostMapping
    public String validationResult(@Valid @ModelAttribute("response") TimeStampResponseDto response,
        BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "validate";
        }

        model.addAttribute("validationResult",
            validateTimeStampResponseService.validateTimeStampResponse(response.getBase64EncodedResponse()));
        return "validation-result";
    }

    @ExceptionHandler(InvalidTspResponseException.class)
    public ModelAndView handleInvalidTspResponse() {
        Map<String, Object> model = Map.of(
            "invalidTspResponse", true,
            "response", new TimeStampResponseDto());

        return new ModelAndView("validate", model, BAD_REQUEST);
    }

}
