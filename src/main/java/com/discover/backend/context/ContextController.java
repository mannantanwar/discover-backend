package com.discover.backend.context;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/context")
public class ContextController {

    private final ContextService contextService;

    @GetMapping
    public List<ContextSuggestion> getContextSuggestions(@RequestParam Double lat, @RequestParam Double lng) {
        ContextInput input = new ContextInput();
        input.setLatitude(lat);
        input.setLongitude(lng);
        input.setNow(LocalDateTime.now());
        return contextService.getSuggestions(input);
    }
}
