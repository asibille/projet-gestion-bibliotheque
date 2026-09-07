# Gestion Bibliothèque

Système de gestion de bibliothèque basé sur une architecture microservices Spring Boot
(Eureka, Config Server, API Gateway, communication inter-services via Feign).

Projet réalisé dans le cadre du cours **MicroService Spring Boot** — Module 10, exercice final.

## Architecture

```
                              eureka-server (8761)
                                     ▲
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
        api-gateway (8080)   book-service (8091)      loan-service (8092)
                              base H2 "bookdb"          base H2 "loandb"
                                     ▲                      │
                                     └──────── Feign ────────┘
                              config-server (8888)
                          sert les fichiers de config-repo/
```

## Services

| Service | Port | Rôle |
|---|---|---|
| `eureka-server` | 8761 | Annuaire des services |
| `config-server` | 8888 | Configuration centralisée (profil `native`, sert `config-repo/`) |
| `api-gateway` | 8080 | Point d'entrée unique, routage vers les microservices |
| `book-service` | 8091 | CRUD du catalogue de livres, endpoints de gestion du stock |
| `loan-service` | 8092 | Gestion des emprunts, appelle `book-service` en lecture et en écriture (Feign) |

## Démarrage

### Avec Maven (en local)

Démarrer les services **dans cet ordre** (chacun dans un terminal séparé, depuis son propre dossier) :

```bash
# 1. eureka-server
cd eureka-server && mvn spring-boot:run

# 2. config-server
cd config-server && mvn spring-boot:run

# 3. api-gateway
cd api-gateway && mvn spring-boot:run

# 4. book-service
cd book-service && mvn spring-boot:run

# 5. loan-service
cd loan-service && mvn spring-boot:run
```

### Avec Docker Compose

```bash
docker compose up --build
```

> Le `docker-compose.yml` racine ne construit actuellement que `config-server`,
> `book-service` et `loan-service` (les seuls modules présents dans ce repo pour l'instant).
> Décommentez les sections `eureka-server`, `api-gateway`, `product-service` et
> `order-service` dans `docker-compose.yml` (et dans le `pom.xml` racine) au fur et à
> mesure que ces modules sont ajoutés au repo.

## Endpoints principaux

### book-service (via la gateway : `/api/books/**`)

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/books` | Lister tous les livres |
| GET | `/api/books/{id}` | Récupérer un livre |
| POST | `/api/books` | Créer un livre |
| PUT | `/api/books/{id}` | Mettre à jour un livre |
| DELETE | `/api/books/{id}` | Supprimer un livre |
| PATCH | `/api/books/{id}/decrement-stock` | Décrémente le stock (409 si épuisé) |
| PATCH | `/api/books/{id}/increment-stock` | Réincrémente le stock |

### loan-service (via la gateway : `/api/loans/**`)

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/loans` | Lister tous les emprunts |
| GET | `/api/loans/{id}` | Récupérer un emprunt |
| POST | `/api/loans` | Créer un emprunt |
| PATCH | `/api/loans/{id}/return` | Marquer un emprunt comme rendu |

Détails de la logique métier (règles de création/retour d'un emprunt, gestion des
erreurs Feign) : voir `loan-service/README.md`.

## Tests

Depuis la racine, pour lancer tous les modules :

```bash
mvn test
```

Ou par module :

```bash
cd book-service && mvn test
cd loan-service && mvn test
```

Les tests couvrent en particulier :
- Le refus de décrémenter un stock déjà à 0 (409, `book-service`)
- Le refus de créer un emprunt pour un livre inexistant (400) ou en rupture de stock (409)
- Le cas de concurrence TOCTOU (stock épuisé entre la vérification et la décrémentation)
- Le refus de rendre deux fois le même emprunt (409)

## Structure du repo

```
gestion-bibliotheque/
├── pom.xml                    
├── docker-compose.yml
├── config-repo/               
│   ├── book-service.yml
│   └── loan-service.yml
├── config-server/
├── book-service/
├── loan-service/
├── eureka-server/             
├── api-gateway/                
├── product-service/           
└── order-service/             
```
