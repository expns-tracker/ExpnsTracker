package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;


@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalModelAttributes {

    private final UserService userService;

    @ModelAttribute
    public void addUserToModel(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)) {

            Object principal = auth.getPrincipal();

            if (principal instanceof String userId) {
                try {
                    User user = userService.getUser(userId);
                    model.addAttribute("currentUser", user);
                } catch (Exception e) {
                    log.error("Failed to load user into model for Id: {}", userId, e);
                }
            }
            else if (principal instanceof User user) {
                model.addAttribute("currentUser", user);
            }
        }
    }
}