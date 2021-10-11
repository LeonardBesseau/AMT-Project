# Les boulangers
Projet pour le cours d'AMT à la HEIG-VD

## Description
Boutique en ligne de boulangerie et pâtisserie.

## Pré-requis
- Postgresql (v13 ou supérieur)
- Java 11

## Deployement
Télécharger la release et l'extraire. Ajouter les données de connection à la base de données dans le fichier .env.
Lancer le serveur avec `java -jar target/quarkus-app/quarkus-run.jar`


## Installation
Cloner le repository. Pour lancer la base de donnée en local, effectuer un `docker-compose up` dans le dossier `docker`. 
Pour compiler l'application, utiliser `mvn compile quarkus:dev:`.


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
