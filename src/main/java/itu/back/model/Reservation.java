package itu.back.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int idClient;
    private int nombrePassage;
    private Timestamp dateHeureArrive;
    private Timestamp dateReservation;
    private int idHotel;
    
    // Nouveaux champs pour planification véhicule
    private Integer idVehicule;
    private Integer idAeroport;
    private BigDecimal distanceKm;
    private Integer tempsEstimeMinutes;
    private Timestamp heureDepart;

    // Relations (pour l'affichage)
    private Client client;
    private Hotel hotel;
    private Vehicule vehicule;
    private Aeroport aeroport;

    // Constructeurs
    public Reservation() {
    }

    public Reservation(int id, int idClient, int nombrePassage, Timestamp dateHeureArrive,
            Timestamp dateReservation, int idHotel) {
        this.id = id;
        this.idClient = idClient;
        this.nombrePassage = nombrePassage;
        this.dateHeureArrive = dateHeureArrive;
        this.dateReservation = dateReservation;
        this.idHotel = idHotel;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdClient() {
        return idClient;
    }

    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }

    public int getNombrePassage() {
        return nombrePassage;
    }

    public void setNombrePassage(int nombrePassage) {
        this.nombrePassage = nombrePassage;
    }

    public Timestamp getDateHeureArrive() {
        return dateHeureArrive;
    }

    public void setDateHeureArrive(Timestamp dateHeureArrive) {
        this.dateHeureArrive = dateHeureArrive;
    }

    public Timestamp getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(Timestamp dateReservation) {
        this.dateReservation = dateReservation;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public Integer getIdVehicule() {
        return idVehicule;
    }

    public void setIdVehicule(Integer idVehicule) {
        this.idVehicule = idVehicule;
    }

    public Integer getIdAeroport() {
        return idAeroport;
    }

    public void setIdAeroport(Integer idAeroport) {
        this.idAeroport = idAeroport;
    }

    public BigDecimal getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(BigDecimal distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getTempsEstimeMinutes() {
        return tempsEstimeMinutes;
    }

    public void setTempsEstimeMinutes(Integer tempsEstimeMinutes) {
        this.tempsEstimeMinutes = tempsEstimeMinutes;
    }

    public Timestamp getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(Timestamp heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public Aeroport getAeroport() {
        return aeroport;
    }

    public void setAeroport(Aeroport aeroport) {
        this.aeroport = aeroport;
    }

    /**
     * Retourne le temps formaté en heures et minutes
     */
    public String getTempsFormate() {
        if (tempsEstimeMinutes == null) return "N/A";
        int heures = tempsEstimeMinutes / 60;
        int minutes = tempsEstimeMinutes % 60;
        if (heures > 0) {
            return heures + "h " + minutes + "min";
        }
        return minutes + " min";
    }
}
