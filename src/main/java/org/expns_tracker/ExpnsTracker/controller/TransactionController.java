package org.expns_tracker.ExpnsTracker.controller;

import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.dto.TransactionDto;
import org.expns_tracker.ExpnsTracker.entity.Category;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.repository.CategoryRepository;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.service.CategoryService;
import org.expns_tracker.ExpnsTracker.service.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @GetMapping("")
    public String transactions(Model model,
                                   @AuthenticationPrincipal String userId,
                                   @RequestParam(defaultValue = "0") int page) {
        model.addAttribute("activePage", "transactions");

        if (userId != null) {
            int pageSize = 20;

            List<TransactionDto> transactions = transactionService.getTransactions(userId, page, pageSize);

            // Calculate pagination metadata
            long totalItems = transactionService.countTransactions(userId);
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);

            model.addAttribute("transactions", transactions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
        }
        return "transactions/transactions";
    }

    @GetMapping("/new")
    public String showAddTransactionForm(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("types", TransactionType.values());
        try {
            Map<String, String> categoriesMap = categoryService.getCategoriesMap();
            model.addAttribute("categoriesMap", categoriesMap);

        } catch (Exception e) {
            model.addAttribute("categoriesMap", new HashMap<>());
        }
        return "transactions/new";
    }

    @PostMapping("/new")
    public String addTransaction(
            @ModelAttribute("transaction") Transaction transaction,
            BindingResult bindingResult,
            @AuthenticationPrincipal String userId
    ) {
        if (bindingResult.hasErrors()) {
            return "transactions/new";
        }

        transactionService.save(transaction, userId);

        return "redirect:/transactions";
    }
}
