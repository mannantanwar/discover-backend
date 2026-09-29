package com.discover.backend.context;

import lombok.Data;

import java.util.List;

@Data
public class ContextSuggestion {
    private String message;
    private List<String> suggestedTags;
}
