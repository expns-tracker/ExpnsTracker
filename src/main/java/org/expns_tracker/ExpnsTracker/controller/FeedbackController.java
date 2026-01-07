package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.service.FeedbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping
    public String showFeedbackForm(Model model) {

        model.addAttribute("activePage", "feedback");
        return "feedback/feedback";
    }

    @PostMapping
    public String submitFeedback(@ModelAttribute("currentUser") User user,
                                 String category,
                                 String message,
                                 RedirectAttributes ra) {
        try {
            feedbackService.submitFeedback(user, category, message);
            ra.addFlashAttribute("successMessage", "Thank you! Your feedback has been submitted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to submit feedback. Please try again.");
        }
        return "redirect:/";
    }
}