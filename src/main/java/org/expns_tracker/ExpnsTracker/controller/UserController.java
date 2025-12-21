package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String getProfile() {

        return "profile/profile";
    }

    @PostMapping("/update")
    public String completeProfile(@ModelAttribute("currentUser")User user,
                                  RedirectAttributes redirectAttributes) {

        user.setProfileCompleted(true);
        userService.save(user);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Profile updated successfully!"
        );
        return "redirect:/profile";
    }

    @GetMapping("/settings")
    public String settings(){
        return "profile/settings";
    }


    @PostMapping("/settings/update")
    public String updateSettings(@ModelAttribute("currentUser") User user,
                                 RedirectAttributes redirectAttributes) {
        userService.save(user);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Settings updated successfully!"
        );
        return "redirect:/profile";
    }
}
