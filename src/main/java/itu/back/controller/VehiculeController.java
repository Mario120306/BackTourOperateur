package itu.back.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.itu.framework.ModelView;
import com.itu.framework.annotation.Controller;
import com.itu.framework.annotation.GetMapping;
import com.itu.framework.annotation.Json;
import com.itu.framework.annotation.PostMapping;
import com.itu.framework.annotation.RequestParam;
import com.itu.framework.response.JsonResponse;

import itu.back.model.TypeCarburant;
import itu.back.model.Vehicule;
import itu.back.util.DatabaseConnection;

@Controller
public class VehiculeController {

    // ==================== LISTE DES VEHICULES ====================
    @GetMapping("/vehicule/list")
    public ModelView listVehicules() {
        ModelView mv = new ModelView();
        try {
            List<Vehicule> vehicules = getAllVehicules();
            mv.addItem("vehicules", vehicules);
            mv.setView("/vehicule-list.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors du chargement des véhicules : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== API JSON - Liste des véhicules ====================
    @GetMapping("/api/vehicules")
    @Json
    public JsonResponse getVehiculesJson() {
        try {
            List<Vehicule> vehicules = getAllVehicules();
            return JsonResponse.success(vehicules);
        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur : " + e.getMessage());
        }
    }

    // ==================== FORMULAIRE AJOUT VEHICULE ====================
    @GetMapping("/vehicule/form")
    public ModelView showForm() {
        ModelView mv = new ModelView();
        try {
            List<TypeCarburant> typesCarburant = getAllTypesCarburant();
            mv.addItem("typesCarburant", typesCarburant);
            mv.addItem("vehicule", new Vehicule());
            mv.addItem("isEdit", false);
            mv.setView("/vehicule-form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors du chargement du formulaire : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== FORMULAIRE MODIFICATION ====================
    @GetMapping("/vehicule/edit")
    public ModelView showEditForm(@RequestParam("id") int id) {
        ModelView mv = new ModelView();
        try {
            Vehicule vehicule = getVehiculeById(id);
            if (vehicule == null) {
                mv.addItem("error", "Véhicule non trouvé");
                mv.setView("/error.jsp");
                return mv;
            }
            List<TypeCarburant> typesCarburant = getAllTypesCarburant();
            mv.addItem("typesCarburant", typesCarburant);
            mv.addItem("vehicule", vehicule);
            mv.addItem("isEdit", true);
            mv.setView("/vehicule-form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== ENREGISTRER (CREATE) ====================
    @PostMapping("/vehicule/save")
    public ModelView saveVehicule(
            @RequestParam("marque") String marque,
            @RequestParam("modele") String modele,
            @RequestParam("nombrePlaces") int nombrePlaces,
            @RequestParam("reference") String reference,
            @RequestParam("vitesseMoyenne") int vitesseMoyenne,
            @RequestParam("typeCarburantId") int typeCarburantId) {

        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO vehicule (marque, modele, nombre_places, reference, vitesse_moyenne, type_carburant_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, marque);
            stmt.setString(2, modele);
            stmt.setInt(3, nombrePlaces);
            stmt.setString(4, reference);
            stmt.setInt(5, vitesseMoyenne);
            stmt.setInt(6, typeCarburantId);

            int rows = stmt.executeUpdate();
            stmt.close();

            if (rows > 0) {
                mv.addItem("success", "Véhicule ajouté avec succès !");
            }

            // Rediriger vers la liste
            List<Vehicule> vehicules = getAllVehicules();
            mv.addItem("vehicules", vehicules);
            mv.setView("/vehicule-list.jsp");

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    // ==================== MODIFIER (UPDATE) ====================
    @PostMapping("/vehicule/update")
    public ModelView updateVehicule(
            @RequestParam("id") int id,
            @RequestParam("marque") String marque,
            @RequestParam("modele") String modele,
            @RequestParam("nombrePlaces") int nombrePlaces,
            @RequestParam("reference") String reference,
            @RequestParam("vitesseMoyenne") int vitesseMoyenne,
            @RequestParam("typeCarburantId") int typeCarburantId) {

        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "UPDATE vehicule SET marque = ?, modele = ?, nombre_places = ?, reference = ?, " +
                    "vitesse_moyenne = ?, type_carburant_id = ? WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, marque);
            stmt.setString(2, modele);
            stmt.setInt(3, nombrePlaces);
            stmt.setString(4, reference);
            stmt.setInt(5, vitesseMoyenne);
            stmt.setInt(6, typeCarburantId);
            stmt.setInt(7, id);

            int rows = stmt.executeUpdate();
            stmt.close();

            if (rows > 0) {
                mv.addItem("success", "Véhicule modifié avec succès !");
            }

            // Rediriger vers la liste
            List<Vehicule> vehicules = getAllVehicules();
            mv.addItem("vehicules", vehicules);
            mv.setView("/vehicule-list.jsp");

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la modification : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    // ==================== SUPPRIMER (DELETE) ====================
    @GetMapping("/vehicule/delete")
    public ModelView deleteVehicule(@RequestParam("id") int id) {
        ModelView mv = new ModelView();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "DELETE FROM vehicule WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();
            stmt.close();

            if (rows > 0) {
                mv.addItem("success", "Véhicule supprimé avec succès !");
            } else {
                mv.addItem("error", "Véhicule non trouvé");
            }

            // Rediriger vers la liste
            List<Vehicule> vehicules = getAllVehicules();
            mv.addItem("vehicules", vehicules);
            mv.setView("/vehicule-list.jsp");

        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la suppression : " + e.getMessage());
            mv.setView("/error.jsp");
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return mv;
    }

    // ==================== METHODES PRIVEES ====================

    private List<Vehicule> getAllVehicules() throws SQLException {
        List<Vehicule> vehicules = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT v.*, tc.reference AS tc_reference, tc.nom AS tc_nom " +
                    "FROM vehicule v " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "ORDER BY v.marque, v.modele";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

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

    private Vehicule getVehiculeById(int id) throws SQLException {
        Vehicule vehicule = null;
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT v.*, tc.reference AS tc_reference, tc.nom AS tc_nom " +
                    "FROM vehicule v " +
                    "LEFT JOIN type_carburant tc ON v.type_carburant_id = tc.id " +
                    "WHERE v.id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setMarque(rs.getString("marque"));
                vehicule.setModele(rs.getString("modele"));
                vehicule.setNombrePlaces(rs.getInt("nombre_places"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setVitesseMoyenne(rs.getInt("vitesse_moyenne"));
                vehicule.setTypeCarburantId(rs.getInt("type_carburant_id"));

                TypeCarburant tc = new TypeCarburant();
                tc.setId(rs.getInt("type_carburant_id"));
                tc.setReference(rs.getString("tc_reference"));
                tc.setNom(rs.getString("tc_nom"));
                vehicule.setTypeCarburant(tc);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return vehicule;
    }

    private List<TypeCarburant> getAllTypesCarburant() throws SQLException {
        List<TypeCarburant> types = new ArrayList<>();
        Connection conn = null;

        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT * FROM type_carburant ORDER BY nom";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                TypeCarburant tc = new TypeCarburant();
                tc.setId(rs.getInt("id"));
                tc.setReference(rs.getString("reference"));
                tc.setNom(rs.getString("nom"));
                types.add(tc);
            }

            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }

        return types;
    }
}
