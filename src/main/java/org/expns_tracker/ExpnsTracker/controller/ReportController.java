package org.expns_tracker.ExpnsTracker.controller;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.dto.ReportRequest;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.repository.CategoryRepository;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.expns_tracker.ExpnsTracker.service.CategoryService;
import org.expns_tracker.ExpnsTracker.service.CsvExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final CsvExportService csvExportService;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @GetMapping("/export")
    public String showReportPage(Model model) {
        ReportRequest request = new ReportRequest();
        request.setStartDate(LocalDate.now().withDayOfMonth(1));
        request.setEndDate(LocalDate.now());

        model.addAttribute("reportRequest", request);
        return "reports/export_transactions";
    }

    @PostMapping("/export/generate")
    public String generateReport(@AuthenticationPrincipal String userId,
                                 @ModelAttribute ReportRequest request,
                                 RedirectAttributes redirectAttributes) {
        try {
            csvExportService.triggerExport(userId, request.getStartDate(), request.getEndDate());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Export started! The process runs in the background. Please wait.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred.");
        }

        return "redirect:/reports/export";
    }

    @GetMapping("/export/download")
    public ResponseEntity<Resource> downloadReport(@AuthenticationPrincipal String userId) {
        try {
            Resource csvContent = csvExportService.getLastExportResource(userId);

            if (csvContent == null || !csvContent.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=transactions_report.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .contentLength(csvContent.contentLength())
                    .body(csvContent);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("")
    public String showCharts(Model model,
                             @AuthenticationPrincipal String userId,
                             @RequestParam(required = false) LocalDate startDate,
                             @RequestParam(required = false) LocalDate endDate) {

        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("activePage", "charts");

        if (userId == null) return "reports/charts"; // Empty view if not logged in

        List<Transaction> transactions = fetchTransactions(userId, startDate, endDate);

        Map<String, Double> categoryMap = transactions.stream()
                .filter(t -> t.getAmount() < 0) // Only expenses
                .collect(Collectors.groupingBy(
                        t -> t.getCategoryId() != null ?
                                categoryService.getCategoryName(t.getCategoryId()) :
                                "Uncategorized",
                        Collectors.summingDouble(t -> Math.abs(t.getAmount()))
                ));

        model.addAttribute("categoryLabels", categoryMap.keySet());
        model.addAttribute("categoryValues", categoryMap.values());

        double totalIncome = transactions.stream()
                .filter(t -> t.getAmount() > 0)
                .mapToDouble(Transaction::getAmount).sum();

        double totalExpense = transactions.stream()
                .filter(t -> t.getAmount() < 0)
                .mapToDouble(t -> Math.abs(t.getAmount())).sum();

        model.addAttribute("flowLabels", List.of("Income", "Expenses"));
        model.addAttribute("flowValues", List.of(totalIncome, totalExpense));

        return "reports/charts";
    }

    private List<Transaction> fetchTransactions(String userId, LocalDate start, LocalDate end) {
        Date d1 = Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date d2 = Date.from(end.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        try {
            return transactionRepository.findByUserIdAndDateBetween(
                    userId,
                    com.google.cloud.Timestamp.of(d1),
                    com.google.cloud.Timestamp.of(d2)
            );
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }


}