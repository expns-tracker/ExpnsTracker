package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

//    @GetMapping("/connect")
//    public String startConnection(){
//        return "redirect:" + this.tinkService.generateTinkLinkUrl();
//    }

    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String error,
                           RedirectAttributes redirectAttributes) {

        if (error != null) {
            log.error("Callback error from Tink: {}", error);
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Failed connection: " + error
            );
            return "redirect:/";
        }

        if (code == null) {
            log.error("Callback received but code is null!");
            redirectAttributes.addFlashAttribute(
                    "errorMessage", "Failed to authenticate: authorization code missing."
            );
            return "redirect:/";
        }

        try {
            String accessToken = String.valueOf(this.tinkService.getTokens(code));

            JsonNode transactionsJson = this.tinkService.fetchTransactions(accessToken);
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
}
