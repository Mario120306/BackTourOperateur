package itu.back.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        private Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule;

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

        public Map<Vehicule, List<InfosTrajet>> getInfosTrajetParVehicule() {
            return infosTrajetParVehicule;
        }

        public void setInfosTrajetParVehicule(Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule) {
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
        // Historique des trajets pendant la simulation pour vérifier la disponibilité
        List<long[]> trajetsOccupes; // chaque entrée = [depart_ms, retour_ms]

        public VehiculeAvecCapacite(Vehicule v) {
            this.vehicule = v;
            this.placesRestantes = v.getNombrePlaces();
            this.reservations = new ArrayList<>();
            this.segments = new ArrayList<>();
            this.trajetsOccupes = new ArrayList<>();
        }

        public boolean peutAccueillir(int nombrePassagers) {
            return placesRestantes >= nombrePassagers;
        }

        public void ajouterReservation(Reservation r) {
            reservations.add(r);
            placesRestantes -= r.getNombrePassage();
        }

        /**
         * Vérifie si le véhicule est disponible à l'heure donnée
         * (aucun trajet en cours : tous les trajets précédents sont terminés)
         */
        public boolean estDisponibleA(Timestamp heure) {
            long heureMs = heure.getTime();
            for (long[] trajet : trajetsOccupes) {
                // Le véhicule est occupé si : depart <= heure < retour
                if (heureMs >= trajet[0] && heureMs < trajet[1]) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Réinitialise le véhicule pour un nouveau trajet (nouveau groupe de départ)
         */
        public void reinitialiserPourNouveauTrajet() {
            // Sauvegarder le trajet précédent si existant
            if (heureDepart != null && heureRetour != null) {
                trajetsOccupes.add(new long[] { heureDepart.getTime(), heureRetour.getTime() });
            }
            this.placesRestantes = vehicule.getNombrePlaces();
            this.reservations = new ArrayList<>();
            this.heureDepart = null;
            this.heureRetour = null;
            this.dureeTrajetMinutes = 0;
            this.segments = new ArrayList<>();
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
     * Règle de disponibilité : un véhicule déjà parti ne peut être réassigné
     * que s'il est déjà revenu (heure_retour <= heure de départ du nouveau groupe).
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
            VehiculeAvecCapacite vac = new VehiculeAvecCapacite(v);
            vehiculesDisponibles.add(vac);
        }

        // Note : le tri des véhicules se fait dynamiquement à chaque assignation
        // selon les critères : moins de trajets, places les plus proches, diesel prioritaire

        // Copie des réservations pour pouvoir les modifier
        List<Reservation> reservationsNonAssignees = new ArrayList<>(reservations);

        // Liste pour garder trace des réservations qui n'ont pas pu être assignées
        List<Reservation> reservationsImpossiblesAAssigner = new ArrayList<>();

        // ETAPE 1 : Trier par heure d'arrivée
        reservationsNonAssignees.sort((r1, r2) -> r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive()));

        // ETAPE 1b : Regrouper par temps d'attente et déterminer l'heure de départ groupée
        // L'heure de départ du groupe = l'heure d'arrivée la plus tardive du groupe
        Map<Integer, Timestamp> heureDepartParReservation = new HashMap<>();
        List<List<Reservation>> groupesDeDepart;
        if (tempsAttenteMinutes > 0) {
            groupesDeDepart = regroupeParTempsAttente(reservationsNonAssignees, tempsAttenteMinutes);
            for (List<Reservation> groupe : groupesDeDepart) {
                Timestamp heureDepartGroupe = groupe.get(groupe.size() - 1).getDateHeureArrive();
                for (Reservation r : groupe) {
                    heureDepartParReservation.put(r.getId(), heureDepartGroupe);
                }
            }
        } else {
            // Pas de regroupement : chaque réservation est son propre groupe
            groupesDeDepart = new ArrayList<>();
            for (Reservation r : reservationsNonAssignees) {
                heureDepartParReservation.put(r.getId(), r.getDateHeureArrive());
                List<Reservation> singleGroup = new ArrayList<>();
                singleGroup.add(r);
                groupesDeDepart.add(singleGroup);
            }
            // Fusionner les groupes ayant la même heure de départ
            groupesDeDepart = fusionnerGroupesMemeHeure(groupesDeDepart);
        }

        // ETAPE 2 : Le tri des réservations par passagers se fait dans chaque groupe (ETAPE 3)

        // ETAPE 3 : Traiter chaque groupe de départ séquentiellement
        // Après chaque groupe, calculer les horaires pour permettre la réutilisation des véhicules
        // Les réservations non assignées dans un groupe sont reportées au groupe suivant
        Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule = new HashMap<>();
        List<Reservation> reservationsReportees = new ArrayList<>();

        for (int indexGroupe = 0; indexGroupe < groupesDeDepart.size(); indexGroupe++) {
            List<Reservation> groupe = groupesDeDepart.get(indexGroupe);
            boolean estDernierGroupe = (indexGroupe == groupesDeDepart.size() - 1);
            Timestamp heureDepartGroupe = heureDepartParReservation.get(groupe.get(0).getId());

            // Ajouter les réservations reportées du groupe précédent
            List<Reservation> reservationsGroupe = new ArrayList<>(reservationsReportees);
            // Mettre à jour l'heure de départ des réservations reportées vers ce groupe
            for (Reservation r : reservationsReportees) {
                heureDepartParReservation.put(r.getId(), heureDepartGroupe);
            }
            reservationsReportees.clear();
            reservationsGroupe.addAll(groupe);

            // Trier par nombre de passagers décroissant
            reservationsGroupe.sort((r1, r2) -> Integer.compare(r2.getNombrePassage(), r1.getNombrePassage()));

            while (!reservationsGroupe.isEmpty()) {
                Reservation reservation = reservationsGroupe.get(0);
                boolean assignee = false;

                // Chercher le meilleur véhicule selon les critères :
                // 1. Disponible à l'heure de départ du groupe
                // 2. Capacité suffisante et même groupe de départ
                // 3. Moins de trajets effectués
                // 4. Nombre de places le plus proche du nombre de passagers
                // 5. Diesel prioritaire
                VehiculeAvecCapacite meilleurVehicule = null;

                for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                    // Vérifier la disponibilité du véhicule à l'heure de départ du groupe
                    if (!vehiculeAvecCap.estDisponibleA(heureDepartGroupe)) {
                        continue; // Véhicule en trajet, pas encore revenu
                    }

                    // Vérifier si le véhicule est vide OU s'il a des réservations du même groupe
                    boolean memeGroupe = vehiculeAvecCap.reservations.isEmpty() ||
                            heureDepartParReservation.get(vehiculeAvecCap.reservations.get(0).getId())
                                    .equals(heureDepartGroupe);

                    if (!memeGroupe || !vehiculeAvecCap.peutAccueillir(reservation.getNombrePassage())) {
                        continue;
                    }

                    // Ce véhicule est candidat, le comparer au meilleur actuel
                    if (meilleurVehicule == null) {
                        meilleurVehicule = vehiculeAvecCap;
                    } else {
                        // Critère 1 : moins de trajets effectués
                        int trajetsCandidat = vehiculeAvecCap.trajetsOccupes.size();
                        int trajetsMeilleur = meilleurVehicule.trajetsOccupes.size();
                        if (trajetsCandidat != trajetsMeilleur) {
                            if (trajetsCandidat < trajetsMeilleur) {
                                meilleurVehicule = vehiculeAvecCap;
                            }
                            continue;
                        }

                        // Critère 2 : nombre de places le plus proche du nombre de passagers
                        int diffCandidat = vehiculeAvecCap.vehicule.getNombrePlaces() - reservation.getNombrePassage();
                        int diffMeilleur = meilleurVehicule.vehicule.getNombrePlaces() - reservation.getNombrePassage();
                        if (diffCandidat != diffMeilleur) {
                            if (diffCandidat < diffMeilleur) {
                                meilleurVehicule = vehiculeAvecCap;
                            }
                            continue;
                        }

                        // Critère 3 : diesel prioritaire
                        boolean candidatDiesel = vehiculeAvecCap.vehicule.getTypeCarburant() != null &&
                                CARBURANT_PRIORITAIRE.equals(vehiculeAvecCap.vehicule.getTypeCarburant().getReference());
                        boolean meilleurDiesel = meilleurVehicule.vehicule.getTypeCarburant() != null &&
                                CARBURANT_PRIORITAIRE.equals(meilleurVehicule.vehicule.getTypeCarburant().getReference());
                        if (candidatDiesel && !meilleurDiesel) {
                            meilleurVehicule = vehiculeAvecCap;
                        }
                    }
                }

                // Assigner au meilleur véhicule trouvé
                if (meilleurVehicule != null) {
                    meilleurVehicule.ajouterReservation(reservation);
                    reservationsGroupe.remove(0);
                    reservationsNonAssignees.remove(reservation);
                    assignee = true;

                    // OPTIMISATION : Chercher d'autres réservations du même groupe à ajouter
                    if (meilleurVehicule.placesRestantes > 0) {
                        List<Reservation> aSupprimer = new ArrayList<>();

                        for (Reservation autreReservation : reservationsGroupe) {
                            if (meilleurVehicule.peutAccueillir(autreReservation.getNombrePassage())) {
                                meilleurVehicule.ajouterReservation(autreReservation);
                                aSupprimer.add(autreReservation);

                                if (meilleurVehicule.placesRestantes == 0) {
                                    break;
                                }
                            }
                        }
                        reservationsGroupe.removeAll(aSupprimer);
                        reservationsNonAssignees.removeAll(aSupprimer);
                    }
                }

                // Si la réservation n'a pas pu être assignée
                if (!assignee) {
                    Reservation nonAssignee = reservationsGroupe.remove(0);
                    if (estDernierGroupe) {
                        // Dernier groupe de la journée : impossible à assigner
                        reservationsImpossiblesAAssigner.add(nonAssignee);
                    } else {
                        // Reporter au prochain groupe
                        reservationsReportees.add(nonAssignee);
                    }
                }
            }

            // Après chaque groupe, calculer les horaires des véhicules assignés
            // pour pouvoir vérifier la disponibilité lors du prochain groupe
            for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                if (!vehiculeAvecCap.reservations.isEmpty() && vehiculeAvecCap.heureDepart == null) {
                    calculerHoraires(vehiculeAvecCap, conn, heureDepartGroupe);

                    // Stocker les résultats pour ce véhicule (accumuler si multi-trajets)
                    if (vehiculeAvecCap.heureDepart != null && vehiculeAvecCap.heureRetour != null) {
                        // Accumuler les réservations (ne pas écraser les précédentes)
                        List<Reservation> existantes = vehiculesAvecReservations.get(vehiculeAvecCap.vehicule);
                        if (existantes == null) {
                            existantes = new ArrayList<>();
                            vehiculesAvecReservations.put(vehiculeAvecCap.vehicule, existantes);
                        }
                        existantes.addAll(vehiculeAvecCap.reservations);

                        // Accumuler les infos de trajet
                        List<InfosTrajet> trajets = infosTrajetParVehicule.get(vehiculeAvecCap.vehicule);
                        if (trajets == null) {
                            trajets = new ArrayList<>();
                            infosTrajetParVehicule.put(vehiculeAvecCap.vehicule, trajets);
                        }
                        trajets.add(new InfosTrajet(vehiculeAvecCap.heureDepart, vehiculeAvecCap.heureRetour,
                                        vehiculeAvecCap.dureeTrajetMinutes, vehiculeAvecCap.segments));

                        // Préparer le véhicule pour un éventuel nouveau trajet
                        // (sauvegarde le trajet actuel dans trajetsOccupes et réinitialise)
                        vehiculeAvecCap.reinitialiserPourNouveauTrajet();
                    }
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
     * Fusionne les groupes de réservations ayant la même heure de départ
     */
    private static List<List<Reservation>> fusionnerGroupesMemeHeure(List<List<Reservation>> groupes) {
        if (groupes.size() <= 1) {
            return groupes;
        }

        Map<Long, List<Reservation>> parHeure = new HashMap<>();
        List<Long> ordreHeures = new ArrayList<>();

        for (List<Reservation> groupe : groupes) {
            long heureMs = groupe.get(0).getDateHeureArrive().getTime();
            if (!parHeure.containsKey(heureMs)) {
                parHeure.put(heureMs, new ArrayList<>());
                ordreHeures.add(heureMs);
            }
            parHeure.get(heureMs).addAll(groupe);
        }

        ordreHeures.sort(Long::compareTo);
        List<List<Reservation>> resultat = new ArrayList<>();
        for (Long heure : ordreHeures) {
            resultat.add(parHeure.get(heure));
        }
        return resultat;
    }

    /**
     * Calcule les horaires de départ et retour pour un véhicule et ses réservations
     * Trajet optimisé : Aéroport → Hôtels (triés par distance puis alphabétique) →
     * Aéroport
     * 
     * @param vehiculeAvecCap   Véhicule avec ses réservations
     * @param conn              Connexion à la base de données
     * @param heureDepartGroupe Heure de départ du groupe (= dernière arrivée du groupe)
     * @throws SQLException
     */
    private static void calculerHoraires(VehiculeAvecCapacite vehiculeAvecCap, Connection conn, Timestamp heureDepartGroupe) throws SQLException {
        List<Reservation> reservations = vehiculeAvecCap.reservations;
        Vehicule vehicule = vehiculeAvecCap.vehicule;

        if (reservations.isEmpty()) {
            return;
        }

        // L'heure de départ = l'heure de départ du groupe (dernière arrivée du groupe)
        Timestamp heureArrivee = heureDepartGroupe;
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
        int aeroportId = getAeroportId(conn);

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
            // La distance retour (Hotel -> Aeroport) est la même que (Aeroport -> Hotel)
            BigDecimal distanceRetour = getDistance(conn, null, aeroportId, dernierHotel);

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
        int aeroportId = getAeroportId(conn);
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
        return distanceKm
                .multiply(new BigDecimal(60))
                .divide(new BigDecimal(vitesseMoyenneKmH), 0, RoundingMode.HALF_UP)
                .intValue();
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
        if (idFromHotel == null && idFromAeroport == null) {
            return null;
        }

        // 1) Recherche directe
        String sqlDirect;
        if (idFromHotel != null) {
            sqlDirect = "SELECT valeur FROM distance WHERE id_from_hotel = ? AND id_from_aeroport IS NULL AND id_to = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDirect)) {
                stmt.setInt(1, idFromHotel);
                stmt.setInt(2, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("valeur");
                    }
                }
            }
        } else {
            sqlDirect = "SELECT valeur FROM distance WHERE id_from_aeroport = ? AND id_from_hotel IS NULL AND id_to = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlDirect)) {
                stmt.setInt(1, idFromAeroport);
                stmt.setInt(2, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("valeur");
                    }
                }
            }

            // 2) Fallback aéroport : ignorer l'ID aéroport (si IDs décalés en base)
            String sqlAnyAirport = "SELECT valeur FROM distance WHERE id_from_hotel IS NULL AND id_from_aeroport IS NOT NULL AND id_to = ? ORDER BY id LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAnyAirport)) {
                stmt.setInt(1, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("valeur");
                    }
                }
            }
        }

        // 3) Fallback hôtel↔hôtel : sens inverse
        if (idFromHotel != null) {
            String sqlInverse = "SELECT valeur FROM distance WHERE id_from_hotel = ? AND id_from_aeroport IS NULL AND id_to = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInverse)) {
                stmt.setInt(1, idTo);
                stmt.setInt(2, idFromHotel);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getBigDecimal("valeur");
                    }
                }
            }
        }

        return null;
    }

    /**
     * Récupère l'ID de l'aéroport à utiliser pour la simulation.
     * On prend le premier aéroport défini en base pour éviter un ID codé en dur.
     */
    private static int getAeroportId(Connection conn) throws SQLException {
        String sql = "SELECT id FROM aeroport ORDER BY id ASC LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return 1;
    }
}
