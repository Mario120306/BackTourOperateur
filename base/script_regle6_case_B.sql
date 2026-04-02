INSERT INTO type_carburant (reference, nom) VALUES
  ('DSL', 'Diesel'),
  ('ESS', 'Essence');

INSERT INTO client (nom, prenom, email) VALUES
  ('B1', 'Test', 'b1@test.com'),
  ('B2', 'Test', 'b2@test.com');

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
  ('M', 'veh_B_1', 10, 'VEH_B_1', 50, '12:00:00', 1);

INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
  (NULL, 1, 1, 2.00);

INSERT INTO reservation (id_client, nombre_passage, date_heure_arrive, id_hotel)
VALUES
  (1, 10, '2026-04-02 13:00:00', 1),
  (2,  5, '2026-04-02 13:10:00', 1),
  (1,  1, '2026-04-02 15:00:00', 1);
