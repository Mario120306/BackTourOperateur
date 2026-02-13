package itu.back.model;

public class TypeCarburant {
    private int id;
    private String reference;
    private String nom;

    // Constructeurs
    public TypeCarburant() {
    }

    public TypeCarburant(int id, String reference, String nom) {
        this.id = id;
        this.reference = reference;
        this.nom = nom;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
