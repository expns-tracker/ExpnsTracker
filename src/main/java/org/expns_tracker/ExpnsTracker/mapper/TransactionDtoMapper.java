package org.expns_tracker.ExpnsTracker.mapper;

import lombok.RequiredArgsConstructor;
import org.expns_tracker.ExpnsTracker.dto.TransactionDto;
import org.expns_tracker.ExpnsTracker.entity.Transaction;
import org.expns_tracker.ExpnsTracker.service.CategoryService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionDtoMapper {
    private final CategoryService categoryService;

    public TransactionDto map(Transaction transaction) {
        return TransactionDto.builder()
                .id(transaction.getId())
                .date(transaction.getDate())
                .type(transaction.getType())
                .userId(transaction.getUserId())
                .amount(transaction.getAmount())
                .categoryName(categoryService.getCategoryName(transaction.getCategoryId()))
                .description(transaction.getDescription())
                .build();
    }

}
