-- ============================================
-- Script de données minimales pour tester le regroupement
-- par temps d'attente sur une base vierge
-- Exécuter sql.sql d'abord, puis ce fichier
-- ============================================

-- Clients
INSERT INTO client (nom, prenom, email) VALUES
('Dupont', 'Jean', 'jean.dupont@email.com'),
('Martin', 'Sophie', 'sophie.martin@email.com'),
('Bernard', 'Pierre', 'pierre.bernard@email.com'),
('Dubois', 'Marie', 'marie.dubois@email.com'),
('Leroy', 'Luc', 'luc.leroy@email.com'),
('Moreau', 'Claire', 'claire.moreau@email.com');

-- Hôtels (proches d'Antananarivo)
INSERT INTO hotel (nom, adresse, ville) VALUES
('Grand Hotel Ivato', '23 Route de l''Aéroport', 'Antananarivo'),
('Hotel Colbert', '29 Rue Prince Ratsimamanga', 'Antananarivo'),
('Palissandre Hotel', '17 Rue Ratsitatane', 'Antananarivo'),
('Hotel Le Royal', '45 Rue Rainibetsimisaraka', 'Antananarivo');

-- Types de carburant
INSERT INTO type_carburant (reference, nom) VALUES
('ESS', 'Essence'),
('DSL', 'Diesel');

-- Véhicules
INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, type_carburant_id) VALUES
('Renault', 'Master', 15, 'VEH-001', 70, 2),
('Toyota', 'Hiace', 8, 'VEH-002', 80, 2),
('Ford', 'Transit', 5, 'VEH-003', 85, 1);

-- Aéroport
INSERT INTO aeroport (code, libelle) VALUES
('TNR', 'Aéroport International Ivato');

-- Distances depuis l'aéroport Ivato (id=1) vers les hôtels
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 1, 1, 5.0),    -- Aéroport → Grand Hotel Ivato
(NULL, 1, 2, 8.5),    -- Aéroport → Hotel Colbert
(NULL, 1, 3, 7.0),    -- Aéroport → Palissandre Hotel
(NULL, 1, 4, 6.0);    -- Aéroport → Hotel Le Royal

-- Distances entre hôtels
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(1, NULL, 2, 3.5),    -- Grand Hotel Ivato → Hotel Colbert
(1, NULL, 3, 2.0),    -- Grand Hotel Ivato → Palissandre
(1, NULL, 4, 1.5),    -- Grand Hotel Ivato → Le Royal
(2, NULL, 3, 1.8),    -- Hotel Colbert → Palissandre
(2, NULL, 4, 2.0),    -- Hotel Colbert → Le Royal
(3, NULL, 4, 1.2);    -- Palissandre → Le Royal

-- Paramètres
INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par défaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('MARGE_PLACES', '0', 'Nombre de places de marge pour optimisation'),
('TEMPS_ATTENTE_MINUTES', '30', 'Durée d attente en minutes pour regrouper les départs (0 = pas de regroupement)');

-- ============================================
-- RESERVATIONS DE TEST : 2026-03-15
-- Temps d'attente = 30 minutes
-- ============================================
--
-- GROUPE A : vols arrivant entre 08:05 et 08:20 (fenêtre de 30 min depuis 08:05)
--   → départ groupé = 08:20 (le dernier vol du groupe)
--
-- GROUPE B : vol arrivant à 09:00 (plus de 30 min après 08:05 → nouveau groupe)
--   Puis 09:25 est dans la fenêtre de 30 min depuis 09:00
--   → départ groupé = 09:25
--
-- GROUPE C : vol arrivant à 10:45 (plus de 30 min après 09:00 → seul)
--   → départ = 10:45

INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES
(1, 2, 3, '2026-03-15 08:05:00'),   -- Dupont, Hotel Colbert, 3 pers, vol 08:05
(2, 3, 2, '2026-03-15 08:15:00'),   -- Martin, Palissandre, 2 pers, vol 08:15
(3, 1, 4, '2026-03-15 08:20:00'),   -- Bernard, Grand Hotel, 4 pers, vol 08:20
(4, 4, 2, '2026-03-15 09:00:00'),   -- Dubois, Le Royal, 2 pers, vol 09:00
(5, 2, 3, '2026-03-15 09:25:00'),   -- Leroy, Hotel Colbert, 3 pers, vol 09:25
(6, 1, 2, '2026-03-15 10:45:00');   -- Moreau, Grand Hotel, 2 pers, vol 10:45

-- ============================================
-- RÉSULTATS ATTENDUS (TEMPS_ATTENTE = 30 min) :
-- ============================================
-- Groupe A (08:05, 08:15, 08:20) → départ 08:20, total 9 passagers
--   → VEH-001 Renault Master 15 places (diesel prioritaire)
--
-- Groupe B (09:00, 09:25) → départ 09:25, total 5 passagers
--   → VEH-002 Toyota Hiace 8 places (diesel)
--
-- Groupe C (10:45) → départ 10:45, total 2 passagers
--   → VEH-003 Ford Transit 5 places (essence)
-- ============================================
