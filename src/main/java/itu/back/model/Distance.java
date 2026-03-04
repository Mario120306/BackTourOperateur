package itu.back.model;

import java.math.BigDecimal;

public class Distance {
    private int id;
    private Integer idFromHotel; // Peut être NULL si départ depuis aéroport
    private Integer idFromAeroport; // Peut être NULL si départ depuis hôtel
    private int idTo; // ID de l'hôtel de destination (NOT NULL)
    private BigDecimal valeur; // Distance en km

    // Objets liés (pour jointures)
    private Hotel hotelFrom;
    private Aeroport aeroportFrom;
    private Hotel hotelTo;

    // Constructeurs
    public Distance() {
    }

    public Distance(int id, Integer idFromHotel, Integer idFromAeroport, int idTo, BigDecimal valeur) {
        this.id = id;
        this.idFromHotel = idFromHotel;
        this.idFromAeroport = idFromAeroport;
        this.idTo = idTo;
        this.valeur = valeur;
    }

    public Distance(Integer idFromHotel, Integer idFromAeroport, int idTo, BigDecimal valeur) {
        this.idFromHotel = idFromHotel;
        this.idFromAeroport = idFromAeroport;
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

    public Integer getIdFromHotel() {
        return idFromHotel;
    }

    public void setIdFromHotel(Integer idFromHotel) {
        this.idFromHotel = idFromHotel;
    }

    public Integer getIdFromAeroport() {
        return idFromAeroport;
    }

    public void setIdFromAeroport(Integer idFromAeroport) {
        this.idFromAeroport = idFromAeroport;
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

    public Hotel getHotelFrom() {
        return hotelFrom;
    }

    public void setHotelFrom(Hotel hotelFrom) {
        this.hotelFrom = hotelFrom;
    }

    public Aeroport getAeroportFrom() {
        return aeroportFrom;
    }

    public void setAeroportFrom(Aeroport aeroportFrom) {
        this.aeroportFrom = aeroportFrom;
    }

    public Hotel getHotelTo() {
        return hotelTo;
    }

    public void setHotelTo(Hotel hotelTo) {
        this.hotelTo = hotelTo;
    }

    @Override
    public String toString() {
        return "Distance{id=" + id + ", idFromHotel=" + idFromHotel + ", idFromAeroport=" + idFromAeroport + ", idTo="
                + idTo + ", valeur=" + valeur + "}";
    }
}
