package org.expns_tracker.ExpnsTracker.service;

import org.expns_tracker.ExpnsTracker.entity.Category;
import org.expns_tracker.ExpnsTracker.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category1;
    private Category category2;

    @BeforeEach
    void setUp() {
        category1 = new Category();
        category1.setCategoryId("cat_1");
        category1.setCategoryName("Groceries");

        category2 = new Category();
        category2.setCategoryId("cat_2");
        category2.setCategoryName("Transport");
    }

    @Test
    void getCategoriesMap_InitializesCacheSuccessfully() throws ExecutionException, InterruptedException {

        when(categoryRepository.findAll()).thenReturn(List.of(category1, category2));

        Map<String, String> result = categoryService.getCategoriesMap();

        assertEquals(2, result.size());
        assertEquals("Groceries", result.get("cat_1"));
        assertEquals("Transport", result.get("cat_2"));

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoriesMap_ReturnsExistingCache_WhenAlreadyLoaded() throws ExecutionException, InterruptedException {

        when(categoryRepository.findAll()).thenReturn(List.of(category1));

        categoryService.getCategoriesMap();
        Map<String, String> result = categoryService.getCategoriesMap();

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get("cat_1"));

        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoriesMap_HandlesEmptyRepository() throws ExecutionException, InterruptedException {

        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        Map<String, String> result = categoryService.getCategoriesMap();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCategoryName_ReturnsCorrectName_WhenFound() throws ExecutionException, InterruptedException {
        when(categoryRepository.findAll()).thenReturn(List.of(category1));

        categoryService.getCategoriesMap();

        String name = categoryService.getCategoryName("cat_1");
        assertEquals("Groceries", name);
    }

    @Test
    void getCategoryName_ReturnsNull_WhenNotFound() throws ExecutionException, InterruptedException {

        when(categoryRepository.findAll()).thenReturn(List.of(category1));
        categoryService.getCategoriesMap();

        String name = categoryService.getCategoryName("non_existent_id");

        assertNull(name);
    }
}