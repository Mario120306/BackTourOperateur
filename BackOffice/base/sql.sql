

CREATE DATABASE tour_operateur;
\c tour_operateur;
CREATE TABLE reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_client INT NOT NULL,
    nombre_passage INT NOT NULL,
    date_heure_arrive DATETIME NOT NULL,
    date_reservation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_hotel INT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES client(id),
    FOREIGN KEY (id_hotel) REFERENCES hotel(id)
);
CREATE TABLE client (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    prenom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);
CREATE TABLE hotel (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    adresse VARCHAR(255) NOT NULL,
    ville VARCHAR(255) NOT NULL,
    pays VARCHAR(255) NOT NULL
);
