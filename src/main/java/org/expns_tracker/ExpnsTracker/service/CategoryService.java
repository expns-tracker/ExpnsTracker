package org.expns_tracker.ExpnsTracker.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.expns_tracker.ExpnsTracker.entity.Category;
import org.expns_tracker.ExpnsTracker.repository.CategoryRepository;
import org.expns_tracker.ExpnsTracker.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final Map<String, String> categoryCache = new ConcurrentHashMap<>();

    @PostConstruct
    public Map<String, String> getCategoriesMap() throws ExecutionException, InterruptedException {
        if (categoryCache.isEmpty()) {
            List<Category> categories = categoryRepository.findAll();

            categories.forEach((category) -> {
                categoryCache.put(category.getCategoryId(), category.getCategoryName());
            });
        }

        return categoryCache;
    }

    public String getCategoryName(String categoryId){
        return categoryCache.get(categoryId);
    }

}
