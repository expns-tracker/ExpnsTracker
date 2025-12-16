package org.expns_tracker.ExpnsTracker.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.Dto.ReportRequest;
import org.expns_tracker.ExpnsTracker.service.CsvExportService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final CsvExportService csvExportService;

    @GetMapping
    public String showReportPage(Model model) {
        ReportRequest request = new ReportRequest();
        request.setStartDate(LocalDate.now().withDayOfMonth(1));
        request.setEndDate(LocalDate.now());

        model.addAttribute("reportRequest", request);
        return "reports/reports";
    }

    @PostMapping("/generate")
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

        return "redirect:/reports";
    }

    @GetMapping("/download")
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


}