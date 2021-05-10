package org.tigase.messenger.phone.pro.API;


import org.tigase.messenger.phone.pro.models.Verification;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface { //sendsms/send-sms/send/955190656/Hola/mois
@POST("sendsms/send-sms/send/{numero}/{message}/{expediteur}")
     Call<String> getsendsms(@Path("numero") String numero, @Path("message") String message, @Path("expediteur") String expediteur);
@POST("gener/gener-II")
     Call<String> getCode();

// FOR DATABASE
// liste de tous les numeros
@GET("v/listenumero")
     Call<List<Verification>> getListenumero();

// objet by numero
@GET("v/chec/{numero}")
     Call<Verification> getVerificationByNumero(@Path("numero") String numero);

     // sauvegarder dans la table verification
@POST("v/save/{numero}/{code}/{etat}")
     Call<Verification> saveVerification1(@Path("numero") String numero, @Path("code") String code, @Path("etat") Integer etat);

     // modifier le code
@PUT("v/update/{numero}/{code}")
     Call<Verification> UpdateVerification(@Path("numero") String numero, @Path("code") String code);

}
