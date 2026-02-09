package itu.back.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.itu.framework.ModelView;
import com.itu.framework.annotation.Controller;
import com.itu.framework.annotation.GetMapping;
import com.itu.framework.annotation.Json;
import com.itu.framework.annotation.PostMapping;
import com.itu.framework.annotation.RequestParam;
import com.itu.framework.response.JsonResponse;

import itu.back.model.Client;
import itu.back.model.Hotel;
import itu.back.model.Reservation;
import itu.back.model.ReservationDTO;
import itu.back.util.DatabaseConnection;

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

            mv.setView("/reservation-form.jsp");
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

            String sql = "INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idClient);
            stmt.setInt(2, idHotel);
            stmt.setInt(3, nombrePassage);
            stmt.setTimestamp(4, Timestamp.valueOf(dateHeureArrive.replace("T", " ") + ":00"));

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                mv.addItem("success", "Réservation enregistrée avec succès !");

                // Récupérer les listes pour réafficher le formulaire
                List<Client> clients = getAllClients();
                mv.addItem("clients", clients);

                List<Hotel> hotels = getAllHotels();
                mv.addItem("hotels", hotels);

                mv.setView("/reservation-form.jsp");
            }

            stmt.close();

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
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
            String sql = "SELECT r.*, c.nom AS client_nom, c.prenom AS client_prenom, c.email AS client_email, " +
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville, h.pays AS hotel_pays " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "ORDER BY r.date_reservation DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdClient(rs.getInt("id_client"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setNombrePassage(rs.getInt("nombre_passage"));
                reservation.setDateHeureArrive(rs.getTimestamp("date_heure_arrive"));
                reservation.setDateReservation(rs.getTimestamp("date_reservation"));

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
                hotel.setPays(rs.getString("hotel_pays"));
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
                hotel.setPays(rs.getString("pays"));
                hotels.add(hotel);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return hotels;
    }
}
