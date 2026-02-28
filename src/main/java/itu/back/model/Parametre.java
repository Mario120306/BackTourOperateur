package itu.back.model;

public class Parametre {
    private int id;
    private String code;
    private String valeur;
    private String description;

    // Constructeurs
    public Parametre() {
    }

    public Parametre(int id, String code, String valeur, String description) {
        this.id = id;
        this.code = code;
        this.valeur = valeur;
        this.description = description;
    }

    public Parametre(String code, String valeur, String description) {
        this.code = code;
        this.valeur = valeur;
        this.description = description;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValeur() {
        return valeur;
    }

    public void setValeur(String valeur) {
        this.valeur = valeur;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Parametre{id=" + id + ", code='" + code + "', valeur='" + valeur + "', description='" + description + "'}";
    }
}
