package org.expns_tracker.ExpnsTracker.controller;

import com.google.firebase.auth.FirebaseAuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/session-login")
    public ResponseEntity<?> sessionLogin(HttpServletRequest request, @RequestHeader("Authorization") String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Authorization header missing or invalid");
        }

        HttpSession session = request.getSession(true);

        try {
            String authToken = authHeader.replace("Bearer ", "");
            authService.authenticateUser(authToken, session);

        } catch (FirebaseAuthException | ExecutionException | InterruptedException e) {
            log.error(e);
            return ResponseEntity.badRequest().body("Authentication failed");
        }

        return ResponseEntity.ok("Login successful");
    }
}
