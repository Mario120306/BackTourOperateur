Azafady kely

Aona tsara mo ry zareo le sprint 7 am Mr naina

Ohatra hoe misy 
V1 8
V2 6

R1 7
R2 4

De le r1 miditra am le v1 de le r2 ampidirina amze tomobil mety amnazy ndrindra ve aa hoe fenona lo ze tomobil misy olona c'est à dire v1?

Sa ve hoe  tsy miova le hoe ze supérieur ou égal amnazy aloha ny ny hidirany de any am tsosy zany vo sarahana?



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
('Toyota',   'LongTrip', 8, 'VEH-1', 60, 1),  -- V1 Diesel 10 places
('Mercedes', 'City9',     6, 'VEH-2',  60, 1);  -- V2 Diesel 9 places


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
(1, 1,15, '2026-03-25 10:00:00'),  -- R1 : 10 pers, Hotel Loin (ira sur V1, trajet long)
(2, 1,4, '2026-03-25 10:05:00') ,
(3, 1,3, '2026-03-25 10:05:00'); -- R2 :  9 pers, Hotel Centre (ira sur V2, trajet court)
 -- R3 :  8 pers, Hotel Ville (sera reportee au groupe suivant)

-- Groupe 2 (nouvelle fenetre a partir de 11:00, > 20 min apres 10:00)


--Donne fitsarana 19/03/2026  
INSERT INTO client




réservation,nb,date,heure,hotel
Client1,7,19/3/26,08:00:00,hotel 1
Client2,20,19/3/26,08:00:00,hotel 2
Client3,3,19/3/26,09:10:00,hotel 1
Client4,10,19/3/26,09:15:00,hotel 1
Client5,5,19/3/26,09:20:00,hotel 1
Client6,12,19/3/26,13:30:00,hotel 1


véhicule,Place,type,heure disponibilité
véhicule 1,5,diesel,08:00:00
véhicule 2,5,essence,08:00:00
véhicule 3,12,diesel,08:00:00
véhicule 4,8,diesel,08:00:00
véhicule 5,12,essence,13:00:00


véhicule,client,nb pers,heure depart,heure retour,min durée
véhicule 3,Client 2,12,08:00:00,09:24:00,84
véhicule 3,Client 4,10,09:24:00,13:00:00,216
          ,Client 3,2,09:24:00,13:00:00,216
véhicule 4,Client 2,8,09:24:00,13:06:00,222
          ,Client 3,1,09:24:00,13:06:00,222
véhicule 1,Client 1,5,09:24:00,13:00:00,216
véhicule 2,Client 1,2,09:24:00,13:00:00,216
          ,Client 5,3,09:24:00,13:00:00,216
véhicule 5,Client 6,12,13:30:00,17:00:00,210
véhicule 1,Client 5,2,13:30:00,17:00:00,210