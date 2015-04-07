package com.yobalabs.socialwf.activity;

import com.yobalabs.socialwf.SocialWfApp;
import com.yobalabs.socialwf.network.SocialWatchFaceApi;

import android.support.v7.app.ActionBarActivity;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class BaseActivity extends ActionBarActivity {

    public SocialWatchFaceApi getSocialWatchFaceApi() {
        return getApp().getSocialWatchFaceApi();
    }

    private SocialWfApp getApp() {
        return (SocialWfApp) getApplication();
    }
}
