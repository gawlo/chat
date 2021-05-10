package org.tigase.messenger.phone.pro.db;

import android.os.Parcel;
import android.os.Parcelable;

public class Tb_Verification implements Parcelable {
    private int id;
    private String numero;
    private String etat;
     public Tb_Verification(){

     }
     public Tb_Verification(int id, String numero, String etat){
         this.id = id;
         this.numero = numero;
         this.etat = etat;
     }

    protected Tb_Verification(Parcel in) {
        id = in.readInt();
        numero = in.readString();
        etat = in.readString();
    }

    public static final Creator<Tb_Verification> CREATOR = new Creator<Tb_Verification>() {
        @Override
        public Tb_Verification createFromParcel(Parcel in) {
            return new Tb_Verification(in);
        }

        @Override
        public Tb_Verification[] newArray(int size) {
            return new Tb_Verification[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(numero);
        dest.writeString(etat);
    }
}
