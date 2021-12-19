# Les Boulangers
Dans le cadre du projet du cours AMT (Application multi-tiers) à la HEIG-VD, nous sommes chargés de créer un site web de e-commerce décomposé en microservices. 

Le repository du service d'authentification est disponible via ce lien: https://github.com/K-do/AMT-AuthService/tree/dev 

## Description
Nous avons choisi de créer une boutique en ligne de boulangerie et pâtisserie.

## Pré-requis
- [Postgresql](https://www.postgresql.org/download/) (v13 ou supérieure)
- [Java 11](https://adoptopenjdk.net/installation.html)
- [Docker](https://docs.docker.com/get-docker/) et [docker-compose](https://docs.docker.com/compose/install/) (optionnels)

## Déploiement pour la production
- Télécharger la release et l'extraire. 
- Ajouter les données de connexion à la base de données dans le fichier `config/application.properties` (il peut être nécessaire de le créer).
- Ajouter la clé publique (nommée `publicKey.pem`) dans le dossier contenant l'exécutable. Si vous souhaitez changer le nom ou l'emplacement de la clé, il suffit d'ajouter à la configuration `mp.jwt.verify.publickey.location=[LOCATION]` où **LOCATION** est l'emplacement de la clé. Pour la génération de la clé, se reférer à la documentation du [service d'authentification](https://github.com/K-do/AMT-AuthService/tree/dev)
- Indiquer l'adresse du serveur d'authentification avec la propriété : `auth.server.url=[URL]` dans la config.
- Lancer le serveur avec `java -jar target/quarkus-app/quarkus-run.jar`.


## Installation pour le developpement
Les étapes ci-dessous permettent de mettre en place l'environnement de développement en local afin de travailler sur le projet :

1. Cloner le repository. 

2. Mettre en place la base de données

   - Standalone

   Si vous disposez déjà de PostgreSQL, vous pouvez créer une nouvelle base de données ou en utiliser une existante.
Pour créer une base de données depuis psql :

   ```sql
   create database [DATABASE_NAME];
   ```

   Une fois la configuration terminée, vous pouvez mettre les informations de connexions dans le fichier `config/application.properties`. (L'utilisateur à fournir doit avoir des droits de lecture et d'écriture)

   - Docker : `docker-compose up` dans le dossier `docker`

   Pour autant que la DB soit vide, Liquibase se chargera de créer les tables nécessaires au lancement du projet. 

3. Copier la clé publique du serveur d'authentification dans `main/ressources`. La clé publique doit se nommer `publicKey.pem` sinon il faudra modifier le fichier `main/ressources/application.properties`. L'algorithme de vérification de signature est ES256. Si les clés générées utilisent un autre algorithme, il faudra modifier le fichier `main/ressources/application.properties` comme avant.

3. Lancer l'application en mode *dev* avec `mvn compile quarkus:dev`.

4. Se connecter avec un navigateur sur localhost:8080.


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
