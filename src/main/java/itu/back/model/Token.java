package itu.back.model;

import java.sql.Timestamp;

public class Token {
    private int id;
    private String token;
    private Timestamp dateExpiration;
    private Timestamp dateCreation;

    // Constructeurs
    public Token() {
    }

    public Token(String token, Timestamp dateExpiration) {
        this.token = token;
        this.dateExpiration = dateExpiration;
    }

    public Token(int id, String token, Timestamp dateExpiration, Timestamp dateCreation) {
        this.id = id;
        this.token = token;
        this.dateExpiration = dateExpiration;
        this.dateCreation = dateCreation;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getDateExpiration() {
        return dateExpiration;
    }

    public void setDateExpiration(Timestamp dateExpiration) {
        this.dateExpiration = dateExpiration;
    }

    public Timestamp getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Timestamp dateCreation) {
        this.dateCreation = dateCreation;
    }

    /**
     * Vérifie si le token est expiré
     * 
     * @return true si le token est expiré, false sinon
     */
    public boolean isExpired() {
        return dateExpiration != null && dateExpiration.before(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", dateExpiration=" + dateExpiration +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
