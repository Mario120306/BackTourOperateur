-- Script d'insertion de données de test pour PostgreSQL

-- Insertion de clients
INSERT INTO client (nom, prenom, email) VALUES
('Dupont', 'Jean', 'jean.dupont@email.com'),
('Martin', 'Sophie', 'sophie.martin@email.com'),
('Bernard', 'Pierre', 'pierre.bernard@email.com'),
('Dubois', 'Marie', 'marie.dubois@email.com'),
('Leroy', 'Luc', 'luc.leroy@email.com'),
('Moreau', 'Claire', 'claire.moreau@email.com'),
('Simon', 'Thomas', 'thomas.simon@email.com'),
('Laurent', 'Emma', 'emma.laurent@email.com'),
('Lefebvre', 'Alexandre', 'alexandre.lefebvre@email.com'),
('Michel', 'Julie', 'julie.michel@email.com');

-- Insertion d'hôtels
INSERT INTO hotel (nom, adresse, ville, pays) VALUES
('Hotel Paradise', '15 Avenue des Palmiers', 'Nosy Be', 'Madagascar'),
('Grand Hotel Ivato', '23 Route de l''Aéroport', 'Antananarivo', 'Madagascar'),
('Hotel Colbert', '29 Rue Prince Ratsimamanga', 'Antananarivo', 'Madagascar'),
('Hotel Le Louvre', '8 Rue Marechal Joffre', 'Antsirabe', 'Madagascar'),
('Palissandre Hotel', '17 Rue Ratsitatane', 'Antananarivo', 'Madagascar'),
('Hotel Plage de Majunga', '5 Boulevard Maritime', 'Mahajanga', 'Madagascar'),
('Hotel Sunny Beach', '12 Rue de la Plage', 'Ifaty', 'Madagascar'),
('Hotel des Thermes', '34 Avenue de l''Indépendance', 'Antsirabe', 'Madagascar'),
('Hotel Le Royal', '45 Rue Rainibetsimisaraka', 'Antananarivo', 'Madagascar'),
('Hotel Vanille', '7 Boulevard de la Mer', 'Nosy Be', 'Madagascar');

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

-- Insertion des distances (en km) entre aéroports et hôtels
-- Distance depuis l'aéroport Ivato (id=1) vers les hôtels
INSERT INTO distance (id_from, id_to, valeur) VALUES
(1, 2, 15.5),   -- Ivato vers Nosy Be (représente une distance symbolique locale)
(1, 3, 25.0),  -- Ivato vers Tuléar
(1, 4, 12.8),  -- Ivato vers Mahajanga
(1, 5, 8.5),   -- Ivato vers Toamasina
(2, 1, 15.5),  -- Nosy Be vers Ivato
(2, 3, 45.0),  -- Nosy Be vers Tuléar
(3, 1, 25.0),  -- Tuléar vers Ivato
(4, 1, 12.8),  -- Mahajanga vers Ivato
(5, 1, 8.5);   -- Toamasina vers Ivato

-- Insertion des distances (en km) entre aéroports et hôtels
-- Aéroport Ivato (TNR, id=1) vers les hôtels
INSERT INTO distance_hotel (id_aeroport, id_hotel, valeur) VALUES
(1, 1, 520.0),  -- Ivato vers Hotel Paradise (Nosy Be)
(1, 2, 5.0),    -- Ivato vers Grand Hotel Ivato (Antananarivo)
(1, 3, 8.5),    -- Ivato vers Hotel Colbert (Antananarivo)
(1, 4, 170.0),  -- Ivato vers Hotel Le Louvre (Antsirabe)
(1, 5, 7.0),    -- Ivato vers Palissandre Hotel (Antananarivo)
(1, 6, 560.0),  -- Ivato vers Hotel Plage de Majunga (Mahajanga)
(1, 7, 950.0),  -- Ivato vers Hotel Sunny Beach (Ifaty/Tuléar)
(1, 8, 175.0),  -- Ivato vers Hotel des Thermes (Antsirabe)
(1, 9, 6.0),    -- Ivato vers Hotel Le Royal (Antananarivo)
(1, 10, 525.0); -- Ivato vers Hotel Vanille (Nosy Be)

-- Aéroport Nosy Be (NOS, id=2) vers les hôtels locaux
INSERT INTO distance_hotel (id_aeroport, id_hotel, valeur) VALUES
(2, 1, 12.0),   -- Fascene vers Hotel Paradise (Nosy Be)
(2, 10, 15.0);  -- Fascene vers Hotel Vanille (Nosy Be)

-- Aéroport Mahajanga (MJN, id=4) vers hôtel local
INSERT INTO distance_hotel (id_aeroport, id_hotel, valeur) VALUES
(4, 6, 8.0);    -- Mahajanga vers Hotel Plage de Majunga

-- Aéroport Tuléar (TLE, id=3) vers hôtel local
INSERT INTO distance_hotel (id_aeroport, id_hotel, valeur) VALUES
(3, 7, 25.0);   -- Tuléar vers Hotel Sunny Beach (Ifaty)

-- Insertion des paramètres
INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par défaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('MARGE_PLACES', '0', 'Nombre de places de marge pour optimisation'),
('TEMPS_ARRET_MINUTES', '30', 'Temps d arret à destination en minutes (dépose/récupération passagers)');
