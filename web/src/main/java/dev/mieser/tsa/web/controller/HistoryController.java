package dev.mieser.tsa.web.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/web/history", produces = MediaType.TEXT_HTML_VALUE)
public class HistoryController {

    @GetMapping
    public String history() {
        return "history";
    }

}
