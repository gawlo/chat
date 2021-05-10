package org.tigase.messenger.phone.pro.API;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCLient {
    public static final String BASE_URL = "https://sauron.orange-bissau.com/";
    private static Retrofit retrofit = null;

    public static final String BASE_URL_db = "https://sauron.orange-bissau.com/gm/getaway/";

    private static Retrofit retrofit_db = null;

    public static Retrofit getClient() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getClient_db(){
/*        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();*/
        Gson gson_db = new GsonBuilder()
                .setLenient()
                .create();
        if(retrofit_db == null){
            retrofit_db = new Retrofit.Builder()
                    .baseUrl(BASE_URL_db)
  //                  .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson_db))
                    .build();
        }
        return retrofit_db;
    }
}
