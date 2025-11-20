# Guide de Test WebSocket - Service Messagerie

## 🚀 Démarrage du Service

Le service de messagerie démarre sur le port **8083** avec WebSocket activé.

**URL WebSocket:** `ws://localhost:8083/ws`

## 🔧 Configuration Frontend

Le frontend doit se connecter au WebSocket avec cette URL et un token JWT valide :

```javascript
const ws = new WebSocket('ws://localhost:8083/ws?token=YOUR_JWT_TOKEN');
```

## 📨 Format des Messages WebSocket

Quand un message est envoyé, le destinataire reçoit automatiquement :

```json
{
  "type": "NEW_MESSAGE",
  "data": {
    "id": "message_id",
    "content": "Contenu du message",
    "sender": {
      "id": 123,
      "email": "sender@example.com"
    },
    "createdAt": "2024-01-01T10:00:00",
    "conversationId": 456
  },
  "timestamp": "2024-01-01T10:00:00.000Z"
}
```

## 🧪 Tests Disponibles

### 1. Debug JWT Token
```bash
POST /api/messages/debug-jwt
Content-Type: application/json

{
  "token": "YOUR_JWT_TOKEN_HERE"
}
```

### 2. Vérifier les Sessions WebSocket
```bash
GET /api/messages/debug-sessions
Authorization: Bearer YOUR_JWT_TOKEN
```

### 3. Vérifier le Statut WebSocket
```bash
GET /api/messages/websocket-status
Authorization: Bearer YOUR_JWT_TOKEN
```

### 4. Envoyer un Message de Test
```bash
POST /api/messages/test-websocket/123
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "message": "Test message via WebSocket"
}
```

### 5. Envoyer un Message Normal
```bash
POST /api/messages/send
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json

{
  "receiverId": 123,
  "annonceId": 456,
  "content": "Bonjour, je suis intéressé par votre annonce"
}
```

## 🔍 Debug Frontend

Dans la console du navigateur, exécutez :

```javascript
// Activer le debug WebSocket
debugWebSocketFlow();

// Vérifier la connexion
console.log('WebSocket state:', ws.readyState);
// 0 = CONNECTING, 1 = OPEN, 2 = CLOSING, 3 = CLOSED

// Tester votre token JWT
fetch('/api/messages/debug-jwt', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ token: 'YOUR_JWT_TOKEN' })
}).then(r => r.json()).then(console.log);
```

## ✅ Vérifications

1. **Service démarré** : Vérifiez que le service tourne sur le port 8083
2. **JWT valide** : Testez avec `/api/messages/debug-jwt`
3. **WebSocket connecté** : Vérifiez avec `/api/messages/debug-sessions`
4. **Messages reçus** : Les messages doivent apparaître en temps réel
5. **Format correct** : Les messages doivent avoir le format JSON attendu

## 📝 Ordre de test recommandé

1. Démarrer le service messagerie
2. Tester le JWT avec `/debug-jwt`
3. Se connecter au WebSocket depuis le frontend
4. Vérifier la session avec `/debug-sessions`
5. Envoyer un message de test
6. Vérifier la réception en temps réel

## 🐛 Troubleshooting

### Étape 1: Vérifier le JWT
```bash
# Testez votre token
curl -X POST http://localhost:8083/api/messages/debug-jwt \
  -H "Content-Type: application/json" \
  -d '{"token":"YOUR_JWT_TOKEN"}'
```

### Étape 2: Vérifier la connexion WebSocket
- **Connexion refusée** : Vérifiez que le service tourne sur le port 8083
- **Token invalide** : Utilisez l'endpoint `/debug-jwt` pour valider votre token
- **Format URL** : Assurez-vous d'utiliser `ws://localhost:8083/ws?token=JWT_TOKEN`

### Étape 3: Vérifier les messages
- **Messages non reçus** : Vérifiez les logs du service pour voir les tentatives d'envoi
- **Format incorrect** : Vérifiez que le frontend écoute le type "NEW_MESSAGE"
- **Sessions** : Utilisez `/debug-sessions` pour voir si l'utilisateur est connecté

### Logs à surveiller
```
🔗 WebSocket connection established
🔍 Extracted token: [PRESENT]
✅ User ID X authenticated and session registered
🔔 Tentative notification WebSocket
✅ Message WebSocket envoyé au destinataire
```