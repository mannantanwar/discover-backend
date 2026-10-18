package com.discover.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthFilter jwtAuthFilter;
    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf->csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/saved-places/**").authenticated()
                        .requestMatchers("/api/v1/taste-profile/**").authenticated()
                        .requestMatchers("/api/v1/users/onboarding-seen").authenticated()
                        .requestMatchers("/api/v1/home-feed/**").authenticated()
                        .anyRequest().permitAll()
                )
                // /api/** gets a clean 401 JSON when not logged in; everything else (browser login links)
                // keeps oauth2Login's default of redirecting to Google
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        apiAuthenticationEntryPoint,
                        PathPatternRequestMatcher.withDefaults().matcher("/api/**")))
                .oauth2Login(oauth2->
                    oauth2.successHandler(oAuth2SuccessHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
