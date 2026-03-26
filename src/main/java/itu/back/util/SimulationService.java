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

    /**
     * Classe pour encapsuler les résultats de la simulation
     */
    public static class ResultatSimulation {
        private Map<Vehicule, List<Reservation>> vehiculesAvecReservations;
        private List<Reservation> reservationsNonAssignees;
        private Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule;
        // Heure de depart par "ligne" de reservation dans la simulation
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

        public Map<Reservation, Timestamp> getHeureDepartParReservation() {
            return heureDepartParReservation;
        }

        public void setHeureDepartParReservation(Map<Reservation, Timestamp> heureDepartParReservation) {
            this.heureDepartParReservation = heureDepartParReservation;
        }

        /**
         * Log texte simple pour faciliter la comparaison des résultats de simulation
         * sans passer par l'interface.
         */
        public void logResultats(String contexte) {
            System.out.println("===== RESULTAT SIMULATION" + (contexte != null ? " - " + contexte : "") + " =====");

            // Résumé global
            int totalReservations = 0;
            int totalPassagers = 0;
            for (Map.Entry<Vehicule, List<Reservation>> e : vehiculesAvecReservations.entrySet()) {
                for (Reservation r : e.getValue()) {
                    totalReservations++;
                    totalPassagers += r.getNombrePassage();
                }
            }
            System.out.println("Vehicules utilises : " + vehiculesAvecReservations.entrySet().stream()
                    .filter(en -> !en.getValue().isEmpty()).count());
            System.out.println("Lignes de reservation assignees : " + totalReservations + " (" + totalPassagers
                    + " passagers)");
            System.out.println("Reservations non assignees : " + reservationsNonAssignees.size());

            // Détail par véhicule
            for (Map.Entry<Vehicule, List<Reservation>> entry : vehiculesAvecReservations.entrySet()) {
                Vehicule v = entry.getKey();
                List<Reservation> resVehicule = entry.getValue();
                if (resVehicule.isEmpty()) {
                    continue;
                }
                System.out.println();
                System.out.println("Vehicule " + (v.getReference() != null ? v.getReference() : ("#" + v.getId()))
                        + " (" + v.getNombrePlaces() + " places)");

                for (Reservation r : resVehicule) {
                    Timestamp hDep = heureDepartParReservation.get(r);
                    String heureStr = (hDep != null ? hDep.toString() : "?");
                    String client = (r.getClient() != null ? r.getClient().getNom() : ("client#" + r.getIdClient()));
                    String hotel = (r.getHotel() != null ? r.getHotel().getNom() : ("hotel#" + r.getIdHotel()));
                    System.out.println("  - " + client + " -> " + hotel + " : " + r.getNombrePassage()
                            + " passagers, depart=" + heureStr);
                }
            }

            // Détail des non assignées
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

    /**
     * Remplit un véhicule qui a encore des places libres en cherchant, parmi les
     * réservations restantes du groupe, celles dont le nombre de passagers est le
     * plus proche de la capacité restante du véhicule.
     * La réservation choisie peut être prise en totalité ou partiellement ; le
     * reste éventuel est remis dans la liste pour être traité plus tard.
     */
    private static void remplirVehiculeAvecAutresReservations(
            VehiculeAvecCapacite vehiculeAvecCap,
            List<Reservation> reservationsGroupe,
            Timestamp heureDepartGroupe,
            Map<Reservation, Timestamp> heureDepartParReservation) {

        while (vehiculeAvecCap.placesRestantes > 0 && !reservationsGroupe.isEmpty()) {
            int capaciteRestante = vehiculeAvecCap.placesRestantes;

            // Chercher la réservation dont le nombre de passagers est le plus proche de
            // la capacité restante du véhicule
            Reservation meilleureReservation = null;
            int meilleureDiff = Integer.MAX_VALUE;

            for (Reservation candidate : reservationsGroupe) {
                int nb = candidate.getNombrePassage();
                int diff = Math.abs(nb - capaciteRestante);
                if (diff < meilleureDiff) {
                    meilleureDiff = diff;
                    meilleureReservation = candidate;
                } else if (diff == meilleureDiff && meilleureReservation != null) {
                    // En cas d'égalité, on peut privilégier la réservation avec plus de
                    // passagers pour mieux remplir le véhicule
                    if (candidate.getNombrePassage() > meilleureReservation.getNombrePassage()) {
                        meilleureReservation = candidate;
                    }
                }
            }

            if (meilleureReservation == null) {
                break;
            }

            int nbPassagers = meilleureReservation.getNombrePassage();
            int aAssigner = Math.min(nbPassagers, capaciteRestante);
            if (aAssigner <= 0) {
                break;
            }

            // Créer une partie de cette réservation pour ce véhicule
            Reservation partie = copierReservation(meilleureReservation, aAssigner);
            vehiculeAvecCap.ajouterReservation(partie);
            heureDepartParReservation.put(partie, heureDepartGroupe);

            // Mettre à jour la réservation restante dans la liste du groupe
            reservationsGroupe.remove(meilleureReservation);
            int reste = nbPassagers - aAssigner;
            if (reste > 0) {
                Reservation resteReservation = copierReservation(meilleureReservation, reste);
                // On remet le reste dans la liste pour être traité plus tard
                reservationsGroupe.add(resteReservation);
            }
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
         * et si son heure de disponibilité initiale est atteinte.
         */
        public boolean estDisponibleA(Timestamp heure) {
            long heureMs = heure.getTime();

            // Respecter l'heure de disponibilité initiale du véhicule (si définie)
            if (vehicule.getHeureDisponibilite() != null) {
                long dispoMs = vehicule.getHeureDisponibilite().getTime() % (24L * 60L * 60L * 1000L);
                long heureJourMs = heureMs % (24L * 60L * 60L * 1000L);
                if (heureJourMs < dispoMs) {
                    return false;
                }
            }

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
     * @param reservations        Liste des réservations pour la date
     * @param vehicules           Liste de tous les véhicules disponibles
     * @param conn                Connexion à la base de données
     * @param tempsAttenteMinutes Durée du temps d'attente pour regrouper les
     *                            départs (0 = pas de regroupement)
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
        // selon les critères : moins de trajets, places les plus proches, diesel
        // prioritaire

        // Copie des réservations pour pouvoir les modifier
        List<Reservation> reservationsNonAssignees = new ArrayList<>(reservations);

        // Liste pour garder trace des réservations qui n'ont pas pu être assignées
        List<Reservation> reservationsImpossiblesAAssigner = new ArrayList<>();

        // ETAPE 1 : Trier par heure d'arrivée
        reservationsNonAssignees.sort((r1, r2) -> r1.getDateHeureArrive().compareTo(r2.getDateHeureArrive()));

        // ETAPE 1b : Regrouper par temps d'attente et déterminer l'heure de départ
        // groupée
        // L'heure de départ du groupe = l'heure d'arrivée la plus tardive du groupe
        // On memorise aussi l'heure de depart par reservation (objet) pour l'affichage
        Map<Reservation, Timestamp> heureDepartParReservation = new HashMap<>();
        List<List<Reservation>> groupesDeDepart;
        if (tempsAttenteMinutes > 0) {
            groupesDeDepart = regroupeParTempsAttente(reservationsNonAssignees, tempsAttenteMinutes);
            // L'heure exacte de départ de chaque groupe sera recalculée dynamiquement
            // plus bas en fonction de la disponibilité des véhicules (retours dans la
            // tranche), on initialise ici à l'heure d'arrivée la plus tardive du groupe
            // uniquement comme base.
            for (List<Reservation> groupe : groupesDeDepart) {
                Timestamp heureDepartGroupe = groupe.get(groupe.size() - 1).getDateHeureArrive();
                for (Reservation r : groupe) {
                    heureDepartParReservation.put(r, heureDepartGroupe);
                }
            }
        } else {
            // Pas de regroupement : chaque réservation est son propre groupe
            groupesDeDepart = new ArrayList<>();
            for (Reservation r : reservationsNonAssignees) {
                heureDepartParReservation.put(r, r.getDateHeureArrive());
                List<Reservation> singleGroup = new ArrayList<>();
                singleGroup.add(r);
                groupesDeDepart.add(singleGroup);
            }
            // Fusionner les groupes ayant la même heure de départ
            groupesDeDepart = fusionnerGroupesMemeHeure(groupesDeDepart);
        }

        // ETAPE 2 : Le tri des réservations par passagers se fait dans chaque groupe
        // (ETAPE 3)

        // ETAPE 3 : Traiter chaque groupe de départ séquentiellement
        // Après chaque groupe, calculer les horaires pour permettre la réutilisation
        // des véhicules
        // Les réservations non assignées dans un groupe sont reportées au groupe
        // suivant
        Map<Vehicule, List<InfosTrajet>> infosTrajetParVehicule = new HashMap<>();
        List<Reservation> reservationsReportees = new ArrayList<>();

        for (int indexGroupe = 0; indexGroupe < groupesDeDepart.size(); indexGroupe++) {
            List<Reservation> groupe = groupesDeDepart.get(indexGroupe);
            boolean estDernierGroupe = (indexGroupe == groupesDeDepart.size() - 1);
            Timestamp heureDepartGroupe = heureDepartParReservation.get(groupe.get(0));

            // Recalculer dynamiquement l'heure de départ du groupe en tenant compte :
            // - de la tranche de regroupement (30mn, etc.)
            // - des heures de retour des véhicules précédents et de leur
            // heure_disponibilite initiale
            if (tempsAttenteMinutes > 0 && !groupe.isEmpty()) {
                // Début de la tranche = première arrivée du groupe
                Timestamp debutTranche = groupe.get(0).getDateHeureArrive();
                long debutMs = debutTranche.getTime();
                long finMs = debutMs + tempsAttenteMinutes * 60L * 1000L;

                long departMs = heureDepartGroupe.getTime();

                // Pour chaque véhicule, calculer sa prochaine heure possible de départ
                // dans cette tranche, et pousser l'heure de départ du groupe si besoin.
                for (VehiculeAvecCapacite vac : vehiculesDisponibles) {
                    long candidateMs = debutMs;

                    // Tenir compte de l'heure de disponibilité initiale (heure_disponibilite)
                    if (vac.vehicule.getHeureDisponibilite() != null) {
                        long millisParJour = 24L * 60L * 60L * 1000L;
                        long baseJour = debutMs - (debutMs % millisParJour);
                        long dispoJour = vac.vehicule.getHeureDisponibilite().getTime() % millisParJour;
                        long dispoMs = baseJour + dispoJour;
                        if (dispoMs > candidateMs) {
                            candidateMs = dispoMs;
                        }
                    }

                    // Tenir compte du dernier retour connu de ce véhicule
                    long dernierRetour = -1L;
                    for (long[] trajet : vac.trajetsOccupes) {
                        if (trajet[1] > dernierRetour) {
                            dernierRetour = trajet[1];
                        }
                    }
                    if (dernierRetour > 0 && dernierRetour > candidateMs) {
                        candidateMs = dernierRetour;
                    }

                    // Si le véhicule devient disponible dans la tranche, on ajuste l'heure
                    // de départ du groupe pour attendre ce retour (max sur tous les
                    // véhicules et réservations).
                    if (candidateMs >= debutMs && candidateMs <= finMs) {
                        if (candidateMs > departMs) {
                            departMs = candidateMs;
                        }
                    }
                }

                heureDepartGroupe = new Timestamp(departMs);

                // Mettre à jour l'heure de départ mémorisée pour toutes les réservations
                // de ce groupe (y compris les reportées qui vont être ajoutées juste
                // après).
                for (Reservation r : groupe) {
                    heureDepartParReservation.put(r, heureDepartGroupe);
                }
            }

            // Ajouter les réservations reportées du groupe précédent
            List<Reservation> reservationsGroupe = new ArrayList<>(reservationsReportees);
            // Mettre à jour l'heure de départ des réservations reportées vers ce groupe
            for (Reservation r : reservationsReportees) {
                heureDepartParReservation.put(r, heureDepartGroupe);
            }
            reservationsReportees.clear();
            reservationsGroupe.addAll(groupe);

            // Trier par nombre de passagers décroissant
            reservationsGroupe.sort((r1, r2) -> Integer.compare(r2.getNombrePassage(), r1.getNombrePassage()));

            while (!reservationsGroupe.isEmpty()) {
                // On traite les réservations dans l'ordre décroissant de passagers
                Reservation reservationOriginale = reservationsGroupe.remove(0);
                int passagersRestants = reservationOriginale.getNombrePassage();

                // On peut repartir une même réservation sur plusieurs véhicules
                // du même groupe. Pour chaque "portion", on choisit le véhicule
                // le plus adapté selon les critères, jusqu'à épuisement des
                // passagers ou des capacités disponibles.
                while (passagersRestants > 0) {
                    VehiculeAvecCapacite meilleurVehicule = null;

                    // Chercher le meilleur véhicule selon les critères :
                    // 1. Disponible à l'heure de départ du groupe
                    // 2. Même groupe de départ
                    // 3. Capacité restante la plus proche du nombre de passagers restants
                    // 4. Moins de trajets effectués
                    // 5. Type de carburant prioritaire
                    // 6. Aléatoire en cas de parfaite égalité
                    for (VehiculeAvecCapacite vehiculeAvecCap : vehiculesDisponibles) {
                        // Vérifier la disponibilité du véhicule à l'heure de départ du groupe
                        if (!vehiculeAvecCap.estDisponibleA(heureDepartGroupe)) {
                            continue; // Véhicule en trajet, pas encore revenu
                        }

                        // Vérifier si le véhicule est vide OU s'il a des réservations du même groupe
                        boolean memeGroupe = vehiculeAvecCap.reservations.isEmpty() ||
                                heureDepartParReservation.get(vehiculeAvecCap.reservations.get(0))
                                        .equals(heureDepartGroupe);

                        // Si le véhicule n'est pas dans ce groupe ou n'a plus de place, on ignore
                        if (!memeGroupe || vehiculeAvecCap.placesRestantes <= 0) {
                            continue;
                        }

                        // Ce véhicule est candidat, le comparer au meilleur actuel
                        if (meilleurVehicule == null) {
                            meilleurVehicule = vehiculeAvecCap;
                        } else {
                            // 1) Prioriser d'abord les véhicules qui peuvent prendre TOUTE la réservation
                            boolean candidatSuffisant = vehiculeAvecCap.placesRestantes >= passagersRestants;
                            boolean meilleurSuffisant = meilleurVehicule.placesRestantes >= passagersRestants;

                            if (candidatSuffisant && !meilleurSuffisant) {
                                // Nouveau candidat peut prendre toute la reservation, l'ancien non
                                meilleurVehicule = vehiculeAvecCap;
                                continue;
                            } else if (!candidatSuffisant && meilleurSuffisant) {
                                // Garder le meilleur actuel qui peut prendre toute la reservation
                                continue;
                            }

                            // 2) Si les deux sont suffisants OU les deux sont insuffisants,
                            // on revient au critère "capacité la plus proche".
                            int diffCandidat = Math.abs(vehiculeAvecCap.placesRestantes - passagersRestants);
                            int diffMeilleur = Math.abs(meilleurVehicule.placesRestantes - passagersRestants);
                            if (diffCandidat < diffMeilleur) {
                                meilleurVehicule = vehiculeAvecCap;
                                continue;
                            } else if (diffCandidat > diffMeilleur) {
                                continue;
                            }

                            // 3) Moins de trajets effectués
                            int trajetsCandidat = vehiculeAvecCap.trajetsOccupes.size();
                            int trajetsMeilleur = meilleurVehicule.trajetsOccupes.size();
                            if (trajetsCandidat < trajetsMeilleur) {
                                meilleurVehicule = vehiculeAvecCap;
                                continue;
                            } else if (trajetsCandidat > trajetsMeilleur) {
                                continue;
                            }

                            // 4) Type de carburant prioritaire
                            boolean candidatDiesel = vehiculeAvecCap.vehicule.getTypeCarburant() != null &&
                                    CARBURANT_PRIORITAIRE
                                            .equals(vehiculeAvecCap.vehicule.getTypeCarburant().getReference());
                            boolean meilleurDiesel = meilleurVehicule.vehicule.getTypeCarburant() != null &&
                                    CARBURANT_PRIORITAIRE
                                            .equals(meilleurVehicule.vehicule.getTypeCarburant().getReference());
                            if (candidatDiesel && !meilleurDiesel) {
                                meilleurVehicule = vehiculeAvecCap;
                                continue;
                            } else if (!candidatDiesel && meilleurDiesel) {
                                continue;
                            }

                            // 5) Aléatoire si tout est identique
                            if (Math.random() < 0.5) {
                                meilleurVehicule = vehiculeAvecCap;
                            }
                        }
                    }

                    // Aucun véhicule disponible pour cette réservation dans ce groupe
                    if (meilleurVehicule == null) {
                        break;
                    }

                    // Nombre de passagers que l'on peut mettre dans ce véhicule
                    int aAssigner = Math.min(meilleurVehicule.placesRestantes, passagersRestants);
                    if (aAssigner <= 0) {
                        break;
                    }

                    // Créer une "partie" de la réservation pour la simulation, avec seulement
                    // le nombre de passagers effectivement pris par ce véhicule
                    Reservation partieReservation = copierReservation(reservationOriginale, aAssigner);
                    meilleurVehicule.ajouterReservation(partieReservation);
                    // Memoriser l'heure de depart pour cette ligne de reservation
                    heureDepartParReservation.put(partieReservation, heureDepartGroupe);

                    passagersRestants -= aAssigner;

                    // Nouvelle logique : si le véhicule a encore des places libres
                    // après avoir pris toute cette réservation, essayer de le
                    // "remplir" avec d'autres réservations du même groupe dont le
                    // nombre de passagers est le plus proche de sa capacité restante.
                    if (meilleurVehicule.placesRestantes > 0 && !reservationsGroupe.isEmpty()) {
                        remplirVehiculeAvecAutresReservations(meilleurVehicule, reservationsGroupe,
                                heureDepartGroupe, heureDepartParReservation);
                    }
                }

                // Si après tentative il reste des passagers non assignés pour cette réservation
                if (passagersRestants > 0) {
                    Reservation reste = copierReservation(reservationOriginale, passagersRestants);
                    if (estDernierGroupe) {
                        // Dernier groupe de la journée : impossible à assigner (totalement ou
                        // partiellement)
                        reservationsImpossiblesAAssigner.add(reste);
                    } else {
                        // Reporter la partie restante au prochain groupe
                        reservationsReportees.add(reste);
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
        resultat.setHeureDepartParReservation(heureDepartParReservation);

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
     * @param heureDepartGroupe Heure de départ du groupe (= dernière arrivée du
     *                          groupe)
     * @throws SQLException
     */
    private static void calculerHoraires(VehiculeAvecCapacite vehiculeAvecCap, Connection conn,
            Timestamp heureDepartGroupe) throws SQLException {
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

            // L'heure de départ = heure d'arrivée du vol (le véhicule récupère les
            // passagers à l'aéroport)
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
     * Crée une copie légère d'une réservation en ajustant uniquement le nombre de
     * passagers.
     * Utilisée pour représenter les fractions d'une réservation réparties sur
     * plusieurs véhicules
     * dans la simulation, sans toucher à la réservation d'origine en base.
     */
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
