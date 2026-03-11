-- ============================================
-- SCRIPT DE TEST - REGROUPEMENT PAR TEMPS D'ATTENTE
-- ============================================
-- Ce script permet de tester le regroupement des réservations
-- selon le temps d'attente paramétrable (TEMPS_ATTENTE_MINUTES).
--
-- RÈGLE : La première arrivée ouvre une fenêtre de X minutes.
-- Toutes les réservations arrivant dans cette fenêtre sont regroupées.
-- Le départ effectif du groupe = l'heure d'arrivée la plus tardive du groupe.
--
-- Exemple avec TEMPS_ATTENTE_MINUTES = 30 :
--   Réservations à 08:05, 08:15, 08:20
--   → Fenêtre ouverte à 08:05, fermeture à 08:35
--   → Les 3 sont dans le même groupe, départ = 08:20 (dernière arrivée)

-- ============================================
-- S'assurer que le paramètre est configuré
-- ============================================
INSERT INTO parametre (code, valeur, description)
VALUES ('TEMPS_ATTENTE_MINUTES', '30', 'Durée d attente en minutes pour regrouper les départs')
ON CONFLICT (code) DO UPDATE SET valeur = '30';

-- Afficher les réservations existantes pour le 15 mars 2026
SELECT 
    r.id,
    c.nom as client,
    h.nom as hotel,
    r.nombre_passage,
    r.date_heure_arrive
FROM reservation r
JOIN client c ON r.id_client = c.id
JOIN hotel h ON r.id_hotel = h.id
WHERE DATE(r.date_heure_arrive) = '2026-03-15'
ORDER BY r.date_heure_arrive;

-- ============================================
-- TEST 1 : Groupe avec temps d'attente (08:05-08:20)
-- ============================================
-- 3 réservations dans une fenêtre de 30 min
-- Fenêtre ouverte à 08:05, tout jusqu'à 08:35 est regroupé
-- Départ effectif = 08:20 (dernière arrivée du groupe)

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 08:05:00'
WHERE id = 1;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 08:15:00'
WHERE id = 2;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 08:20:00'
WHERE id = 3;

-- ============================================
-- TEST 2 : Deuxième groupe (10:00-10:25)
-- ============================================
-- 2 réservations dans une nouvelle fenêtre (bien après le groupe 1)
-- Départ effectif = 10:25

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 10:00:00'
WHERE id = 4;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 10:25:00'
WHERE id = 5;

-- ============================================
-- TEST 3 : Réservation isolée (14:00)
-- ============================================
-- Seule dans sa fenêtre → départ = 14:00

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 14:00:00'
WHERE id = 6;

-- ============================================
-- TEST 4 : Juste à la limite de la fenêtre
-- ============================================
-- 08:05 + 30 min = 08:35
-- Réservation à 08:34 → DOIT être dans le groupe 1
-- Réservation à 08:36 → NE DOIT PAS être dans le groupe 1

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 08:34:00'
WHERE id = 7;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 09:30:00'
WHERE id = 8;

-- Vérifier les modifications
SELECT 
    r.id,
    c.nom as client,
    h.nom as hotel,
    r.nombre_passage,
    TO_CHAR(r.date_heure_arrive, 'HH24:MI') as heure_arrivee,
    r.date_heure_arrive
FROM reservation r
JOIN client c ON r.id_client = c.id
JOIN hotel h ON r.id_hotel = h.id
WHERE DATE(r.date_heure_arrive) = '2026-03-15'
ORDER BY r.date_heure_arrive;

-- ============================================
-- RÉSULTAT ATTENDU (TEMPS_ATTENTE = 30 min) :
-- ============================================
-- Groupe 1 (fenêtre 08:05-08:35) :
--   IDs 1 (08:05), 2 (08:15), 3 (08:20), 7 (08:34)
--   → Départ effectif = 08:34 (dernière arrivée)
--   → Assignés au même véhicule (si capacité suffisante)
--
-- Groupe 2 (fenêtre 09:30-10:00) :
--   ID 8 (09:30)
--   → Départ effectif = 09:30
--
-- Groupe 3 (fenêtre 10:00-10:30) :
--   IDs 4 (10:00), 5 (10:25)
--   → Départ effectif = 10:25
--
-- Groupe 4 (fenêtre 14:00-14:30) :
--   ID 6 (14:00)
--   → Départ effectif = 14:00
-- ============================================

-- ============================================
-- RESTAURER LES DONNÉES ORIGINALES
-- ============================================
-- Si vous voulez revenir aux données de test initiales,
-- exécutez à nouveau le fichier data.sql

-- ============================================
-- TEST AVEC GRANDES CAPACITÉS
-- ============================================
-- Tester le regroupement avec beaucoup de passagers à la même heure

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 16:00:00',
    nombre_passage = 8
WHERE id = 9;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 16:00:00',
    nombre_passage = 7
WHERE id = 10;

-- Ces deux réservations (15 passagers total) devraient être ensemble
-- si un véhicule a une capacité suffisante

-- Afficher le résumé final
SELECT 
    TO_CHAR(r.date_heure_arrive, 'HH24:MI') as heure,
    COUNT(*) as nb_reservations,
    SUM(r.nombre_passage) as total_passagers
FROM reservation r
WHERE DATE(r.date_heure_arrive) = '2026-03-15'
GROUP BY TO_CHAR(r.date_heure_arrive, 'HH24:MI')
ORDER BY heure;
