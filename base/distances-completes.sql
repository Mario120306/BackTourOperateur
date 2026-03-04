-- ============================================
-- DISTANCES COMPLÈTES ENTRE HÔTELS
-- ============================================
-- Ajout des distances manquantes entre les hôtels d'Antananarivo

-- Distances depuis Hotel Colbert (id=3)
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(3, NULL, 2, 3.5)  -- Hotel Colbert vers Grand Hotel Ivato (inverse de 2→3)
ON CONFLICT DO NOTHING;

-- Distances depuis Palissandre Hotel (id=5)
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(5, NULL, 2, 2.0),  -- Palissandre Hotel vers Grand Hotel Ivato (inverse de 2→5)
(5, NULL, 3, 1.8)   -- Palissandre Hotel vers Hotel Colbert (inverse de 3→5)
ON CONFLICT DO NOTHING;

-- Distances depuis Hotel Le Royal (id=9)
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(9, NULL, 2, 1.5),  -- Hotel Le Royal vers Grand Hotel Ivato (inverse de 2→9)
(9, NULL, 3, 2.0),  -- Hotel Le Royal vers Hotel Colbert (inverse de 3→9)
(9, NULL, 5, 1.2)   -- Hotel Le Royal vers Palissandre Hotel (inverse de 5→9)
ON CONFLICT DO NOTHING;

-- Distances de retour vers l'aéroport depuis chaque hôtel d'Antananarivo
INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES
(2, NULL, 1, 5.0),   -- Grand Hotel Ivato vers Aéroport Ivato
(3, NULL, 1, 8.5),   -- Hotel Colbert vers Aéroport Ivato
(5, NULL, 1, 7.0),   -- Palissandre Hotel vers Aéroport Ivato
(9, NULL, 1, 6.0)    -- Hotel Le Royal vers Aéroport Ivato
ON CONFLICT DO NOTHING;

-- Pour PostgreSQL, il faut un identifiant unique, donc on doit utiliser une contrainte unique
-- Si la table distance n'a pas de contrainte unique, on doit d'abord supprimer les doublons potentiels

-- Version sécurisée pour PostgreSQL (sans ON CONFLICT)
-- Vérifier d'abord si les distances existent

-- Insertion sécurisée des distances manquantes
DO $$
BEGIN
    -- Hotel Colbert vers Grand Hotel Ivato
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 3 AND id_from_aeroport IS NULL AND id_to = 2) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (3, NULL, 2, 3.5);
    END IF;
    
    -- Palissandre Hotel vers Grand Hotel Ivato
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 5 AND id_from_aeroport IS NULL AND id_to = 2) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (5, NULL, 2, 2.0);
    END IF;
    
    -- Palissandre Hotel vers Hotel Colbert
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 5 AND id_from_aeroport IS NULL AND id_to = 3) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (5, NULL, 3, 1.8);
    END IF;
    
    -- Hotel Le Royal vers Grand Hotel Ivato
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 9 AND id_from_aeroport IS NULL AND id_to = 2) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (9, NULL, 2, 1.5);
    END IF;
    
    -- Hotel Le Royal vers Hotel Colbert
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 9 AND id_from_aeroport IS NULL AND id_to = 3) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (9, NULL, 3, 2.0);
    END IF;
    
    -- Hotel Le Royal vers Palissandre Hotel
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 9 AND id_from_aeroport IS NULL AND id_to = 5) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (9, NULL, 5, 1.2);
    END IF;
    
    -- Retours vers l'aéroport
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 2 AND id_from_aeroport IS NULL AND id_to = 1) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (2, NULL, 1, 5.0);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 3 AND id_from_aeroport IS NULL AND id_to = 1) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (3, NULL, 1, 8.5);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 5 AND id_from_aeroport IS NULL AND id_to = 1) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (5, NULL, 1, 7.0);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM distance WHERE id_from_hotel = 9 AND id_from_aeroport IS NULL AND id_to = 1) THEN
        INSERT INTO distance (id_from_hotel, id_from_aeroport, id_to, valeur) VALUES (9, NULL, 1, 6.0);
    END IF;
END $$;

-- Vérification
SELECT 
    h1.nom as origine,
    h2.nom as destination,
    d.valeur as distance_km
FROM distance d
LEFT JOIN hotel h1 ON d.id_from_hotel = h1.id
LEFT JOIN hotel h2 ON d.id_to = h2.id
WHERE d.id_from_aeroport IS NULL
ORDER BY h1.nom, h2.nom;
