package org.tigase.messenger.phone.pro.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class Verification implements Serializable {
    @SerializedName("id")
    private Integer id;
    @SerializedName("numero")
    private String numero;
    @SerializedName("code")
    private String code;
    @SerializedName("date_debut")
    private Date date_debut;
    @SerializedName("date_fin")
    private Date date_fin;
    @SerializedName("etat")
    private Integer etat;

    public Verification(){

    }

    public Verification(String numero, String code, Date date_debut, Date date_fin, Integer etat){
        this.numero = numero;
        this.code = code;
        this.date_debut = date_debut;
        this.date_fin = date_fin;
        this.etat = etat;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getDate_debut() {
        return date_debut;
    }

    public void setDate_debut(Date date_debut) {
        this.date_debut = date_debut;
    }

    public Date getDate_fin() {
        return date_fin;
    }

    public void setDate_fin(Date date_fin) {
        this.date_fin = date_fin;
    }

    public Integer getEtat() {
        return etat;
    }

    public void setEtat(Integer etat) {
        this.etat = etat;
    }
}
