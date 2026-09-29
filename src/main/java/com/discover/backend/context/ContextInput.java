package com.discover.backend.context;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ContextInput {
    private Double latitude;
    private Double longitude;
    private LocalDateTime now;
}
