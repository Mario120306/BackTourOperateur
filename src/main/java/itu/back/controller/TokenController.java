package itu.back.controller;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.itu.framework.annotation.Controller;
import com.itu.framework.annotation.GetMapping;
import com.itu.framework.annotation.Json;
import com.itu.framework.annotation.PostMapping;
import com.itu.framework.annotation.RequestParam;
import com.itu.framework.response.JsonResponse;

import itu.back.model.Token;
import itu.back.util.TokenUtil;

/**
 * Contrôleur pour la gestion des tokens JWT.
 * 
 * Note: Les endpoints de ce contrôleur ne sont PAS protégés par le TokenFilter
 * car ils sont utilisés pour générer de nouveaux tokens.
 */
@Controller
public class TokenController {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Génère un nouveau token avec la durée de validité par défaut (24h)
     * 
     * POST /token/generate
     */
    @PostMapping("/token/generate")
    @Json
    public JsonResponse generateToken() {
        return generateTokenWithValidity(24);
    }

    /**
     * Génère un nouveau token avec une durée de validité personnalisée
     * 
     * POST /token/generate/{validityHours}
     * 
     * @param validityHours durée de validité en heures
     */
    @PostMapping("/token/generate/{validityHours}")
    @Json
    public JsonResponse generateTokenCustom(@RequestParam("validityHours") int validityHours) {
        return generateTokenWithValidity(validityHours);
    }

    /**
     * Génère un token avec une durée de validité spécifiée
     */
    private JsonResponse generateTokenWithValidity(int validityHours) {
        try {
            if (validityHours <= 0) {
                return JsonResponse.error("La durée de validité doit être positive.", 400);
            }

            long validityMs = (long) validityHours * 60 * 60 * 1000;
            Token token = TokenUtil.generateToken(validityMs);

            // Créer un objet de réponse avec les informations du token
            TokenResponse response = new TokenResponse();
            response.token = token.getToken();
            response.expiresAt = dateFormat.format(token.getDateExpiration());
            response.validityHours = validityHours;

            return JsonResponse.success(response);

        } catch (SQLException e) {
            return JsonResponse.serverError("Erreur lors de la génération du token: " + e.getMessage());
        }
    }

    /**
     * Valide un token existant
     * 
     * GET /token/validate
     * Header: Authorization: Bearer <token>
     */
    @GetMapping("/token/validate")
    @Json
    public JsonResponse validateToken(@RequestParam("token") String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return JsonResponse.error("Token manquant.", 400);
        }

        boolean isValid = TokenUtil.validateToken(tokenValue);
        Token token = TokenUtil.getToken(tokenValue);

        if (token == null) {
            return JsonResponse.notFound("Token non trouvé.");
        }

        TokenValidationResponse response = new TokenValidationResponse();
        response.valid = isValid;
        response.expired = token.isExpired();
        response.expiresAt = dateFormat.format(token.getDateExpiration());

        if (isValid) {
            return JsonResponse.success(response);
        } else {
            return JsonResponse.error("Token invalide ou expiré.", 401);
        }
    }

    /**
     * Révoque (supprime) un token
     * 
     * POST /token/revoke
     */
    @PostMapping("/token/revoke")
    @Json
    public JsonResponse revokeToken(@RequestParam("token") String tokenValue) {
        if (tokenValue == null || tokenValue.isEmpty()) {
            return JsonResponse.error("Token manquant.", 400);
        }

        boolean deleted = TokenUtil.deleteToken(tokenValue);

        if (deleted) {
            return JsonResponse.success("Token révoqué avec succès.");
        } else {
            return JsonResponse.notFound("Token non trouvé.");
        }
    }

    /**
     * Nettoie tous les tokens expirés
     * 
     * POST /token/cleanup
     */
    @PostMapping("/token/cleanup")
    @Json
    public JsonResponse cleanupTokens() {
        int cleaned = TokenUtil.cleanExpiredTokens();
        return JsonResponse.success("Nombre de tokens expirés supprimés: " + cleaned);
    }

    // Classes internes pour les réponses JSON
    private static class TokenResponse {
        public String token;
        public String expiresAt;
        public int validityHours;
    }

    private static class TokenValidationResponse {
        public boolean valid;
        public boolean expired;
        public String expiresAt;
    }
}
