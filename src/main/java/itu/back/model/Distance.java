package itu.back.model;

import java.math.BigDecimal;

public class Distance {
    private int id;
    private int idFrom;
    private int idTo;
    private BigDecimal valeur; // en km
    private Aeroport aeroportFrom;
    private Aeroport aeroportTo;

    // Constructeurs
    public Distance() {
    }

    public Distance(int id, int idFrom, int idTo, BigDecimal valeur) {
        this.id = id;
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.valeur = valeur;
    }

    public Distance(int idFrom, int idTo, BigDecimal valeur) {
        this.idFrom = idFrom;
        this.idTo = idTo;
        this.valeur = valeur;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(int idFrom) {
        this.idFrom = idFrom;
    }

    public int getIdTo() {
        return idTo;
    }

    public void setIdTo(int idTo) {
        this.idTo = idTo;
    }

    public BigDecimal getValeur() {
        return valeur;
    }

    public void setValeur(BigDecimal valeur) {
        this.valeur = valeur;
    }

    public Aeroport getAeroportFrom() {
        return aeroportFrom;
    }

    public void setAeroportFrom(Aeroport aeroportFrom) {
        this.aeroportFrom = aeroportFrom;
    }

    public Aeroport getAeroportTo() {
        return aeroportTo;
    }

    public void setAeroportTo(Aeroport aeroportTo) {
        this.aeroportTo = aeroportTo;
    }

    @Override
    public String toString() {
        return "Distance{id=" + id + ", idFrom=" + idFrom + ", idTo=" + idTo + ", valeur=" + valeur + "}";
    }
}
