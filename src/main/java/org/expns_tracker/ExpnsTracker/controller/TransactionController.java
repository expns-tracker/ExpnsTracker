package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.dto.TransactionDto;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.service.CategoryService;
import org.expns_tracker.ExpnsTracker.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Log4j2
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
            @AuthenticationPrincipal String userId,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "transactions/new";
        }

        transactionService.addTransaction(transaction, userId);

        redirectAttributes.addFlashAttribute(
                "successMessage", "Transaction added successfully!"
        );

        return "redirect:/transactions";
    }
    @GetMapping("/edit/{id}")
    public String showEditTransactionForm(@PathVariable String id, Model model) throws ExecutionException, InterruptedException {
        Transaction transaction = transactionRepository.findById(id);

        if (transaction == null) {
            return "redirect:/transactions";
        }

        try {
            Map<String, String> categoriesMap = categoryService.getCategoriesMap();
            model.addAttribute("categoriesMap", categoriesMap);

        } catch (Exception e) {
            model.addAttribute("categoriesMap", new HashMap<>());
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute("types", TransactionType.values());
        return "transactions/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateTransaction(
            @PathVariable String id,
            @ModelAttribute("transaction") Transaction transaction,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            log.error(Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            try {
                Map<String, String> categoriesMap = categoryService.getCategoriesMap();
                model.addAttribute("categoriesMap", categoriesMap);

            } catch (Exception e) {
                model.addAttribute("categoriesMap", new HashMap<>());
            }

            model.addAttribute("transaction", transaction);
            model.addAttribute("types", TransactionType.values());

            return "transactions/edit";
        }

        transactionService.updateTransaction(transaction);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Transaction updated successfully!"
        );

        return "redirect:/transactions";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransaction(
            @PathVariable String id,
            @AuthenticationPrincipal String userId,
            RedirectAttributes redirectAttributes
    ) {

        if (!userId.equals(transactionService.getTransaction(id).getUserId())){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not authorized to delete this transaction."
            );
        }

        transactionService.delete(id, userId);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Transaction has been deleted!"
        );
        return "redirect:/transactions";
    }

    @GetMapping("/all")
    public String listTransactions(Model model, @AuthenticationPrincipal String userId) throws ExecutionException, InterruptedException {
        model.addAttribute("transactions",
                transactionRepository.findByUserId(userId));
        return "transactions";
    }
}
