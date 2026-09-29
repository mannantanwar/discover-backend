package com.discover.backend.context;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FestivalRule implements ContextRule {

    private record Festival(String message, List<String> tags) {
    }

    // hardcoded and year-specific on purpose — most Indian festivals follow a lunar calendar,
    // so these dates don't recur on the same Gregorian date next year. Needs a manual update
    // each year; a real lunar-calendar calculation is out of scope for v1.
    private static final Map<LocalDate, Festival> FESTIVAL_CALENDAR = Map.of(
            LocalDate.of(2026, 11, 8), new Festival("Diwali is almost here — time for something sweet", List.of("mithai", "sweets")),
            LocalDate.of(2026, 3, 4), new Festival("Holi vibes — something festive?", List.of("gujiya", "thandai")),
            LocalDate.of(2026, 8, 26), new Festival("Janmashtami special", List.of("makhan", "sweets"))
    );

    @Override
    public Optional<ContextSuggestion> evaluate(ContextInput input) {
        Festival festival = FESTIVAL_CALENDAR.get(input.getNow().toLocalDate());
        if (festival == null) {
            return Optional.empty();
        }

        ContextSuggestion suggestion = new ContextSuggestion();
        suggestion.setMessage(festival.message());
        suggestion.setSuggestedTags(festival.tags());
        return Optional.of(suggestion);
    }
}
