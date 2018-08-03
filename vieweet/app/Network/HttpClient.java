package com.vieweet.app.Network;

import com.vieweet.app.Constants;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface HttpClient {

    @Multipart
    @POST
    @Headers(Constants.USER_AGENT)
    Call<ServerResponse> LoginTask(
            @Url String url,
            @Part("login") RequestBody login,
            @Part("password") RequestBody password,
            @Part("todo") RequestBody todo);

    @Multipart
    @POST
    @Headers(Constants.USER_AGENT)
    Call<ServerResponse> RegisterTask(
            @Url String url,
            @Part("login") RequestBody login,
            @Part("password") RequestBody password,
            @Part("todo") RequestBody todo);

    @POST
    @Headers(Constants.USER_AGENT)
    Call<ServerResponse> InitTask(
            @Url String url);

    @Multipart
    @POST
    @Headers(Constants.USER_AGENT)
    Call<ServerResponse> SynchronizeTask(
            @Url String url,
            @Part("MD5ALB") RequestBody md5Albums,
            @Part("MD5PIC") RequestBody md5Pictures,
            @Part("MD5VID") RequestBody md5Videos,
            @Part("MD5HOT") RequestBody md5Hotspot,
            @Part("MD5CAM") RequestBody md5Cameras);

    @Multipart
    @POST
    @Headers(Constants.USER_AGENT)
    Call<ServerResponse> UpdateTask(
            @Url String url,
            @Part("data") RequestBody data);

}
