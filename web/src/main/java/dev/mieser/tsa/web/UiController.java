package dev.mieser.tsa.web;

import dev.mieser.tsa.integration.api.QueryTimeStampResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Transactional
@RequiredArgsConstructor
@RequestMapping(path = "/web")
public class UiController {

    private final QueryTimeStampResponseService queryResponseService;

    @GetMapping("/overview")
    public String overview(@RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "25") int size, Model model) {
        model.addAttribute("responses", queryResponseService.findAll(PageRequest.of(page, size)));
        return "overview";
    }

}

