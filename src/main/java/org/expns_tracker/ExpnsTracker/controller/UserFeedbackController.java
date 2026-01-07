package org.expns_tracker.ExpnsTracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/feedback")
public class UserFeedbackController {

    @GetMapping
    public String feedback(Model model) {

        List<Map<String, Object>> feedbackList = new ArrayList<>();

        feedbackList.add(Map.of(
                "id", 1L,
                "category", "Bug",
                "message", "App crashes on submit",
                "status", "RESPONDED",
                "date", "05 Jun 2025"
        ));

        feedbackList.add(Map.of(
                "id", 2L,
                "category", "Feature",
                "message", "Please add CSV export",
                "status", "OPEN",
                "date", "07 Jun 2025"
        ));

        model.addAttribute("feedbackList", feedbackList);
        return "feedback";
    }

    @GetMapping("/{id}")
    public String feedbackChat(@PathVariable Long id, Model model) {

        model.addAttribute("feedback", Map.of(
                "id", id,
                "category", "Bug",
                "status", "RESPONDED"
        ));

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("fromAdmin", false, "message", "The app crashes on submit"));
        messages.add(Map.of("fromAdmin", true, "message", "Thanks, we fixed this in the latest update."));
        messages.add(Map.of("fromAdmin", false, "message", "Confirmed, it works now."));

        model.addAttribute("messages", messages);
        return "feedback-chat";
    }

    @PostMapping
    public String submit(@RequestParam String category,
                         @RequestParam String message) {
        return "redirect:/feedback";
    }

    @PostMapping("/respond")
    public String respond(@RequestParam Long feedbackId,
                          @RequestParam String message) {
        return "redirect:/feedback/" + feedbackId;
    }
}
