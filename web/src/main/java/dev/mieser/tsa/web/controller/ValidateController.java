package dev.mieser.tsa.web.controller;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.web.dto.TimeStampResponseDto;

@Controller
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public class ValidateController {

    private final ValidateTimeStampResponseService validateTimeStampResponseService;

    @GetMapping("/web/validate")
    public String validateResponse(@ModelAttribute("response") TimeStampResponseDto response) {
        return "validate";
    }

    @PostMapping("/web/validate")
    public String validationResult(@Valid @ModelAttribute("response") TimeStampResponseDto response, BindingResult bindingResult,
        Model model) {
        if (bindingResult.hasErrors()) {
            return "validate";
        }

        model.addAttribute("validationResult",
            validateTimeStampResponseService.validateTimeStampResponse(response.getBase64EncodedResponse()));
        return "validation-result";
    }

}
