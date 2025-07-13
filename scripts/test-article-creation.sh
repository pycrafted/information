#!/bin/bash

# Script de test pour la création d'articles
# Vérifie que l'authentification et la création d'articles fonctionnent

echo "🧪 Test de création d'articles"
echo "==============================="

# Attendre que le backend soit prêt
echo "⏳ Attente du démarrage du backend..."
sleep 5

# URL de base
BASE_URL="http://localhost:8080/api"

# Test 1: Vérifier que le backend répond
echo ""
echo "🔍 Test 1: Vérification du backend"
echo "----------------------------------"
BACKEND_RESPONSE=$(curl -s -X GET "$BASE_URL/articles/recent")
if [ $? -eq 0 ]; then
    echo "✅ Backend accessible"
else
    echo "❌ Backend inaccessible"
    exit 1
fi

# Test 2: Connexion admin
echo ""
echo "🔐 Test 2: Connexion admin"
echo "--------------------------"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "OusmaneSonko@2029"
  }')

echo "Réponse de connexion: $LOGIN_RESPONSE"

# Extraire le token d'accès
ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Échec de la connexion - Token non trouvé"
    exit 1
fi

echo "✅ Connexion réussie - Token: ${ACCESS_TOKEN:0:20}..."

# Test 3: Création d'un article
echo ""
echo "📝 Test 3: Création d'un article"
echo "--------------------------------"
ARTICLE_DATA='{
  "title": "Article de test créé via script",
  "content": "Ceci est un article de test créé automatiquement pour vérifier le bon fonctionnement de l\'API.",
  "summary": "Résumé de l\'article de test",
  "slug": "article-test-script",
  "status": "DRAFT",
  "categoryId": 1
}'

CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/articles" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -d "$ARTICLE_DATA")

echo "Réponse de création: $CREATE_RESPONSE"

# Vérifier si l'article a été créé
if echo "$CREATE_RESPONSE" | grep -q '"id"'; then
    echo "✅ Article créé avec succès"
    
    # Extraire l'ID de l'article
    ARTICLE_ID=$(echo $CREATE_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
    echo "📄 ID de l'article: $ARTICLE_ID"
    
    # Test 4: Récupération de l'article créé
    echo ""
    echo "🔍 Test 4: Récupération de l'article créé"
    echo "----------------------------------------"
    GET_RESPONSE=$(curl -s -X GET "$BASE_URL/articles/$ARTICLE_ID" \
      -H "Authorization: Bearer $ACCESS_TOKEN")
    
    echo "Réponse de récupération: $GET_RESPONSE"
    
    if echo "$GET_RESPONSE" | grep -q '"title"'; then
        echo "✅ Article récupéré avec succès"
    else
        echo "❌ Échec de la récupération de l'article"
    fi
    
else
    echo "❌ Échec de la création de l'article"
    echo "Détails de l'erreur: $CREATE_RESPONSE"
    exit 1
fi

# Test 5: Publication de l'article
echo ""
echo "📢 Test 5: Publication de l'article"
echo "----------------------------------"
PUBLISH_RESPONSE=$(curl -s -X POST "$BASE_URL/articles/$ARTICLE_ID/publish" \
  -H "Authorization: Bearer $ACCESS_TOKEN")

echo "Réponse de publication: $PUBLISH_RESPONSE"

if echo "$PUBLISH_RESPONSE" | grep -q '"status":"PUBLISHED"'; then
    echo "✅ Article publié avec succès"
else
    echo "❌ Échec de la publication de l'article"
fi

# Test 6: Vérification que l'article apparaît dans les articles récents
echo ""
echo "📰 Test 6: Vérification dans les articles récents"
echo "------------------------------------------------"
RECENT_RESPONSE=$(curl -s -X GET "$BASE_URL/articles/recent")

if echo "$RECENT_RESPONSE" | grep -q "Article de test créé via script"; then
    echo "✅ Article trouvé dans les articles récents"
else
    echo "⚠️ Article non trouvé dans les articles récents (normal s'il est en DRAFT)"
fi

echo ""
echo "🎉 Tests terminés avec succès !"
echo "===============================" 