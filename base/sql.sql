-- Active: 1743756076635@@127.0.0.1@5432@tour_operateur

CREATE DATABASE tour_operateur;
\c tour_operateur;

CREATE TABLE client (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    ville VARCHAR(255) NOT NULL,
    pays VARCHAR(255) NOT NULL
);
CREATE TABLE type_carburant (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL UNIQUE,
    nom VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    marque VARCHAR(255) NOT NULL,
    modele VARCHAR(255) NOT NULL,
    nombre_places INT NOT NULL,
    reference VARCHAR(255) NOT NULL UNIQUE,
    vitesse_moyenne INT NOT NULL,
    type_carburant_id INT NOT NULL,
    FOREIGN KEY (type_carburant_id) REFERENCES type_carburant(id)

);  
CREATE TABLE reservation (
    id SERIAL PRIMARY KEY,
    id_client INT NOT NULL,
    nombre_passage INT NOT NULL,
    date_heure_arrive TIMESTAMP NOT NULL,
    date_reservation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_hotel INT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id),
    FOREIGN KEY (id_hotel) REFERENCES hotel(id)
);