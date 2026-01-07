package org.expns_tracker.ExpnsTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "0") int page,
                            Model model) {

        // =========================
        // STATS (DUMMY)
        // =========================
        model.addAttribute("activations", 1440);
        model.addAttribute("deactivated", 234);
        model.addAttribute("feedbackResponses", 333);
        model.addAttribute("casesSolved", 128);

        // =========================
        // ACCOUNT REQUESTS (DUMMY)
        // =========================
        List<Map<String, Object>> accountRequests = new ArrayList<>();

        Map<String, Object> req1 = new HashMap<>();
        req1.put("id", 1L);
        req1.put("email", "alex@example.com");
        req1.put("type", "Activation");
        req1.put("reason", "Email verification pending");
        req1.put("date", "06 Jun 2025");
        accountRequests.add(req1);

        Map<String, Object> req2 = new HashMap<>();
        req2.put("id", 2L);
        req2.put("email", "sarah@example.com");
        req2.put("type", "Deactivation");
        req2.put("reason", "User requested account closure");
        req2.put("date", "05 Jun 2025");
        accountRequests.add(req2);

        model.addAttribute("accountRequests", accountRequests);

        // =========================
        // CURRENT FEEDBACK (DUMMY)
        // =========================
        List<Map<String, Object>> currentFeedback = new ArrayList<>();

        Map<String, Object> fb1 = new HashMap<>();
        fb1.put("id", 1L);
        fb1.put("email", "john@example.com");
        fb1.put("category", "Bug");
        fb1.put("message", "App crashes on submit");
        fb1.put("date", "05 Jun 2025");
        currentFeedback.add(fb1);

        Map<String, Object> fb2 = new HashMap<>();
        fb2.put("id", 2L);
        fb2.put("email", "anna@example.com");
        fb2.put("category", "Feature");
        fb2.put("message", "Export to CSV request");
        fb2.put("date", "04 Jun 2025");
        currentFeedback.add(fb2);

        model.addAttribute("currentFeedback", currentFeedback);

        // =========================
        // FEEDBACK HISTORY (DUMMY)
        // =========================
        List<Map<String, Object>> feedbackHistory = new ArrayList<>();

        Map<String, Object> hist1 = new HashMap<>();
        hist1.put("id", 3L);
        hist1.put("email", "mark@example.com");
        hist1.put("category", "UX");
        hist1.put("date", "01 Jun 2025");
        feedbackHistory.add(hist1);

        Map<String, Object> hist2 = new HashMap<>();
        hist2.put("id", 4L);
        hist2.put("email", "lisa@example.com");
        hist2.put("category", "Performance");
        hist2.put("date", "30 May 2025");
        feedbackHistory.add(hist2);

        model.addAttribute("feedbackHistory", feedbackHistory);

        // =========================
        // PAGINATION (DUMMY)
        // =========================
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", 3);

        // =========================
        // NOTIFICATIONS (DUMMY)
        // =========================
        model.addAttribute("notificationCount",
                currentFeedback.size() + accountRequests.size());

        return "admin/dashboard";
    }
}
