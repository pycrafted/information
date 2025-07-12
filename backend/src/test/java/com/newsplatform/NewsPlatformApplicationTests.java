package com.newsplatform;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration de l'application Spring Boot
 * Couche Application : Vérification du chargement du contexte Spring
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Tests d'intégration de l'application")
class NewsPlatformApplicationTests {

	@Autowired
	private ApplicationContext applicationContext;

	@Test
	@DisplayName("Le contexte Spring doit se charger correctement")
	void contextLoads() {
		// Vérifier que le contexte Spring s'est bien chargé
		assertNotNull(applicationContext, "Le contexte Spring doit être initialisé");
		assertTrue(applicationContext.getBeanDefinitionCount() > 0, 
			"Le contexte doit contenir au moins un bean");
	}

}
