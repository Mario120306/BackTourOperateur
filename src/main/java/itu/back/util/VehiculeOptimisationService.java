package itu.back.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import itu.back.model.Aeroport;
import itu.back.model.Distance;
import itu.back.model.Parametre;
import itu.back.model.TypeCarburant;
import itu.back.model.Vehicule;

/**
 * Service pour l'optimisation des véhicules lors des réservations.
 * 
 * Règles d'optimisation:
 * 1. Trouver le véhicule avec le nombre de places >= nombre de passagers
 * 2. Prendre le véhicule le plus optimal (nombre de places le plus proche)
 * 3. En cas d'égalité de places, priorité au Diesel
 * 4. Vérifier la disponibilité du véhicule (pas de chevauchement avec d'autres réservations)
 */
public class VehiculeOptimisationService {

    /**
     * Trouve le véhicule optimal pour un nombre de passagers donné (sans vérification de disponibilité)
     * 
     * @param nombrePassagers le nombre de passagers à transporter
     * @return le véhicule optimal ou null si aucun véhicule disponible
     * @throws SQLException en cas d'erreur de base de données
     */
    public static Vehicule trouverVehiculeOptimal(int nombrePassagers) throws SQLException {
        return trouverVehiculeOptimalDisponible(nombrePassagers, null, null);
    }

    /**
     * Trouve le véhicule optimal pour un nombre de passagers donné avec vérification de disponibilité
     * Un véhicule peut faire plusieurs voyages par jour tant que les créneaux ne se chevauchent pas.
     * Créneau occupé = heure_depart à (heure_arrivee + temps_arret)
     * 
     * @param nombrePassagers le nombre de passagers à transporter
     * @param heureDepart l'heure de départ prévue (peut être null pour ignorer la disponibilité)
     * @param heureArrivee l'heure d'arrivée prévue (peut être null pour ignorer la disponibilité)
     * @return le véhicule optimal disponible ou null si aucun véhicule disponible
     * @throws SQLException en cas d'erreur de base de données
     */
    public static Vehicule trouverVehiculeOptimalDisponible(int nombrePassagers, 
            Timestamp heureDepart, Timestamp heureArrivee) throws SQLException {
        List<Vehicule> vehiculesDisponibles = getVehiculesCapaciteSuffisante(nombrePassagers);
        
        if (vehiculesDisponibles.isEmpty()) {
            return null;
        }
        
        // Trouver le véhicule optimal
        Vehicule vehiculeOptimal = null;
        String carburantPrioritaire = getParametreValeur("CARBURANT_PRIORITAIRE", "DSL");
        
        for (Vehicule v : vehiculesDisponibles) {
            // Vérifier la disponibilité si les heures sont fournies
            if (heureDepart != null && heureArrivee != null) {
                if (!isVehiculeDisponible(v.getId(), heureDepart, heureArrivee)) {
                    continue; // Véhicule occupé, passer au suivant
                }
            }
            
            if (vehiculeOptimal == null) {
                vehiculeOptimal = v;
            } else {
                // Comparer par nombre de places (le plus petit qui suffit)
                if (v.getNombrePlaces() < vehiculeOptimal.getNombrePlaces()) {
                    vehiculeOptimal = v;
                } else if (v.getNombrePlaces() == vehiculeOptimal.getNombrePlaces()) {
                    // En cas d'égalité, priorité au carburant prioritaire (Diesel par défaut)
                    if (v.getTypeCarburant() != null && 
                        carburantPrioritaire.equals(v.getTypeCarburant().getReference()) &&
                        (vehiculeOptimal.getTypeCarburant() == null || 
                         !carburantPrioritaire.equals(vehiculeOptimal.getTypeCarburant().getReference()))) {
                        vehiculeOptimal = v;
                    }
                }
            }
        }
        
        return vehiculeOptimal;
    }

    /**
     * Vérifie si un véhicule est disponible pour un créneau donné
     * Un véhicule est occupé de heure_depart à (heure_arrivee + temps_arret)
     * 
     * @param idVehicule l'ID du véhicule à vérifier
     * @param heureDepart l'heure de départ souhaitée
     * @param heureArrivee l'heure d'arrivée souhaitée
     * @return true si le véhicule est disponible, false sinon
     */
    public static boolean isVehiculeDisponible(int idVehicule, Timestamp heureDepart, 
            Timestamp heureArrivee) throws SQLException {
        Connection conn = null;
        boolean disponible = true;
        
        try {
            conn = DatabaseConnection.getConnection();
            
            // Récupérer le temps d'arrêt en minutes
            int tempsArretMinutes = Integer.parseInt(getParametreValeur("TEMPS_ARRET_MINUTES", "30"));
            long tempsArretMillis = tempsArretMinutes * 60 * 1000L;
            
            // Calculer la fin du nouveau créneau (arrivée + temps d'arrêt)
            Timestamp finNouveauCreneau = new Timestamp(heureArrivee.getTime() + tempsArretMillis);
            
            // Vérifier les chevauchements avec les réservations existantes
            // Chevauchement si: nouveau_depart < existant_fin ET existant_depart < nouveau_fin
            String sql = "SELECT COUNT(*) FROM reservation " +
                    "WHERE id_vehicule = ? " +
                    "AND heure_depart IS NOT NULL " +
                    "AND date_heure_arrive IS NOT NULL " +
                    "AND ? < (date_heure_arrive + (? || ' minutes')::interval) " +
                    "AND heure_depart < ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idVehicule);
            stmt.setTimestamp(2, heureDepart);
            stmt.setInt(3, tempsArretMinutes);
            stmt.setTimestamp(4, finNouveauCreneau);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                disponible = false;
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return disponible;
    }

    /**
     * Récupère tous les véhicules ayant une capacité suffisante pour le nombre de passagers
     * triés par nombre de places croissant puis par type de carburant (Diesel en premier)
     */
    private static List<Vehicule> getVehiculesCapaciteSuffisante(int nombrePassagers) throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            // Récupérer le carburant prioritaire
            String carburantPrioritaire = getParametreValeur("CARBURANT_PRIORITAIRE", "DSL");
            
            // Requête triée par nombre de places croissant, puis Diesel en priorité
            String sql = "SELECT v.*, tc.reference AS tc_reference, tc.nom AS tc_nom " +
                    "FROM vehicule v " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "WHERE v.nombre_places >= ? " +
                    "ORDER BY v.nombre_places ASC, " +
                    "CASE WHEN tc.reference = ? THEN 0 ELSE 1 END ASC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, nombrePassagers);
            stmt.setString(2, carburantPrioritaire);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setId(rs.getInt("id"));
                v.setMarque(rs.getString("marque"));
                v.setModele(rs.getString("modele"));
                v.setNombrePlaces(rs.getInt("nombre_places"));
                v.setReference(rs.getString("reference"));
                v.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));
                v.setTypeCarburantId(rs.getInt("type_carburant_id"));
                
                TypeCarburant tc = new TypeCarburant();
                tc.setId(rs.getInt("type_carburant_id"));
                tc.setReference(rs.getString("tc_reference"));
                tc.setNom(rs.getString("tc_nom"));
                v.setTypeCarburant(tc);
                
                vehicules.add(v);
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return vehicules;
    }

    /**
     * Calcule le temps de trajet en minutes basé sur la distance et la vitesse moyenne du véhicule
     * 
     * @param distanceKm la distance en kilomètres
     * @param vehicule le véhicule (pour sa vitesse moyenne)
     * @return le temps estimé en minutes
     */
    public static int calculerTempsTrajetMinutes(BigDecimal distanceKm, Vehicule vehicule) throws SQLException {
        int vitesseMoyenne;
        
        if (vehicule != null && vehicule.getVitesseMoyenne() > 0) {
            vitesseMoyenne = vehicule.getVitesseMoyenne();
        } else {
            // Utiliser la vitesse par défaut du paramètre
            String vitesseDefault = getParametreValeur("VITESSE_MOYENNE_DEFAULT", "60");
            vitesseMoyenne = Integer.parseInt(vitesseDefault);
        }
        
        // Temps en heures = distance / vitesse, converti en minutes
        double tempsHeures = distanceKm.doubleValue() / vitesseMoyenne;
        return (int) Math.ceil(tempsHeures * 60);
    }

    /**
     * Récupère la distance entre deux aéroports
     */
    public static Distance getDistance(int idFrom, int idTo) throws SQLException {
        Distance distance = null;
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT d.*, " +
                    "af.code AS from_code, af.libelle AS from_libelle, " +
                    "at.code AS to_code, at.libelle AS to_libelle " +
                    "FROM distance d " +
                    "LEFT JOIN aeroport af ON d.id_from = af.id " +
                    "LEFT JOIN aeroport at ON d.id_to = at.id " +
                    "WHERE d.id_from = ? AND d.id_to = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idFrom);
            stmt.setInt(2, idTo);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                distance = new Distance();
                distance.setId(rs.getInt("id"));
                distance.setIdFrom(rs.getInt("id_from"));
                distance.setIdTo(rs.getInt("id_to"));
                distance.setValeur(rs.getBigDecimal("valeur"));
                
                Aeroport from = new Aeroport();
                from.setId(rs.getInt("id_from"));
                from.setCode(rs.getString("from_code"));
                from.setLibelle(rs.getString("from_libelle"));
                distance.setAeroportFrom(from);
                
                Aeroport to = new Aeroport();
                to.setId(rs.getInt("id_to"));
                to.setCode(rs.getString("to_code"));
                to.setLibelle(rs.getString("to_libelle"));
                distance.setAeroportTo(to);
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return distance;
    }

    /**
     * Récupère la distance entre un aéroport et un hôtel
     * Utilise la table distance_hotel si disponible, sinon retourne une distance par défaut
     * 
     * @param idAeroport l'id de l'aéroport de départ
     * @param idHotel l'id de l'hôtel de destination
     * @return la distance ou null si non trouvée
     */
    public static Distance getDistanceAeroportHotel(int idAeroport, int idHotel) throws SQLException {
        Distance distance = null;
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            // Chercher dans la table distance_hotel si elle existe
            String sql = "SELECT * FROM distance_hotel WHERE id_aeroport = ? AND id_hotel = ?";
            
            try {
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, idAeroport);
                stmt.setInt(2, idHotel);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    distance = new Distance();
                    distance.setId(rs.getInt("id"));
                    distance.setIdFrom(idAeroport);
                    distance.setIdTo(idHotel);
                    distance.setValeur(rs.getBigDecimal("valeur"));
                }
                
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                // Table n'existe pas, on retourne null et le controller utilisera une valeur par défaut
                distance = null;
            }
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return distance;
    }

    /**
     * Récupère tous les aéroports
     */
    public static List<Aeroport> getAllAeroports() throws SQLException {
        List<Aeroport> aeroports = new ArrayList<>();
        Connection conn = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM aeroport ORDER BY libelle";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Aeroport a = new Aeroport();
                a.setId(rs.getInt("id"));
                a.setCode(rs.getString("code"));
                a.setLibelle(rs.getString("libelle"));
                aeroports.add(a);
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return aeroports;
    }

    /**
     * Récupère la valeur d'un paramètre par son code
     */
    public static String getParametreValeur(String code, String defaultValue) throws SQLException {
        Connection conn = null;
        String valeur = defaultValue;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT valeur FROM parametre WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                valeur = rs.getString("valeur");
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return valeur;
    }

    /**
     * Récupère un paramètre complet par son code
     */
    public static Parametre getParametreByCode(String code) throws SQLException {
        Connection conn = null;
        Parametre parametre = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM parametre WHERE code = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                parametre = new Parametre();
                parametre.setId(rs.getInt("id"));
                parametre.setCode(rs.getString("code"));
                parametre.setValeur(rs.getString("valeur"));
                parametre.setDescription(rs.getString("description"));
            }
            
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return parametre;
    }
}
