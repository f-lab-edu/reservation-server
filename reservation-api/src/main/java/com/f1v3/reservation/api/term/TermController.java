package com.f1v3.reservation.api.term;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * {class name}.
 *
 * @author Seungjo, Jeong
 */
@RequestMapping("/v1")
@RestController
@RequiredArgsConstructor
public class TermController {

    private final TermService termService;

    @GetMapping("/terms")
    public TermResponse latestTerms() {
        return termService.lastestTerms();
    }

}
