INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('C1', 'Test', 'c1@test.com'),
  ('C2', 'Test', 'c2@test.com'),
  ('C3', 'Test', 'c3@test.com'),
  ('C4', 'Test', 'c4@test.com'),
  ('C5', 'Test', 'c5@test.com'),
  ('C6', 'Test', 'c6@test.com');

INSERT INTO hotel (nom, adresse, ville) VALUES
  ('HOTEL_NEAR_D', 'Adresse near', 'Ville'),
  ('HOTEL_FAR_D', 'Adresse far', 'Ville'),
  ('HOTEL_B', 'Adresse b', 'Ville');

INSERT INTO aeroport (code, libelle) VALUES
  ('TNR', 'Aeroport International Ivato');

INSERT INTO parametre (code, valeur, description) VALUES
  ('VITESSE_MOYENNE_DEFAULT', '60', 'Vitesse moyenne par defaut en km/h'),
  ('CARBURANT_PRIORITAIRE', 'DSL', 'Type carburant prioritaire'),
  ('TEMPS_ARRET_MINUTES', '30', 'Temps arret hotel en minutes'),
  ('TEMPS_ATTENTE_MINUTES', '30', 'Temps attente regroupement en minutes');

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('M', 'veh_D_1', 6, 'VEH_D_1', 60, '08:00:00', 1),
  ('M', 'veh_D_2', 6, 'VEH_D_2', 60, '08:00:00', 1),
  ('M', 'veh_D_3', 6, 'VEH_D_3', 60, '08:00:00', 1),
  ('M', 'veh_B_1', 10, 'VEH_B_1', 60, '12:00:00', 1);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 30.00),
  (NULL, 1, 2, 300.00),
  (NULL, 1, 3, 30.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1,  6, '2026-04-02 08:00:00', 1),
  (2,  6, '2026-04-02 08:00:00', 1),
  (3,  6, '2026-04-02 08:00:00', 1),
  (4,  5, '2026-04-02 08:10:00', 2),
  (5,  5, '2026-04-02 08:15:00', 2),
  (6,  5, '2026-04-02 08:20:00', 2),
  (1, 10, '2026-04-02 12:00:00', 3),
  (2,  5, '2026-04-02 12:10:00', 3),
  (3,  1, '2026-04-02 15:00:00', 3);
