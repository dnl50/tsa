package dev.mieser.tsa.web;

import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@Transactional
@RequiredArgsConstructor
@RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
public class ThymeleafController {

    private final QueryTimeStampResponseService queryResponseService;

    private final ValidateTimeStampResponseService validateTimeStampResponseService;

    @GetMapping("/")
    public String index() {
        log.info("Redirecting GET request to '/web/history'.");
        return "redirect:/web/history";
    }

    @GetMapping("/web/history")
    public String overview(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size, Model model) {
        model.addAttribute("responses", queryResponseService.findAll(PageRequest.of(page, size)));
        return "history";
    }

    @GetMapping("/web/details")
    public String responseDetails(@RequestParam("id") Long id, Model model) {
        return null;
    }

}

