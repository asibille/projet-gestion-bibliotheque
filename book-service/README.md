# book-service

Microservice de gestion du catalogue de livres de la bibliothèque.

## Démarrage

Prérequis : `eureka-server` (8761) et `config-server` (8888) démarrés.

```bash
mvn spring-boot:run
```

Le service démarre sur le port **8091** et s'enregistre auprès d'Eureka.

## Endpoints

| Méthode | URL | Description |
|---|---|---|
| GET | `/api/books` | Lister tous les livres |
| GET | `/api/books/{id}` | Récupérer un livre |
| POST | `/api/books` | Créer un livre (`availableCopies` = `totalCopies`) |
| PUT | `/api/books/{id}` | Mettre à jour un livre |
| DELETE | `/api/books/{id}` | Supprimer un livre |
| PATCH | `/api/books/{id}/decrement-stock` | Décrémente le stock (409 si épuisé) |
| PATCH | `/api/books/{id}/increment-stock` | Réincrémente le stock (jamais > totalCopies) |

## Exemple de payload (POST /api/books)

```json
{
  "title": "1984",
  "author": "George Orwell",
  "isbn": "978-0451524935",
  "totalCopies": 3
}
```

## Tests

```bash
mvn test
```

Couvre notamment le scénario de stock épuisé (409 Conflict).
