package itu.back.dto;

import itu.back.model.Client;
import itu.back.model.Hotel;
import itu.back.model.Reservation;

/**
 * DTO pour la sérialisation JSON des réservations
 */
public class ReservationDTO {
    private int id;
    private int idClient;
    private int idHotel;
    private int nombrePassage;
    private String dateHeureArrive;
    private Client client;
    private Hotel hotel;

    public ReservationDTO() {
    }

    public ReservationDTO(Reservation reservation) {
        this.id = reservation.getId();
        this.idClient = reservation.getIdClient();
        this.idHotel = reservation.getIdHotel();
        this.nombrePassage = reservation.getNombrePassage();
        this.dateHeureArrive = reservation.getDateHeureArrive() != null
                ? reservation.getDateHeureArrive().toString()
                : null;
        this.client = reservation.getClient();
        this.hotel = reservation.getHotel();
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
