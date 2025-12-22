package org.expns_tracker.ExpnsTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    // =========================
    // VIEW FEEDBACK DETAILS
    // =========================
    @GetMapping("/{id}")
    public String viewFeedback(@PathVariable Long id, Model model) {

        // ----- DUMMY DATA -----
        Map<String, Object> feedback = new HashMap<>();
        feedback.put("id", id);
        feedback.put("userEmail", "john@example.com");
        feedback.put("category", "Bug");
        feedback.put("message", "The app crashes when I submit a transaction.");
        feedback.put("createdAt", LocalDate.now().minusDays(2).toString());

        model.addAttribute("feedback", feedback);

        return "admin/admin-feedback";
    }

    // =========================
    // RESPOND TO FEEDBACK
    // =========================
    @PostMapping("/respond")
    public String respondToFeedback(@RequestParam Long id,
                                    @RequestParam String response) {

        // TODO: Save response to database
        // TODO: Mark feedback as responded
        // TODO: Notify user (email / in-app)

        System.out.println("Feedback ID: " + id);
        System.out.println("Admin Response: " + response);

        return "redirect:/admin/dashboard";
    }
}
