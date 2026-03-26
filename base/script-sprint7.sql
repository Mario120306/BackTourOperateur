INSERT INTO type_carburant (reference, nom) VALUES
('DSL', 'Diesel'),
('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('Client1', 'Test', 'client1@test.com'),
  ('Client2', 'Test', 'client2@test.com'),
  ('Client3', 'Test', 'client3@test.com'),
  ('Client4', 'Test', 'client4@test.com'),
  ('Client5', 'Test', 'client5@test.com'),
  ('Client6', 'Test', 'client6@test.com');

INSERT INTO hotel (nom, adresse, ville) VALUES
  ('hotel1', 'Adresse hotel1', 'Ville1'),
  ('hotel2', 'Adresse hotel2', 'Ville2');

INSERT INTO aeroport (code, libelle) VALUES
('TNR', 'Aeroport International Ivato'); 

INSERT INTO parametre (code, valeur, description) VALUES
('VITESSE_MOYENNE_DEFAULT', '50', 'Vitesse moyenne par defaut en km/h'),
('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
('TEMPS_ARRET_MINUTES', '30', 'Temps darret a lhotel en minutes'),
('TEMPS_ATTENTE_MINUTES', '30', 'Temps dattente pour regrouper les reservations (en minutes)');


-- Les IDs suivants supposent des tables vides avant insertion :
-- type_carburant : 1 = DSL, 2 = ESS
-- aeroport      : 1 = TNR
-- hotel         : 1 = hotel1, 2 = hotel2
-- client        : 1..6 dans l'ordre ci-dessus

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('Marque1', 'vehicule1',  5, 'VEH1', 50, '09:00:00', 1), -- DSL
  ('Marque2', 'vehicule2',  5, 'VEH2', 50, '09:00:00', 2), -- ESS
  ('Marque3', 'vehicule3', 12, 'VEH3', 50, '00:00:00', 1), -- DSL, dispo dès minuit
  ('Marque4', 'vehicule4',  9, 'VEH4', 50, '09:00:00', 1), -- DSL
  ('Marque5', 'vehicule5', 12, 'VEH5', 50, '13:00:00', 2); -- ESS, dispo à 13h


INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 90.00),  -- aeroport(1) -> hotel1(1)
  (NULL, 1, 2, 35.00),  -- aeroport(1) -> hotel2(2)
  (1,    NULL, 2, 60.00); -- hotel1(1)  -> hotel2(2)

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  -- Client1, 7 passagers, 09:00, hotel1
  (1,  7, '2026-03-19 09:00:00', 1),

  -- Client2, 20 passagers, 08:00, hotel2
  (2, 20, '2026-03-19 08:00:00', 2),

  -- Client3, 3 passagers, 09:10, hotel1
  (3,  3, '2026-03-19 09:10:00', 1),

  -- Client4, 10 passagers, 09:15, hotel1
  (4, 10, '2026-03-19 09:15:00', 1),

  -- Client5, 5 passagers, 09:20, hotel1
  (5,  5, '2026-03-19 09:20:00', 1),

  -- Client6, 12 passagers, 13:00, hotel1
  (6, 12, '2026-03-19 13:00:00', 1);
