package itu.back.filter;

import java.io.IOException;
import java.io.PrintWriter;

import itu.back.util.TokenUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtre pour vérifier la présence et la validité du token JWT
 * pour les endpoints API qui nécessitent une authentification.
 */
@WebFilter(urlPatterns = "/api/*")
public class TokenFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialisation si nécessaire
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestUri = httpRequest.getRequestURI();

        // Exclure les endpoints de gestion des tokens (ils ne nécessitent pas
        // d'authentification)
        if (requestUri.contains("/token/")) {
            chain.doFilter(request, response);
            return;
        }

        // Vérifier si c'est un endpoint API
        if (requestUri.contains("/api/")) {
            // Récupérer le token depuis le header Authorization
            String authHeader = httpRequest.getHeader("Authorization");

            // Vérifier si le token est présent
            if (authHeader == null || authHeader.isEmpty()) {
                sendUnauthorizedResponse(httpResponse,
                        "Token manquant. Veuillez fournir un token dans le header Authorization.");
                return;
            }

            // Vérifier le format du token (Bearer token)
            if (!authHeader.startsWith("Bearer ")) {
                sendUnauthorizedResponse(httpResponse, "Format de token invalide. Utilisez: Bearer <votre_token>");
                return;
            }

            // Extraire et valider le token
            String token = authHeader.substring(7);

            if (!TokenUtil.validateToken(token)) {
                // Vérifier si le token existe mais est expiré
                itu.back.model.Token tokenObj = TokenUtil.getToken(token);
                if (tokenObj != null && tokenObj.isExpired()) {
                    sendUnauthorizedResponse(httpResponse, "Token expiré. Veuillez générer un nouveau token.");
                } else {
                    sendUnauthorizedResponse(httpResponse, "Token invalide ou inexistant.");
                }
                return;
            }

            // Token valide, continuer la chaîne
            chain.doFilter(request, response);
        } else {
            // Pas un endpoint API, continuer sans vérification
            chain.doFilter(request, response);
        }
    }

    /**
     * Envoie une réponse 401 Unauthorized en JSON
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json; charset=UTF-8");

        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"success\": false,");
        out.print("\"status\": 401,");
        out.print("\"error\": \"Unauthorized\",");
        out.print("\"message\": \"" + message + "\"");
        out.print("}");
        out.flush();
    }

    @Override
    public void destroy() {
        // Nettoyage si nécessaire
    }
}
