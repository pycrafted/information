#!/usr/bin/env bash
set -e

# Move to project root (one level above this script)
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "🔧 Setting up News Platform development environment …"

# --- Environment files -------------------------------------------------------
if [ ! -f .env.local ]; then
  echo "📄 Creating .env.local from .env.example"
  cp .env.example .env.local
fi

if [ ! -f .env ]; then
  echo "📄 Creating .env from config/.env.dev"
  cp config/.env.dev .env
fi

# --- Database ----------------------------------------------------------------
echo "🐘 Starting PostgreSQL container …"
docker-compose up -d db

echo "⏳ Waiting for database to initialise …"
sleep 10

# --- Frontend ----------------------------------------------------------------
if ! command -v pnpm >/dev/null 2>&1; then
  echo "⚠️  pnpm not found – installing globally via npm …"
  npm install -g pnpm
fi

echo "🎨 Installing frontend dependencies …"
( cd frontend && pnpm install )

# --- Backend -----------------------------------------------------------------
echo "📦 Building backend …"
( cd backend && ./gradlew build -x test )

# --- Services ----------------------------------------------------------------
echo "🚀 Starting all services …"
docker-compose up -d

# --- Done --------------------------------------------------------------------

echo "✅ Development environment ready!"
echo "🌐 Frontend: http://localhost:3000"
echo "🔧 Backend:   http://localhost:8080"
echo "📚 API Docs:  http://localhost:8080/swagger-ui.html"
echo "📚 API Docs: http://localhost:8080/swagger-ui.html"