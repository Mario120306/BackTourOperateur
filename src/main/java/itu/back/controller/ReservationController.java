package itu.back.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.itu.framework.ModelView;
import com.itu.framework.annotation.Controller;
import com.itu.framework.annotation.GetMapping;
import com.itu.framework.annotation.Json;
import com.itu.framework.annotation.PostMapping;
import com.itu.framework.annotation.RequestParam;
import com.itu.framework.response.JsonResponse;

import itu.back.dto.ReservationDTO;
import itu.back.model.Client;
import itu.back.model.Hotel;
import itu.back.model.Reservation;
import itu.back.model.TypeCarburant;
import itu.back.model.Vehicule;
import itu.back.util.DatabaseConnection;
import itu.back.util.SimulationService;
import itu.back.util.VehiculeOptimisationService;

@Controller
public class ReservationController {

    @GetMapping("/reservation/form")
    public ModelView showForm() {
        ModelView mv = new ModelView();

        try {
            // Récupérer la liste des clients
            List<Client> clients = getAllClients();
            mv.addItem("clients", clients);

            // Récupérer la liste des hôtels
            List<Hotel> hotels = getAllHotels();
            mv.addItem("hotels", hotels);

            mv.setView("/reservation/form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors du chargement des données : " + e.getMessage());
            mv.setView("/error.jsp");
        }

        return mv;
    }

    @PostMapping("/reservation/save")
    public ModelView saveReservation(
            @RequestParam("idClient") int idClient,
            @RequestParam("idHotel") int idHotel,
            @RequestParam("nombrePassage") int nombrePassage,
            @RequestParam("dateHeureArrive") String dateHeureArrive) {

        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Calculer l'heure d'arrivée
            Timestamp heureArrivee = Timestamp.valueOf(dateHeureArrive.replace("T", " ") + ":00");

            // Insérer la réservation
            String sql = "INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idClient);
            stmt.setInt(2, idHotel);
            stmt.setInt(3, nombrePassage);
            stmt.setTimestamp(4, heureArrivee);

            int rows = stmt.executeUpdate();
            stmt.close();

            if (rows > 0) {
                mv.addItem("success", "Réservation créée avec succès !");
            }

            // Récupérer les listes pour réafficher le formulaire
            mv.addItem("clients", getAllClients());
            mv.addItem("hotels", getAllHotels());
            mv.setView("/reservation/form.jsp");

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    // ==================== API VEHICULE OPTIMAL ====================
    @GetMapping("/api/vehicule/optimal")
    @Json
    public JsonResponse getVehiculeOptimal(@RequestParam("nombrePassagers") int nombrePassagers) {
        try {
            Vehicule vehicule = VehiculeOptimisationService.trouverVehiculeOptimal(nombrePassagers);
            if (vehicule != null) {
                return JsonResponse.success(vehicule);
            } else {
                return JsonResponse.notFound("Aucun véhicule disponible pour " + nombrePassagers + " passagers");
            }
        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur : " + e.getMessage());
        }
    }

    @GetMapping("/api/reservations")
    @Json
    public JsonResponse getAllReservations() {
        try {
            List<Reservation> reservations = getAllReservationsFromDb();
            // Convertir en DTO pour éviter les problèmes de sérialisation Timestamp
            List<ReservationDTO> dtos = new ArrayList<>();
            for (Reservation r : reservations) {
                dtos.add(new ReservationDTO(r));
            }
            return JsonResponse.success(dtos);
        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur lors de la récupération des réservations : " + e.getMessage());
        }
    }

    private List<Reservation> getAllReservationsFromDb() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.*, " +
                    "c.nom AS client_nom, c.prenom AS client_prenom, c.email AS client_email, " +
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "ORDER BY r.date_heure_arrive DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdClient(rs.getInt("id_client"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setNombrePassage(rs.getInt("nombre_passage"));
                reservation.setDateHeureArrive(rs.getTimestamp("date_heure_arrive"));

                // Client
                Client client = new Client();
                client.setId(rs.getInt("id_client"));
                client.setNom(rs.getString("client_nom"));
                client.setPrenom(rs.getString("client_prenom"));
                client.setEmail(rs.getString("client_email"));
                reservation.setClient(client);

                // Hotel
                Hotel hotel = new Hotel();
                hotel.setId(rs.getInt("id_hotel"));
                hotel.setNom(rs.getString("hotel_nom"));
                hotel.setAdresse(rs.getString("hotel_adresse"));
                hotel.setVille(rs.getString("hotel_ville"));
                reservation.setHotel(hotel);

                reservations.add(reservation);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return reservations;
    }

    private List<Client> getAllClients() throws SQLException {
        List<Client> clients = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM client ORDER BY nom, prenom";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setNom(rs.getString("nom"));
                client.setPrenom(rs.getString("prenom"));
                client.setEmail(rs.getString("email"));
                clients.add(client);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return clients;
    }

    private List<Hotel> getAllHotels() throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM hotel ORDER BY nom";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setId(rs.getInt("id"));
                hotel.setNom(rs.getString("nom"));
                hotel.setAdresse(rs.getString("adresse"));
                hotel.setVille(rs.getString("ville"));
                hotels.add(hotel);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return hotels;
    }

    // ==================== AFFICHAGE DES RESERVATIONS PAR DATE ====================
    @GetMapping("/reservation/par-date/form")
    public ModelView showReservationsByDateForm() {
        ModelView mv = new ModelView();
        mv.setView("/reservation/par-date.jsp");
        return mv;
    }

    @GetMapping("/reservation/par-date")
    public ModelView showReservationsByDate(@RequestParam("date") String dateStr) {
        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                conn = DatabaseConnection.getConnection();

                // Récupérer les réservations pour la date
                List<Reservation> reservations = getReservationsByDate(dateStr);

                // Récupérer tous les véhicules disponibles
                List<Vehicule> tousVehicules = getAllVehicules();

                // SIMULATION : Assigner les véhicules aux réservations selon l'algorithme
                SimulationService.ResultatSimulation resultatSimulation = SimulationService
                        .simulerAssignation(reservations, tousVehicules, conn);

                Map<Vehicule, List<Reservation>> vehiculesAvecReservations = resultatSimulation
                        .getVehiculesAvecReservations();
                List<Reservation> reservationsNonAssignees = resultatSimulation.getReservationsNonAssignees();
                Map<Vehicule, SimulationService.InfosTrajet> infosTrajetParVehicule = resultatSimulation
                        .getInfosTrajetParVehicule();

                // Compter les véhicules utilisés (avec au moins une réservation)
                long nombreVehiculesUtilises = vehiculesAvecReservations.values().stream()
                        .filter(liste -> !liste.isEmpty())
                        .count();

                // Compter les réservations assignées
                int nombreReservationsAssignees = vehiculesAvecReservations.values().stream()
                        .mapToInt(List::size)
                        .sum();

                mv.addItem("vehiculesAvecReservations", vehiculesAvecReservations);
                mv.addItem("reservationsNonAssignees", reservationsNonAssignees);
                mv.addItem("infosTrajetParVehicule", infosTrajetParVehicule);
                mv.addItem("dateRecherche", dateStr);
                mv.addItem("nombreVehicules", (int) nombreVehiculesUtilises);
                mv.addItem("nombreReservationsTotal", reservations.size());
                mv.addItem("nombreReservationsAssignees", nombreReservationsAssignees);
                mv.addItem("nombreReservationsNonAssignees", reservationsNonAssignees.size());
            }
            mv.setView("/reservation/par-date.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la récupération des données : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    @GetMapping("/api/reservations/par-date")
    @Json
    public JsonResponse getReservationsByDateApi(@RequestParam("date") String dateStr) {
        try {
            List<Reservation> reservations = getReservationsByDate(dateStr);
            List<ReservationDTO> dtos = new ArrayList<>();
            for (Reservation r : reservations) {
                dtos.add(new ReservationDTO(r));
            }
            return JsonResponse.success(dtos);
        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur : " + e.getMessage());
        }
    }

    private List<Reservation> getReservationsByDate(String dateStr) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.*, " +
                    "c.nom AS client_nom, c.prenom AS client_prenom, c.email AS client_email, " +
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "WHERE DATE(r.date_heure_arrive) = ? " +
                    "ORDER BY r.date_heure_arrive ASC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, java.sql.Date.valueOf(dateStr));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdClient(rs.getInt("id_client"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setNombrePassage(rs.getInt("nombre_passage"));
                reservation.setDateHeureArrive(rs.getTimestamp("date_heure_arrive"));

                Client client = new Client();
                client.setId(rs.getInt("id_client"));
                client.setNom(rs.getString("client_nom"));
                client.setPrenom(rs.getString("client_prenom"));
                client.setEmail(rs.getString("client_email"));
                reservation.setClient(client);

                Hotel hotel = new Hotel();
                hotel.setId(rs.getInt("id_hotel"));
                hotel.setNom(rs.getString("hotel_nom"));
                hotel.setAdresse(rs.getString("hotel_adresse"));
                hotel.setVille(rs.getString("hotel_ville"));
                reservation.setHotel(hotel);

                reservations.add(reservation);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return reservations;
    }

    // ==================== METHODES UTILITAIRES ====================
    private List<Vehicule> getAllVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT v.*, tc.nom AS carburant_nom, tc.reference AS carburant_ref " +
                    "FROM vehicule v " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "ORDER BY v.marque, v.modele";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setMarque(rs.getString("marque"));
                vehicule.setModele(rs.getString("modele"));
                vehicule.setNombrePlaces(rs.getInt("nombre_places"));
                vehicule.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));

                int idTypeCarburant = rs.getInt("type_carburant_id");
                if (!rs.wasNull()) {
                    TypeCarburant tc = new TypeCarburant();
                    tc.setId(idTypeCarburant);
                    tc.setNom(rs.getString("carburant_nom"));
                    tc.setReference(rs.getString("carburant_ref"));
                    vehicule.setTypeCarburant(tc);
                }

                vehicules.add(vehicule);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return vehicules;
    }
}
