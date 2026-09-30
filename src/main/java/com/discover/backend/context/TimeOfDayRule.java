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
            suggestion.setSuggestedTags(List.of("breakfast", "light", "healthy", "warm", "baked",
                    "bread", "eggy", "south-indian", "tea", "coffee", "beverage", "vegetarian"));
        } else if (hour >= 11 && hour < 16) {
            suggestion.setMessage("Time for lunch");
            suggestion.setSuggestedTags(List.of("lunch", "filling", "rich", "spicy", "thali", "rice",
                    "bread", "curry", "north-indian", "south-indian", "main-course"));
        } else if (hour >= 16 && hour < 20) {
            suggestion.setMessage("Evening snack time");
            suggestion.setSuggestedTags(List.of("snack", "fried", "crispy", "tangy", "spicy", "chaat",
                    "street-food", "savory", "baked", "tea", "coffee", "beverage"));
        } else {
            suggestion.setMessage("Late night cravings");
            suggestion.setSuggestedTags(List.of("dinner", "rich", "filling", "comfort-food", "spicy",
                    "grilled", "tandoori", "non-vegetarian", "dessert", "sweet", "main-course"));
        }
        return Optional.of(suggestion);
    }
}
