package com.discover.backend.context;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class TimeOfDayRule implements ContextRule {

    @Override
    public Optional<ContextSuggestion> evaluate(ContextInput input) {
        int hour = input.getNow().getHour();

        ContextSuggestion suggestion = new ContextSuggestion();
        if (hour >= 5 && hour < 11) {
            suggestion.setMessage("Good morning! Start your day right");
            suggestion.setSuggestedTags(List.of("breakfast", "chai"));
        } else if (hour >= 11 && hour < 16) {
            suggestion.setMessage("Time for lunch");
            suggestion.setSuggestedTags(List.of("lunch", "thali"));
        } else if (hour >= 16 && hour < 20) {
            suggestion.setMessage("Evening chai time");
            suggestion.setSuggestedTags(List.of("chai", "snacks"));
        } else {
            suggestion.setMessage("Late night bites");
            suggestion.setSuggestedTags(List.of("dinner", "comfort-food"));
        }
        return Optional.of(suggestion);
    }
}
