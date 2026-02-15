package itu.back.util;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import itu.back.model.Token;

/**
 * Classe main pour générer un token JWT depuis la ligne de commande
 * 
 * Usage: java itu.back.util.TokenGenerator [validityHours]
 * 
 * Exemples:
 * java itu.back.util.TokenGenerator -> Génère un token valide 24h
 * java itu.back.util.TokenGenerator 48 -> Génère un token valide 48h
 * java itu.back.util.TokenGenerator 1 -> Génère un token valide 1h
 */
public class TokenGenerator {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("      GENERATEUR DE TOKEN JWT");
        System.out.println("==============================================");
        System.out.println();

        try {
            // Durée de validité par défaut : 24 heures
            long validityHours = 24;

            // Si un argument est passé, l'utiliser comme durée de validité
            if (args.length > 0) {
                try {
                    validityHours = Long.parseLong(args[0]);
                    if (validityHours <= 0) {
                        System.err.println("Erreur: La durée de validité doit être positive.");
                        System.exit(1);
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erreur: L'argument doit être un nombre entier (heures).");
                    System.err.println("Usage: java itu.back.util.TokenGenerator [validityHours]");
                    System.exit(1);
                }
            }

            // Convertir en millisecondes
            long validityMs = validityHours * 60 * 60 * 1000;

            System.out.println("Génération d'un nouveau token...");
            System.out.println("Durée de validité: " + validityHours + " heure(s)");
            System.out.println();

            // Générer le token
            Token token = TokenUtil.generateToken(validityMs);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            System.out.println("TOKEN GENERE AVEC SUCCES!");
            System.out.println("----------------------------------------------");
            System.out.println();
            System.out.println("Token: " + token.getToken());
            System.out.println();
            System.out.println("Date d'expiration: " + dateFormat.format(token.getDateExpiration()));
            System.out.println("----------------------------------------------");
            System.out.println();
            System.out.println("Pour utiliser ce token dans vos appels API:");
            System.out.println("  Header: Authorization: Bearer " + token.getToken());
            System.out.println();
            System.out.println("Exemple avec curl:");
            System.out.println("  curl -H \"Authorization: Bearer " + token.getToken()
                    + "\" http://localhost:8080/api/reservations");
            System.out.println();
            System.out.println("==============================================");

            // Nettoyer les tokens expirés
            int cleaned = TokenUtil.cleanExpiredTokens();
            if (cleaned > 0) {
                System.out.println("Note: " + cleaned + " token(s) expiré(s) ont été nettoyés.");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la génération du token:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
