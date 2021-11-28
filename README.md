# Les boulangers
Projet pour le cours d'AMT à la HEIG-VD

## Description
Boutique en ligne de boulangerie et pâtisserie.

## Pré-requis
- Postgresql (v13 ou supérieur)
- Java 11
- Docker et docker-compose (optionnel)

## Déploiement
Télécharger la release et l'extraire. Ajouter les données de connexion à la base de données dans le fichier `config/application.properties` (il peut être nécessaire de le créer).
Lancer le serveur avec `java -jar target/quarkus-app/quarkus-run.jar`


## Installation
1. Cloner le repository. 

2. Mettre en place de la base de données

   - Standalone

   Si vous disposez déjà de postgres, vous pouvez créez une nouvelle base de données ou en utiliser une existante. Les scripts pour créer les tables se trouvent dans `sql/tables`. 

   Une fois la configuration terminée, vous pouvez mettre les informations de connexions dans le fichier `config/application.properties`. (L'utilisateur à fournir doit avoir des droits de lecture et d'écriture)

   - Docker
     1. `docker-compose up` dans le dossier `docker`
     2. Se connecter à la base de données et exécuter les scripts se trouvant dans  `sql/tables`.

3. Lancer l'application en mode *dev* avec `mvn compile quarkus:dev`.

4. Se connecter avec un navigateur à sur localhost:8080. La page d’accueil retourne un 404 avec la liste des routes.


## Contribution
Les Pull Requests sont les bienvenues. Pour des changements majeurs, ouvrez s'il vous plaît une issue pour discuter de ce que vous souhaitez changer.
Soyez sûrs de mettre à jour les tests si nécessaire.

## Licence
[MIT](https://choosealicense.com/licenses/mit/)

## Auteurs
- Léonard Besseau
- Alexandra Cerottini
- Miguel Do Vale Lopes
- Fiona Gamboni
- Nicolas Ogi
