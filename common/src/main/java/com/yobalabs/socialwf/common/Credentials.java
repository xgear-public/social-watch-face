package com.yobalabs.socialwf.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class Credentials implements Parcelable {

    private String mUsername;

    private String mPassword;

    private String mDeviceId;

    public Credentials(String username, String password, String deviceId) {
        this(username, password);
        mDeviceId = deviceId;
    }

    public Credentials(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mUsername);
        dest.writeString(this.mPassword);
        dest.writeString(this.mDeviceId);
    }

    private Credentials(Parcel in) {
        this.mUsername = in.readString();
        this.mPassword = in.readString();
        this.mDeviceId = in.readString();
    }

    public static final Creator<Credentials> CREATOR
            = new Creator<Credentials>() {
        public Credentials createFromParcel(Parcel source) {
            return new Credentials(source);
        }

        public Credentials[] newArray(int size) {
            return new Credentials[size];
        }
    };

    @Override
    public String toString() {
        return "Credentials{" +
                "mUsername='" + mUsername + '\'' +
                ", mPassword='" + mPassword + '\'' +
                ", mDeviceId='" + mDeviceId + '\'' +
                '}';
    }
}
