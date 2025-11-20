#!/bin/bash

echo "🚀 Test du Service Messagerie WebSocket"
echo "========================================"

# Test 1: Vérifier que le service est démarré
echo "📡 Test 1: Vérification du service..."
curl -s http://localhost:8083/api/messages/health | jq '.' || echo "❌ Service non accessible"

echo ""
echo "🔑 Test 2: Debug JWT (remplacez YOUR_JWT_TOKEN)..."
echo "curl -X POST http://localhost:8083/api/messages/debug-jwt \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"token\":\"YOUR_JWT_TOKEN\"}'"

echo ""
echo "🔌 Test 3: Connexion WebSocket"
echo "URL: ws://localhost:8083/ws?token=YOUR_JWT_TOKEN"

echo ""
echo "📝 Instructions:"
echo "1. Remplacez YOUR_JWT_TOKEN par votre vrai token JWT"
echo "2. Testez d'abord avec /debug-jwt pour valider le token"
echo "3. Connectez-vous au WebSocket depuis le frontend"
echo "4. Envoyez un message via /api/messages/send"

echo ""
echo "🔍 Logs à surveiller:"
echo "- 🔗 WebSocket connection established"
echo "- 🎯 Extracted user ID from token"
echo "- ✅ User authenticated and session registered"
echo "- 🔔 Tentative notification WebSocket"