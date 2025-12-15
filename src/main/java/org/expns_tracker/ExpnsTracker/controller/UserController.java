package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.Currency;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/complete")
    public String showCompleteProfileForm(Model model, @AuthenticationPrincipal String userId) {

        User user = userService.getUser(userId); // user logat

        model.addAttribute("user", user);
        model.addAttribute("currencies", Currency.values());

        return "profile/user_profile_form";
    }

    @PostMapping("/complete")
    public String completeProfile(@ModelAttribute("user") User user) {

        user.setProfileCompleted(true);
        userService.save(user);

        return "redirect:/";
    }
}
