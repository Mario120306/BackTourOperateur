INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('Client1', 'Test', 'client1@test.com'),
  ('Client2', 'Test', 'client2@test.com'),
  ('Client3', 'Test', 'client3@test.com'),
  ('Client4', 'Test', 'client4@test.com');

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

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('Marque1', 'vehicule1', 10, 'VEH1', 50, '08:00:00', 1),
  ('Marque2', 'vehicule2',  8, 'VEH2', 50, '08:00:00', 1);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1,  2.00),
  (NULL, 1, 2, 10.00),
  (1,    NULL, 2,  1.00),
  (2,    NULL, 1,  1.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1, 10, '2026-04-01 09:00:00', 1),
  (2,  9, '2026-04-01 09:05:00', 2),
  (3,  6, '2026-04-01 09:10:00', 1),
  (4,  5, '2026-04-01 09:20:00', 2),
  (1,  6, '2026-04-01 09:45:00', 1),
  (2,  5, '2026-04-01 09:55:00', 2);
