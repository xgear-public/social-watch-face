package com.yobalabs.socialwf.settings;

import com.yobalabs.socialwf.model.Credentials;

import android.content.Context;


/**
 * Created by yauhen.pelkin on 2/12/15.
 */
public class MainSettings extends BaseSettings {

    private static final String PREFERENCES_NAME_LIB = "com.yobalabs.socialwf.common";

    private static final String KEY_LOGIN = "key_login";

    private static final String KEY_PASS = "key_pass";

    private static final String KEY_REG_ID = "key_reg_id";

    private static volatile MainSettings sInstance;

    private Context mAppContext;

    private MainSettings() {
    }

    public static MainSettings getInstance() {
        MainSettings localInstance = sInstance;
        if (localInstance == null) {
            synchronized (MainSettings.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new MainSettings();
                }
            }
        }
        return localInstance;
    }

    public void init(Context context) {
        mAppContext = context.getApplicationContext();
    }

    public Credentials getCredentials() {
        String login = read().getString(KEY_LOGIN, "");
        String pass = read().getString(KEY_LOGIN, "");
        return new Credentials(login, pass);
    }

    public void setCredentials(Credentials credentials) {
        write().putString(KEY_LOGIN, credentials.getUsername())
                .putString(KEY_PASS, credentials.getPassword()).commit();
    }

    public void setRegId(String regId) {
        write().putString(KEY_REG_ID, regId).commit();
    }

    public String getRegId() {
        return read().getString(KEY_REG_ID, "");
    }

    @Override
    public String getPrefsFileName() {
        return PREFERENCES_NAME_LIB;
    }

    @Override
    public Context getAppContext() {
        return mAppContext;
    }

}
