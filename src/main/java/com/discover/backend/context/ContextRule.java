package com.discover.backend.context;

import java.util.Optional;

public interface ContextRule {
    Optional<ContextSuggestion> evaluate (ContextInput input);
}
