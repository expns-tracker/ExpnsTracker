package org.expns_tracker.ExpnsTracker.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.cloud.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.entity.enums.TransactionType;
import org.expns_tracker.ExpnsTracker.repository.CategoryRepository;
import org.expns_tracker.ExpnsTracker.service.CategoryService;
import org.expns_tracker.ExpnsTracker.service.LocalCategorizer;
import org.expns_tracker.ExpnsTracker.service.TinkService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Log4j2
public class TinkTransactionMapper {

    private final TinkService tinkService;
    private final CategoryService categoryService;
    private final LocalCategorizer localCategorizer;

    public Transaction mapTinkTransaction(JsonNode tinkTx, String userId) {
        log.info(tinkTx);

        String tinkId = tinkTx.get("id").asText();

        JsonNode valueNode = tinkTx.get("amount").get("value");
        long unscaled = Long.parseLong(valueNode.get("unscaledValue").asText());
        int scale = Integer.parseInt(valueNode.get("scale").asText());
        Double amount = unscaled * Math.pow(10, -scale);

        String dateStr = tinkTx.get("dates").get("booked").asText();
        LocalDate localDate = LocalDate.parse(dateStr);
        Date javaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Timestamp firestoreTimestamp = Timestamp.of(javaDate);

        String description = tinkTx.get("descriptions").get("display").asText();

        String category = tinkTx.has("categoryId") ? tinkTx.get("categoryId").asText() : null;

        Map<String, String> categoryMap;
        try {
             categoryMap = categoryService.getCategoriesMap();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        Set<String> categorySet = categoryMap.keySet();
        if (category != null && !categorySet.contains(category)) {
            category = tinkService.getCategoryParent(category);
        } else {
            category = localCategorizer.categorize(description, amount);
        }

        return Transaction.builder()
                .id(tinkId)
                .userId(userId)
                .amount(amount)
                .description(description)
                .date(firestoreTimestamp)
                .categoryId(category)
                .type(amount < 0 ? TransactionType.EXPENSE : TransactionType.INCOME)
                .build();
    }
}
