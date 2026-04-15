INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('C1', 'Test', 'c1@test.com'),
  ('C2', 'Test', 'c2@test.com'),
  ('C3', 'Test', 'c3@test.com'),
  ('C4', 'Test', 'c4@test.com');

INSERT INTO hotel (nom, adresse, ville) VALUES
  ('HOTEL1', 'Adresse near', 'Ville'),
  ('HOTEL2', 'Adresse far', 'Ville');

INSERT INTO aeroport (code, libelle) VALUES
  ('TNR', 'Aeroport International Ivato');

INSERT INTO parametre (code, valeur, description) VALUES
  ('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par defaut en km/h'),
  ('CARBURANT_PRIORITAIRE', 'DSL', 'Type carburant prioritaire'),
  ('TEMPS_ARRET_MINUTES', '30', 'Temps arret hotel en minutes'),
  ('TEMPS_ATTENTE_MINUTES', '30', 'Temps attente regroupement en minutes');

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('M', 'Vehicule1', 10, 'Vehicule1', 60, '00:00:00', 1),
  ('M', 'Vehicule2', 8, 'Vehicule2', 60, '08:00:00', 1),
  ('M', 'Vehicule3', 8, 'Vehicule3', 60, '08:00:00', 2),
  ('M', 'Vehicule4', 12, 'Vehicule4', 60, '09:00:00', 2);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 90.00),
  (NULL, 1, 2, 65.00),
  (1, NULL, 2, 10.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1,  20, '2026-04-02 06:00:00', 1),
  (2,  6, '2026-04-02 08:15:00', 1),
  (3, 10, '2026-04-02 09:00:00', 1),
  (4, 6, '2026-04-02 09:10:00', 2);
