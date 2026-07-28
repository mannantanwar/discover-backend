package com.discover.backend.security;

import com.discover.backend.event.EventService;
import com.discover.backend.user.User;
import com.discover.backend.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepo;
    private final JwtService jwtService;
    private final EventService eventService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId();
        String sub = getTheProviderSubFromOAuth(oauth2User, registrationId);

        Optional<User> existingUser = userRepo.findByProviderSub(sub);

        User user;
        if (existingUser.isPresent()) {
            // we found the user by their Google sub — just log them in
            user = existingUser.get();
        } else {
            // no user with this Google sub yet — check if the email already exists first
            String email = getEmailFromOAuth(oauth2User);
            Optional<User> oldUser = userRepo.findByEmail(email);

            if (oldUser.isPresent()) {
                // this person signed up another way before — link this Google account to that existing user
                User linkedUser = oldUser.get();
                linkedUser.setProviderSub(sub);
                linkedUser.setAuthProvider(registrationId.toUpperCase());
                user = userRepo.save(linkedUser);
            } else {
                // genuinely new user
                User newUser = User.builder()
                        .publicId(UUID.randomUUID())
                        .email(email)
                        .emailVerified(true)
                        .displayName((String) oauth2User.getAttribute("name"))
                        .avatarUrl((String) oauth2User.getAttribute("picture"))
                        .authProvider(registrationId.toUpperCase())
                        .providerSub(sub)
                        .build();
                user = userRepo.save(newUser);
            }
        }

        eventService.record(user, "LOGIN", null, null, Map.of("source", registrationId));

        String jwt = jwtService.generateToken(user.getPublicId());

        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + jwt + "\"}");
    }

    private String getEmailFromOAuth(OAuth2User user) {
        return (String) user.getAttribute("email");
    }

    private String getTheProviderSubFromOAuth(OAuth2User user, String registrationId) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> (String) user.getAttribute("sub");
            default -> "";
        };
    }
}
