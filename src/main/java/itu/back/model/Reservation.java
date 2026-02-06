package itu.back.model;

import java.sql.Timestamp;

public class Reservation {
    private int id;
    private int idClient;
    private int nombrePassage;
    private Timestamp dateHeureArrive;
    private Timestamp dateReservation;
    private int idHotel;

    // Relations (pour l'affichage)
    private Client client;
    private Hotel hotel;

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
}
