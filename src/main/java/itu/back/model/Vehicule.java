package itu.back.model;

public class Vehicule {
    private int id;
    private String marque;
    private String modele;
    private int nombrePlaces;
    private String reference;
    private int vitesseMoyenne;
    private int typeCarburantId;
    private TypeCarburant typeCarburant;

    // Constructeurs
    public Vehicule() {
    }

    public Vehicule(int id, String marque, String modele, int nombrePlaces, String reference, int vitesseMoyenne, int typeCarburantId) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.nombrePlaces = nombrePlaces;
        this.reference = reference;
        this.vitesseMoyenne = vitesseMoyenne;
        this.typeCarburantId = typeCarburantId;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public int getNombrePlaces() {
        return nombrePlaces;
    }

    public void setNombrePlaces(int nombrePlaces) {
        this.nombrePlaces = nombrePlaces;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getVitesseMoyenne() {
        return vitesseMoyenne;
    }

    public void setVitesseMoyenne(int vitesseMoyenne) {
        this.vitesseMoyenne = vitesseMoyenne;
    }

    public int getTypeCarburantId() {
        return typeCarburantId;
    }

    public void setTypeCarburantId(int typeCarburantId) {
        this.typeCarburantId = typeCarburantId;
    }

    public TypeCarburant getTypeCarburant() {
        return typeCarburant;
    }

    public void setTypeCarburant(TypeCarburant typeCarburant) {
        this.typeCarburant = typeCarburant;
    }
}
