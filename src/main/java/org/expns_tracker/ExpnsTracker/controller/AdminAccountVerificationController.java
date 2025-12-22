package org.expns_tracker.ExpnsTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountVerificationController {

    // =========================
    // VIEW ACCOUNT VERIFICATION
    // =========================
    @GetMapping("/verify/{id}")
    public String verifyAccount(@PathVariable Long id, Model model) {

        // ----- ACCOUNT (DUMMY) -----
        Map<String, Object> account = new HashMap<>();
        account.put("id", id);
        account.put("email", "alex@example.com");
        account.put("status", "PENDING");

        model.addAttribute("account", account);

        // ----- CHAT MESSAGES (DUMMY) -----
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> m1 = new HashMap<>();
        m1.put("fromAdmin", false);
        m1.put("message", "I uploaded my ID for verification.");
        messages.add(m1);

        Map<String, Object> m2 = new HashMap<>();
        m2.put("fromAdmin", true);
        m2.put("message", "Thanks, we are reviewing your documents.");
        messages.add(m2);

        Map<String, Object> m3 = new HashMap<>();
        m3.put("fromAdmin", false);
        m3.put("message", "Please let me know if anything else is needed.");
        messages.add(m3);

        model.addAttribute("messages", messages);

        return "admin/admin-account-verification";
    }

    // =========================
    // SEND CHAT MESSAGE
    // =========================
    @PostMapping("/verify/respond")
    public String respond(@RequestParam Long accountId,
                          @RequestParam String message) {

        System.out.println("Account ID: " + accountId);
        System.out.println("Admin message: " + message);

        return "redirect:/admin/accounts/verify/" + accountId;
    }

    // =========================
    // APPROVE ACCOUNT
    // =========================
    @PostMapping("/verify/approve")
    public String approve(@RequestParam Long accountId) {

        System.out.println("Account approved: " + accountId);

        return "redirect:/admin/dashboard";
    }

    // =========================
    // REJECT ACCOUNT
    // =========================
    @PostMapping("/verify/reject")
    public String reject(@RequestParam Long accountId) {

        System.out.println("Account rejected: " + accountId);

        return "redirect:/admin/dashboard";
    }
}
