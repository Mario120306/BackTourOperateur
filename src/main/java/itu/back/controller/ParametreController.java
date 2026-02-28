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

import itu.back.model.Parametre;
import itu.back.util.DatabaseConnection;

@Controller
public class ParametreController {

    // ==================== FORMULAIRE AJOUT PARAMETRE ====================
    @GetMapping("/parametre/form")
    public ModelView showForm() {
        ModelView mv = new ModelView();
        try {
            List<Parametre> parametres = getAllParametres();
            mv.addItem("parametres", parametres);
            mv.addItem("parametre", new Parametre());
            mv.addItem("isEdit", false);
            mv.setView("/parametre-form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors du chargement du formulaire : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== INSERTION PARAMETRE ====================
    @PostMapping("/parametre/insert")
    public ModelView insertParametre(
            @RequestParam("code") String code,
            @RequestParam("valeur") String valeur,
            @RequestParam("description") String description) {

        ModelView mv = new ModelView();
        try {
            if (code == null || code.trim().isEmpty()) {
                mv.addItem("error", "Le code est obligatoire.");
                mv.addItem("parametres", getAllParametres());
                mv.setView("/parametre-form.jsp");
                return mv;
            }
            if (valeur == null || valeur.trim().isEmpty()) {
                mv.addItem("error", "La valeur est obligatoire.");
                mv.addItem("parametres", getAllParametres());
                mv.setView("/parametre-form.jsp");
                return mv;
            }

            Parametre p = new Parametre(code.trim(), valeur.trim(),
                    (description != null && !description.trim().isEmpty()) ? description.trim() : null);
            insertParametre(p);

            mv.addItem("success", "Paramètre \"" + code.trim() + "\" ajouté avec succès.");
            mv.addItem("parametres", getAllParametres());
            mv.addItem("parametre", new Parametre());
            mv.addItem("isEdit", false);
            mv.setView("/parametre-form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de l'insertion : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== API JSON - Liste des paramètres ====================
    @GetMapping("/api/parametres")
    @Json
    public JsonResponse getParametresJson() {
        try {
            List<Parametre> parametres = getAllParametres();
            return JsonResponse.success(parametres);
        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur : " + e.getMessage());
        }
    }

    // ==================== SUPPRESSION PARAMETRE ====================
    @PostMapping("/parametre/delete")
    public ModelView deleteParametre(@RequestParam("id") int id) {
        ModelView mv = new ModelView();
        try {
            deleteParametreById(id);
            mv.addItem("success", "Paramètre supprimé avec succès.");
            mv.addItem("parametres", getAllParametres());
            mv.addItem("parametre", new Parametre());
            mv.addItem("isEdit", false);
            mv.setView("/parametre-form.jsp");
        } catch (SQLException e) {
            mv.addItem("error", "Erreur lors de la suppression : " + e.getMessage());
            mv.setView("/error.jsp");
        }
        return mv;
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private List<Parametre> getAllParametres() throws SQLException {
        List<Parametre> parametres = new ArrayList<>();
        String sql = "SELECT id, code, valeur, description FROM parametre ORDER BY code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Parametre p = new Parametre();
                p.setId(rs.getInt("id"));
                p.setCode(rs.getString("code"));
                p.setValeur(rs.getString("valeur"));
                p.setDescription(rs.getString("description"));
                parametres.add(p);
            }
        }
        return parametres;
    }

    private void insertParametre(Parametre p) throws SQLException {
        String sql = "INSERT INTO parametre (code, valeur, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getCode());
            ps.setString(2, p.getValeur());
            ps.setString(3, p.getDescription());
            ps.executeUpdate();
        }
    }

    private void deleteParametreById(int id) throws SQLException {
        String sql = "DELETE FROM parametre WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
