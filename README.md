# Market Place - Backend (Mémoire)

Ce dépôt contient les services backend de l'application "Market Place" développée pour le mémoire : microservices Spring Boot (annonce-service, messagerie, user-service, ...).

Objectif de ce README
- Donner une vue d'ensemble du projet.
- Expliquer comment lancer les services backend localement.
- Expliquer comment ajouter et référencer des captures d'écran du front dans la documentation.
- Rappeler les bonnes pratiques de sécurité (ne jamais committer de clés/credentials).

Structure du dépôt (exemples)
- annonce-service/ : service d'annonces
- messagerie/ : service de messagerie (WebSocket)
- user-service/ : gestion des utilisateurs (ex: contient un fichier de clé de service -> attention)
- README.md : ce fichier
- docs/screenshots/ : dossier conseillé pour stocker les captures d'écran du front

Architecture (brève)
- Chaque service est une application Spring Boot indépendante.
- Communication inter-services via REST (Feign) ou WebSocket selon le besoin.
- Données : MongoDB (ou autre suivant la configuration).


