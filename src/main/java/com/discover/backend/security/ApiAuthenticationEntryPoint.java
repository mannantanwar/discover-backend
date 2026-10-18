package com.discover.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

// what Spring Security does when an /api/** request isn't logged in (no token, bad or expired token).
// Runs inside the security filters, before any controller — so GlobalExceptionHandler never sees this
// case, and the error JSON is written by hand here in the same shape as ErrorResponse.
@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String BODY =
            "{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Login required or token expired.\",\"details\":[]}}";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(BODY);
    }
}
