package com.newsplatform.service;

import com.newsplatform.entity.Category;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des catégories selon les principes DDD.
 * Couche Service : Logique métier et orchestration des opérations.
 * 
 * Responsabilités :
 * - CRUD complet avec validation métier
 * - Gestion de la hiérarchie des catégories
 * - Validation des règles d'intégrité
 * - Orchestration des opérations complexes
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2024
 */
@Service
@Transactional
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    
    // ===============================================
    // OPÉRATIONS DE CRÉATION
    // ===============================================
    
    /**
     * Crée une nouvelle catégorie racine.
     * Valide l'unicité du nom et slug.
     * 
     * @param name nom de la catégorie
     * @param description description de la catégorie
     * @return catégorie créée
     * @throws ValidationException si les données sont invalides
     */
    public Category createRootCategory(String name, String description) {
        validateCategoryName(name);
        validateCategoryDescription(description);
        
        // Vérifier l'unicité du nom
        if (categoryRepository.existsByName(name.trim())) {
            throw new ValidationException("Une catégorie avec ce nom existe déjà");
        }
        
        Category category = new Category(name.trim(), description != null ? description.trim() : null);
        return categoryRepository.save(category);
    }
    
    /**
     * Crée une nouvelle sous-catégorie.
     * Valide la hiérarchie et les contraintes métier.
     * 
     * @param name nom de la catégorie
     * @param description description de la catégorie
     * @param parentId ID de la catégorie parente
     * @return sous-catégorie créée
     * @throws ValidationException si les données sont invalides
     * @throws ResourceNotFoundException si le parent n'existe pas
     */
    public Category createSubCategory(String name, String description, UUID parentId) {
        validateCategoryName(name);
        validateCategoryDescription(description);
        
        // Récupérer la catégorie parente
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie parente non trouvée"));
        
        // Vérifier l'unicité du nom
        if (categoryRepository.existsByName(name.trim())) {
            throw new ValidationException("Une catégorie avec ce nom existe déjà");
        }
        
        Category category = new Category(name.trim(), description != null ? description.trim() : null, parent);
        return categoryRepository.save(category);
    }
    
    // ===============================================
    // OPÉRATIONS DE LECTURE
    // ===============================================
    
    /**
     * Récupère toutes les catégories avec pagination.
     * 
     * @param pageable configuration de pagination
     * @return page de catégories
     */
    @Transactional(readOnly = true)
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }
    
    /**
     * Récupère toutes les catégories racines.
     * 
     * @return liste des catégories racines
     */
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findRootCategories();
    }
    
    /**
     * Récupère une catégorie par son ID.
     * 
     * @param id identifiant de la catégorie
     * @return catégorie trouvée
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     */
    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
    }
    
    /**
     * Récupère une catégorie par son slug.
     * 
     * @param slug slug de la catégorie
     * @return catégorie trouvée
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     */
    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
    }
    
    /**
     * Récupère les sous-catégories d'une catégorie.
     * 
     * @param parentId ID de la catégorie parente
     * @return liste des sous-catégories
     */
    @Transactional(readOnly = true)
    public List<Category> getSubCategories(UUID parentId) {
        Category parent = getCategoryById(parentId);
        return categoryRepository.findDirectChildren(parent);
    }
    
    // ===============================================
    // OPÉRATIONS DE MODIFICATION
    // ===============================================
    
    /**
     * Met à jour les informations d'une catégorie.
     * 
     * @param id ID de la catégorie à modifier
     * @param name nouveau nom
     * @param description nouvelle description
     * @return catégorie mise à jour
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     * @throws ValidationException si les données sont invalides
     */
    public Category updateCategory(UUID id, String name, String description) {
        Category category = getCategoryById(id);
        
        validateCategoryName(name);
        validateCategoryDescription(description);
        
        // Vérifier l'unicité du nom si changé
        if (!category.getName().equals(name.trim()) && 
            categoryRepository.existsByName(name.trim())) {
            throw new ValidationException("Une catégorie avec ce nom existe déjà");
        }
        
        category.updateInfo(name.trim(), description != null ? description.trim() : null);
        return categoryRepository.save(category);
    }
    
    /**
     * Déplace une catégorie vers un nouveau parent.
     * 
     * @param categoryId ID de la catégorie à déplacer
     * @param newParentId ID du nouveau parent (null pour racine)
     * @return catégorie déplacée
     * @throws ResourceNotFoundException si les catégories n'existent pas
     * @throws ValidationException si le déplacement viole les règles métier
     */
    public Category moveCategory(UUID categoryId, UUID newParentId) {
        Category category = getCategoryById(categoryId);
        
        Category newParent = null;
        if (newParentId != null) {
            newParent = getCategoryById(newParentId);
            
            // Vérifier que la catégorie ne devient pas parente d'elle-même
            if (categoryId.equals(newParentId)) {
                throw new ValidationException("Une catégorie ne peut pas être son propre parent");
            }
            
            // Vérifier qu'on ne crée pas de cycle
            Category current = newParent;
            while (current != null) {
                if (current.getId().equals(categoryId)) {
                    throw new ValidationException("Le déplacement créerait un cycle dans la hiérarchie");
                }
                current = current.getParent();
            }
        }
        
        category.moveToParent(newParent);
        return categoryRepository.save(category);
    }
    
    // ===============================================
    // OPÉRATIONS DE SUPPRESSION
    // ===============================================
    
    /**
     * Supprime une catégorie si elle ne contient pas d'articles ou de sous-catégories.
     * 
     * @param id ID de la catégorie à supprimer
     * @throws ResourceNotFoundException si la catégorie n'existe pas
     * @throws ValidationException si la suppression viole les règles métier
     */
    public void deleteCategory(UUID id) {
        Category category = getCategoryById(id);
        
        if (!category.canBeDeleted()) {
            throw new ValidationException(
                "Impossible de supprimer une catégorie contenant des articles ou des sous-catégories"
            );
        }
        
        categoryRepository.delete(category);
    }
    
    // ===============================================
    // MÉTHODES DE VALIDATION PRIVÉES
    // ===============================================
    
    private void validateCategoryName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Le nom de la catégorie est obligatoire");
        }
        if (name.trim().length() < 2 || name.trim().length() > 100) {
            throw new ValidationException("Le nom doit contenir entre 2 et 100 caractères");
        }
    }
    
    private void validateCategoryDescription(String description) {
        if (description != null && description.length() > 500) {
            throw new ValidationException("La description ne peut pas dépasser 500 caractères");
        }
    }
    
    // ===============================================
    // MÉTHODE DE TEST POUR DIAGNOSTIQUER
    // ===============================================
    
    /**
     * Compte simple des catégories pour diagnostic.
     */
    @Transactional(readOnly = true)
    public long getCategoryCount() {
        return categoryRepository.count();
    }
}
