package itu.back.controller;

import java.math.BigDecimal;
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

import itu.back.model.Aeroport;
import itu.back.model.Client;
import itu.back.model.Distance;
import itu.back.model.Hotel;
import itu.back.model.Reservation;
import itu.back.model.ReservationDTO;
import itu.back.model.TypeCarburant;
import itu.back.model.Vehicule;
import itu.back.util.DatabaseConnection;
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

            // Récupérer la liste des aéroports
            List<Aeroport> aeroports = VehiculeOptimisationService.getAllAeroports();
            mv.addItem("aeroports", aeroports);

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
            @RequestParam("dateHeureArrive") String dateHeureArrive,
            @RequestParam("idAeroport") int idAeroport) {

        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();

            // Calculer l'heure d'arrivée
            Timestamp heureArrivee = Timestamp.valueOf(dateHeureArrive.replace("T", " ") + ":00");

            // Insérer la réservation SANS véhicule assigné (non assignée)
            String sql = "INSERT INTO reservation (id_client, id_hotel, nombre_passage, date_heure_arrive, id_aeroport) " +
                    "VALUES (?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idClient);
            stmt.setInt(2, idHotel);
            stmt.setInt(3, nombrePassage);
            stmt.setTimestamp(4, heureArrivee);
            stmt.setInt(5, idAeroport);

            int rows = stmt.executeUpdate();
            stmt.close();

            if (rows > 0) {
                mv.addItem("success", "Réservation créée avec succès ! <br/><strong>Statut :</strong> Non assignée<br/>" +
                        "<a href='" + "reservation/non-assignees" + "'>Voir les réservations à assigner</a>");
            }

            // Récupérer les listes pour réafficher le formulaire
            mv.addItem("clients", getAllClients());
            mv.addItem("hotels", getAllHotels());
            mv.addItem("aeroports", VehiculeOptimisationService.getAllAeroports());
            mv.setView("/reservation-form.jsp");

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    private String formatTemps(int minutes) {
        int heures = minutes / 60;
        int mins = minutes % 60;
        if (heures > 0) {
            return heures + "h " + mins + "min";
        }
        return mins + " min";
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
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville, h.pays AS hotel_pays, " +
                    "v.marque, v.modele, v.nombre_places, v.reference AS v_reference, v.vitesse_moyenne, v.type_carburant_id, " +
                    "tc.reference AS tc_reference, tc.nom AS tc_nom, " +
                    "a.code AS aeroport_code, a.libelle AS aeroport_libelle " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "LEFT JOIN vehicule v ON r.id_vehicule = v.id " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "LEFT JOIN aeroport a ON r.id_aeroport = a.id " +
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
                
                // Nouveaux champs planification
                reservation.setIdVehicule(rs.getObject("id_vehicule") != null ? rs.getInt("id_vehicule") : null);
                reservation.setIdAeroport(rs.getObject("id_aeroport") != null ? rs.getInt("id_aeroport") : null);
                reservation.setDistanceKm(rs.getBigDecimal("distance_km"));
                reservation.setTempsEstimeMinutes(rs.getObject("temps_estime_minutes") != null ? rs.getInt("temps_estime_minutes") : null);
                reservation.setHeureDepart(rs.getTimestamp("heure_depart"));

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

                // Vehicule (si présent)
                if (rs.getObject("id_vehicule") != null) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id_vehicule"));
                    vehicule.setMarque(rs.getString("marque"));
                    vehicule.setModele(rs.getString("modele"));
                    vehicule.setNombrePlaces(rs.getInt("nombre_places"));
                    vehicule.setReference(rs.getString("v_reference"));
                    vehicule.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));
                    vehicule.setTypeCarburantId(rs.getInt("type_carburant_id"));

                    if (rs.getString("tc_reference") != null) {
                        TypeCarburant tc = new TypeCarburant();
                        tc.setId(rs.getInt("type_carburant_id"));
                        tc.setReference(rs.getString("tc_reference"));
                        tc.setNom(rs.getString("tc_nom"));
                        vehicule.setTypeCarburant(tc);
                    }
                    reservation.setVehicule(vehicule);
                }

                // Aeroport (si présent)
                if (rs.getObject("id_aeroport") != null) {
                    Aeroport aeroport = new Aeroport();
                    aeroport.setId(rs.getInt("id_aeroport"));
                    aeroport.setCode(rs.getString("aeroport_code"));
                    aeroport.setLibelle(rs.getString("aeroport_libelle"));
                    reservation.setAeroport(aeroport);
                }

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

    // ==================== AFFICHAGE DES RESERVATIONS PAR DATE ====================
    @GetMapping("/reservation/par-date/form")
    public ModelView showReservationsByDateForm() {
        ModelView mv = new ModelView();
        mv.setView("/reservation-par-date.jsp");
        return mv;
    }

    @GetMapping("/reservation/par-date")
    public ModelView showReservationsByDate(@RequestParam("date") String dateStr) {
        ModelView mv = new ModelView();
        
        try {
            if (dateStr != null && !dateStr.isEmpty()) {
                List<Reservation> reservations = getReservationsByDate(dateStr);
                mv.addItem("reservations", reservations);
                mv.addItem("dateRecherche", dateStr);
                mv.addItem("nombreReservations", reservations.size());
            }
            mv.setView("/reservation-par-date.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la récupération des réservations : " + e.getMessage());
            mv.setView("/error.jsp");
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
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville, h.pays AS hotel_pays, " +
                    "v.marque, v.modele, v.nombre_places, v.reference AS v_reference, v.vitesse_moyenne, v.type_carburant_id, " +
                    "tc.reference AS tc_reference, tc.nom AS tc_nom, " +
                    "a.code AS aeroport_code, a.libelle AS aeroport_libelle " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "LEFT JOIN vehicule v ON r.id_vehicule = v.id " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "LEFT JOIN aeroport a ON r.id_aeroport = a.id " +
                    "WHERE DATE(r.date_heure_arrive) = ? " +
                    "ORDER BY r.heure_depart ASC, r.date_heure_arrive ASC";
            
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
                reservation.setDateReservation(rs.getTimestamp("date_reservation"));
                
                reservation.setIdVehicule(rs.getObject("id_vehicule") != null ? rs.getInt("id_vehicule") : null);
                reservation.setIdAeroport(rs.getObject("id_aeroport") != null ? rs.getInt("id_aeroport") : null);
                reservation.setDistanceKm(rs.getBigDecimal("distance_km"));
                reservation.setTempsEstimeMinutes(rs.getObject("temps_estime_minutes") != null ? rs.getInt("temps_estime_minutes") : null);
                reservation.setHeureDepart(rs.getTimestamp("heure_depart"));

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
                hotel.setPays(rs.getString("hotel_pays"));
                reservation.setHotel(hotel);

                if (rs.getObject("id_vehicule") != null) {
                    Vehicule vehicule = new Vehicule();
                    vehicule.setId(rs.getInt("id_vehicule"));
                    vehicule.setMarque(rs.getString("marque"));
                    vehicule.setModele(rs.getString("modele"));
                    vehicule.setNombrePlaces(rs.getInt("nombre_places"));
                    vehicule.setReference(rs.getString("v_reference"));
                    vehicule.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));
                    vehicule.setTypeCarburantId(rs.getInt("type_carburant_id"));

                    if (rs.getString("tc_reference") != null) {
                        TypeCarburant tc = new TypeCarburant();
                        tc.setId(rs.getInt("type_carburant_id"));
                        tc.setReference(rs.getString("tc_reference"));
                        tc.setNom(rs.getString("tc_nom"));
                        vehicule.setTypeCarburant(tc);
                    }
                    reservation.setVehicule(vehicule);
                }

                if (rs.getObject("id_aeroport") != null) {
                    Aeroport aeroport = new Aeroport();
                    aeroport.setId(rs.getInt("id_aeroport"));
                    aeroport.setCode(rs.getString("aeroport_code"));
                    aeroport.setLibelle(rs.getString("aeroport_libelle"));
                    reservation.setAeroport(aeroport);
                }

                reservations.add(reservation);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return reservations;
    }

    // ==================== RESERVATIONS NON ASSIGNEES ====================
    @GetMapping("/reservation/non-assignees")
    public ModelView showReservationsNonAssignees() {
        ModelView mv = new ModelView();
        
        try {
            List<Reservation> reservations = getReservationsNonAssignees();
            mv.addItem("reservations", reservations);
            mv.addItem("nombreReservations", reservations.size());
            mv.setView("/reservation-non-assignees.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la récupération des réservations : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        
        return mv;
    }

    @PostMapping("/reservation/assigner")
    public ModelView assignerVehicule(@RequestParam("idReservation") int idReservation) {
        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            
            // 1. Récupérer les infos de la réservation
            String sqlSelect = "SELECT * FROM reservation WHERE id = ?";
            PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
            stmtSelect.setInt(1, idReservation);
            ResultSet rs = stmtSelect.executeQuery();
            
            if (!rs.next()) {
                mv.addItem("error", "Réservation non trouvée.");
                mv.addItem("reservations", getReservationsNonAssignees());
                mv.setView("/reservation-non-assignees.jsp");
                return mv;
            }
            
            int nombrePassage = rs.getInt("nombre_passage");
            int idAeroport = rs.getInt("id_aeroport");
            int idHotel = rs.getInt("id_hotel");
            Timestamp heureArrivee = rs.getTimestamp("date_heure_arrive");
            rs.close();
            stmtSelect.close();
            
            // 2. Calculer la distance
            Distance distance = VehiculeOptimisationService.getDistanceAeroportHotel(idAeroport, idHotel);
            BigDecimal distanceKm = (distance != null) ? distance.getValeur() : new BigDecimal("15.0");
            
            // 3. Estimer le temps pour trouver le véhicule disponible
            int tempsEstimeDefault = VehiculeOptimisationService.calculerTempsTrajetMinutes(distanceKm, null);
            long tempsMillisDefault = tempsEstimeDefault * 60 * 1000L;
            Timestamp heureDepartEstimee = new Timestamp(heureArrivee.getTime() - tempsMillisDefault);
            
            // 4. Trouver le véhicule optimal disponible
            Vehicule vehiculeOptimal = VehiculeOptimisationService.trouverVehiculeOptimalDisponible(
                    nombrePassage, heureDepartEstimee, heureArrivee);
            
            if (vehiculeOptimal == null) {
                mv.addItem("error", "Aucun véhicule disponible avec une capacité suffisante pour " 
                        + nombrePassage + " passagers sur ce créneau horaire.");
                mv.addItem("reservations", getReservationsNonAssignees());
                mv.setView("/reservation-non-assignees.jsp");
                return mv;
            }
            
            // 5. Calculer le temps exact avec la vitesse du véhicule
            int tempsMinutes = VehiculeOptimisationService.calculerTempsTrajetMinutes(distanceKm, vehiculeOptimal);
            long tempsMillis = tempsMinutes * 60 * 1000L;
            Timestamp heureDepart = new Timestamp(heureArrivee.getTime() - tempsMillis);
            
            // 6. Mettre à jour la réservation
            String sqlUpdate = "UPDATE reservation SET id_vehicule = ?, distance_km = ?, " +
                    "temps_estime_minutes = ?, heure_depart = ? WHERE id = ?";
            PreparedStatement stmtUpdate = conn.prepareStatement(sqlUpdate);
            stmtUpdate.setInt(1, vehiculeOptimal.getId());
            stmtUpdate.setBigDecimal(2, distanceKm);
            stmtUpdate.setInt(3, tempsMinutes);
            stmtUpdate.setTimestamp(4, heureDepart);
            stmtUpdate.setInt(5, idReservation);
            
            int rows = stmtUpdate.executeUpdate();
            stmtUpdate.close();
            
            if (rows > 0) {
                StringBuilder successMsg = new StringBuilder();
                successMsg.append("Véhicule assigné avec succès !<br/>");
                successMsg.append("<strong>Véhicule :</strong> ")
                        .append(vehiculeOptimal.getMarque()).append(" ")
                        .append(vehiculeOptimal.getModele()).append(" (")
                        .append(vehiculeOptimal.getNombrePlaces()).append(" places");
                if (vehiculeOptimal.getTypeCarburant() != null) {
                    successMsg.append(", ").append(vehiculeOptimal.getTypeCarburant().getNom());
                }
                successMsg.append(")<br/>");
                successMsg.append("<strong>Distance :</strong> ").append(distanceKm).append(" km<br/>");
                successMsg.append("<strong>Temps estimé :</strong> ").append(formatTemps(tempsMinutes)).append("<br/>");
                successMsg.append("<strong>Heure de départ :</strong> ").append(heureDepart);
                mv.addItem("success", successMsg.toString());
            }
            
            mv.addItem("reservations", getReservationsNonAssignees());
            mv.addItem("nombreReservations", getReservationsNonAssignees().size());
            mv.setView("/reservation-non-assignees.jsp");
            
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'assignation : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
        
        return mv;
    }

    private List<Reservation> getReservationsNonAssignees() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT r.*, " +
                    "c.nom AS client_nom, c.prenom AS client_prenom, c.email AS client_email, " +
                    "h.nom AS hotel_nom, h.adresse AS hotel_adresse, h.ville AS hotel_ville, h.pays AS hotel_pays, " +
                    "a.code AS aeroport_code, a.libelle AS aeroport_libelle " +
                    "FROM reservation r " +
                    "LEFT JOIN client c ON r.id_client = c.id " +
                    "LEFT JOIN hotel h ON r.id_hotel = h.id " +
                    "LEFT JOIN aeroport a ON r.id_aeroport = a.id " +
                    "WHERE r.id_vehicule IS NULL " +
                    "ORDER BY r.date_heure_arrive ASC";
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
                reservation.setIdAeroport(rs.getObject("id_aeroport") != null ? rs.getInt("id_aeroport") : null);

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
                hotel.setPays(rs.getString("hotel_pays"));
                reservation.setHotel(hotel);

                if (rs.getObject("id_aeroport") != null) {
                    Aeroport aeroport = new Aeroport();
                    aeroport.setId(rs.getInt("id_aeroport"));
                    aeroport.setCode(rs.getString("aeroport_code"));
                    aeroport.setLibelle(rs.getString("aeroport_libelle"));
                    reservation.setAeroport(aeroport);
                }

                reservations.add(reservation);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return reservations;
    }
}
