package itu.back.util;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
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
        private List<SegmentTrajet> segments;

        public InfosTrajet(Timestamp heureDepart, Timestamp heureRetour, int dureeTrajetMinutes,
                List<SegmentTrajet> segments) {
            this.heureDepart = heureDepart;
            this.heureRetour = heureRetour;
            this.dureeTrajetMinutes = dureeTrajetMinutes;
            this.segments = segments;
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

        public List<SegmentTrajet> getSegments() {
            return segments;
        }
    }

    /**
     * Classe pour stocker les détails d'un segment de trajet
     */
    public static class SegmentTrajet {
        private String origine;
        private String destination;
        private BigDecimal distanceKm;
        private int dureeMinutes;

        public SegmentTrajet(String origine, String destination, BigDecimal distanceKm, int dureeMinutes) {
            this.origine = origine;
            this.destination = destination;
            this.distanceKm = distanceKm;
            this.dureeMinutes = dureeMinutes;
        }

        public String getOrigine() {
            return origine;
        }

        public String getDestination() {
            return destination;
        }

        public BigDecimal getDistanceKm() {
            return distanceKm;
        }

        public int getDureeMinutes() {
            return dureeMinutes;
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
        List<SegmentTrajet> segments;

        public VehiculeAvecCapacite(Vehicule v) {
            this.vehicule = v;
            this.placesRestantes = v.getNombrePlaces();
            this.reservations = new ArrayList<>();
            this.segments = new ArrayList<>();
        }

        public boolean peutAccueillir(int nombrePassagers) {
            return placesRestantes >= nombrePassagers;
        }

        public void ajouterReservation(Reservation r) {
            reservations.add(r);
            placesRestantes -= r.getNombrePassage();
        }

        public Timestamp getHeureArriveePremiere() {
            if (reservations.isEmpty())
                return null;
            return reservations.get(0).getDateHeureArrive();
        }
    }

    /**
     * Simule l'assignation des véhicules aux réservations (sans regroupement)
     */
    public static ResultatSimulation simulerAssignation(
            List<Reservation> reservations,
            List<Vehicule> vehicules,
            Connection conn) throws SQLException {
        return simulerAssignation(reservations, vehicules, conn, 0);
    }

    /**
     * Simule l'assignation des véhicules aux réservations pour une date donnée
     * avec regroupement par temps d'attente.
     * 
     * @param reservations         Liste des réservations pour la date
     * @param vehicules            Liste de tous les véhicules disponibles
     * @param conn                 Connexion à la base de données
     * @param tempsAttenteMinutes  Durée du temps d'attente pour regrouper les départs (0 = pas de regroupement)
     * @return ResultatSimulation
     * @throws SQLException
     */
    public static ResultatSimulation simulerAssignation(
            List<Reservation> reservations,
            List<Vehicule> vehicules,
            Connection conn,
            int tempsAttenteMinutes) throws SQLException {

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

        // ETAPE 1 : Trier par heure d'arrivée
        reservationsNonAssignees.sort((r1, r2) -> r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive()));

        // ETAPE 1b : Regrouper par temps d'attente et déterminer l'heure de départ groupée
        // L'heure de départ du groupe = l'heure d'arrivée la plus tardive du groupe
        Map<Integer, Timestamp> heureDepartParReservation = new HashMap<>();
        if (tempsAttenteMinutes > 0) {
            List<List<Reservation>> groupes = regroupeParTempsAttente(reservationsNonAssignees, tempsAttenteMinutes);
            for (List<Reservation> groupe : groupes) {
                Timestamp heureDepartGroupe = groupe.get(groupe.size() - 1).getDateHeureArrive();
                for (Reservation r : groupe) {
                    heureDepartParReservation.put(r.getId(), heureDepartGroupe);
                }
            }
        } else {
            // Pas de regroupement : chaque réservation garde son heure d'arrivée
            for (Reservation r : reservationsNonAssignees) {
                heureDepartParReservation.put(r.getId(), r.getDateHeureArrive());
            }
        }

        // ETAPE 2 : Trier par heure de départ groupée puis par nombre de passagers (décroissant)
        reservationsNonAssignees.sort((r1, r2) -> {
            Timestamp h1 = heureDepartParReservation.get(r1.getId());
            Timestamp h2 = heureDepartParReservation.get(r2.getId());
            int compareHeure = h1.compareTo(h2);
            if (compareHeure != 0)
                return compareHeure;
            return Integer.compare(r2.getNombrePassage(), r1.getNombrePassage());
        });

        // ETAPE 3 : Assigner les réservations (celles avec le même départ groupé)
        while (!reservationsNonAssignees.isEmpty()) {
            Reservation reservation = reservationsNonAssignees.get(0);
            Timestamp heureDepartReservation = heureDepartParReservation.get(reservation.getId());
            boolean assignee = false;

            // Chercher un véhicule disponible avec assez de places
            for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                // Vérifier si le véhicule est vide OU s'il a des réservations du même groupe de départ
                boolean memeGroupe = vehiculeAvecCap.reservations.isEmpty() ||
                        heureDepartParReservation.get(vehiculeAvecCap.reservations.get(0).getId())
                                .equals(heureDepartReservation);

                if (memeGroupe && vehiculeAvecCap.peutAccueillir(reservation.getNombrePassage())) {
                    // Assigner la réservation
                    vehiculeAvecCap.ajouterReservation(reservation);
                    reservationsNonAssignees.remove(0);
                    assignee = true;

                    // OPTIMISATION : Chercher d'autres réservations du même groupe à ajouter
                    if (vehiculeAvecCap.placesRestantes > 0) {
                        Timestamp heureVehicule = heureDepartParReservation
                                .get(vehiculeAvecCap.reservations.get(0).getId());
                        List<Reservation> aSupprimer = new ArrayList<>();

                        for (Reservation autreReservation : reservationsNonAssignees) {
                            if (heureDepartParReservation.get(autreReservation.getId()).equals(heureVehicule) &&
                                    vehiculeAvecCap.peutAccueillir(autreReservation.getNombrePassage())) {
                                vehiculeAvecCap.ajouterReservation(autreReservation);
                                aSupprimer.add(autreReservation);

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
                            new InfosTrajet(vehiculeAvecCap.heureDepart, vehiculeAvecCap.heureRetour,
                                    vehiculeAvecCap.dureeTrajetMinutes, vehiculeAvecCap.segments));
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
     * Trajet optimisé : Aéroport → Hôtels (triés par distance puis alphabétique) →
     * Aéroport
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

        // L'heure de départ = l'heure d'arrivée la plus tardive du groupe
        Timestamp heureArrivee = reservations.get(0).getDateHeureArrive();
        for (Reservation r : reservations) {
            if (r.getDateHeureArrive().after(heureArrivee)) {
                heureArrivee = r.getDateHeureArrive();
            }
        }
        vehiculeAvecCap.segments = new ArrayList<>();

        // Récupérer les hôtels uniques et leurs informations
        Map<Integer, String> hotelsMap = new HashMap<>();
        for (Reservation r : reservations) {
            if (!hotelsMap.containsKey(r.getIdHotel())) {
                hotelsMap.put(r.getIdHotel(), r.getHotel().getNom());
            }
        }

        // Optimiser l'ordre des hôtels : plus courte distance d'abord, puis
        // alphabétique
        List<Integer> ordreHotels = optimiserOrdreHotels(conn, new ArrayList<>(hotelsMap.keySet()), hotelsMap);

        // Calculer le trajet complet
        BigDecimal distanceTotale = BigDecimal.ZERO;
        int vitesseMoyenne = vehicule.getVitesseMoyenne();
        int aeroportId = 1; // Aéroport Ivato

        // Segment 1 : Aéroport → Premier Hôtel
        if (!ordreHotels.isEmpty()) {
            int premierHotel = ordreHotels.get(0);
            String nomPremierHotel = hotelsMap.get(premierHotel);
            BigDecimal distance = getDistance(conn, null, aeroportId, premierHotel);

            if (distance != null && vitesseMoyenne > 0) {
                distanceTotale = distanceTotale.add(distance);
                int dureeMinutes = calculerDureeMinutes(distance, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet("Aéroport Ivato", nomPremierHotel, distance, dureeMinutes));
            } else if (vitesseMoyenne > 0) {
                // Fallback : utiliser une estimation basée sur la distance moyenne
                BigDecimal distanceEstimee = new BigDecimal("15.0"); // 15 km par défaut depuis l'aéroport
                distanceTotale = distanceTotale.add(distanceEstimee);
                int dureeMinutes = calculerDureeMinutes(distanceEstimee, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet("Aéroport Ivato", nomPremierHotel, distanceEstimee, dureeMinutes));
            }
        }

        // Segments inter-hôtels : Hôtel1 → Hôtel2 → ...
        for (int i = 0; i < ordreHotels.size() - 1; i++) {
            int hotelDepart = ordreHotels.get(i);
            int hotelArrivee = ordreHotels.get(i + 1);
            String nomHotelDepart = hotelsMap.get(hotelDepart);
            String nomHotelArrivee = hotelsMap.get(hotelArrivee);

            BigDecimal distance = getDistance(conn, hotelDepart, null, hotelArrivee);
            if (distance != null && vitesseMoyenne > 0) {
                distanceTotale = distanceTotale.add(distance);
                int dureeMinutes = calculerDureeMinutes(distance, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet(nomHotelDepart, nomHotelArrivee, distance, dureeMinutes));
            } else {
                // Distance non trouvée dans la base - utiliser une estimation (distance moyenne
                // en ville)
                BigDecimal distanceEstimee = new BigDecimal("3.0"); // 3 km par défaut
                distanceTotale = distanceTotale.add(distanceEstimee);
                int dureeMinutes = calculerDureeMinutes(distanceEstimee, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet(nomHotelDepart, nomHotelArrivee, distanceEstimee, dureeMinutes));
            }
        }

        // Segment final : Dernier Hôtel → Aéroport
        if (!ordreHotels.isEmpty()) {
            int dernierHotel = ordreHotels.get(ordreHotels.size() - 1);
            String nomDernierHotel = hotelsMap.get(dernierHotel);
            BigDecimal distanceRetour = getDistance(conn, dernierHotel, null, aeroportId);

            // Si distance Hôtel → Aéroport non trouvée, utiliser la distance inverse (Aéroport → Hôtel)
            if (distanceRetour == null) {
                distanceRetour = getDistance(conn, null, aeroportId, dernierHotel);
            }

            if (distanceRetour != null && vitesseMoyenne > 0) {
                distanceTotale = distanceTotale.add(distanceRetour);
                int dureeMinutes = calculerDureeMinutes(distanceRetour, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet(nomDernierHotel, "Aéroport Ivato", distanceRetour, dureeMinutes));
            } else if (vitesseMoyenne > 0) {
                // Fallback : utiliser une estimation basée sur la distance moyenne
                BigDecimal distanceEstimee = new BigDecimal("15.0"); // 15 km par défaut vers l'aéroport
                distanceTotale = distanceTotale.add(distanceEstimee);
                int dureeMinutes = calculerDureeMinutes(distanceEstimee, vitesseMoyenne);
                vehiculeAvecCap.segments
                        .add(new SegmentTrajet(nomDernierHotel, "Aéroport Ivato", distanceEstimee, dureeMinutes));
            }
        }

        // Calculer le temps de trajet total en sommant les segments
        if (vitesseMoyenne > 0 && !vehiculeAvecCap.segments.isEmpty()) {
            // Calculer le temps total de trajet (somme de tous les segments)
            int tempsTrajetTotal = 0;
            for (SegmentTrajet segment : vehiculeAvecCap.segments) {
                tempsTrajetTotal += segment.getDureeMinutes();
            }

            // L'heure de départ = heure d'arrivée du vol (le véhicule récupère les passagers à l'aéroport)
            vehiculeAvecCap.heureDepart = heureArrivee;

            // L'heure de retour = heure de départ + temps total du trajet
            vehiculeAvecCap.heureRetour = new Timestamp(heureArrivee.getTime() + (tempsTrajetTotal * 60 * 1000));

            vehiculeAvecCap.dureeTrajetMinutes = tempsTrajetTotal;
        }
    }

    /**
     * Optimise l'ordre des hôtels : plus courte distance d'abord, puis alphabétique
     * Utilise un algorithme glouton pour minimiser la distance totale
     * 
     * @param conn      Connexion DB
     * @param hotelIds  Liste des IDs d'hôtels à organiser
     * @param hotelsMap Map ID → Nom d'hôtel
     * @return Liste ordonnée des IDs d'hôtels
     * @throws SQLException
     */
    private static List<Integer> optimiserOrdreHotels(Connection conn, List<Integer> hotelIds,
            Map<Integer, String> hotelsMap) throws SQLException {
        if (hotelIds.size() <= 1) {
            return hotelIds;
        }

        List<Integer> ordreOptimal = new ArrayList<>();
        List<Integer> restants = new ArrayList<>(hotelIds);
        int aeroportId = 1;
        int positionActuelle = -1; // -1 = aéroport

        // Algorithme glouton : choisir le plus proche à chaque étape
        while (!restants.isEmpty()) {
            int meilleurHotel = -1;
            BigDecimal meilleureDistance = null;
            String meilleurNom = null;

            for (int hotelId : restants) {
                BigDecimal distance;
                if (positionActuelle == -1) {
                    // Depuis l'aéroport
                    distance = getDistance(conn, null, aeroportId, hotelId);
                } else {
                    // Depuis le dernier hôtel
                    distance = getDistance(conn, positionActuelle, null, hotelId);
                }

                if (distance != null) {
                    // Priorité 1 : distance la plus courte
                    // Priorité 2 : ordre alphabétique en cas d'égalité
                    if (meilleureDistance == null ||
                            distance.compareTo(meilleureDistance) < 0 ||
                            (distance.compareTo(meilleureDistance) == 0 &&
                                    hotelsMap.get(hotelId).compareToIgnoreCase(meilleurNom) < 0)) {
                        meilleureDistance = distance;
                        meilleurHotel = hotelId;
                        meilleurNom = hotelsMap.get(hotelId);
                    }
                } else if (meilleureDistance == null) {
                    // Si aucune distance n'est disponible pour aucun hôtel, prendre par ordre
                    // alphabétique
                    if (meilleurHotel == -1 || hotelsMap.get(hotelId).compareToIgnoreCase(meilleurNom) < 0) {
                        meilleurHotel = hotelId;
                        meilleurNom = hotelsMap.get(hotelId);
                    }
                }
            }

            if (meilleurHotel != -1) {
                ordreOptimal.add(meilleurHotel);
                restants.remove(Integer.valueOf(meilleurHotel));
                positionActuelle = meilleurHotel;
            } else {
                // Cas extrême : ajouter tous les restants par ordre alphabétique
                restants.sort((a, b) -> hotelsMap.get(a).compareToIgnoreCase(hotelsMap.get(b)));
                ordreOptimal.addAll(restants);
                break;
            }
        }

        return ordreOptimal;
    }

    /**
     * Regroupe les réservations par fenêtre de temps d'attente.
     * La première arrivée ouvre une fenêtre de tempsAttenteMinutes.
     * Toutes les réservations arrivant dans cette fenêtre sont regroupées.
     * Le départ effectif du groupe sera l'heure d'arrivée la plus tardive.
     *
     * @param reservationsTriees  Réservations triées par heure d'arrivée croissante
     * @param tempsAttenteMinutes Durée de la fenêtre d'attente en minutes
     * @return Liste de groupes de réservations
     */
    private static List<List<Reservation>> regroupeParTempsAttente(
            List<Reservation> reservationsTriees, int tempsAttenteMinutes) {
        List<List<Reservation>> groupes = new ArrayList<>();
        if (reservationsTriees.isEmpty()) {
            return groupes;
        }

        List<Reservation> groupeActuel = new ArrayList<>();
        Timestamp debutFenetre = null;

        for (Reservation r : reservationsTriees) {
            if (debutFenetre == null) {
                // Première réservation : ouvre une nouvelle fenêtre
                debutFenetre = r.getDateHeureArrive();
                groupeActuel.add(r);
            } else {
                long diffMs = r.getDateHeureArrive().getTime() - debutFenetre.getTime();
                long diffMinutes = diffMs / (60 * 1000);
                if (diffMinutes <= tempsAttenteMinutes) {
                    // Dans la fenêtre : ajouter au groupe actuel
                    groupeActuel.add(r);
                } else {
                    // Hors fenêtre : sauvegarder le groupe actuel et en commencer un nouveau
                    groupes.add(groupeActuel);
                    groupeActuel = new ArrayList<>();
                    groupeActuel.add(r);
                    debutFenetre = r.getDateHeureArrive();
                }
            }
        }

        if (!groupeActuel.isEmpty()) {
            groupes.add(groupeActuel);
        }

        return groupes;
    }

    /**
     * Calcule la durée en minutes pour une distance donnée
     */
    private static int calculerDureeMinutes(BigDecimal distanceKm, int vitesseMoyenneKmH) {
        if (vitesseMoyenneKmH <= 0)
            return 0;
        BigDecimal heures = distanceKm.divide(new BigDecimal(vitesseMoyenneKmH), 4, BigDecimal.ROUND_HALF_UP);
        return heures.multiply(new BigDecimal(60)).intValue();
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
