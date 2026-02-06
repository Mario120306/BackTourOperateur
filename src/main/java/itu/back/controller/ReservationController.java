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
import com.itu.framework.annotation.PostMapping;
import com.itu.framework.annotation.RequestParam;

import itu.back.model.Client;
import itu.back.model.Hotel;
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
