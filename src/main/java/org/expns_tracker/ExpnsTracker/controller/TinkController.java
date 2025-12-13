package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.expns_tracker.ExpnsTracker.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tink")
@Log4j2
public class TinkController {
    final TinkService tinkService;
    final UserService userService;

    @GetMapping("/connect")
    public String startConnection(@AuthenticationPrincipal String userId){
        String tinkUserId = userService.getTinkUserId(userId);
        log.info("tinkUserId: {}", tinkUserId);
        return "redirect:" + this.tinkService.generateTinkLinkUrl(tinkUserId);
    }

    @GetMapping("/callback")
    public String callback(@AuthenticationPrincipal String userId,
                           @RequestParam(required = false) String error,
                           RedirectAttributes redirectAttributes) {

        if (error != null) {
            log.error("Callback error from Tink: {}", error);
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Failed connection: " + error
            );
            return "redirect:/";
        }

        String tinkUserId = userService.getTinkUserId(userId);

        String code = this.tinkService.getUserAccessCode(tinkUserId);

        try {
            log.info("Callback received for code: {}", code);
            String token = this.tinkService.getAccessToken(code);
            JsonNode transactionsJson = this.tinkService.fetchTransactions(token, null);
            log.info("Transactions JSON: {}", transactionsJson.toString());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Connection to bank account successful!"
            );

        } catch (Exception e) {
            log.error("Exception during bank sync", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Server error during bank sync: " + e.getMessage());
        }

        return "redirect:/";
    }

    @GetMapping("/load-transactions")
    public String loadTransactions(@AuthenticationPrincipal String userId,
                                   RedirectAttributes redirectAttributes) {
        String tinkUserId = userService.getTinkUserId(userId);
        try {
            String code = this.tinkService.getUserAccessCode(tinkUserId);
            String token = this.tinkService.getAccessToken(code);
            JsonNode transactions = this.tinkService.fetchTransactions(token, null);
            log.info("Transactions JSON: {}", transactions.toString());

            redirectAttributes.addFlashAttribute(
                    "successMessage", "Successfully loaded transactions!"
            );
        } catch (Exception e) {
            log.error("Exception during loading transactions", e);
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Server error during loading transactions!"
            );
        }

        return "redirect:/";
    }
}
