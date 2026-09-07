# config-server

Serveur de configuration centralisée (Spring Cloud Config) pour tous les microservices du projet.

## Démarrage

```bash
mvn spring-boot:run
```

Le service démarre sur le port **8888** et sert les fichiers du dossier `../config-repo` (à la racine du projet, en profil `native`).

## Vérifier qu'un service récupère bien sa config

```bash
curl http://localhost:8888/book-service/default
curl http://localhost:8888/loan-service/default
```

Chaque microservice doit déclarer dans son `application.yml` :

```yaml
spring:
  config:
    import: "optional:configserver:http://localhost:8888"
```

## Tests

```bash
mvn test
```

Vérifie que le serveur sert correctement un fichier de configuration de test.
