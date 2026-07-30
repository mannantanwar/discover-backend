package com.discover.backend.security;

import com.discover.backend.event.EventService;
import com.discover.backend.user.User;
import com.discover.backend.user.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;
    private final EventService eventService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        try {
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String registrationId = token.getAuthorizedClientRegistrationId();
            String sub = getTheProviderSubFromOAuth(oauth2User, registrationId);
            String email = (String) oauth2User.getAttribute("email");
            String displayName = (String) oauth2User.getAttribute("name");
            String avatarUrl = (String) oauth2User.getAttribute("picture");

            User user = userService.upsertFromOAuth(email, sub, displayName, avatarUrl, registrationId.toUpperCase());

            eventService.record(user, "LOGIN", null, null, Map.of("source", registrationId));

            String jwt = jwtService.generateToken(user.getPublicId());

            response.setContentType("application/json");
            response.getWriter().write("{\"token\":\"" + jwt + "\"}");

        } catch (Exception ex) {
            log.error("Google OAuth2 login failed", ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":{\"code\":\"LOGIN_FAILED\",\"message\":\"Something went wrong while signing you in. Please try again.\"}}"
            );
        }
    }


    private String getTheProviderSubFromOAuth(OAuth2User user, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) user.getAttribute("sub");
            default -> "";
        };
    }
}
