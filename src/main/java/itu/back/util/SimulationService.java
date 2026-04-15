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
 * 2. Pour chaque réservation, parcourir les véhicules disponibles par ordre
 * de priorité et leur affecter des passagers
 * 3. Si la capacité restante d'un véhicule est insuffisante, on peut
 * diviser la réservation et n'affecter qu'une partie des passagers,
 * puis essayer d'assigner le reste sur d'autres véhicules du même groupe
 * 4. Calculer les horaires de départ et retour selon les distances et vitesse
 * moyenne
 */
public class SimulationService {

    // Carburant prioritaire (Diesel)
    private static final String CARBURANT_PRIORITAIRE = "DSL";

    private static final boolean SIMULATION_LOGS = true;

    private static void simLog(String message) {
        if (!SIMULATION_LOGS)
            return;
        String thread = Thread.currentThread() != null ? Thread.currentThread().getName() : "?";
        System.out.println("[SIM][" + thread + "] " + message);
    }

    private static void simLog(String message, long startMs) {
        if (!SIMULATION_LOGS)
            return;
        long elapsed = System.currentTimeMillis() - startMs;
        simLog(message + " (" + elapsed + " ms)");
    }

    // =========================================================================
    // Classes internes
    // =========================================================================

    public static class ResultatSimulation {
        private Map<Vehicule, List<Reservation>> vehiculesAvecReservations;
        private List<Reservation> reservationsNonAssignees;
        private Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule;
        private Map<Reservation, Timestamp> heureDepartParReservation;

        public ResultatSimulation() {
            this.vehiculesAvecReservations = new HashMap<>();
            this.reservationsNonAssignees = new ArrayList<>();
            this.infosTrajetParVehicule = new HashMap<>();
            this.heureDepartParReservation = new HashMap<>();
        }

        public Map<Vehicule, List<Reservation>> getVehiculesAvecReservations() {
            return vehiculesAvecReservations;
        }

        public void setVehiculesAvecReservations(Map<Vehicule, List<Reservation>> v) {
            this.vehiculesAvecReservations = v;
        }

        public List<Reservation> getReservationsNonAssignees() {
            return reservationsNonAssignees;
        }

        public void setReservationsNonAssignees(List<Reservation> r) {
            this.reservationsNonAssignees = r;
        }

        public Map<Vehicule, List<InfosTrajet>> getInfosTrajetParVehicule() {
            return infosTrajetParVehicule;
        }

        public void setInfosTrajetParVehicule(Map<Vehicule, List<InfosTrajet>> m) {
            this.infosTrajetParVehicule = m;
        }

        public Map<Reservation, Timestamp> getHeureDepartParReservation() {
            return heureDepartParReservation;
        }

        public void setHeureDepartParReservation(Map<Reservation, Timestamp> m) {
            this.heureDepartParReservation = m;
        }

        public void logResultats(String contexte) {
            System.out.println("===== RESULTAT SIMULATION" + (contexte != null ? " - " + contexte : "") + " =====");
            int totalReservations = 0, totalPassagers = 0;
            for (Map.Entry<Vehicule, List<Reservation>> e : vehiculesAvecReservations.entrySet()) {
                for (Reservation r : e.getValue()) {
                    totalReservations++;
                    totalPassagers += r.getNombrePassage();
                }
            }
            System.out.println("Vehicules utilises : "
                    + vehiculesAvecReservations.entrySet().stream().filter(en -> !en.getValue().isEmpty()).count());
            System.out.println(
                    "Lignes de reservation assignees : " + totalReservations + " (" + totalPassagers + " passagers)");
            System.out.println("Reservations non assignees : " + reservationsNonAssignees.size());
            for (Map.Entry<Vehicule, List<Reservation>> entry : vehiculesAvecReservations.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> resVehicule = entry.getValue();
                if (resVehicule.isEmpty())
                    continue;
                System.out.println();
                System.out.println("Vehicule " + (v.getReference() != null ? v.getReference() : ("#" + v.getId()))
                        + " (" + v.getNombrePlaces() + " places)");
                for (Reservation r : resVehicule) {
                    Timestamp hDep = heureDepartParReservation.get(r);
                    String client = (r.getClient() != null ? r.getClient().getNom() : ("client#" + r.getIdClient()));
                    String hotel = (r.getHotel() != null ? r.getHotel().getNom() : ("hotel#" + r.getIdHotel()));
                    System.out.println("  - " + client + " -> " + hotel + " : " + r.getNombrePassage()
                            + " passagers, depart=" + (hDep != null ? hDep : "?"));
                }
            }
            if (!reservationsNonAssignees.isEmpty()) {
                System.out.println();
                System.out.println("Reservations NON assignees :");
                for (Reservation r : reservationsNonAssignees) {
                    String client = (r.getClient() != null ? r.getClient().getNom() : ("client#" + r.getIdClient()));
                    String hotel = (r.getHotel() != null ? r.getHotel().getNom() : ("hotel#" + r.getIdHotel()));
                    System.out.println("  - " + client + " -> " + hotel + " : " + r.getNombrePassage()
                            + " passagers, arrivee=" + r.getDateHeureArrive());
                }
            }
            System.out.println("===== FIN RESULTAT SIMULATION =====");
        }
    }

    public static class InfosTrajet {
        private Timestamp heureDepart, heureRetour;
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

    public static class SegmentTrajet {
        private String origine, destination;
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

    private static class VehiculeAvecCapacite {
        Vehicule vehicule;
        int placesRestantes;
        List<Reservation> reservations;
        Timestamp heureDepart, heureRetour;
        int dureeTrajetMinutes;
        List<SegmentTrajet> segments;
        List<long[]> trajetsOccupes;

        public VehiculeAvecCapacite(Vehicule v) {
            this.vehicule = v;
            this.placesRestantes = v.getNombrePlaces();
            this.reservations = new ArrayList<>();
            this.segments = new ArrayList<>();
            this.trajetsOccupes = new ArrayList<>();
        }

        public boolean peutAccueillir(int n) {
            return placesRestantes >= n;
        }

        public void ajouterReservation(Reservation r) {
            reservations.add(r);
            placesRestantes -= r.getNombrePassage();
        }

        public boolean estDisponibleA(Timestamp heure) {
            long heureMs = heure.getTime();
            if (vehicule.getHeureDisponibilite() != null) {
                long dispoMs = vehicule.getHeureDisponibilite().getTime() % (24L * 60L * 60L * 1000L);
                long heureJourMs = heureMs % (24L * 60L * 60L * 1000L);
                if (heureJourMs < dispoMs)
                    return false;
            }
            for (long[] trajet : trajetsOccupes) {
                if (heureMs >= trajet[0] && heureMs < trajet[1])
                    return false;
            }
            return true;
        }

        public void reinitialiserPourNouveauTrajet() {
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
            return reservations.isEmpty() ? null : reservations.get(0).getDateHeureArrive();
        }
    }

    private static class ReservationArrivante {
        final Reservation reservation;
        final List<Reservation> groupeOrigine;

        ReservationArrivante(Reservation r, List<Reservation> g) {
            this.reservation = r;
            this.groupeOrigine = g;
        }
    }

    private static class RetourVehicule {
        final VehiculeAvecCapacite vehicule;
        final long retourMs;

        RetourVehicule(VehiculeAvecCapacite v, long r) {
            this.vehicule = v;
            this.retourMs = r;
        }
    }

    // =========================================================================
    // CORRECTION PROBLÈME 1 — sélection best-fit : exact > supérieur > inférieur
    // Appliquée dans remplirVehiculeAvecAutresReservations ET
    // remplirVehiculeAvecReservationsArrivees
    // =========================================================================

    /**
     * Sélectionne la meilleure réservation parmi une liste selon la règle :
     * 1. Égale à la capacité restante (exact)
     * 2. Supérieure la plus proche
     * 3. Inférieure la plus proche
     */
    private static Reservation selectionnerMeilleureReservation(
            List<Reservation> candidates, int capaciteRestante) {
        Reservation meilleure = null;
        int meilleureDiff = Integer.MAX_VALUE;

        for (Reservation candidate : candidates) {
            int nb = candidate.getNombrePassage();
            if (meilleure == null) {
                meilleure = candidate;
                meilleureDiff = Math.abs(nb - capaciteRestante);
                continue;
            }
            int nbMeilleur = meilleure.getNombrePassage();
            boolean candidatExact = (nb == capaciteRestante);
            boolean meilleurExact = (nbMeilleur == capaciteRestante);
            boolean candidatSup = (nb > capaciteRestante);
            boolean meilleurSup = (nbMeilleur > capaciteRestante);

            // Priorité 1 : exact
            if (candidatExact && !meilleurExact) {
                meilleure = candidate;
                meilleureDiff = 0;
                continue;
            }
            if (!candidatExact && meilleurExact) {
                continue;
            }

            // Priorité 2 : supérieur le plus proche
            if (candidatSup && !meilleurSup) {
                meilleure = candidate;
                meilleureDiff = nb - capaciteRestante;
                continue;
            }
            if (!candidatSup && meilleurSup) {
                continue;
            }

            // Priorité 3 : inférieur le plus proche (ou supérieur le plus proche si les
            // deux sont sup)
            int diff = Math.abs(nb - capaciteRestante);
            if (diff < meilleureDiff) {
                meilleure = candidate;
                meilleureDiff = diff;
            }
        }
        return meilleure;
    }

    private static void remplirVehiculeAvecAutresReservations(
            VehiculeAvecCapacite vehiculeAvecCap,
            List<Reservation> reservationsGroupe,
            Timestamp heureDepartGroupe,
            Map<Reservation, Timestamp> heureDepartParReservation) {

        while (vehiculeAvecCap.placesRestantes > 0 && !reservationsGroupe.isEmpty()) {
            int capaciteRestante = vehiculeAvecCap.placesRestantes;

            // CORRECTION P1 : utilise la sélection exact > sup > inf
            Reservation meilleureReservation = selectionnerMeilleureReservation(reservationsGroupe, capaciteRestante);

            if (meilleureReservation == null)
                break;

            int nbPassagers = meilleureReservation.getNombrePassage();
            int aAssigner = Math.min(nbPassagers, capaciteRestante);
            if (aAssigner <= 0)
                break;

            Reservation partie = copierReservation(meilleureReservation, aAssigner);
            vehiculeAvecCap.ajouterReservation(partie);
            heureDepartParReservation.put(partie, heureDepartGroupe);

            reservationsGroupe.remove(meilleureReservation);
            int reste = nbPassagers - aAssigner;
            if (reste > 0) {
                reservationsGroupe.add(copierReservation(meilleureReservation, reste));
            }
        }
    }

    private static void remplirVehiculeAvecReservationsArrivees(
            VehiculeAvecCapacite vehiculeAvecCap,
            List<ReservationArrivante> poolArrivees,
            Timestamp heureCourante,
            Map<Reservation, Timestamp> heureDepartParReservation) {

        while (vehiculeAvecCap.placesRestantes > 0 && !poolArrivees.isEmpty()) {
            int capaciteRestante = vehiculeAvecCap.placesRestantes;

            // CORRECTION P1 : utilise la sélection exact > sup > inf sur le pool arrivé
            List<Reservation> candidatesReservations = new ArrayList<>();
            for (ReservationArrivante ra : poolArrivees)
                candidatesReservations.add(ra.reservation);
            Reservation meilleureRes = selectionnerMeilleureReservation(candidatesReservations, capaciteRestante);
            if (meilleureRes == null)
                break;

            // Retrouver le ReservationArrivante correspondant
            ReservationArrivante meilleur = null;
            for (ReservationArrivante ra : poolArrivees) {
                if (ra.reservation == meilleureRes) {
                    meilleur = ra;
                    break;
                }
            }
            if (meilleur == null)
                break;

            Reservation reservationOrigine = meilleur.reservation;
            int nbPassagers = reservationOrigine.getNombrePassage();
            int aAssigner = Math.min(nbPassagers, capaciteRestante);
            if (aAssigner <= 0)
                break;

            Reservation partie = copierReservation(reservationOrigine, aAssigner);
            vehiculeAvecCap.ajouterReservation(partie);
            heureDepartParReservation.put(partie, heureCourante);

            int reste = nbPassagers - aAssigner;
            if (reste <= 0) {
                if (meilleur.groupeOrigine != null) {
                    meilleur.groupeOrigine.remove(reservationOrigine);
                    recalculerHeureDepartBaseGroupe(meilleur.groupeOrigine, heureDepartParReservation);
                }
            } else {
                reservationOrigine.setNombrePassage(reste);
            }
            poolArrivees.remove(meilleur);
        }
    }

    // =========================================================================

    private static void assignerReservationsSurVehiculesAuDepart(
            List<Reservation> reservations,
            List<VehiculeAvecCapacite> vehicules,
            Timestamp heureDepart,
            Map<Reservation, Timestamp> heureDepartParReservation) {

        if (reservations == null || reservations.isEmpty() || vehicules == null || vehicules.isEmpty())
            return;

        reservations.sort((r1, r2) -> Integer.compare(r2.getNombrePassage(), r1.getNombrePassage()));

        long safety = 0;
        long safetyMax = Math.max(10_000L, (long) reservations.size() * (long) Math.max(1, vehicules.size()) * 200L);

        while (!reservations.isEmpty()) {
            safety++;
            if (safety > safetyMax) {
                simLog("ERREUR: garde-fou allocation simultanee (Regle6) declenche: reservations="
                        + reservations.size() + ", vehicules=" + vehicules.size() + ", depart=" + heureDepart);
                return;
            }
            int totalPlacesRestantes = 0;
            for (VehiculeAvecCapacite v : vehicules)
                totalPlacesRestantes += Math.max(0, v.placesRestantes);
            if (totalPlacesRestantes <= 0) {
                simLog("Regle6: allocation stoppee (plus de places disponibles). RestantReservations="
                        + reservations.size() + ", depart=" + heureDepart);
                return;
            }

            Reservation reservationOriginale = reservations.remove(0);
            int passagersRestants = reservationOriginale.getNombrePassage();
            int passagersAssignesSurCetteReservation = 0;

            while (passagersRestants > 0) {
                VehiculeAvecCapacite meilleurVehicule = null;
                for (VehiculeAvecCapacite vehiculeAvecCap : vehicules) {
                    if (vehiculeAvecCap.placesRestantes <= 0)
                        continue;
                    if (meilleurVehicule == null) {
                        meilleurVehicule = vehiculeAvecCap;
                        continue;
                    }

                    boolean candidatSuffisant = vehiculeAvecCap.placesRestantes >= passagersRestants;
                    boolean meilleurSuffisant = meilleurVehicule.placesRestantes >= passagersRestants;
                    if (candidatSuffisant && !meilleurSuffisant) {
                        meilleurVehicule = vehiculeAvecCap;
                        continue;
                    }
                    if (!candidatSuffisant && meilleurSuffisant) {
                        continue;
                    }

                    int diffCandidat = Math.abs(vehiculeAvecCap.placesRestantes - passagersRestants);
                    int diffMeilleur = Math.abs(meilleurVehicule.placesRestantes - passagersRestants);
                    if (diffCandidat < diffMeilleur) {
                        meilleurVehicule = vehiculeAvecCap;
                        continue;
                    }
                    if (diffCandidat > diffMeilleur) {
                        continue;
                    }

                    int trajetsCandidat = vehiculeAvecCap.trajetsOccupes.size();
                    int trajetsMeilleur = meilleurVehicule.trajetsOccupes.size();
                    if (trajetsCandidat < trajetsMeilleur) {
                        meilleurVehicule = vehiculeAvecCap;
                        continue;
                    }
                    if (trajetsCandidat > trajetsMeilleur) {
                        continue;
                    }

                    boolean candidatDiesel = vehiculeAvecCap.vehicule.getTypeCarburant() != null &&
                            CARBURANT_PRIORITAIRE.equals(vehiculeAvecCap.vehicule.getTypeCarburant().getReference());
                    boolean meilleurDiesel = meilleurVehicule.vehicule.getTypeCarburant() != null &&
                            CARBURANT_PRIORITAIRE.equals(meilleurVehicule.vehicule.getTypeCarburant().getReference());
                    if (candidatDiesel && !meilleurDiesel) {
                        meilleurVehicule = vehiculeAvecCap;
                        continue;
                    }
                    if (!candidatDiesel && meilleurDiesel) {
                        continue;
                    }

                    if (Math.random() < 0.5)
                        meilleurVehicule = vehiculeAvecCap;
                }

                if (meilleurVehicule == null)
                    break;
                int aAssigner = Math.min(meilleurVehicule.placesRestantes, passagersRestants);
                if (aAssigner <= 0)
                    break;

                Reservation partieReservation = copierReservation(reservationOriginale, aAssigner);
                meilleurVehicule.ajouterReservation(partieReservation);
                heureDepartParReservation.put(partieReservation, heureDepart);
                passagersRestants -= aAssigner;
                passagersAssignesSurCetteReservation += aAssigner;

                if (meilleurVehicule.placesRestantes > 0 && !reservations.isEmpty()) {
                    remplirVehiculeAvecAutresReservations(meilleurVehicule, reservations, heureDepart,
                            heureDepartParReservation);
                }
            }

            if (passagersRestants > 0) {
                Reservation reste = copierReservation(reservationOriginale, passagersRestants);
                if (passagersAssignesSurCetteReservation <= 0) {
                    reservations.add(0, reste);
                    simLog("Regle6: allocation stoppee (aucun progres). RestePassagers=" + passagersRestants
                            + ", restantReservations=" + reservations.size() + ", depart=" + heureDepart);
                    return;
                }
                reservations.add(reste);
                reservations.sort((r1, r2) -> Integer.compare(r2.getNombrePassage(), r1.getNombrePassage()));
            }
        }
    }

    private static int trouverIndexProchainGroupeNonVide(List<List<Reservation>> groupesDeDepart, int indexCourant) {
        for (int i = indexCourant + 1; i < groupesDeDepart.size(); i++) {
            List<Reservation> g = groupesDeDepart.get(i);
            if (g != null && !g.isEmpty())
                return i;
        }
        return -1;
    }

    private static void recalculerHeureDepartBaseGroupe(List<Reservation> groupe,
            Map<Reservation, Timestamp> heureDepartParReservation) {
        if (groupe == null || groupe.isEmpty())
            return;
        Timestamp base = groupe.get(groupe.size() - 1).getDateHeureArrive();
        for (Reservation r : groupe)
            heureDepartParReservation.put(r, base);
    }

    private static List<ReservationArrivante> collecterReservationsArrivantAvant(
            List<List<Reservation>> groupesDeDepart, int indexDebut, long limiteMs) {
        List<ReservationArrivante> result = new ArrayList<>();
        if (indexDebut < 0)
            return result;
        for (int i = indexDebut; i < groupesDeDepart.size(); i++) {
            List<Reservation> groupe = groupesDeDepart.get(i);
            if (groupe == null || groupe.isEmpty())
                continue;
            for (Reservation r : groupe) {
                if (r.getDateHeureArrive() != null && r.getDateHeureArrive().getTime() <= limiteMs) {
                    result.add(new ReservationArrivante(r, groupe));
                }
            }
        }
        result.sort((a, b) -> a.reservation.getDateHeureArrive().compareTo(b.reservation.getDateHeureArrive()));
        return result;
    }

    // =========================================================================
    // Point d'entrée public
    // =========================================================================

    public static ResultatSimulation simulerAssignation(
            List<Reservation> reservations, List<Vehicule> vehicules, Connection conn) throws SQLException {
        return simulerAssignation(reservations, vehicules, conn, 0);
    }

    public static ResultatSimulation simulerAssignation(
            List<Reservation> reservations,
            List<Vehicule> vehicules,
            Connection conn,
            int tempsAttenteMinutes) throws SQLException {

        final long startMs = System.currentTimeMillis();
        int totalPassagersInput = 0;
        if (reservations != null) {
            for (Reservation r : reservations) {
                if (r != null)
                    totalPassagersInput += Math.max(0, r.getNombrePassage());
            }
        }
        simLog("Debut simulation: reservations=" + (reservations != null ? reservations.size() : 0)
                + ", vehicules=" + (vehicules != null ? vehicules.size() : 0)
                + ", tempsAttenteMinutes=" + tempsAttenteMinutes
                + ", totalPassagers=" + totalPassagersInput);

        ResultatSimulation resultat = new ResultatSimulation();
        Map<Vehicule, List<Reservation>> vehiculesAvecReservations = new HashMap<>();
        List<VehiculeAvecCapacite> vehiculesDisponibles = new ArrayList<>();
        if (vehicules != null) {
            for (Vehicule v : vehicules)
                vehiculesDisponibles.add(new VehiculeAvecCapacite(v));
        }

        List<Reservation> reservationsNonAssignees = new ArrayList<>(reservations);
        List<Reservation> reservationsImpossiblesAAssigner = new ArrayList<>();

        reservationsNonAssignees.sort((r1, r2) -> r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive()));

        Map<Reservation, Timestamp> heureDepartParReservation = new HashMap<>();
        List<List<Reservation>> groupesDeDepart;

        if (tempsAttenteMinutes > 0) {
            // CORRECTION PROBLÈME 4 : fenêtre extensible (première arrivée + tempsAttente,
            // puis si une nouvelle arrivée dépasse la fin courante, la fenêtre est
            // prolongée)
            groupesDeDepart = regroupeParTempsAttente(reservationsNonAssignees, tempsAttenteMinutes);
            simLog("Groupes construits: " + groupesDeDepart.size() + " (regroupement=" + tempsAttenteMinutes + "mn)",
                    startMs);
            for (List<Reservation> groupe : groupesDeDepart) {
                Timestamp heureDepartGroupe = groupe.get(groupe.size() - 1).getDateHeureArrive();
                for (Reservation r : groupe)
                    heureDepartParReservation.put(r, heureDepartGroupe);
            }
        } else {
            groupesDeDepart = new ArrayList<>();
            for (Reservation r : reservationsNonAssignees) {
                heureDepartParReservation.put(r, r.getDateHeureArrive());
                List<Reservation> sg = new ArrayList<>();
                sg.add(r);
                groupesDeDepart.add(sg);
            }
            groupesDeDepart = fusionnerGroupesMemeHeure(groupesDeDepart);
            simLog("Groupes construits: " + groupesDeDepart.size() + " (sans regroupement)", startMs);
        }

        Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule = new HashMap<>();
        List<Reservation> reservationsReportees = new ArrayList<>();

        final long MAX_ITERATIONS_GLOBAL = Math.max(10_000L,
                (long) Math.max(1, totalPassagersInput)
                        * (long) Math.max(2, vehicules != null ? vehicules.size() : 1) * 20L);
        long iterationGlobal = 0L;

        for (int indexGroupe = 0; indexGroupe < groupesDeDepart.size(); indexGroupe++) {
            List<Reservation> groupe = groupesDeDepart.get(indexGroupe);
            if (groupe == null || groupe.isEmpty())
                continue;

            int indexProchainGroupeNonVide = trouverIndexProchainGroupeNonVide(groupesDeDepart, indexGroupe);
            boolean estDernierGroupe = (indexProchainGroupeNonVide == -1);

            Timestamp heureDepartGroupe = heureDepartParReservation.get(groupe.get(0));
            if (heureDepartGroupe == null) {
                heureDepartGroupe = groupe.get(groupe.size() - 1).getDateHeureArrive();
                for (Reservation r : groupe)
                    heureDepartParReservation.put(r, heureDepartGroupe);
            }

            if (tempsAttenteMinutes > 0 && !groupe.isEmpty()) {
                Timestamp debutTranche = groupe.get(0).getDateHeureArrive();
                long debutMs = debutTranche.getTime();
                long finMs = debutMs + tempsAttenteMinutes * 60L * 1000L;
                long departMs = heureDepartGroupe.getTime();

                for (VehiculeAvecCapacite vac : vehiculesDisponibles) {
                    long candidateMs = debutMs;

                    if (vac.vehicule.getHeureDisponibilite() != null) {
                        long millisParJour = 24L * 60L * 60L * 1000L;
                        long baseJour = debutMs - (debutMs % millisParJour);
                        long dispoJour = vac.vehicule.getHeureDisponibilite().getTime() % millisParJour;
                        long dispoMs = baseJour + dispoJour;
                        if (dispoMs > candidateMs)
                            candidateMs = dispoMs;
                    }

                    long dernierRetour = -1L;
                    for (long[] trajet : vac.trajetsOccupes) {
                        if (trajet[1] > dernierRetour)
                            dernierRetour = trajet[1];
                    }
                    if (dernierRetour > 0 && dernierRetour > candidateMs)
                        candidateMs = dernierRetour;

                    // CORRECTION PROBLÈME 2 : un véhicule dont le retour est après finMs
                    // est exclu du groupe courant (il sera traité via la règle 6 ou le groupe
                    // suivant).
                    // Seuls les véhicules disponibles DANS la tranche [debutMs, finMs] sont pris en
                    // compte.
                    if (candidateMs >= debutMs && candidateMs <= finMs) {
                        if (candidateMs > departMs)
                            departMs = candidateMs;
                    }
                    // Si candidateMs > finMs : véhicule non disponible dans cette tranche → ignoré
                    // ici.
                }

                heureDepartGroupe = new Timestamp(departMs);
                for (Reservation r : groupe)
                    heureDepartParReservation.put(r, heureDepartGroupe);
            }

            List<Reservation> reservationsGroupe = new ArrayList<>(reservationsReportees);
            int nbReporteesAjoutees = reservationsGroupe.size();
            for (Reservation r : reservationsReportees)
                heureDepartParReservation.put(r, heureDepartGroupe);
            reservationsReportees.clear();
            reservationsGroupe.addAll(groupe);
            reservationsGroupe.sort((r1, r2) -> Integer.compare(r2.getNombrePassage(), r1.getNombrePassage()));

            simLog("Traitement groupe " + (indexGroupe + 1) + "/" + groupesDeDepart.size()
                    + ": total=" + reservationsGroupe.size()
                    + " (groupe=" + groupe.size() + ", reportees=" + nbReporteesAjoutees + ")"
                    + ", heureDepart=" + heureDepartGroupe, startMs);

            long iterationGroupe = 0L;

            while (!reservationsGroupe.isEmpty()) {
                iterationGlobal++;
                iterationGroupe++;
                if (iterationGlobal > MAX_ITERATIONS_GLOBAL) {
                    simLog("ERREUR: garde-fou declenche (iterationGlobal=" + iterationGlobal
                            + ", max=" + MAX_ITERATIONS_GLOBAL + ") groupe=" + (indexGroupe + 1)
                            + ", restantGroupe=" + reservationsGroupe.size()
                            + ", reportees=" + reservationsReportees.size(), startMs);
                    throw new IllegalStateException(
                            "Simulation bloquee (garde-fou): trop d'iterations. Voir logs [SIM].");
                }
                if (iterationGroupe % 200 == 0) {
                    simLog("Progress groupe " + (indexGroupe + 1)
                            + ": restant=" + reservationsGroupe.size()
                            + ", reportees=" + reservationsReportees.size(), startMs);
                }

                Reservation reservationOriginale = reservationsGroupe.remove(0);
                int passagersRestants = reservationOriginale.getNombrePassage();

                while (passagersRestants > 0) {
                    iterationGlobal++;
                    if (iterationGlobal > MAX_ITERATIONS_GLOBAL) {
                        simLog("ERREUR: garde-fou declenche dans boucle d'assignation (passagersRestants="
                                + passagersRestants + ", groupe=" + (indexGroupe + 1) + ")", startMs);
                        throw new IllegalStateException(
                                "Simulation bloquee (garde-fou): boucle d'assignation. Voir logs [SIM].");
                    }

                    VehiculeAvecCapacite meilleurVehicule = null;

                    for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                        if (!vehiculeAvecCap.estDisponibleA(heureDepartGroupe))
                            continue;

                        boolean memeGroupe = vehiculeAvecCap.reservations.isEmpty() ||
                                heureDepartParReservation.get(vehiculeAvecCap.reservations.get(0))
                                        .equals(heureDepartGroupe);
                        if (!memeGroupe || vehiculeAvecCap.placesRestantes <= 0)
                            continue;

                        if (meilleurVehicule == null) {
                            meilleurVehicule = vehiculeAvecCap;
                            continue;
                        }

                        boolean candidatSuffisant = vehiculeAvecCap.placesRestantes >= passagersRestants;
                        boolean meilleurSuffisant = meilleurVehicule.placesRestantes >= passagersRestants;
                        if (candidatSuffisant && !meilleurSuffisant) {
                            meilleurVehicule = vehiculeAvecCap;
                            continue;
                        }
                        if (!candidatSuffisant && meilleurSuffisant) {
                            continue;
                        }

                        int diffCandidat = Math.abs(vehiculeAvecCap.placesRestantes - passagersRestants);
                        int diffMeilleur = Math.abs(meilleurVehicule.placesRestantes - passagersRestants);
                        if (diffCandidat < diffMeilleur) {
                            meilleurVehicule = vehiculeAvecCap;
                            continue;
                        }
                        if (diffCandidat > diffMeilleur) {
                            continue;
                        }

                        int trajetsCandidat = vehiculeAvecCap.trajetsOccupes.size();
                        int trajetsMeilleur = meilleurVehicule.trajetsOccupes.size();
                        if (trajetsCandidat < trajetsMeilleur) {
                            meilleurVehicule = vehiculeAvecCap;
                            continue;
                        }
                        if (trajetsCandidat > trajetsMeilleur) {
                            continue;
                        }

                        boolean candidatDiesel = vehiculeAvecCap.vehicule.getTypeCarburant() != null &&
                                CARBURANT_PRIORITAIRE
                                        .equals(vehiculeAvecCap.vehicule.getTypeCarburant().getReference());
                        boolean meilleurDiesel = meilleurVehicule.vehicule.getTypeCarburant() != null &&
                                CARBURANT_PRIORITAIRE
                                        .equals(meilleurVehicule.vehicule.getTypeCarburant().getReference());
                        if (candidatDiesel && !meilleurDiesel) {
                            meilleurVehicule = vehiculeAvecCap;
                            continue;
                        }
                        if (!candidatDiesel && meilleurDiesel) {
                            continue;
                        }

                        if (Math.random() < 0.5)
                            meilleurVehicule = vehiculeAvecCap;
                    }

                    if (meilleurVehicule == null)
                        break;
                    int aAssigner = Math.min(meilleurVehicule.placesRestantes, passagersRestants);
                    if (aAssigner <= 0)
                        break;

                    Reservation partieReservation = copierReservation(reservationOriginale, aAssigner);
                    meilleurVehicule.ajouterReservation(partieReservation);
                    heureDepartParReservation.put(partieReservation, heureDepartGroupe);
                    passagersRestants -= aAssigner;

                    if (meilleurVehicule.placesRestantes > 0 && !reservationsGroupe.isEmpty()) {
                        remplirVehiculeAvecAutresReservations(meilleurVehicule, reservationsGroupe,
                                heureDepartGroupe, heureDepartParReservation);
                    }
                }

                if (passagersRestants > 0) {
                    Reservation reste = copierReservation(reservationOriginale, passagersRestants);
                    if (estDernierGroupe)
                        reservationsImpossiblesAAssigner.add(reste);
                    else
                        reservationsReportees.add(reste);
                }
            }

            // Calcul des horaires après chaque groupe
            for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                if (!vehiculeAvecCap.reservations.isEmpty() && vehiculeAvecCap.heureDepart == null) {
                    calculerHoraires(vehiculeAvecCap, conn, heureDepartGroupe);
                    if (vehiculeAvecCap.heureDepart != null && vehiculeAvecCap.heureRetour != null) {
                        List<Reservation> existantes = vehiculesAvecReservations
                                .computeIfAbsent(vehiculeAvecCap.vehicule, k -> new ArrayList<>());
                        existantes.addAll(vehiculeAvecCap.reservations);
                        List<InfosTrajet> trajets = infosTrajetParVehicule.computeIfAbsent(vehiculeAvecCap.vehicule,
                                k -> new ArrayList<>());
                        trajets.add(new InfosTrajet(vehiculeAvecCap.heureDepart, vehiculeAvecCap.heureRetour,
                                vehiculeAvecCap.dureeTrajetMinutes, vehiculeAvecCap.segments));
                        vehiculeAvecCap.reinitialiserPourNouveauTrajet();
                    }
                }
            }

            // RÈGLE 6
            if (!reservationsReportees.isEmpty() && !estDernierGroupe && tempsAttenteMinutes > 0) {
                List<Reservation> prochainGroupe = groupesDeDepart.get(indexProchainGroupeNonVide);
                if (prochainGroupe != null && !prochainGroupe.isEmpty()) {
                    Timestamp debutProchainGroupe = prochainGroupe.get(0).getDateHeureArrive();
                    long debutProchainMs = debutProchainGroupe.getTime();

                    List<RetourVehicule> retours = new ArrayList<>();
                    for (VehiculeAvecCapacite vac : vehiculesDisponibles) {
                        long dernierRetour = -1L;
                        for (long[] trajet : vac.trajetsOccupes) {
                            if (trajet[1] > dernierRetour)
                                dernierRetour = trajet[1];
                        }
                        // CORRECTION PROBLÈME 2 : seuls les véhicules revenant AVANT le prochain groupe
                        // participent à la règle 6 ; ceux revenant après seront disponibles pour le
                        // groupe suivant.
                        if (dernierRetour > 0 && dernierRetour < debutProchainMs) {
                            retours.add(new RetourVehicule(vac, dernierRetour));
                        }
                    }
                    retours.sort((a, b) -> Long.compare(a.retourMs, b.retourMs));

                    if (!retours.isEmpty()) {
                        simLog("Regle6: reportees=" + reservationsReportees.size()
                                + ", retoursAvantProchainGroupe=" + retours.size()
                                + ", debutProchainGroupe=" + debutProchainGroupe, startMs);
                    }

                    for (int idxRetour = 0; idxRetour < retours.size(); idxRetour++) {
                        long retourMs = retours.get(idxRetour).retourMs;
                        Timestamp heureDisponible = new Timestamp(retourMs);
                        long finFenetreMs = heureDisponible.getTime() + tempsAttenteMinutes * 60L * 1000L;

                        // Regrouper les véhicules revenant exactement au même instant
                        List<VehiculeAvecCapacite> vehiculesRevenantEnMemeTemps = new ArrayList<>();
                        while (idxRetour < retours.size() && retours.get(idxRetour).retourMs == retourMs) {
                            VehiculeAvecCapacite vac = retours.get(idxRetour).vehicule;
                            vac.placesRestantes = vac.vehicule.getNombrePlaces();
                            vac.reservations = new ArrayList<>();
                            vac.heureDepart = null;
                            vac.heureRetour = null;
                            vac.dureeTrajetMinutes = 0;
                            vac.segments = new ArrayList<>();
                            vehiculesRevenantEnMemeTemps.add(vac);
                            idxRetour++;
                        }
                        idxRetour--;

                        if (!vehiculesRevenantEnMemeTemps.isEmpty()) {
                            simLog("Regle6: retour=" + heureDisponible
                                    + ", fenetreFin=" + new Timestamp(finFenetreMs)
                                    + ", vehicules=" + vehiculesRevenantEnMemeTemps.size()
                                    + ", reporteesAvant=" + reservationsReportees.size(), startMs);
                        }

                        if (!reservationsReportees.isEmpty() && !vehiculesRevenantEnMemeTemps.isEmpty()) {
                            simLog("Regle6: allocation simultanee START (reportees=" + reservationsReportees.size()
                                    + ", vehicules=" + vehiculesRevenantEnMemeTemps.size()
                                    + ", retour=" + heureDisponible + ")", startMs);
                            assignerReservationsSurVehiculesAuDepart(reservationsReportees,
                                    vehiculesRevenantEnMemeTemps, heureDisponible, heureDepartParReservation);
                            simLog("Regle6: reporteesApresAllocation=" + reservationsReportees.size(), startMs);
                        }

                        // === Fenêtre de remplissage au niveau du lot de retours ===
                        // Idée: si un véhicule contient des non-assignés et qu'il lui manque des
                        // places,
                        // il doit être rempli automatiquement avec les réservations qui arrivent dans
                        // la fenêtre.
                        // Les véhicules « vides » ne doivent pas consommer ces arrivées avant ceux déjà
                        // chargés.
                        List<VehiculeAvecCapacite> vehiculesAvecNonAssignes = new ArrayList<>();
                        for (VehiculeAvecCapacite v : vehiculesRevenantEnMemeTemps) {
                            if (v != null && v.reservations != null && !v.reservations.isEmpty()) {
                                vehiculesAvecNonAssignes.add(v);
                            }
                        }

                        if (!vehiculesAvecNonAssignes.isEmpty()) {
                            // Stabiliser l'ordre (déterministe) sans utiliser la capacité.
                            vehiculesAvecNonAssignes.sort((a, b) -> {
                                String ra = a.vehicule != null ? a.vehicule.getReference() : null;
                                String rb = b.vehicule != null ? b.vehicule.getReference() : null;
                                if (ra == null && rb == null) {
                                    int ida = a.vehicule != null ? a.vehicule.getId() : 0;
                                    int idb = b.vehicule != null ? b.vehicule.getId() : 0;
                                    return Integer.compare(ida, idb);
                                }
                                if (ra == null)
                                    return 1;
                                if (rb == null)
                                    return -1;
                                return ra.compareToIgnoreCase(rb);
                            });

                            List<ReservationArrivante> fluxArrivants = collecterReservationsArrivantAvant(
                                    groupesDeDepart, indexProchainGroupeNonVide, finFenetreMs);
                            List<ReservationArrivante> poolArrivees = new ArrayList<>();
                            Timestamp derniereArriveeDansFenetre = null;

                            // Marquer ceux déjà pleins au retour (départ immédiat à heureDisponible)
                            Map<VehiculeAvecCapacite, Timestamp> departEffectif = new HashMap<>();
                            List<VehiculeAvecCapacite> vehiculesActifs = new ArrayList<>();
                            for (VehiculeAvecCapacite v : vehiculesAvecNonAssignes) {
                                if (v.placesRestantes <= 0) {
                                    departEffectif.put(v, heureDisponible);
                                } else {
                                    vehiculesActifs.add(v);
                                }
                            }

                            boolean auMoinsUneArriveeDansFenetre = false;
                            for (ReservationArrivante arrivante : fluxArrivants) {
                                Timestamp tArr = arrivante.reservation.getDateHeureArrive();
                                if (tArr == null)
                                    continue;
                                if (tArr.getTime() >= heureDisponible.getTime() && tArr.getTime() <= finFenetreMs) {
                                    auMoinsUneArriveeDansFenetre = true;
                                    break;
                                }
                            }

                            if (!auMoinsUneArriveeDansFenetre) {
                                // Aucune arrivée → tous les véhicules non-pleins partent au retour
                                for (VehiculeAvecCapacite v : vehiculesActifs) {
                                    departEffectif.put(v, heureDisponible);
                                }
                            } else {
                                // Traitement chronologique des arrivées. À chaque arrivée, on tente de remplir
                                // automatiquement les véhicules portant déjà des non-assignés.
                                for (ReservationArrivante arrivante : fluxArrivants) {
                                    Timestamp tArr = arrivante.reservation.getDateHeureArrive();
                                    if (tArr == null)
                                        continue;
                                    if (tArr.getTime() < heureDisponible.getTime() || tArr.getTime() > finFenetreMs)
                                        continue;

                                    derniereArriveeDansFenetre = tArr;
                                    poolArrivees.add(arrivante);

                                    // Remplir chaque véhicule actif à cette heure (sans déplacer les non-assignés
                                    // déjà à bord ; on ne fait que compléter avec les arrivées/pool).
                                    for (VehiculeAvecCapacite v : vehiculesActifs) {
                                        if (v.placesRestantes <= 0)
                                            continue;

                                        // Toujours prioriser les non-assignés restants (s'il en reste encore)
                                        if (v.placesRestantes > 0 && !reservationsReportees.isEmpty()) {
                                            remplirVehiculeAvecAutresReservations(v, reservationsReportees, tArr,
                                                    heureDepartParReservation);
                                        }

                                        if (v.placesRestantes > 0 && !poolArrivees.isEmpty()) {
                                            remplirVehiculeAvecReservationsArrivees(v, poolArrivees, tArr,
                                                    heureDepartParReservation);
                                        }

                                        if (v.placesRestantes <= 0 && !departEffectif.containsKey(v)) {
                                            // Le véhicule devient plein à cette arrivée → départ immédiat
                                            departEffectif.put(v, tArr);
                                        }
                                    }
                                }

                                // Ceux qui ne sont pas devenus pleins partent à la DERNIÈRE arrivée
                                // (et non à finFenetre).
                                for (VehiculeAvecCapacite v : vehiculesActifs) {
                                    if (!departEffectif.containsKey(v)) {
                                        departEffectif.put(v,
                                                derniereArriveeDansFenetre != null ? derniereArriveeDansFenetre
                                                        : new Timestamp(finFenetreMs));
                                    }
                                }
                            }

                            // Finaliser les trajets intermédiaires pour chaque véhicule ayant des
                            // non-assignés.
                            for (VehiculeAvecCapacite v : vehiculesAvecNonAssignes) {
                                Timestamp hDepEff = departEffectif.getOrDefault(v, heureDisponible);

                                for (Reservation r : v.reservations) {
                                    heureDepartParReservation.put(r, hDepEff);
                                }

                                simLog("Regle6: departIntermediaire vehicule="
                                        + (v.vehicule.getReference() != null ? v.vehicule.getReference()
                                                : ("#" + v.vehicule.getId()))
                                        + ", depart=" + hDepEff
                                        + ", reservations=" + v.reservations.size()
                                        + ", placesRestantes=" + v.placesRestantes, startMs);

                                calculerHoraires(v, conn, hDepEff);

                                if (v.heureRetour != null) {
                                    simLog("Regle6: retourIntermediaire vehicule="
                                            + (v.vehicule.getReference() != null ? v.vehicule.getReference()
                                                    : ("#" + v.vehicule.getId()))
                                            + ", retour=" + v.heureRetour, startMs);
                                }

                                if (v.heureDepart != null && v.heureRetour != null) {
                                    List<Reservation> existantes = vehiculesAvecReservations.computeIfAbsent(v.vehicule,
                                            k -> new ArrayList<>());
                                    existantes.addAll(v.reservations);
                                    List<InfosTrajet> trajets = infosTrajetParVehicule.computeIfAbsent(v.vehicule,
                                            k -> new ArrayList<>());
                                    trajets.add(new InfosTrajet(v.heureDepart, v.heureRetour, v.dureeTrajetMinutes,
                                            v.segments));
                                    v.reinitialiserPourNouveauTrajet();
                                }
                            }
                        }

                        if (reservationsReportees.isEmpty())
                            break;
                    }
                }
            }
        }

        if (!reservationsReportees.isEmpty()) {
            reservationsImpossiblesAAssigner.addAll(reservationsReportees);
            reservationsReportees.clear();
        }

        if (vehicules != null) {
            for (Vehicule v : vehicules) {
                vehiculesAvecReservations.computeIfAbsent(v, k -> new ArrayList<>());
            }
        }

        resultat.setVehiculesAvecReservations(vehiculesAvecReservations);
        resultat.setReservationsNonAssignees(reservationsImpossiblesAAssigner);
        resultat.setInfosTrajetParVehicule(infosTrajetParVehicule);
        resultat.setHeureDepartParReservation(heureDepartParReservation);

        long vehiculesUtilises = vehiculesAvecReservations.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty()).count();
        int lignesAssignees = vehiculesAvecReservations.values().stream()
                .mapToInt(v -> v != null ? v.size() : 0).sum();
        simLog("Fin simulation: vehiculesUtilises=" + vehiculesUtilises
                + ", lignesAssignees=" + lignesAssignees
                + ", nonAssignees=" + reservationsImpossiblesAAssigner.size(), startMs);

        return resultat;
    }

    // =========================================================================
    // CORRECTION PROBLÈME 4 — fenêtre extensible
    // La fenêtre commence à la 1ère arrivée. Si une nouvelle réservation arrive
    // après la fin courante, la fenêtre est prolongée de tempsAttenteMinutes
    // depuis cette nouvelle arrivée (et ainsi de suite).
    // =========================================================================
    private static List<List<Reservation>> regroupeParTempsAttente(
            List<Reservation> reservationsTriees, int tempsAttenteMinutes) {
        List<List<Reservation>> groupes = new ArrayList<>();
        if (reservationsTriees.isEmpty())
            return groupes;

        List<Reservation> groupeActuel = new ArrayList<>();
        long finFenetreMs = -1L;

        for (Reservation r : reservationsTriees) {
            long arrMs = r.getDateHeureArrive().getTime();
            if (finFenetreMs < 0) {
                // Première réservation : ouvre la fenêtre
                finFenetreMs = arrMs + tempsAttenteMinutes * 60L * 1000L;
                groupeActuel.add(r);
            } else if (arrMs <= finFenetreMs) {
                // Dans la fenêtre courante : on prolonge si cette arrivée repousse la fin
                long nouvelleFin = arrMs + tempsAttenteMinutes * 60L * 1000L;
                if (nouvelleFin > finFenetreMs)
                    finFenetreMs = nouvelleFin;
                groupeActuel.add(r);
            } else {
                // Hors fenêtre : fermer le groupe actuel, en ouvrir un nouveau
                groupes.add(groupeActuel);
                groupeActuel = new ArrayList<>();
                groupeActuel.add(r);
                finFenetreMs = arrMs + tempsAttenteMinutes * 60L * 1000L;
            }
        }

        if (!groupeActuel.isEmpty())
            groupes.add(groupeActuel);
        return groupes;
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static List<List<Reservation>> fusionnerGroupesMemeHeure(List<List<Reservation>> groupes) {
        if (groupes.size() <= 1)
            return groupes;
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
        for (Long heure : ordreHeures)
            resultat.add(parHeure.get(heure));
        return resultat;
    }

    private static void calculerHoraires(VehiculeAvecCapacite vehiculeAvecCap, Connection conn,
            Timestamp heureDepartGroupe) throws SQLException {
        List<Reservation> reservations = vehiculeAvecCap.reservations;
        Vehicule vehicule = vehiculeAvecCap.vehicule;
        if (reservations.isEmpty())
            return;

        Timestamp heureArrivee = heureDepartGroupe;
        vehiculeAvecCap.segments = new ArrayList<>();

        Map<Integer, String> hotelsMap = new HashMap<>();
        for (Reservation r : reservations) {
            if (!hotelsMap.containsKey(r.getIdHotel()))
                hotelsMap.put(r.getIdHotel(), r.getHotel().getNom());
        }

        List<Integer> ordreHotels = optimiserOrdreHotels(conn, new ArrayList<>(hotelsMap.keySet()), hotelsMap);
        int vitesseMoyenne = vehicule.getVitesseMoyenne();
        int aeroportId = getAeroportId(conn);

        if (!ordreHotels.isEmpty()) {
            int premierHotel = ordreHotels.get(0);
            String nomPremierHotel = hotelsMap.get(premierHotel);
            BigDecimal distance = getDistance(conn, null, aeroportId, premierHotel);
            if (distance == null)
                distance = new BigDecimal("15.0");
            if (vitesseMoyenne > 0) {
                vehiculeAvecCap.segments.add(new SegmentTrajet("Aéroport Ivato", nomPremierHotel, distance,
                        calculerDureeMinutes(distance, vitesseMoyenne)));
            }
        }

        for (int i = 0; i < ordreHotels.size() - 1; i++) {
            int hDep = ordreHotels.get(i), hArr = ordreHotels.get(i + 1);
            String nomDep = hotelsMap.get(hDep), nomArr = hotelsMap.get(hArr);
            BigDecimal distance = getDistance(conn, hDep, null, hArr);
            if (distance == null)
                distance = new BigDecimal("3.0");
            if (vitesseMoyenne > 0) {
                vehiculeAvecCap.segments.add(
                        new SegmentTrajet(nomDep, nomArr, distance, calculerDureeMinutes(distance, vitesseMoyenne)));
            }
        }

        if (!ordreHotels.isEmpty()) {
            int dernierHotel = ordreHotels.get(ordreHotels.size() - 1);
            String nomDernierHotel = hotelsMap.get(dernierHotel);
            BigDecimal distanceRetour = getDistance(conn, null, aeroportId, dernierHotel);
            if (distanceRetour == null)
                distanceRetour = new BigDecimal("15.0");
            if (vitesseMoyenne > 0) {
                vehiculeAvecCap.segments.add(new SegmentTrajet(nomDernierHotel, "Aéroport Ivato", distanceRetour,
                        calculerDureeMinutes(distanceRetour, vitesseMoyenne)));
            }
        }

        if (vitesseMoyenne > 0 && !vehiculeAvecCap.segments.isEmpty()) {
            int tempsTrajetTotal = 0;
            for (SegmentTrajet s : vehiculeAvecCap.segments)
                tempsTrajetTotal += s.getDureeMinutes();
            vehiculeAvecCap.heureDepart = heureArrivee;
            vehiculeAvecCap.heureRetour = new Timestamp(heureArrivee.getTime() + (tempsTrajetTotal * 60L * 1000L));
            vehiculeAvecCap.dureeTrajetMinutes = tempsTrajetTotal;
        }
    }

    private static List<Integer> optimiserOrdreHotels(Connection conn, List<Integer> hotelIds,
            Map<Integer, String> hotelsMap) throws SQLException {
        if (hotelIds.size() <= 1)
            return hotelIds;
        List<Integer> ordreOptimal = new ArrayList<>();
        List<Integer> restants = new ArrayList<>(hotelIds);
        int aeroportId = getAeroportId(conn);
        int positionActuelle = -1;

        while (!restants.isEmpty()) {
            int meilleurHotel = -1;
            BigDecimal meilleureDistance = null;
            String meilleurNom = null;
            for (int hotelId : restants) {
                BigDecimal distance = (positionActuelle == -1)
                        ? getDistance(conn, null, aeroportId, hotelId)
                        : getDistance(conn, positionActuelle, null, hotelId);
                if (distance != null) {
                    if (meilleureDistance == null || distance.compareTo(meilleureDistance) < 0 ||
                            (distance.compareTo(meilleureDistance) == 0 &&
                                    hotelsMap.get(hotelId).compareToIgnoreCase(meilleurNom) < 0)) {
                        meilleureDistance = distance;
                        meilleurHotel = hotelId;
                        meilleurNom = hotelsMap.get(hotelId);
                    }
                } else if (meilleureDistance == null) {
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
                restants.sort((a, b) -> hotelsMap.get(a).compareToIgnoreCase(hotelsMap.get(b)));
                ordreOptimal.addAll(restants);
                break;
            }
        }
        return ordreOptimal;
    }

    private static int calculerDureeMinutes(BigDecimal distanceKm, int vitesseMoyenneKmH) {
        if (vitesseMoyenneKmH <= 0)
            return 0;
        return distanceKm.multiply(new BigDecimal(60))
                .divide(new BigDecimal(vitesseMoyenneKmH), 0, RoundingMode.HALF_UP).intValue();
    }

    private static Reservation copierReservation(Reservation source, int nombrePassagers) {
        Reservation copie = new Reservation();
        copie.setId(source.getId());
        copie.setIdClient(source.getIdClient());
        copie.setIdHotel(source.getIdHotel());
        copie.setNombrePassage(nombrePassagers);
        copie.setDateHeureArrive(source.getDateHeureArrive());
        copie.setClient(source.getClient());
        copie.setHotel(source.getHotel());
        copie.setIdVehicule(source.getIdVehicule());
        copie.setVehicule(source.getVehicule());
        return copie;
    }

    private static BigDecimal getDistance(Connection conn, Integer idFromHotel, Integer idFromAeroport, int idTo)
            throws SQLException {
        if (idFromHotel == null && idFromAeroport == null)
            return null;

        if (idFromHotel != null) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valeur FROM distance WHERE id_from_hotel = ? AND id_from_aeroport IS NULL AND id_to = ?")) {
                stmt.setInt(1, idFromHotel);
                stmt.setInt(2, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getBigDecimal("valeur");
                }
            }
        } else {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valeur FROM distance WHERE id_from_aeroport = ? AND id_from_hotel IS NULL AND id_to = ?")) {
                stmt.setInt(1, idFromAeroport);
                stmt.setInt(2, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getBigDecimal("valeur");
                }
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valeur FROM distance WHERE id_from_hotel IS NULL AND id_from_aeroport IS NOT NULL AND id_to = ? ORDER BY id LIMIT 1")) {
                stmt.setInt(1, idTo);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getBigDecimal("valeur");
                }
            }
        }

        if (idFromHotel != null) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valeur FROM distance WHERE id_from_hotel = ? AND id_from_aeroport IS NULL AND id_to = ?")) {
                stmt.setInt(1, idTo);
                stmt.setInt(2, idFromHotel);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next())
                        return rs.getBigDecimal("valeur");
                }
            }
        }
        return null;
    }

    private static int getAeroportId(Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM aeroport ORDER BY id ASC LIMIT 1");
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                return rs.getInt("id");
        }
        return 1;
    }
}