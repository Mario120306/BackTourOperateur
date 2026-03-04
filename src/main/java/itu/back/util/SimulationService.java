package itu.back.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import itu.back.model.Reservation;
import itu.back.model.Vehicule;

/**
 * Service de simulation pour l'assignation optimale des véhicules aux
 * réservations.
 * 
 * Algorithme :
 * 1. Trier les réservations par nombre de passagers (décroissant)
 * 2. Pour chaque réservation, assigner à un véhicule disponible
 * 3. Optimiser en ajoutant d'autres réservations si le véhicule n'est pas plein
 * 4. Calculer les horaires de départ et retour selon les distances et vitesse
 * moyenne
 */
public class SimulationService {

    // Carburant prioritaire (Diesel)
    private static final String CARBURANT_PRIORITAIRE = "DSL";

    /**
     * Classe pour encapsuler les résultats de la simulation
     */
    public static class ResultatSimulation {
        private Map<Vehicule, List<Reservation>> vehiculesAvecReservations;
        private List<Reservation> reservationsNonAssignees;
        private Map<Vehicule, InfosTrajet> infosTrajetParVehicule;

        public ResultatSimulation() {
            this.vehiculesAvecReservations = new HashMap<>();
            this.reservationsNonAssignees = new ArrayList<>();
            this.infosTrajetParVehicule = new HashMap<>();
        }

        public Map<Vehicule, List<Reservation>> getVehiculesAvecReservations() {
            return vehiculesAvecReservations;
        }

        public void setVehiculesAvecReservations(Map<Vehicule, List<Reservation>> vehiculesAvecReservations) {
            this.vehiculesAvecReservations = vehiculesAvecReservations;
        }

        public List<Reservation> getReservationsNonAssignees() {
            return reservationsNonAssignees;
        }

        public void setReservationsNonAssignees(List<Reservation> reservationsNonAssignees) {
            this.reservationsNonAssignees = reservationsNonAssignees;
        }
        
        public Map<Vehicule, InfosTrajet> getInfosTrajetParVehicule() {
            return infosTrajetParVehicule;
        }
        
        public void setInfosTrajetParVehicule(Map<Vehicule, InfosTrajet> infosTrajetParVehicule) {
            this.infosTrajetParVehicule = infosTrajetParVehicule;
        }
    }
    
    /**
     * Classe pour stocker les informations de trajet d'un véhicule
     */
    public static class InfosTrajet {
        private Timestamp heureDepart;
        private Timestamp heureRetour;
        private int dureeTrajetMinutes;
        
        public InfosTrajet(Timestamp heureDepart, Timestamp heureRetour, int dureeTrajetMinutes) {
            this.heureDepart = heureDepart;
            this.heureRetour = heureRetour;
            this.dureeTrajetMinutes = dureeTrajetMinutes;
        }
        
        public Timestamp getHeureDepart() {
            return heureDepart;
        }
        
        public Timestamp getHeureRetour() {
            return heureRetour;
        }
        
        public int getDureeTrajetMinutes() {
            return dureeTrajetMinutes;
        }
    }

    /**
     * Classe interne pour gérer l'état d'un véhicule pendant la simulation
     */
    private static class VehiculeAvecCapacite {
        Vehicule vehicule;
        int placesRestantes;
        List<Reservation> reservations;
        Timestamp heureDepart;
        Timestamp heureRetour;
        int dureeTrajetMinutes;

        public VehiculeAvecCapacite(Vehicule v) {
            this.vehicule = v;
            this.placesRestantes = v.getNombrePlaces();
            this.reservations = new ArrayList<>();
        }

        public boolean peutAccueillir(int nombrePassagers) {
            return placesRestantes >= nombrePassagers;
        }

        public void ajouterReservation(Reservation r) {
            reservations.add(r);
            placesRestantes -= r.getNombrePassage();
        }
        
        public Timestamp getHeureArriveePremiere() {
            if (reservations.isEmpty()) return null;
            return reservations.get(0).getDateHeureArrive();
        }
    }

    /**
     * Simule l'assignation des véhicules aux réservations pour une date donnée
     * 
     * @param reservations Liste des réservations pour la date
     * @param vehicules    Liste de tous les véhicules disponibles
     * @param conn         Connexion à la base de données pour calculer les
     *                     distances
     * @return ResultatSimulation contenant les véhicules assignés et les
     *         réservations non assignées
     * @throws SQLException
     */
    public static ResultatSimulation simulerAssignation(
            List<Reservation> reservations,
            List<Vehicule> vehicules,
            Connection conn) throws SQLException {

        // Résultat final
        ResultatSimulation resultat = new ResultatSimulation();
        Map<Vehicule, List<Reservation>> vehiculesAvecReservations = new HashMap<>();

        // État des véhicules pendant la simulation
        List<VehiculeAvecCapacite> vehiculesDisponibles = new ArrayList<>();
        for (Vehicule v : vehicules) {
            vehiculesDisponibles.add(new VehiculeAvecCapacite(v));
        }

        // Trier les véhicules : prioriser le diesel, puis par nombre de places
        // décroissant
        vehiculesDisponibles.sort((v1, v2) -> {
            // D'abord par type de carburant (diesel prioritaire)
            boolean v1Diesel = v1.vehicule.getTypeCarburant() != null &&
                    CARBURANT_PRIORITAIRE.equals(v1.vehicule.getTypeCarburant().getReference());
            boolean v2Diesel = v2.vehicule.getTypeCarburant() != null &&
                    CARBURANT_PRIORITAIRE.equals(v2.vehicule.getTypeCarburant().getReference());

            if (v1Diesel && !v2Diesel)
                return -1;
            if (!v1Diesel && v2Diesel)
                return 1;

            // Ensuite par nombre de places (décroissant)
            return Integer.compare(v2.vehicule.getNombrePlaces(), v1.vehicule.getNombrePlaces());
        });

        // Copie des réservations pour pouvoir les modifier
        List<Reservation> reservationsNonAssignees = new ArrayList<>(reservations);

        // Liste pour garder trace des réservations qui n'ont pas pu être assignées
        List<Reservation> reservationsImpossiblesAAssigner = new ArrayList<>();

        // ETAPE 1 : Trier par heure d'arrivée puis par nombre de passagers (décroissant)
        reservationsNonAssignees.sort((r1, r2) -> {
            // D'abord par heure d'arrivée
            int compareHeure = r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive());
            if (compareHeure != 0) return compareHeure;
            // Ensuite par nombre de passagers (décroissant)
            return Integer.compare(r2.getNombrePassage(), r1.getNombrePassage());
        });

        // ETAPE 2 & 3 : Assigner les réservations (uniquement celles avec la même heure)
        while (!reservationsNonAssignees.isEmpty()) {
            Reservation reservation = reservationsNonAssignees.get(0);
            boolean assignee = false;

            // Chercher un véhicule disponible avec assez de places
            for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                // Vérifier si le véhicule est vide OU s'il a déjà des réservations à la même heure
                boolean memeHeure = vehiculeAvecCap.reservations.isEmpty() || 
                                   reservation.getDateHeureArrive().equals(vehiculeAvecCap.getHeureArriveePremiere());
                
                if (memeHeure && vehiculeAvecCap.peutAccueillir(reservation.getNombrePassage())) {
                    // Assigner la réservation
                    vehiculeAvecCap.ajouterReservation(reservation);
                    reservationsNonAssignees.remove(0);
                    assignee = true;

                    // OPTIMISATION : Chercher d'autres réservations à ajouter si le véhicule n'est pas plein
                    // MAIS UNIQUEMENT celles avec la même heure d'arrivée
                    if (vehiculeAvecCap.placesRestantes > 0) {
                        Timestamp heureVehicule = vehiculeAvecCap.getHeureArriveePremiere();
                        List<Reservation> aSupprimer = new ArrayList<>();
                        
                        for (Reservation autreReservation : reservationsNonAssignees) {
                            // Ne prendre que les réservations à la même heure
                            if (autreReservation.getDateHeureArrive().equals(heureVehicule) &&
                                vehiculeAvecCap.peutAccueillir(autreReservation.getNombrePassage())) {
                                vehiculeAvecCap.ajouterReservation(autreReservation);
                                aSupprimer.add(autreReservation);

                                // Si le véhicule est maintenant plein, arrêter
                                if (vehiculeAvecCap.placesRestantes == 0) {
                                    break;
                                }
                            }
                        }
                        reservationsNonAssignees.removeAll(aSupprimer);
                    }

                    break;
                }
            }

            // Si la réservation n'a pas pu être assignée, la retirer et la sauvegarder
            if (!assignee) {
                reservationsImpossiblesAAssigner.add(reservationsNonAssignees.remove(0));
            }
        }

        // ETAPE 4 : Calculer les horaires de départ et retour pour chaque véhicule
        Map<Vehicule, InfosTrajet> infosTrajetParVehicule = new HashMap<>();
        for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
            if (!vehiculeAvecCap.reservations.isEmpty()) {
                calculerHoraires(vehiculeAvecCap, conn);
                vehiculesAvecReservations.put(vehiculeAvecCap.vehicule, vehiculeAvecCap.reservations);
                
                // Stocker les infos de trajet
                if (vehiculeAvecCap.heureDepart != null && vehiculeAvecCap.heureRetour != null) {
                    infosTrajetParVehicule.put(vehiculeAvecCap.vehicule, 
                        new InfosTrajet(vehiculeAvecCap.heureDepart, vehiculeAvecCap.heureRetour, vehiculeAvecCap.dureeTrajetMinutes));
                }
            }
        }

        // Ajouter les véhicules sans réservations avec liste vide
        for (Vehicule v : vehicules) {
            if (!vehiculesAvecReservations.containsKey(v)) {
                vehiculesAvecReservations.put(v, new ArrayList<>());
            }
        }

        // Remplir le résultat
        resultat.setVehiculesAvecReservations(vehiculesAvecReservations);
        resultat.setReservationsNonAssignees(reservationsImpossiblesAAssigner);
        resultat.setInfosTrajetParVehicule(infosTrajetParVehicule);

        return resultat;
    }

    /**
     * Calcule les horaires de départ et retour pour un véhicule et ses réservations
     * Trajet : Aéroport → Hôtel1 → Hôtel2 → ... → Aéroport
     * 
     * @param vehiculeAvecCap Véhicule avec ses réservations
     * @param conn            Connexion à la base de données
     * @throws SQLException
     */
    private static void calculerHoraires(VehiculeAvecCapacite vehiculeAvecCap, Connection conn) throws SQLException {
        List<Reservation> reservations = vehiculeAvecCap.reservations;
        Vehicule vehicule = vehiculeAvecCap.vehicule;

        if (reservations.isEmpty()) {
            return;
        }

        // Toutes les réservations ont la même heure d'arrivée
        Timestamp heureArrivee = reservations.get(0).getDateHeureArrive();

        // Calculer le trajet complet : Aéroport (id=1) → Hôtel1 → Hôtel2 → ... → Aéroport
        BigDecimal distanceTotale = BigDecimal.ZERO;
        int pointDepart = -1; // -1 pour aéroport
        int aeroportId = 1; // Aéroport Ivato par défaut

        // Trajet : Aéroport → Hôtel1
        if (!reservations.isEmpty()) {
            int premierHotel = reservations.get(0).getIdHotel();
            BigDecimal distance = getDistance(conn, null, aeroportId, premierHotel);
            if (distance != null) {
                distanceTotale = distanceTotale.add(distance);
            }
            pointDepart = premierHotel;
        }

        // Trajets inter-hôtels : Hôtel1 → Hôtel2 → Hôtel3 → ...
        for (int i = 1; i < reservations.size(); i++) {
            int hotelDestination = reservations.get(i).getIdHotel();
            if (hotelDestination != pointDepart) { // Éviter les trajets inutiles
                BigDecimal distance = getDistance(conn, pointDepart, null, hotelDestination);
                if (distance != null) {
                    distanceTotale = distanceTotale.add(distance);
                }
                pointDepart = hotelDestination;
            }
        }

        // Trajet retour : DernierHôtel → Aéroport
        if (pointDepart != -1) {
            BigDecimal distanceRetour = getDistance(conn, pointDepart, null, aeroportId);
            if (distanceRetour != null) {
                distanceTotale = distanceTotale.add(distanceRetour);
            }
        }

        // Calculer le temps de trajet total
        int vitesseMoyenne = vehicule.getVitesseMoyenne();
        if (vitesseMoyenne > 0 && distanceTotale.compareTo(BigDecimal.ZERO) > 0) {
            // Temps en heures = distance / vitesse
            BigDecimal tempsHeures = distanceTotale.divide(new BigDecimal(vitesseMoyenne), 2, BigDecimal.ROUND_HALF_UP);
            // Convertir en minutes
            int tempsMinutes = tempsHeures.multiply(new BigDecimal(60)).intValue();
            
            // Ajouter 30 minutes de temps d'arrêt par hôtel (dépose passagers)
            int tempsArretMinutes = 30 * reservations.size();
            int tempsTotalMinutes = tempsMinutes + tempsArretMinutes;

            // Calculer l'heure de départ (on part avant pour arriver à l'heure)
            vehiculeAvecCap.heureDepart = new Timestamp(heureArrivee.getTime() - (tempsTotalMinutes * 60 * 1000));

            // Calculer l'heure de retour (heure d'arrivée + temps retour)
            vehiculeAvecCap.heureRetour = new Timestamp(heureArrivee.getTime() + (tempsTotalMinutes * 60 * 1000));
            
            vehiculeAvecCap.dureeTrajetMinutes = tempsTotalMinutes;
        }
    }

    /**
     * Récupère la distance entre deux points (aéroport/hôtel)
     * 
     * @param conn           Connexion DB
     * @param idFromHotel    ID de l'hôtel de départ (null si départ d'aéroport)
     * @param idFromAeroport ID de l'aéroport de départ (null si départ d'hôtel)
     * @param idTo           ID de l'hôtel de destination
     * @return Distance en km, ou null si non trouvée
     * @throws SQLException
     */
    private static BigDecimal getDistance(Connection conn, Integer idFromHotel, Integer idFromAeroport, int idTo)
            throws SQLException {
        String sql = "SELECT valeur FROM distance WHERE ";

        if (idFromHotel != null) {
            sql += "id_from_hotel = ? AND id_from_aeroport IS NULL";
        } else if (idFromAeroport != null) {
            sql += "id_from_aeroport = ? AND id_from_hotel IS NULL";
        } else {
            return null;
        }

        sql += " AND id_to = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (idFromHotel != null) {
                stmt.setInt(1, idFromHotel);
            } else {
                stmt.setInt(1, idFromAeroport);
            }
            stmt.setInt(2, idTo);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("valeur");
            }
        }

        return null;
    }
}
