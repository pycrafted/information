package com.newsplatform.repository;

import com.newsplatform.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests simples pour vérifier que les repositories de base fonctionnent.
 * Tests de configuration JPA/Spring Data de base.
 */
@DataJpaTest
@ActiveProfiles("test")
public class SimpleRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void testCategoryRepository() {
        // Arrange - Utiliser constructeur DDD
        Category category = new Category("Test Category", "Description test");

        // Act
        Category saved = categoryRepository.save(category);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Category", saved.getName());
        assertEquals("test-category", saved.getSlug()); // Auto-généré par DDD
        assertEquals("Description test", saved.getDescription());
    }
} 