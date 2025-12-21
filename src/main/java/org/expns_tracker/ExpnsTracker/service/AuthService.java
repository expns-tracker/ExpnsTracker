package org.expns_tracker.ExpnsTracker.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
    final UserRepository userRepository;

    public void authenticateUser(String token, HttpSession session) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);

        this.syncUser(decodedToken.getUid(), decodedToken.getEmail());

        List<String> roles = new ArrayList<>();

        var authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                .toList();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                decodedToken.getUid(),
                decodedToken.getEmail(),
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    private void syncUser(String uid, String email) {

        User user = userRepository.findById(uid);

        if (user != null) {
            return;
        }

        user = User.builder()
                .id(uid)
                .email(email)
                .profileCompleted(false)
                .notifBudget(true)
                .build();

        userRepository.save(user);
    }
}
