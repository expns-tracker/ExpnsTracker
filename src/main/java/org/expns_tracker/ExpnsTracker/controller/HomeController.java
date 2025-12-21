package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.dto.TransactionDto;
import org.expns_tracker.ExpnsTracker.mapper.TransactionDtoMapper;
import org.expns_tracker.ExpnsTracker.service.TransactionService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final TransactionService transactionService;
    private final TransactionDtoMapper transactionDtoMapper;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal String userId) {
        model.addAttribute("activePage", "home");

        if (userId != null) {
            List<TransactionDto> recent = transactionService.getRecentTransactions(userId, 5)
                    .stream()
                    .map(transactionDtoMapper::map)
                    .toList();
            model.addAttribute("recentTransactions", recent);
        }

        return "index";
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
