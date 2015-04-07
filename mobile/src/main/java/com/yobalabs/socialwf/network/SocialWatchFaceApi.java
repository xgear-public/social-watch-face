package com.yobalabs.socialwf.network;

import com.yobalabs.socialwf.model.Credentials;
import com.yobalabs.socialwf.model.LoginResult;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

/**
 * Created by Yauhen on 23.03.2015.
 */
public interface SocialWatchFaceApi {

    @POST("/login")
    void login(@Body Credentials user, Callback<LoginResult> cb);

}
