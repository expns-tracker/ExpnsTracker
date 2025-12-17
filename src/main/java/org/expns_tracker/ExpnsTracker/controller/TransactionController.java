package org.expns_tracker.ExpnsTracker.controller;

import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/new")
    public String showAddTransactionForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("types", TransactionType.values());
        return "new";
    }

    @PostMapping("/new")
    public String addTransaction(
            @ModelAttribute("transaction") Transaction transaction,
            BindingResult bindingResult,
            @AuthenticationPrincipal String userId
    ) {
        if (bindingResult.hasErrors()) {
            return "new";
        }

        transaction.setDate(Timestamp.now());
        transaction.setUserId(userId);

        // Firestore requires an ID; generate if missing
        if (transaction.getId() == null) {
            transaction.setId(java.util.UUID.randomUUID().toString());
        }

        transactionRepository.save(transaction);

        return "redirect:/transactions";
    }
}
