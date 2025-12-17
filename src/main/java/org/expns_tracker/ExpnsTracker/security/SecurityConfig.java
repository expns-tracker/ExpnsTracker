package org.expns_tracker.ExpnsTracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/transactions",
                                "/new",
                                "/nav",
                                "/charts",
                                "/admin",
                                "/profile",
                                "/import",
                                "/dashboard",
                                "/settings",
                                "/api/auth/session-login",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico").permitAll()
                        .anyRequest().authenticated()
                )
//                .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}
