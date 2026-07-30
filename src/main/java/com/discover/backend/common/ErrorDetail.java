package com.discover.backend.common;

import java.util.List;

public record ErrorDetail(String code, String message, List<String> details) {
}
