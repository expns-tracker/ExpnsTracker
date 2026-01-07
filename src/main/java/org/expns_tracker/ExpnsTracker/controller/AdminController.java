package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.config.ApplicationProperties;
import org.expns_tracker.ExpnsTracker.entity.Feedback;
import org.expns_tracker.ExpnsTracker.entity.User;
import org.expns_tracker.ExpnsTracker.entity.enums.Role;
import org.expns_tracker.ExpnsTracker.service.FeedbackService;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Log4j2
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ApplicationProperties applicationProperties;
    private final FeedbackService feedbackService;

    @GetMapping
    public String adminDashboard(Model model, @RequestParam(required = false) String query) {
        List<User> allUsers = userService.getAllUsers();
        if (query != null && !query.isEmpty()) {
            allUsers = allUsers.stream()
                    .filter(u -> u.getEmail().toLowerCase().contains(query.toLowerCase()) ||
                            u.getName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
        log.info("Loaded {} users", allUsers.size());
        long activeCount = allUsers.stream().filter(User::getIsActive).count();
        long adminCount = allUsers.stream().filter(u -> u.getRole() == Role.ADMIN).count();

        model.addAttribute("users", allUsers);
        model.addAttribute("totalUsers", allUsers.size());
        model.addAttribute("inactiveUsers", allUsers.size() - activeCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("activePage", "admin");
        model.addAttribute("query", query);
        model.addAttribute("superadmin", applicationProperties.getSuperadmin());

        return "admin/admin";
    }

    @PostMapping("/users/toggle-status")
    public String toggleUserStatus(@RequestParam String userId,
                                   @AuthenticationPrincipal String currentAdminId,
                                   RedirectAttributes ra) {

        if (userId.equals(currentAdminId)) {
            ra.addFlashAttribute("errorMessage", "You cannot deactivate your own account.");
            return "redirect:/admin";
        }

        try {
            userService.toggleUserStatus(userId);
            ra.addFlashAttribute("successMessage", "User status updated.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", "You do not have permission to make this user inactive.");
        }  catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to update status.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/promote")
    public String promoteUser(@RequestParam String userId, RedirectAttributes ra) {
        try {
            userService.promoteToAdmin(userId);
            ra.addFlashAttribute("successMessage", "User promoted to Admin.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to promote user.");
        }
        return "redirect:/admin";
    }

    @PostMapping("/users/demote")
    public String demoteAdmin(@RequestParam String userId,
                              @AuthenticationPrincipal String currentAdminId,
                              RedirectAttributes ra) {
        if (userId.equals(currentAdminId)) {
            ra.addFlashAttribute("errorMessage", "You cannot demote yourself.");
            return "redirect:/admin";
        }

        try {
            userService.revokeAdminRole(userId);
            ra.addFlashAttribute("successMessage", "Admin role revoked.");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", "You do not have permission to revoke admin rol for this user.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Failed to revoke admin role.");
        }
        return "redirect:/admin";
    }

    @GetMapping("/feedbacks")
    public String listFeedbacks(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String category) {
        int pageSize = 10;

        List<Feedback> feedbacks = feedbackService.getFeedbacksPage(page, pageSize, status, category);

        long totalItems = feedbackService.getTotalCount(status);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        model.addAttribute("feedbacks", feedbacks);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentCategory", category);
        model.addAttribute("activePage", "admin");

        return "admin/admin-feedback";
    }

    @PostMapping("/feedbacks/resolve/{id}")
    public String resolveFeedback(@PathVariable String id, RedirectAttributes ra) {
        try {
            feedbackService.markAsResolved(id);
            ra.addFlashAttribute("successMessage", "Feedback marked as resolved.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Error updating feedback.");
        }
        return "redirect:/admin/feedbacks";
    }
}
