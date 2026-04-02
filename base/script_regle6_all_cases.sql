INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('C1', 'Test', 'c1@test.com'),
  ('C2', 'Test', 'c2@test.com'),
  ('C3', 'Test', 'c3@test.com'),
  ('C4', 'Test', 'c4@test.com'),
  ('C5', 'Test', 'c5@test.com'),
  ('C6', 'Test', 'c6@test.com'),
  ('C7', 'Test', 'c7@test.com'),
  ('C8', 'Test', 'c8@test.com'),
  ('C9', 'Test', 'c9@test.com'),
  ('C10', 'Test', 'c10@test.com'),
  ('C11', 'Test', 'c11@test.com'),
  ('C12', 'Test', 'c12@test.com'),
  ('C13', 'Test', 'c13@test.com'),
  ('C14', 'Test', 'c14@test.com');

INSERT INTO hotel (nom, adresse, ville) VALUES
  ('hotel1', 'Adresse hotel1', 'Ville1'),
  ('hotel2', 'Adresse hotel2', 'Ville2');

INSERT INTO aeroport (code, libelle) VALUES
  ('TNR', 'Aeroport International Ivato');

INSERT INTO parametre (code, valeur, description) VALUES
  ('VITESSE_MOYENNE_DEFAULT', '50', 'Vitesse moyenne par défaut en km/h'),
  ('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
  ('TEMPS_ARRET_MINUTES', '30', 'Temps darret a lhotel en minutes'),
  ('TEMPS_ATTENTE_MINUTES', '30', 'Temps dattente pour regrouper les reservations (en minutes)');

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('M', 'veh_AC_1', 10, 'VEH_AC_1', 50, '08:00:00', 1),
  ('M', 'veh_AC_2',  8, 'VEH_AC_2', 50, '08:00:00', 1),
  ('M', 'veh_D_1',   6, 'VEH_D_1',  50, '10:00:00', 1),
  ('M', 'veh_D_2',   6, 'VEH_D_2',  50, '10:00:00', 1),
  ('M', 'veh_D_3',   6, 'VEH_D_3',  50, '10:00:00', 1),
  ('M', 'veh_B_1',  10, 'VEH_B_1',  50, '12:00:00', 1);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1,  2.00),
  (NULL, 1, 2, 10.00),
  (1,    NULL, 2,  1.00),
  (2,    NULL, 1,  1.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1, 10, '2026-04-02 09:00:00', 1),
  (2,  9, '2026-04-02 09:05:00', 2),
  (3,  6, '2026-04-02 09:10:00', 1),
  (4,  5, '2026-04-02 09:20:00', 2),
  (5,  6, '2026-04-02 09:45:00', 1),
  (6,  5, '2026-04-02 09:55:00', 2);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (7,  6, '2026-04-02 10:30:00', 1),
  (8,  6, '2026-04-02 10:31:00', 1),
  (9,  6, '2026-04-02 10:32:00', 1),
  (10, 5, '2026-04-02 10:33:00', 1),
  (11, 4, '2026-04-02 10:34:00', 1);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (12, 10, '2026-04-02 13:00:00', 1),
  (13,  5, '2026-04-02 13:10:00', 1),
  (14,  1, '2026-04-02 15:00:00', 1);
