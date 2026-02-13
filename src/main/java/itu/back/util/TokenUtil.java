package itu.back.util;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Base64;

import itu.back.model.Token;

/**
 * Utilitaire pour la gestion des tokens JWT
 */
public class TokenUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    // Durée de validité du token par défaut (24 heures en millisecondes)
    private static final long DEFAULT_TOKEN_VALIDITY_MS = 24 * 60 * 60 * 1000;

    /**
     * Génère un nouveau token avec une durée de validité par défaut (24h)
     * 
     * @return le token généré
     * @throws SQLException en cas d'erreur de base de données
     */
    public static Token generateToken() throws SQLException {
        return generateToken(DEFAULT_TOKEN_VALIDITY_MS);
    }

    /**
     * Génère un nouveau token avec une durée de validité personnalisée
     * 
     * @param validityMs durée de validité en millisecondes
     * @return le token généré
     * @throws SQLException en cas d'erreur de base de données
     */
    public static Token generateToken(long validityMs) throws SQLException {
        // Générer un token aléatoire sécurisé
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        String tokenValue = base64Encoder.encodeToString(randomBytes);

        // Calculer la date d'expiration
        Timestamp dateExpiration = new Timestamp(System.currentTimeMillis() + validityMs);

        // Sauvegarder en base de données
        Token token = new Token(tokenValue, dateExpiration);
        saveToken(token);

        return token;
    }

    /**
     * Sauvegarde un token en base de données
     * 
     * @param token le token à sauvegarder
     * @throws SQLException en cas d'erreur de base de données
     */
    public static void saveToken(Token token) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "INSERT INTO token (token, date_expiration) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            stmt.setString(1, token.getToken());
            stmt.setTimestamp(2, token.getDateExpiration());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                token.setId(rs.getInt(1));
            }
            rs.close();
            stmt.close();
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Valide un token - vérifie qu'il existe et n'est pas expiré
     * 
     * @param tokenValue la valeur du token à valider
     * @return true si le token est valide, false sinon
     */
    public static boolean validateToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return false;
        }

        // Retirer le préfixe "Bearer " si présent
        if (tokenValue.startsWith("Bearer ")) {
            tokenValue = tokenValue.substring(7);
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, token, date_expiration, date_creation FROM token WHERE token = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tokenValue);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Timestamp dateExpiration = rs.getTimestamp("date_expiration");
                Timestamp now = new Timestamp(System.currentTimeMillis());

                // Vérifier si le token n'est pas expiré
                boolean isValid = dateExpiration.after(now);

                rs.close();
                stmt.close();
                return isValid;
            }

            rs.close();
            stmt.close();
            return false; // Token non trouvé

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Récupère un token depuis la base de données
     * 
     * @param tokenValue la valeur du token
     * @return le Token ou null si non trouvé
     */
    public static Token getToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return null;
        }

        // Retirer le préfixe "Bearer " si présent
        if (tokenValue.startsWith("Bearer ")) {
            tokenValue = tokenValue.substring(7);
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "SELECT id, token, date_expiration, date_creation FROM token WHERE token = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tokenValue);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Token token = new Token();
                token.setId(rs.getInt("id"));
                token.setToken(rs.getString("token"));
                token.setDateExpiration(rs.getTimestamp("date_expiration"));
                token.setDateCreation(rs.getTimestamp("date_creation"));

                rs.close();
                stmt.close();
                return token;
            }

            rs.close();
            stmt.close();
            return null;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Supprime un token de la base de données
     * 
     * @param tokenValue la valeur du token à supprimer
     * @return true si le token a été supprimé, false sinon
     */
    public static boolean deleteToken(String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return false;
        }

        // Retirer le préfixe "Bearer " si présent
        if (tokenValue.startsWith("Bearer ")) {
            tokenValue = tokenValue.substring(7);
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "DELETE FROM token WHERE token = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, tokenValue);
            int rows = stmt.executeUpdate();
            stmt.close();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }

    /**
     * Supprime tous les tokens expirés de la base de données
     * 
     * @return le nombre de tokens supprimés
     */
    public static int cleanExpiredTokens() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            String sql = "DELETE FROM token WHERE date_expiration < ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            int rows = stmt.executeUpdate();
            stmt.close();
            return rows;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DatabaseConnection.closeConnection(conn);
        }
    }
}
