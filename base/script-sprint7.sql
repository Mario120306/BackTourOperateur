-- Script de jeu de donnees simple pour les tests du sprint 7
-- A executer apres base/sql.sql et base/clear-data.sql
-- psql -U <user> -d tour_operateur -f base/script-sprint7.sql

\c tour_operateur;

-- ==========================
-- Types de carburant
-- ==========================
INSERT INTO type_carburant (reference, nom) VALUES
('DSL', 'Diesel'),
('ESS', 'Essence');

-- ==========================
-- Clients
-- ==========================
INSERT INTO client (nom, prenom, email) VALUES
('Groupe', 'Loin10', 'groupe10@example.com'),   -- id 1
('Groupe', 'Proche9', 'groupe9@example.com'),   -- id 2
('Groupe', 'Report8', 'groupe8@example.com'),   -- id 3
('Groupe', 'Split14', 'groupe14@example.com');  -- id 4

-- ==========================
-- Hotels
-- ==========================
-- H1 : hotel loin (trajet tres long)
-- H2/H3 : hotels proches
INSERT INTO hotel (nom, adresse, ville) VALUES
('Hotel Loin', 'Route Nationale 1, km 120', 'LointaineVille'),  -- id 1
('Hotel Centre', 'Avenue Principale 10', 'Antananarivo'),      -- id 2
('Hotel Ville', 'Rue Secondaire 5', 'Antananarivo');           -- id 3

-- ==========================
-- Aeroport
-- ==========================
INSERT INTO aeroport (code, libelle) VALUES
('TNR', 'Aeroport International Ivato');  -- id 1

-- ==========================
-- Parametres
-- ==========================
-- Vitesse par defaut et priorite carburant pour la simulation
INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par defaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('TEMPS_ARRET_MINUTES', '30', 'Temps darret a lhotel en minutes'),
('TEMPS_ATTENTE_MINUTES', '20', 'Temps dattente pour regrouper les reservations (en minutes)');

-- ==========================
-- Vehicules
-- ==========================
-- V1 : 10 places (Diesel) -> prendra le groupe de 10 vers Hotel Loin (trajet tres long)
-- V2 : 9 places  (Diesel) -> prendra le groupe de 9 vers Hotel Centre (trajet court)
-- V3 : 7 places  (Essence ou autre) -> servira au decoupage de la reservation de 14
INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, type_carburant_id) VALUES
('Toyota',   'LongTrip', 10, 'VEH-LONG-10', 60, 1),  -- V1 Diesel 10 places
('Mercedes', 'City9',     9, 'VEH-CITY-9',  60, 1),  -- V2 Diesel 9 places
('Peugeot',  'City7',     7, 'VEH-CITY-7',  60, 2);  -- V3 Essence 7 places

-- ==========================
-- Distances
-- ==========================
-- Depuis l'aeroport Ivato (id=1)
-- Hotel Loin : 120 km -> ~2h aller, 2h retour => ~4h de trajet
-- Hotels proches : 10 km -> ~10 min aller, 10 min retour => ~20 min
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 1, 1, 120.0),  -- Aeroport -> Hotel Loin
(NULL, 1, 2, 10.0),   -- Aeroport -> Hotel Centre
(NULL, 1, 3, 10.0);   -- Aeroport -> Hotel Ville

-- Les distances inter-hotels ne sont pas indispensables :
-- en leur absence, la simulation utilisera une estimation par defaut.

-- ==========================
-- Reservations (date de test : 2026-03-25)
-- ==========================
-- Objectifs du jeu de donnees :
-- 1) Deux groupes de reservations selon le temps darrivee et TEMPS_ATTENTE_MINUTES=20
-- 2) Dans le premier groupe :
--    - une reservation de 10 places (client 1) vers Hotel Loin
--    - une reservation de 9 places  (client 2) vers Hotel Centre
--    - une reservation de 8 places  (client 3) vers Hotel Ville -> NE TROUVE AUCUNE PLACE
--      (les 2 vehicules seront deja pleins) et sera REPORTEE au groupe suivant
-- 3) Dans le second groupe :
--    - une reservation de 14 places (client 4) vers Hotel Centre
--      qui sera DECOUPEE entre V2 (9 places) et V3 (5 des 7 places)
--    - la reservation de 8 places (client 3) reportee du premier groupe
-- 4) Le vehicule V1 (10 places) effectue un trajet long vers Hotel Loin et
--    ne sera PAS disponible pour le depart du second groupe (trajet ~4h).

-- Groupe 1 (fenetre ouverte a partir de 10:00, 20 minutes d'attente)
INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES
(1, 1, 10, '2026-03-25 10:00:00'),  -- R1 : 10 pers, Hotel Loin (ira sur V1, trajet long)
(2, 2,  9, '2026-03-25 10:05:00'),  -- R2 :  9 pers, Hotel Centre (ira sur V2, trajet court)
(3, 3,  8, '2026-03-25 10:10:00');  -- R3 :  8 pers, Hotel Ville (sera reportee au groupe suivant)

-- Groupe 2 (nouvelle fenetre a partir de 11:00, > 20 min apres 10:00)
INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES
(4, 2, 14, '2026-03-25 11:00:00');  -- R4 : 14 pers, Hotel Centre -> sera decoupee entre V2 et V3

-- Attendu lors de la simulation pour le 2026-03-25 :
-- - Groupe 1 (depart vers ~10:10) :
--   * V1 (10 places) : prend R1 (10 pers) vers Hotel Loin -> trajet ~4h (non dispo a 11:00)
--   * V2 ( 9 places) : prend R2 ( 9 pers) vers Hotel Centre -> trajet court (~20 min, dispo a 11:00)
--   * R3 (8 pers) : aucun vehicule avec des places restantes -> reportee au groupe 2
-- - Groupe 2 (depart vers 11:00) :
--   * R4 (14 pers) :
--       - 9 pers sur V2 (VEH-CITY-9)
--       - 5 pers sur V3 (VEH-CITY-7) -> RESERVATION DECOUPEE
--   * R3 (8 pers) : assignee a V3 (reste 2 places) car V1 est encore en trajet long
--
-- Ces donnees permettent de tester :
-- - le regroupement par temps d'attente en deux groupes,
-- - le report d'une reservation du premier groupe vers le suivant,
-- - l'indisponibilite d'un vehicule a cause d'un trajet long,
-- - la division dune reservation sur plusieurs vehicules.
