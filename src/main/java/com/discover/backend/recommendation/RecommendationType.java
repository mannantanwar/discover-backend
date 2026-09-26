package com.discover.backend.recommendation;

import lombok.Getter;

@Getter
public enum RecommendationType {

    RULE_BASED("Recommendations based on tags from dishes you've rated 4 stars and above, "
            + "falling back to popularity if you have no rating history yet.");

    private final String description;

    RecommendationType(String description) {
        this.description = description;
    }
}
