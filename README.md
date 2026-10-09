# Agenda personnel – application JavaFX / Oracle

Application de bureau de gestion d'événements, réalisée à l'ISG Tunis ([05/2026]) en binôme.

[Capture de l'interface de la Calendrier:](docs/capture_calendrier.jpg)(docs/capture_calendrier2.jpg)


## Fonctionnalités

- **Tableau des événements** : recherche par titre, date ou catégorie ; filtres « Tous », « Aujourd'hui », « À venir », « Importants » ; compteur de résultats ; confirmation avant suppression.
- **Calendrier mensuel** : navigation entre les mois, jour courant mis en valeur, événements colorés selon leur priorité, détail par jour.
- **Formulaire d'ajout et de modification** : formatage automatique de la date et de l'heure pendant la saisie, limites de longueur, validation des champs.
- **Rappels du jour** affichés au démarrage de l'application.
- **Persistance Oracle** via JDBC : ajout, modification, suppression et recherche d'événements.


## Architecture

```
monagenda/
├── model/     Evenement, énumérations TypeEvenement et Priorite
├── gestion/   interface GestionEvenement + implémentation JDBC
├── gui/       AgendaView, CalendrierView, EvenementDialog
└── util/      DBConnection (connexion Oracle)
```

L'accès aux données passe par une interface (`GestionEvenement`) et une implémentation JDBC utilisant des requêtes préparées.

## Prérequis

- JDK Java 25.0.2 et JavaFX JavaFX 25.0.3
- Oracle Database Oracle Database 21c Express Edition (XE)
- Driver JDBC Oracle (`ojdbc11.jar`)

## Installation

1. Créer l'utilisateur et la table avec `database/schema.sql`.
2. Copier `db.properties.example` en `db.properties` et renseigner l'URL, l'utilisateur et le mot de passe (ce fichier n'est pas versionné).
3. Ajouter `ojdbc11.jar` et JavaFX au classpath, puis lancer `monagenda.Launcher`.

## Pistes d'amélioration

- Exécuter les accès à la base dans une tâche de fond JavaFX pour ne pas bloquer l'interface.
- Valider strictement les dates (refuser `31/02`).
- Ajouter des tests unitaires sur la couche d'accès aux données.
