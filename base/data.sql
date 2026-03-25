-- Script d'insertion de données de test pour PostgreSQL

-- Insertion de clients
INSERT INTO client (nom, prenom, email) VALUES
('client1', 'Jean', 'jean.dupont@email.com'),
('client2', 'Sophie', 'sophie.martin@email.com'),
('client3', 'Pierre', 'pierre.bernard@email.com'),
('client4', 'Marie', 'marie.dubois@email.com'),
('client5', 'Luc', 'luc.leroy@email.com'),
('client6', 'Claire', 'claire.moreau@email.com');


-- Insertion d'hôtels
INSERT INTO hotel (nom, adresse, ville) VALUES
('Hotel Paradise', '15 Avenue des Palmiers', 'Nosy Be'),
('Grand Hotel Ivato', '23 Route de l''Aéroport', 'Antananarivo'),
('Hotel Colbert', '29 Rue Prince Ratsimamanga', 'Antananarivo'),
('Hotel Le Louvre', '8 Rue Marechal Joffre', 'Antsirabe'),
('Palissandre Hotel', '17 Rue Ratsitatane', 'Antananarivo'),
('Hotel Plage de Majunga', '5 Boulevard Maritime', 'Mahajanga'),
('Hotel Sunny Beach', '12 Rue de la Plage', 'Ifaty'),
('Hotel des Thermes', '34 Avenue de l''Indépendance', 'Antsirabe'),
('Hotel Le Royal', '45 Rue Rainibetsimisaraka', 'Antananarivo'),
('Hotel Vanille', '7 Boulevard de la Mer', 'Nosy Be');

-- Insertion des types de carburant
INSERT INTO type_carburant (reference, nom) VALUES
('ESS', 'Essence'),
('DSL', 'Diesel'),
('HYB', 'Hybride'),
('ELEC', 'Electrique'),
('GPL', 'GPL'),
('GNV', 'Gaz Naturel');

-- Insertion des véhicules
INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, type_carburant_id) VALUES
('Toyota', 'Hiace', 7, 'VEH-001', 80, 2),
('Mercedes', 'Sprinter', 12, 'VEH-002', 75, 2),
('Ford', 'Transit', 4, 'VEH-003', 85, 1),
('Volkswagen', 'Transporter', 8, 'VEH-004', 80, 2),
('Renault', 'Master', 20, 'VEH-005', 70, 2),
('Peugeot', 'Expert', 6, 'VEH-006', 85, 1),
('Iveco', 'Daily', 15, 'VEH-007', 72, 2),
('Fiat', 'Ducato', 9, 'VEH-008', 78, 1),
('Hyundai', 'H1', 8, 'VEH-009', 82, 2),
('Nissan', 'NV350', 12, 'VEH-010', 76, 1);

-- Insertion des aéroports
INSERT INTO aeroport (code, libelle) VALUES
('TNR', 'Aéroport International Ivato'),
('NOS', 'Aéroport de Fascene - Nosy Be'),
('TLE', 'Aéroport de Tuléar'),
('MJN', 'Aéroport de Mahajanga'),
('TMM', 'Aéroport de Toamasina'),
('DIE', 'Aéroport de Diego Suarez'),
('FTU', 'Aéroport de Fort Dauphin');

-- Insertion des distances (en km)
-- Structure: id_from_hotel (peut être NULL), id_from_aeroport (peut être NULL), id_to (hotel destination), valeur (km)

-- Distances depuis l'aéroport Ivato (TNR, id=1) vers les hôtels
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 1, 1, 520.0),  -- Aéroport Ivato vers Hotel Paradise (Nosy Be)
(NULL, 1, 2, 5.0),    -- Aéroport Ivato vers Grand Hotel Ivato (Antananarivo)
(NULL, 1, 3, 8.5),    -- Aéroport Ivato vers Hotel Colbert (Antananarivo)
(NULL, 1, 4, 170.0),  -- Aéroport Ivato vers Hotel Le Louvre (Antsirabe)
(NULL, 1, 5, 7.0),    -- Aéroport Ivato vers Palissandre Hotel (Antananarivo)
(NULL, 1, 6, 560.0),  -- Aéroport Ivato vers Hotel Plage de Majunga (Mahajanga)
(NULL, 1, 7, 950.0),  -- Aéroport Ivato vers Hotel Sunny Beach (Ifaty/Tuléar)
(NULL, 1, 8, 175.0),  -- Aéroport Ivato vers Hotel des Thermes (Antsirabe)
(NULL, 1, 9, 6.0),    -- Aéroport Ivato vers Hotel Le Royal (Antananarivo)
(NULL, 1, 10, 525.0); -- Aéroport Ivato vers Hotel Vanille (Nosy Be)

-- Distances depuis l'aéroport Nosy Be (NOS, id=2) vers les hôtels locaux
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 2, 1, 12.0),   -- Aéroport Fascene vers Hotel Paradise (Nosy Be)
(NULL, 2, 10, 15.0);  -- Aéroport Fascene vers Hotel Vanille (Nosy Be)

-- Distances depuis l'aéroport Tuléar (TLE, id=3) vers hôtel local
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 3, 7, 25.0);   -- Aéroport Tuléar vers Hotel Sunny Beach (Ifaty)

-- Distances depuis l'aéroport Mahajanga (MJN, id=4) vers hôtel local
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 4, 6, 8.0);    -- Aéroport Mahajanga vers Hotel Plage de Majunga

-- Distances entre hôtels (pour les trajets inter-hôtels)
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(2, NULL, 3, 3.5),    -- Grand Hotel Ivato vers Hotel Colbert
(2, NULL, 5, 2.0),    -- Grand Hotel Ivato vers Palissandre Hotel
(2, NULL, 9, 1.5),    -- Grand Hotel Ivato vers Hotel Le Royal
(3, NULL, 5, 1.8),    -- Hotel Colbert vers Palissandre Hotel
(3, NULL, 9, 2.0),    -- Hotel Colbert vers Hotel Le Royal
(5, NULL, 9, 1.2),    -- Palissandre Hotel vers Hotel Le Royal
(4, NULL, 8, 5.0),    -- Hotel Le Louvre vers Hotel des Thermes (Antsirabe)
(1, NULL, 10, 8.0);   -- Hotel Paradise vers Hotel Vanille (Nosy Be)

-- Insertion des paramètres
INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par défaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('MARGE_PLACES', '0', 'Nombre de places de marge pour optimisation'),
('TEMPS_ARRET_MINUTES', '30', 'Temps d arret à destination en minutes (dépose/récupération passagers)');
-- Insertion de réservations de test pour la date 2026-03-15
-- Pour tester l'optimisation avec regroupement de passagers
INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES
-- Groupe 1 : Grande famille (6 personnes) - doit être assignée en priorité
(1, 3, 6, '2026-03-15 10:00:00'),

-- Groupe 2 : Couple (2 personnes) - peut être combiné avec d'autres
(2, 5, 2, '2026-03-15 10:30:00'),

-- Groupe 3 : Famille moyenne (4 personnes)
(3, 2, 4, '2026-03-15 11:00:00'),

-- Groupe 4 : Trio (3 personnes) - peut être combiné
(4, 9, 3, '2026-03-15 11:30:00'),

-- Groupe 5 : Solo (1 personne) - doit être optimisé avec d'autres
(5, 5, 1, '2026-03-15 12:00:00'),

-- Groupe 6 : Duo (2 personnes) - peut être combiné
(6, 3, 2, '2026-03-15 12:30:00'),

-- Groupe 7 : Famille (5 personnes)
(7, 2, 5, '2026-03-15 13:00:00'),

-- Groupe 8 : Duo (2 personnes) - peut être combiné
(8, 9, 2, '2026-03-15 13:30:00'),

-- Groupe 9 : Grande famille (7 personnes)
(9, 5, 7, '2026-03-15 14:00:00'),

-- Groupe 10 : Solo (1 personne) - optimisation
(10, 3, 1, '2026-03-15 14:30:00');

-- RESULTATS ATTENDUS DE LA SIMULATION POUR 2026-03-15 :
-- Total : 35 passagers répartis sur 10 réservations
-- 
-- Véhicule DSL 20 places (VEH-005 Renault Master) - PRIORITAIRE DIESEL:
--   - 7 pers (id=9) + 5 pers (id=7) + 4 pers (id=3) = 16/20 places
--
-- Véhicule DSL 12 places (VEH-002 Mercedes Sprinter) - PRIORITAIRE DIESEL:
--   - 6 pers (id=1) + 3 pers (id=4) + 2 pers (id=8) = 11/12 places
--
-- Véhicule DSL 8 places (VEH-004 Volkswagen Transporter) - PRIORITAIRE DIESEL:
--   - 2 pers (id=2) + 1 pers (id=5) + 2 pers (id=6) + 1 pers (id=10) = 6/8 places
--
-- Véhicules DSL 7 et 9 places restent disponibles (pas assez de réservations)
-- Véhicules ESSENCE ne sont pas utilisés (diesel prioritaire et capacité suffisante)-- RESERVATIONS ADDITIONNELLES pour 2026-03-20 (test avec depassement de capacite)  
INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES  
(1, 2, 15, '2026-03-20 10:00:00'),  -- Tres grand groupe  
(2, 3, 18, '2026-03-20 10:30:00'),  -- Tres grand groupe  
(3, 5, 12, '2026-03-20 11:00:00'),  -- Grand groupe  
(4, 2, 8, '2026-03-20 11:30:00'),   -- Groupe moyen  
(5, 9, 25, '2026-03-20 12:00:00');  -- IMPOSSIBLE (aucun vehicule n'a 25 places) 
