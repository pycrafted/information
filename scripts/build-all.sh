#!/bin/bash

echo "🚀 Building News Platform..."

# Backend
echo "📦 Building Backend..."
cd backend
./gradlew clean build -x test
cd ..

# Frontend
echo "🎨 Building Frontend..."
cd frontend
pnpm install
pnpm build
cd ..

# Desktop Client
echo "🖥️ Building Desktop Client..."
cd desktop-client
./gradlew clean build
cd ..

echo "✅ Build completed successfully!"