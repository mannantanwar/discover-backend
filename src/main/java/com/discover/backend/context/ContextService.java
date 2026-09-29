package com.discover.backend.context;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContextService {

    // Spring autowires every ContextRule bean into this list for free — no factory needed,
    // since we always want to evaluate all of them, not dispatch to just one
    private final List<ContextRule> contextRules;

    public List<ContextSuggestion> getSuggestions(ContextInput input) {
        return contextRules.stream()
                .map(rule -> rule.evaluate(input))
                .flatMap(Optional::stream)
                .toList();
    }
}
