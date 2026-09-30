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
            LocalDate.of(2026, 3, 4), new Festival(
                    "Holi vibes — gujiya, thandai and something festive?",
                    List.of("sweet", "fried", "crispy", "milky", "creamy", "cooling", "refreshing",
                            "nutty", "saffron", "cardamom", "dry-fruit", "tangy", "spicy", "chaat",
                            "street-food", "festive", "dessert", "beverage", "snack", "vegetarian")),
            LocalDate.of(2026, 8, 26), new Festival(
                    "Janmashtami special — makhan, mishri and milky sweets",
                    List.of("sweet", "milky", "buttery", "creamy", "nutty", "dry-fruit",
                            "cardamom", "saffron", "fasting-friendly", "festive", "dessert",
                            "mithai", "vegetarian")),
            LocalDate.of(2026, 11, 8), new Festival(
                    "Diwali is here — mithai, namkeen and everything festive",
                    List.of("sweet", "syrupy", "rich", "ghee", "milky", "creamy", "nutty",
                            "dry-fruit", "saffron", "cardamom", "fried", "crispy", "savory",
                            "namkeen", "mithai", "festive", "dessert", "snack", "vegetarian"))
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
