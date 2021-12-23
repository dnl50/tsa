package dev.mieser.tsa.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for redirecting HTTP {@code GET} requests to the Servlet Root Path to another Controller Method.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
public class IndexController {

    @GetMapping
    public String index() {
        log.debug("Redirecting GET request from '/' to '/web/history'.");
        return "redirect:/web/history";
    }

}
