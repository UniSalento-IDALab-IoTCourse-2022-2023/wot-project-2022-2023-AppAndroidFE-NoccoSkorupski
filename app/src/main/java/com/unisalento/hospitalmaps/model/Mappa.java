package com.unisalento.hospitalmaps.model;

public class Mappa {
    private String beaconUUID;
    private String nord;
    private String sud;
    private String est;
    private String ovest;
    private int posizione;

    // Metodi getter e setter per le proprietà

    public String getBeaconUUID() {
        return beaconUUID;
    }

    public void setBeaconUUID(String beaconUUID) {
        this.beaconUUID = beaconUUID;
    }

    public String getNord() {
        return nord;
    }

    public void setNord(String nord) {
        this.nord = nord;
    }

    public String getSud() {
        return sud;
    }

    public void setSud(String sud) {
        this.sud = sud;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }

    public String getOvest() {
        return ovest;
    }

    public void setOvest(String ovest) {
        this.ovest = ovest;
    }

    public int getPosizione() {
        return posizione;
    }

    public void setPosizione(int posizione) {
        this.posizione = posizione;
    }
}
