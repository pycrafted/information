#!/bin/bash

echo "🚀 Building News Platform..."

# Backend
echo "📦 Building Backend..."
cd backend
./gradlew clean build -x test
cd ..

# Desktop Client
echo "🖥️ Building Desktop Client..."
cd desktop-client
./gradlew clean build
cd ..

echo "✅ Build completed successfully!"