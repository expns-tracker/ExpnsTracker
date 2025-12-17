package org.expns_tracker.ExpnsTracker.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @GetMapping("/transactions")
    public String transactions(){
        return "transactions";
    }

    @GetMapping("/new")
    public String New(){
        return "new";
    }

    @GetMapping("/admin")
    public String admin(){
        return "admin";
    }

    @GetMapping("/settings")
    public String settings(){
        return "settings";
    }

    @GetMapping("/profile")
    public String profile(){
        return "profile";
    }

    @GetMapping("/import")
    public String imports(){
        return "import";
    }

    @GetMapping("/dashboard")
    public String dashboard(){
        return "dashboard";
    }

    @GetMapping("/charts")
    public String charts(){
        return "charts";
    }

    @GetMapping("/nav")
    public String nav(){
        return "navbar";
    }

    @GetMapping("/register")
    public String register(){

        if (isAuthenticated()) {
            return "redirect:/home";
        }
        return "auth/register";
    }

    @GetMapping("/login")
    public String login(){

        if (isAuthenticated()) {
            return "redirect:/";
        }
        return "auth/login";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || AnonymousAuthenticationToken.class.
                isAssignableFrom(authentication.getClass())) {
            return false;
        }
        return authentication.isAuthenticated();
    }
}
