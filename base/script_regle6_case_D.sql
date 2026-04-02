INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('D1', 'Test', 'd1@test.com'),
  ('D2', 'Test', 'd2@test.com'),
  ('D3', 'Test', 'd3@test.com'),
  ('D4', 'Test', 'd4@test.com'),
  ('D5', 'Test', 'd5@test.com'),
  ('D6', 'Test', 'd6@test.com');

INSERT INTO hotel (nom, adresse, ville) VALUES
  ('hotel1', 'Adresse hotel1', 'Ville1');

INSERT INTO aeroport (code, libelle) VALUES
  ('TNR', 'Aeroport International Ivato');

INSERT INTO parametre (code, valeur, description) VALUES
  ('VITESSE_MOYENNE_DEFAULT', '50', 'Vitesse moyenne par defaut en km/h'),
  ('CARBURANT_PRIORITAIRE', 'DSL', 'Type de carburant prioritaire (Diesel)'),
  ('TEMPS_ARRET_MINUTES', '30', 'Temps arret hotel en minutes'),
  ('TEMPS_ATTENTE_MINUTES', '30', 'Temps attente regroupement en minutes');

INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, heure_disponibilite, type_carburant_id)
VALUES
  ('M', 'veh_D_1', 6, 'VEH_D_1', 50, '08:00:00', 1),
  ('M', 'veh_D_2', 6, 'VEH_D_2', 50, '08:00:00', 1),
  ('M', 'veh_D_3', 6, 'VEH_D_3', 50, '08:00:00', 1);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 2.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1, 6, '2026-04-02 09:00:00', 1),
  (2, 6, '2026-04-02 09:01:00', 1),
  (3, 6, '2026-04-02 09:02:00', 1),
  (4, 5, '2026-04-02 09:03:00', 1),
  (5, 4, '2026-04-02 09:04:00', 1),
  (6, 1, '2026-04-02 10:30:00', 1);
