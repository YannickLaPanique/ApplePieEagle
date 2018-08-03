package com.vieweet.app.Network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vieweet.app.Constants;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {

    private static final String BASE_URL = Constants.URL_SERVER;

    private static Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();


    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY);

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
            .addInterceptor(logging);

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
            .client(httpClient.build());

    private static Retrofit retrofit = builder.build();

    public  static <S> S createService(Class<S> serviceClass){
        return retrofit.create(serviceClass);
    }

}
