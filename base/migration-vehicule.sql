-- ============================================
-- SCRIPT DE MIGRATION - Ajout colonne id_vehicule
-- ============================================
-- Ce script ajoute la colonne id_vehicule à la table reservation
-- pour permettre l'enregistrement des assignations de simulation

-- Vérifier si la colonne existe déjà, sinon l'ajouter
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'reservation' AND column_name = 'id_vehicule'
    ) THEN
        ALTER TABLE reservation ADD COLUMN id_vehicule INT;
        ALTER TABLE reservation ADD CONSTRAINT fk_reservation_vehicule 
            FOREIGN KEY (id_vehicule) REFERENCES vehicule(id);
        RAISE NOTICE 'Colonne id_vehicule ajoutée avec succès';
    ELSE
        RAISE NOTICE 'La colonne id_vehicule existe déjà';
    END IF;
END $$;

-- Vérification
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'reservation' 
ORDER BY ordinal_position;
