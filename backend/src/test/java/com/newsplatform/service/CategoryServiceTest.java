package com.newsplatform.service;

import com.newsplatform.entity.Category;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour CategoryService.
 * Tests simplifiés et fonctionnels respectant les bonnes pratiques.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Tests Unitaires")
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testCategoryId = UUID.randomUUID();
    }

    // ==================== TESTS CRÉATION ====================

    @Test
    @DisplayName("createRootCategory - Doit créer une catégorie racine valide")
    void createRootCategory_ShouldCreateValidRootCategory() {
        // Given
        String name = "Technologie";
        String description = "Articles technologiques";
        Category savedCategory = new Category(name, description);
        
        when(categoryRepository.existsByName(name)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Category result = categoryService.createRootCategory(name, description);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        verify(categoryRepository).existsByName(name);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createRootCategory - Doit rejeter un nom vide")
    void createRootCategory_ShouldRejectEmptyName() {
        // When & Then
        assertThatThrownBy(() -> categoryService.createRootCategory("", "Description"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("nom de la catégorie est obligatoire");
        
        verifyNoInteractions(categoryRepository);
    }

    @Test
    @DisplayName("createRootCategory - Doit rejeter un nom déjà existant")
    void createRootCategory_ShouldRejectDuplicateName() {
        // Given
        String name = "Technologie";
        when(categoryRepository.existsByName(name)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> categoryService.createRootCategory(name, "Description"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("catégorie avec ce nom existe déjà");
        
        verify(categoryRepository).existsByName(name);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSubCategory - Doit créer une sous-catégorie valide")
    void createSubCategory_ShouldCreateValidSubCategory() {
        // Given
        String name = "Intelligence Artificielle";
        String description = "Articles sur l'IA";
        UUID parentId = UUID.randomUUID();
        Category parentCategory = new Category("Technologie", "Parent category");
        Category savedCategory = new Category(name, description, parentCategory);
        
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parentCategory));
        when(categoryRepository.existsByName(name)).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        // When
        Category result = categoryService.createSubCategory(name, description, parentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(name);
        verify(categoryRepository).findById(parentId);
        verify(categoryRepository).existsByName(name);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("createSubCategory - Doit rejeter un parent inexistant")
    void createSubCategory_ShouldRejectNonExistentParent() {
        // Given
        UUID parentId = UUID.randomUUID();
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.createSubCategory("IA", "Description", parentId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Catégorie parente non trouvée");
        
        verify(categoryRepository).findById(parentId);
        verify(categoryRepository, never()).existsByName(any());
        verify(categoryRepository, never()).save(any());
    }

    // ==================== TESTS LECTURE ====================

    @Test
    @DisplayName("getCategoryById - Doit retourner la catégorie existante")
    void getCategoryById_ShouldReturnExistingCategory() {
        // Given
        Category category = new Category("Tech", "Technology");
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(category));

        // When
        Category result = categoryService.getCategoryById(testCategoryId);

        // Then
        assertThat(result).isEqualTo(category);
        verify(categoryRepository).findById(testCategoryId);
    }

    @Test
    @DisplayName("getCategoryById - Doit lancer ResourceNotFoundException pour ID inexistant")
    void getCategoryById_ShouldThrowNotFoundForNonExistentId() {
        // Given
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategoryById(testCategoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Catégorie non trouvée");
        
        verify(categoryRepository).findById(testCategoryId);
    }

    @Test
    @DisplayName("getCategoryBySlug - Doit retourner la catégorie avec le slug donné")
    void getCategoryBySlug_ShouldReturnCategoryWithSlug() {
        // Given
        String slug = "technologie";
        Category category = new Category("Technologie", "Technology articles");
        when(categoryRepository.findBySlug(slug)).thenReturn(Optional.of(category));

        // When
        Category result = categoryService.getCategoryBySlug(slug);

        // Then
        assertThat(result).isEqualTo(category);
        verify(categoryRepository).findBySlug(slug);
    }

    // ==================== TESTS MODIFICATION ====================

    @Test
    @DisplayName("updateCategory - Doit mettre à jour une catégorie existante")
    void updateCategory_ShouldUpdateExistingCategory() {
        // Given
        String newName = "Nouvelle Technologie";
        String newDescription = "Nouvelle description";
        Category existingCategory = spy(new Category("Technologie", "Description"));
        
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName(newName)).thenReturn(false);
        when(categoryRepository.save(existingCategory)).thenReturn(existingCategory);

        // When
        Category result = categoryService.updateCategory(testCategoryId, newName, newDescription);

        // Then
        assertThat(result).isNotNull();
        verify(categoryRepository).findById(testCategoryId);
        verify(categoryRepository).existsByName(newName);
        verify(categoryRepository).save(existingCategory);
        verify(existingCategory).updateInfo(newName, newDescription);
    }

    @Test
    @DisplayName("moveCategory - Doit empêcher qu'une catégorie devienne son propre parent")
    void moveCategory_ShouldPreventSelfParenting() {
        // When & Then
        assertThatThrownBy(() -> categoryService.moveCategory(testCategoryId, testCategoryId))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Une catégorie ne peut pas être son propre parent");
        
        verifyNoInteractions(categoryRepository);
    }

    // ==================== TESTS SUPPRESSION ====================

    @Test
    @DisplayName("deleteCategory - Doit supprimer une catégorie vide")
    void deleteCategory_ShouldDeleteEmptyCategory() {
        // Given
        Category emptyCategory = spy(new Category("Empty", "Description"));
        when(emptyCategory.canBeDeleted()).thenReturn(true);
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(emptyCategory));

        // When
        categoryService.deleteCategory(testCategoryId);

        // Then
        verify(categoryRepository).findById(testCategoryId);
        verify(categoryRepository).delete(emptyCategory);
        verify(emptyCategory).canBeDeleted();
    }

    @Test
    @DisplayName("deleteCategory - Doit empêcher la suppression d'une catégorie avec contenu")
    void deleteCategory_ShouldPreventDeletionWithContent() {
        // Given
        Category categoryWithContent = spy(new Category("With Content", "Description"));
        when(categoryWithContent.canBeDeleted()).thenReturn(false);
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(categoryWithContent));

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(testCategoryId))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("contenant des articles ou des sous-catégories");
        
        verify(categoryRepository).findById(testCategoryId);
        verify(categoryRepository, never()).delete(any());
        verify(categoryWithContent).canBeDeleted();
    }

    // ==================== TESTS VALIDATION ====================

    @Test
    @DisplayName("createRootCategory - Doit valider la longueur du nom")
    void createRootCategory_ShouldValidateNameLength() {
        // When & Then - nom trop court
        assertThatThrownBy(() -> categoryService.createRootCategory("A", "Description"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("entre 2 et 100 caractères");
        
        // When & Then - nom trop long
        String longName = "A".repeat(101);
        assertThatThrownBy(() -> categoryService.createRootCategory(longName, "Description"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("entre 2 et 100 caractères");
        
        verifyNoInteractions(categoryRepository);
    }

    @Test
    @DisplayName("createRootCategory - Doit valider la longueur de la description")
    void createRootCategory_ShouldValidateDescriptionLength() {
        // Given
        String validName = "Technologie";
        String longDescription = "A".repeat(501);

        // When & Then
        assertThatThrownBy(() -> categoryService.createRootCategory(validName, longDescription))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("ne peut pas dépasser 500 caractères");
        
        verifyNoInteractions(categoryRepository);
    }
} 