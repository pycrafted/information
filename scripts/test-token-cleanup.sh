#!/bin/bash

# Script de test pour le nettoyage des tokens
# Vérifie que le problème de tokens en double est résolu

echo "🧪 Test du nettoyage des tokens en double"
echo "=========================================="

# Attendre que le backend soit prêt
echo "⏳ Attente du démarrage du backend..."
sleep 10

# URL de base
BASE_URL="http://localhost:8080/api"

# Test 1: Connexion admin
echo ""
echo "🔐 Test 1: Connexion admin"
echo "-------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "OusmaneSonko@2029"
  }')

echo "Réponse de connexion: $LOGIN_RESPONSE"

# Extraire le token d'accès
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
REFRESH_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Échec de la connexion"
    exit 1
fi

echo "✅ Connexion réussie"
echo "Access Token: ${ACCESS_TOKEN:0:50}..."

# Test 2: Refresh du token plusieurs fois pour créer des doublons
echo ""
echo "🔄 Test 2: Création de tokens en double via refresh"
echo "-------------------------------------------------"

for i in {1..3}; do
    echo "Refresh $i..."
    REFRESH_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/refresh?refreshToken=$REFRESH_TOKEN" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    
    NEW_ACCESS_TOKEN=$(echo $REFRESH_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
    if [ ! -z "$NEW_ACCESS_TOKEN" ]; then
        ACCESS_TOKEN=$NEW_ACCESS_TOKEN
        echo "✅ Refresh $i réussi"
    else
        echo "❌ Refresh $i échoué"
    fi
done

# Test 3: Nettoyage manuel des tokens
echo ""
echo "🧹 Test 3: Nettoyage manuel des tokens"
echo "-------------------------------------"

CLEANUP_RESPONSE=$(curl -s -X POST "$BASE_URL/admin/cleanup-tokens" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json")

echo "Réponse du nettoyage: $CLEANUP_RESPONSE"

# Test 4: Vérification que l'authentification fonctionne toujours
echo ""
echo "🔍 Test 4: Vérification de l'authentification"
echo "--------------------------------------------"

AUTH_TEST=$(curl -s -X GET "$BASE_URL/articles/recent?page=0&size=5" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

if [[ $AUTH_TEST == *"articles"* ]]; then
    echo "✅ Authentification fonctionne après nettoyage"
else
    echo "❌ Problème d'authentification après nettoyage"
    echo "Réponse: $AUTH_TEST"
fi

# Test 5: Test de création d'article (qui causait le problème)
echo ""
echo "📝 Test 5: Test de création d'article"
echo "-----------------------------------"

ARTICLE_RESPONSE=$(curl -s -X POST "$BASE_URL/articles" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Article",
    "content": "Contenu de test",
    "summary": "Résumé de test",
    "categoryId": "00000000-0000-0000-0000-000000000001"
  }')

if [[ $ARTICLE_RESPONSE == *"id"* ]]; then
    echo "✅ Création d'article réussie"
    echo "Article créé: $ARTICLE_RESPONSE"
else
    echo "❌ Échec de création d'article"
    echo "Réponse: $ARTICLE_RESPONSE"
fi

echo ""
echo "🎉 Tests terminés !"
echo "==================" 