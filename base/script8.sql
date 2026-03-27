-- Script de test pour la regle 6 (trajet intermediaire entre deux groupes)
-- Hypothese : utiliser apres clear-data.sql pour partir sur une base vide.

-- Types de carburant
INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

-- Clients
INSERT INTO client (nom, prenom, email) VALUES
  ('Client1', 'Test', 'client1@test.com'),
  ('Client2', 'Test', 'client2@test.com'),
  ('Client3', 'Test', 'client3@test.com'),
  ('Client4', 'Test', 'client4@test.com');

-- Hotels
INSERT INTO hotel (nom, adresse, ville) VALUES
  ('hotel1', 'Adresse hotel1', 'Ville1'),
  ('hotel2', 'Adresse hotel2', 'Ville2');

-- Aeroport
INSERT INTO aeroport (code, libelle) VALUES
  ('TNR', 'Aeroport International Ivato');

-- Parametres (on garde les memes codes que les autres scripts)
INSERT INTO parametre (code, valeur, description) VALUES
  ('VITESSE_MOYENNE_DEFAULT', '50', 'Vitesse moyenne par defaut en km/h'),
  ('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
  ('TEMPS_ARRET_MINUTES', '30', 'Temps darret a lhotel en minutes'),
  ('TEMPS_ATTENTE_MINUTES', '30', 'Temps dattente pour regrouper les reservations (en minutes)');

-- Hypotheses d'IDs apres clear-data.sql :
-- type_carburant : 1 = DSL, 2 = ESS
-- aeroport      : 1 = TNR
-- hotel         : 1 = hotel1, 2 = hotel2
-- client        : 1..4

-- Vehicules
-- V1 : gros vehicule qui fait un premier trajet avec un peu de marge
-- V2 : vehicule qui ne sert qu'au deuxieme groupe
INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('Marque1', 'vehicule1', 12, 'VEH1', 50, '09:00:00', 1), -- DSL
  ('Marque2', 'vehicule2',  9, 'VEH2', 50, '11:00:00', 1); -- DSL, dispo plus tard

-- Distances de base (traject aller + inter-hotels relativement courts
-- pour que le premier trajet revienne AVANT 10h10 et AVANT 11h30)
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 10.00),  -- aeroport -> hotel1
  (NULL, 1, 2, 10.00),  -- aeroport -> hotel2
  (1,    NULL, 2, 10.00); -- hotel1 -> hotel2

-- Scenario de reservations (meme jour) pour tester la regle 6
-- Groupe 1 : autour de 09h00 (avec TEMPS_ATTENTE_MINUTES = 30)
--   - Client1 : 10 pers a 09:00 hotel1
--   - Client2 : 8 pers a 09:10 hotel2
-- On force ainsi un report (8 pers) apres remplissage du premier trajet.
-- Groupe 2 "officiel" : reservation plus tardive vers 11:30.

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  -- Groupe 1 : depart ~09h30
  (1, 10, '2026-04-01 09:00:00', 1),
  (2,  8, '2026-04-01 09:10:00', 2),

  -- Groupe 2 : arrivee plus tardive (prochain groupe)
  (3,  6, '2026-04-01 11:30:00', 1),

  -- Reservation qui peut arriver pendant le trajet intermediaire
  -- (pour voir si elle sera prise ou reportee selon l'heure de depart)
  (4,  4, '2026-04-01 10:10:00', 1);

-- Interpretation attendue (donnees indicatives) :
-- 1) Groupe 1 (~09:00-09:30)
--    - VEH1 prend 10 pers (Client1) + 2 pers de Client2 => 12/12, trajet complet.
--    - Il reste 6 pers de Client2 en "reservationsReportees" pour le groupe suivant.
-- 2) Avec les distances reduites (10 km a 50 km/h ~ 12 min),
--    VEH1 revient largement avant 10h10 et 11h30.
-- 3) Entre la fin du trajet de VEH1 et le debut du prochain groupe (11h30),
--    la regle 6 s'applique : on cree un trajet intermediaire pour VEH1 avec
--    les reservations reportees (6 pers de Client2).
-- 4) Le groupe officiel suivant (~11h30-12:00) utilisera ensuite les
--    reservations restantes (Client3, Client4 selon l'heure de retour
--    du trajet intermediaire) avec les vehicules disponibles.
