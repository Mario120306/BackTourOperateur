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

-- Table pour la gestion des tokens JWT
CREATE TABLE token (
    id SERIAL PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    date_expiration TIMESTAMP NOT NULL,
    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour optimiser la recherche de token
CREATE INDEX idx_token_value ON token(token);
CREATE INDEX idx_token_expiration ON token(date_expiration);