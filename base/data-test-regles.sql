-- ============================================
-- Script de données pour tester TOUTES les règles de gestion
-- Exécuter sql.sql d'abord, puis ce fichier.
-- ============================================

\c tour_operateur;

-- Nettoyage complet des données existantes (reset des IDs)
TRUNCATE TABLE distance RESTART IDENTITY CASCADE;
TRUNCATE TABLE reservation RESTART IDENTITY CASCADE;
TRUNCATE TABLE vehicule RESTART IDENTITY CASCADE;
TRUNCATE TABLE parametre RESTART IDENTITY CASCADE;
TRUNCATE TABLE aeroport RESTART IDENTITY CASCADE;
TRUNCATE TABLE hotel RESTART IDENTITY CASCADE;
TRUNCATE TABLE client RESTART IDENTITY CASCADE;
TRUNCATE TABLE type_carburant RESTART IDENTITY CASCADE;
-- Règles testées :
--   1. Regroupement par temps d'attente (TEMPS_ATTENTE = 20 min)
--   2. Disponibilité véhicule (parti → indisponible tant que pas revenu)
--   3. Priorité au véhicule avec le moins de trajets
--   4. Priorité au véhicule avec la capacité la plus proche du nb passagers
--   5. Priorité au diesel (en cas d'égalité sur les autres critères)
--   6. Report des réservations non assignées au groupe suivant
-- ============================================

-- Clients
INSERT INTO client (nom, prenom, email) VALUES
('Dupont', 'Jean', 'jean.dupont@email.com'),       -- id=1
('Martin', 'Sophie', 'sophie.martin@email.com'),    -- id=2
('Bernard', 'Pierre', 'pierre.bernard@email.com'),  -- id=3
('Dubois', 'Marie', 'marie.dubois@email.com'),      -- id=4
('Leroy', 'Luc', 'luc.leroy@email.com'),            -- id=5
('Moreau', 'Claire', 'claire.moreau@email.com'),     -- id=6
('Petit', 'Thomas', 'thomas.petit@email.com');       -- id=7

-- Hôtels (distances variées depuis l'aéroport)
INSERT INTO hotel (nom, adresse, ville) VALUES
('Hotel Proche', '10 Route Aéroport', 'Antananarivo'),          -- id=1  (5 km)
('Hotel Centre', '45 Ave de l''Indépendance', 'Antananarivo'),  -- id=2  (15 km)
('Hotel Loin', '120 Route Nationale 7', 'Antsirabe'),           -- id=3  (40 km)
('Hotel Grand Luxe', '1 Boulevard du Lac', 'Antsirabe');        -- id=4  (60 km)

-- Types de carburant
INSERT INTO type_carburant (reference, nom) VALUES
('ESS', 'Essence'),  -- id=1
('DSL', 'Diesel');   -- id=2

-- Véhicules (5 véhicules avec capacités et carburants variés)
-- Tous à 60 km/h pour simplifier les calculs de temps de trajet
INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, type_carburant_id) VALUES
('Renault', 'Master', 15, 'VEH-001', 60, 2),      -- id=1, 15 places, DIESEL
('Toyota', 'Hiace', 8, 'VEH-002', 60, 2),          -- id=2, 8 places, DIESEL
('Ford', 'Transit', 5, 'VEH-003', 60, 1),          -- id=3, 5 places, ESSENCE
('Peugeot', 'Boxer', 8, 'VEH-004', 60, 1),         -- id=4, 8 places, ESSENCE
('Mercedes', 'Sprinter', 5, 'VEH-005', 60, 2);     -- id=5, 5 places, DIESEL

-- Aéroport
INSERT INTO aeroport (code, libelle) VALUES
('TNR', 'Aéroport International Ivato');  -- id=1

-- Distances depuis l'aéroport vers les hôtels
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(NULL, 1, 1, 5.0),    -- Aéroport → Hotel Proche      (5 km)
(NULL, 1, 2, 15.0),   -- Aéroport → Hotel Centre       (15 km)
(NULL, 1, 3, 40.0),   -- Aéroport → Hotel Loin         (40 km)
(NULL, 1, 4, 60.0);   -- Aéroport → Hotel Grand Luxe   (60 km)

-- Distances entre hôtels
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(1, NULL, 2, 10.0),   -- Hotel Proche → Hotel Centre
(1, NULL, 3, 35.0),   -- Hotel Proche → Hotel Loin
(1, NULL, 4, 55.0),   -- Hotel Proche → Hotel Grand Luxe
(2, NULL, 3, 25.0),   -- Hotel Centre → Hotel Loin
(2, NULL, 4, 45.0),   -- Hotel Centre → Hotel Grand Luxe
(3, NULL, 4, 20.0);   -- Hotel Loin → Hotel Grand Luxe

-- Paramètres
INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par défaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('MARGE_PLACES', '0', 'Nombre de places de marge pour optimisation'),
('TEMPS_ATTENTE_MINUTES', '20', 'Durée d attente pour regrouper les départs (20 min)');

-- ============================================
-- RESERVATIONS DE TEST : 2026-03-15
-- TEMPS_ATTENTE = 20 minutes
-- ============================================
--
-- GROUPE A : vols arrivant entre 08:00 et 08:15 (fenêtre 20 min depuis 08:00)
--   R1: 13 passagers → Hotel Grand Luxe (60 km)
--   R2:  4 passagers → Hotel Proche (5 km)
--   R3:  7 passagers → Hotel Centre (15 km)
--   → départ groupé = 08:15
--
-- GROUPE B : vols arrivant entre 08:50 et 09:05 (>20 min après 08:00 → nouveau)
--   R4:  7 passagers → Hotel Loin (40 km)
--   R5:  4 passagers → Hotel Proche (5 km)
--   R6: 14 passagers → Hotel Centre (15 km)
--   → départ groupé = 09:05
--
-- GROUPE C : vol arrivant à 10:30 (>20 min après 08:50 → nouveau)
--   R7:  4 passagers → Hotel Proche (5 km)
--   → départ = 10:30
--

INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) VALUES
-- GROUPE A
(1, 4, 13, '2026-03-15 08:00:00'),   -- R1: Dupont, Hotel Grand Luxe, 13 pass
(2, 1,  4, '2026-03-15 08:10:00'),   -- R2: Martin, Hotel Proche, 4 pass
(3, 2,  7, '2026-03-15 08:15:00'),   -- R3: Bernard, Hotel Centre, 7 pass
-- GROUPE B
(4, 3,  7, '2026-03-15 08:50:00'),   -- R4: Dubois, Hotel Loin, 7 pass
(5, 1,  4, '2026-03-15 09:00:00'),   -- R5: Leroy, Hotel Proche, 4 pass
(6, 2, 14, '2026-03-15 09:05:00'),   -- R6: Moreau, Hotel Centre, 14 pass
-- GROUPE C
(7, 1,  4, '2026-03-15 10:30:00');   -- R7: Petit, Hotel Proche, 4 pass

-- ============================================
-- DÉROULEMENT ATTENDU DE LA SIMULATION
-- ============================================
--
-- ┌─────────────────────────────────────────────────────────────────────┐
-- │ GROUPE A — départ 08:15                                           │
-- │ Tous les véhicules ont 0 trajets                                  │
-- ├─────────────────────────────────────────────────────────────────────┤
-- │                                                                    │
-- │ R1 (13 pass) : seul VEH-001 (15 pl) peut accueillir 13 passagers │
-- │   → VEH-001 (diesel, capacité la plus proche)                     │
-- │   Trajet : Aéroport →(60km)→ Hotel Grand Luxe →(60km)→ Aéroport  │
-- │   Durée : 120 km / 60 km/h = 120 min                             │
-- │   Retour : 08:15 + 120 min = 10:15                               │
-- │                                                                    │
-- │ R3 (7 pass) : VEH-002 (8pl,dsl,0tr) vs VEH-004 (8pl,ess,0tr)    │
-- │   Même nb trajets (0), même capacité (8) → diesel prioritaire     │
-- │   → VEH-002 (diesel)                                              │
-- │   Trajet : Aéroport →(15km)→ Hotel Centre →(15km)→ Aéroport      │
-- │   Durée : 30 km / 60 km/h = 30 min                               │
-- │   Retour : 08:15 + 30 min = 08:45                                │
-- │                                                                    │
-- │ R2 (4 pass) : VEH-003 (5pl,ess,0tr) vs VEH-005 (5pl,dsl,0tr)    │
-- │               vs VEH-004 (8pl,ess,0tr)                            │
-- │   Même nb trajets (0). Capacité la plus proche :                  │
-- │     VEH-003/VEH-005 : 5-4=1  vs  VEH-004 : 8-4=4                │
-- │   VEH-003 vs VEH-005 : même capacité → diesel prioritaire        │
-- │   → VEH-005 (diesel, 5 places)                                    │
-- │   Trajet : Aéroport →(5km)→ Hotel Proche →(5km)→ Aéroport        │
-- │   Durée : 10 km / 60 km/h = 10 min                               │
-- │   Retour : 08:15 + 10 min = 08:25                                │
-- │                                                                    │
-- └─────────────────────────────────────────────────────────────────────┘
--
-- ┌─────────────────────────────────────────────────────────────────────┐
-- │ GROUPE B — départ 09:05                                           │
-- │ État des véhicules à 09:05 :                                      │
-- │   VEH-001 : parti 08:15, retour 10:15 → INDISPONIBLE ❌           │
-- │   VEH-002 : parti 08:15, retour 08:45 → disponible ✓ (1 trajet)  │
-- │   VEH-003 : jamais parti                → disponible ✓ (0 trajet) │
-- │   VEH-004 : jamais parti                → disponible ✓ (0 trajet) │
-- │   VEH-005 : parti 08:15, retour 08:25 → disponible ✓ (1 trajet)  │
-- ├─────────────────────────────────────────────────────────────────────┤
-- │                                                                    │
-- │ R6 (14 pass) : seul VEH-001 (15 pl) assez grand, mais             │
-- │   VEH-001 INDISPONIBLE (retour 10:15 > 09:05)                    │
-- │   → AUCUN véhicule → REPORTÉ AU GROUPE C                         │
-- │                                                                    │
-- │ R4 (7 pass) : VEH-002 (8pl,dsl,1tr) vs VEH-004 (8pl,ess,0tr)    │
-- │   VEH-004 a moins de trajets (0 < 1) → VEH-004 gagne             │
-- │   → VEH-004 (essence, 8 places, 0 trajet)                        │
-- │   Trajet : Aéroport →(40km)→ Hotel Loin →(40km)→ Aéroport        │
-- │   Durée : 80 km / 60 km/h = 80 min                               │
-- │   Retour : 09:05 + 80 min = 10:25                                │
-- │                                                                    │
-- │ R5 (4 pass) : VEH-002 (8pl,dsl,1tr) vs VEH-003 (5pl,ess,0tr)    │
-- │               vs VEH-005 (5pl,dsl,1tr)                            │
-- │   VEH-003 a moins de trajets (0) → VEH-003 gagne                 │
-- │   → VEH-003 (essence, 5 places, 0 trajet)                        │
-- │   Trajet : Aéroport →(5km)→ Hotel Proche →(5km)→ Aéroport        │
-- │   Durée : 10 min. Retour : 09:05 + 10 min = 09:15               │
-- │                                                                    │
-- └─────────────────────────────────────────────────────────────────────┘
--
-- ┌─────────────────────────────────────────────────────────────────────┐
-- │ GROUPE C — départ 10:30 (+ R6 reporté du Groupe B)                │
-- │ État des véhicules à 10:30 :                                      │
-- │   VEH-001 : retour 10:15 → disponible ✓ (1 trajet)               │
-- │   VEH-002 : retour 08:45 → disponible ✓ (1 trajet)               │
-- │   VEH-003 : retour 09:15 → disponible ✓ (1 trajet)               │
-- │   VEH-004 : retour 10:25 → disponible ✓ (1 trajet)               │
-- │   VEH-005 : retour 08:25 → disponible ✓ (1 trajet)               │
-- ├─────────────────────────────────────────────────────────────────────┤
-- │                                                                    │
-- │ R6 (14 pass, reporté) : VEH-001 (15pl,dsl,1tr) seul assez grand  │
-- │   → VEH-001 (diesel, 15 places)                                   │
-- │                                                                    │
-- │ R7 (4 pass) : tous ont 1 trajet. Capacité la plus proche :       │
-- │   VEH-003 (5pl,ess) : 5-4=1                                      │
-- │   VEH-005 (5pl,dsl) : 5-4=1                                      │
-- │   VEH-002 (8pl,dsl) : 8-4=4                                      │
-- │   VEH-004 (8pl,ess) : 8-4=4                                      │
-- │   VEH-003 vs VEH-005 : même trajets, même capacité               │
-- │   → diesel prioritaire → VEH-005                                  │
-- │                                                                    │
-- └─────────────────────────────────────────────────────────────────────┘
--
-- ============================================
-- RÉSUMÉ FINAL
-- ============================================
-- Groupe A (08:15) :
--   VEH-001 → R1 (13 pass, Hotel Grand Luxe)
--   VEH-002 → R3 (7 pass, Hotel Centre)
--   VEH-005 → R2 (4 pass, Hotel Proche)
--
-- Groupe B (09:05) :
--   VEH-004 → R4 (7 pass, Hotel Loin)       ← 0 trajets > VEH-002 (1 trajet)
--   VEH-003 → R5 (4 pass, Hotel Proche)     ← 0 trajets > VEH-005 (1 trajet)
--   R6 (14 pass) → REPORTÉ (VEH-001 pas revenu)
--
-- Groupe C (10:30) :
--   VEH-001 → R6 (14 pass, Hotel Centre)    ← reporté du Groupe B
--   VEH-005 → R7 (4 pass, Hotel Proche)     ← diesel > VEH-003 (essence)
--
-- Réservations non assignées : AUCUNE
-- ============================================
