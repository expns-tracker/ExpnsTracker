package org.expns_tracker.ExpnsTracker.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    // private final FirebaseTokenFilter firebaseTokenFilter; // dacă îl folosești

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/session-login").anonymous()
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
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler) // 👈 Aici setăm CustomAuthenticationSuccessHandler
                        .permitAll()
                )
        // .addFilterBefore(firebaseTokenFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}
