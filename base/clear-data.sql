-- Script pour effacer toutes les données des tables
-- Exécuter avec: psql -U <username> -d tour_operateur -f clear-data.sql

\c tour_operateur;

-- Désactiver temporairement les contraintes de clés étrangères
SET session_replication_role = 'replica';

-- Effacer les données de toutes les tables
TRUNCATE TABLE distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE token RESTART IDENTITY CASCADE;
TRUNCATE TABLE parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE aeroport RESTART IDENTITY CASCADE;
TRUNCATE TABLE hotel RESTART IDENTITY CASCADE;
TRUNCATE TABLE client RESTART IDENTITY CASCADE;
TRUNCATE TABLE type_carburant RESTART IDENTITY CASCADE;

-- Réactiver les contraintes de clés étrangères
SET session_replication_role = 'origin';

-- Confirmation
SELECT 'Toutes les données ont été effacées avec succès.' AS message;
