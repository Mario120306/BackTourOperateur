-- ============================================
-- SCRIPT DE TEST - REGROUPEMENT PAR HEURE
-- ============================================
-- Ce script permet de tester le regroupement des réservations
-- selon l'heure d'arrivée identique

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
-- TEST 1 : Même heure (08:00)
-- ============================================
-- Modifier 3 réservations pour qu'elles aient la même heure
-- Ces 3 réservations devraient être regroupées ensemble

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 08:00:00'
WHERE id IN (1, 2, 3);

-- ============================================
-- TEST 2 : Heure différente (10:00)
-- ============================================
-- Modifier 2 réservations pour une autre heure
-- Ces 2 réservations devraient être dans un autre véhicule

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 10:00:00'
WHERE id IN (4, 5);

-- ============================================
-- TEST 3 : Heure isolée (14:00)
-- ============================================
-- Modifier 1 réservation pour une heure isolée
-- Cette réservation devrait être seule dans son véhicule

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 14:00:00'
WHERE id = 6;

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
-- RÉSULTAT ATTENDU :
-- ============================================
-- Groupe 08:00 (IDs 1, 2, 3) → Véhicule 1 (si capacité suffisante)
-- Groupe 10:00 (IDs 4, 5)    → Véhicule 2 (si capacité suffisante)  
-- Groupe 14:00 (ID 6)        → Véhicule 3 (si capacité suffisante)
--
-- Chaque véhicule affichera :
-- - Heure de départ (calculée en remontant depuis l'arrivée)
-- - Heure de retour (après avoir déposé tous les passagers)
-- - Durée totale du trajet
-- ============================================

-- ============================================
-- TEST AVANCÉ : Heures très proches mais différentes
-- ============================================
-- Pour tester que seules les heures EXACTEMENT identiques sont regroupées

-- Créer deux groupes avec 1 minute de différence
UPDATE reservation 
SET date_heure_arrive = '2026-03-15 12:00:00'
WHERE id = 7;

UPDATE reservation 
SET date_heure_arrive = '2026-03-15 12:01:00'
WHERE id = 8;

-- Ces deux réservations NE DOIVENT PAS être dans le même véhicule
-- malgré seulement 1 minute de différence

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
