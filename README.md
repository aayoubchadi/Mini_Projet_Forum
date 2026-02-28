---
title: Mini Projet Forum
emoji: 💬
colorFrom: blue
colorTo: purple
sdk: docker
pinned: false
---

# Mini Projet Forum - TP JEE

## Fonctionnalités livrées (selon le sujet)
- Espace membres (authentification + session)
- Validation d'inscription par email (code OTP réel via Gmail SMTP)
- Gestion des articles (création/suppression)
- Gestion des commentaires (ajout/suppression)
- Gestion du profil (nom, bio, langue)
- Internationalisation FR/EN via JSTL + bundles
- UI moderne (style premium 2026)

## Stack technique
- Oracle Database (JDBC `ojdbc11`)
- JSP/Servlets Jakarta (Tomcat 10+/11)
- SMTP Gmail (`smtp.gmail.com:587`) via `jakarta.mail`

## Configuration Oracle
1. Exécuter le script SQL: [sql/schema_oracle.sql](sql/schema_oracle.sql)
2. Mettre vos paramètres Oracle dans [src/main/webapp/WEB-INF/classes/app.properties](src/main/webapp/WEB-INF/classes/app.properties):
  - `oracle.url`
  - `oracle.user`
  - `oracle.password`

## Configuration Gmail SMTP (OTP)
Dans [src/main/webapp/WEB-INF/classes/app.properties](src/main/webapp/WEB-INF/classes/app.properties):
- `gmail.username` = votre adresse Gmail
- `gmail.appPassword` = mot de passe d'application Google (16 chars)
- `mail.from` = expéditeur

Notes:
- Activez la validation 2FA sur Gmail, puis générez un App Password.
- Le code OTP expire en 10 minutes.

## Comptes / Démarrage rapide
- Un compte seedé existe:
  - email: `admin@forum.local`
  - mot de passe: `admin123`
- Déployer sur Tomcat 10+ (Jakarta)
- URL principale: `/`

## Dépendances JSP/JSTL
Pour Tomcat 10+, placer dans `WEB-INF/lib`:
- `jakarta.servlet.jsp.jstl-api` (3.x)
- `jakarta.servlet.jsp.jstl` (implémentation 3.x)

## Dépendances supplémentaires
Placer aussi dans `WEB-INF/lib`:
- `ojdbc11-23.4.0.24.05.jar`
- `jakarta.mail-2.0.1.jar`
- `jakarta.activation-2.0.1.jar`

## Exigence hébergement gratuit (choix recommandé)
**Choix:** Render (plan gratuit)

**Pourquoi Render pour ce TP :**
- Déploiement simple d'une application Java WAR
- HTTPS par défaut
- CI/CD basique depuis GitHub
- Fiable pour démonstration académique
- UX de déploiement claire pour équipe étudiante

Alternative valide: Railway (simple), mais Render reste plus stable pour un rendu de projet.
