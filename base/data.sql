-- Script d'insertion de données de test pour PostgreSQL

-- Insertion de clients
INSERT INTO client (nom, prenom, email) VALUES
('Dupont', 'Jean', 'jean.dupont@email.com'),
('Martin', 'Sophie', 'sophie.martin@email.com'),
('Bernard', 'Pierre', 'pierre.bernard@email.com'),
('Dubois', 'Marie', 'marie.dubois@email.com'),
('Leroy', 'Luc', 'luc.leroy@email.com'),
('Moreau', 'Claire', 'claire.moreau@email.com'),
('Simon', 'Thomas', 'thomas.simon@email.com'),
('Laurent', 'Emma', 'emma.laurent@email.com'),
('Lefebvre', 'Alexandre', 'alexandre.lefebvre@email.com'),
('Michel', 'Julie', 'julie.michel@email.com');

-- Insertion d'hôtels
INSERT INTO hotel (nom, adresse, ville, pays) VALUES
('Hotel Paradise', '15 Avenue des Palmiers', 'Nosy Be', 'Madagascar'),
('Grand Hotel Ivato', '23 Route de l''Aéroport', 'Antananarivo', 'Madagascar'),
('Hotel Colbert', '29 Rue Prince Ratsimamanga', 'Antananarivo', 'Madagascar'),
('Hotel Le Louvre', '8 Rue Marechal Joffre', 'Antsirabe', 'Madagascar'),
('Palissandre Hotel', '17 Rue Ratsitatane', 'Antananarivo', 'Madagascar'),
('Hotel Plage de Majunga', '5 Boulevard Maritime', 'Mahajanga', 'Madagascar'),
('Hotel Sunny Beach', '12 Rue de la Plage', 'Ifaty', 'Madagascar'),
('Hotel des Thermes', '34 Avenue de l''Indépendance', 'Antsirabe', 'Madagascar'),
('Hotel Le Royal', '45 Rue Rainibetsimisaraka', 'Antananarivo', 'Madagascar'),
('Hotel Vanille', '7 Boulevard de la Mer', 'Nosy Be', 'Madagascar');
