package itu.back.model;

import java.math.BigDecimal;

/**
 * DTO pour la sérialisation JSON des réservations
 */
public class ReservationDTO {
    private int id;
    private int idClient;
    private int idHotel;
    private int nombrePassage;
    private String dateHeureArrive;
    private String dateReservation;
    private Client client;
    private Hotel hotel;
    
    // Nouveaux champs pour l'optimisation véhicule
    private Integer idVehicule;
    private Integer idAeroport;
    private BigDecimal distanceKm;
    private Integer tempsEstimeMinutes;
    private String heureDepart;
    private Vehicule vehicule;
    private Aeroport aeroport;
    private String tempsFormate;

    public ReservationDTO() {
    }

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.idClient = reservation.getIdClient();
        this.idHotel = reservation.getIdHotel();
        this.nombrePassage = reservation.getNombrePassage();
        this.dateHeureArrive = reservation.getDateHeureArrive() != null 
            ? reservation.getDateHeureArrive().toString() : null;
        this.dateReservation = reservation.getDateReservation() != null 
            ? reservation.getDateReservation().toString() : null;
        this.client = reservation.getClient();
        this.hotel = reservation.getHotel();
        
        // Nouveaux champs
        this.idVehicule = reservation.getIdVehicule();
        this.idAeroport = reservation.getIdAeroport();
        this.distanceKm = reservation.getDistanceKm();
        this.tempsEstimeMinutes = reservation.getTempsEstimeMinutes();
        this.heureDepart = reservation.getHeureDepart() != null 
            ? reservation.getHeureDepart().toString() : null;
        this.vehicule = reservation.getVehicule();
        this.aeroport = reservation.getAeroport();
        this.tempsFormate = reservation.getTempsFormate();
    }

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

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public int getNombrePassage() {
        return nombrePassage;
    }

    public void setNombrePassage(int nombrePassage) {
        this.nombrePassage = nombrePassage;
    }

    public String getDateHeureArrive() {
        return dateHeureArrive;
    }

    public void setDateHeureArrive(String dateHeureArrive) {
        this.dateHeureArrive = dateHeureArrive;
    }

    public String getDateReservation() {
        return dateReservation;
    }

    public void setDateReservation(String dateReservation) {
        this.dateReservation = dateReservation;
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

    // Nouveaux getters et setters
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

    public String getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(String heureDepart) {
        this.heureDepart = heureDepart;
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

    public String getTempsFormate() {
        return tempsFormate;
    }

    public void setTempsFormate(String tempsFormate) {
        this.tempsFormate = tempsFormate;
    }
}
