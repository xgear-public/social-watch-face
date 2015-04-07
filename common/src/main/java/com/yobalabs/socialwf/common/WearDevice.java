package com.yobalabs.socialwf.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yauhen.pelkin on 3/4/15.
 */
public class WearDevice implements Parcelable {

    private String mManufacturer;

    private String mModel;

    private int mScreenWidth;

    private int mScreenHeight;

    private boolean mRound;

    private int mChinSize;

    public WearDevice() {
    }

    public WearDevice(String manufacturer, String model, int screenWidth, int screenHeight,
            boolean round, int chinSize) {
        mManufacturer = manufacturer;
        mModel = model;
        mScreenWidth = screenWidth;
        mScreenHeight = screenHeight;
        mRound = round;
        mChinSize = chinSize;
    }

    public String getManufacturer() {
        return mManufacturer;
    }

    public void setManufacturer(String manufacturer) {
        mManufacturer = manufacturer;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public int getScreenWidth() {
        return mScreenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        mScreenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return mScreenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        mScreenHeight = screenHeight;
    }

    public boolean isRound() {
        return mRound;
    }

    public void setRound(boolean round) {
        mRound = round;
    }

    public int getChinSize() {
        return mChinSize;
    }

    public void setChinSize(int chinSize) {
        mChinSize = chinSize;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mManufacturer);
        dest.writeString(this.mModel);
        dest.writeInt(this.mScreenWidth);
        dest.writeInt(this.mScreenHeight);
        dest.writeByte(mRound ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mChinSize);
    }

    private WearDevice(Parcel in) {
        this.mManufacturer = in.readString();
        this.mModel = in.readString();
        this.mScreenWidth = in.readInt();
        this.mScreenHeight = in.readInt();
        this.mRound = in.readByte() != 0;
        this.mChinSize = in.readInt();
    }

    public static final Creator<WearDevice> CREATOR = new Creator<WearDevice>() {
        public WearDevice createFromParcel(Parcel source) {
            return new WearDevice(source);
        }

        public WearDevice[] newArray(int size) {
            return new WearDevice[size];
        }
    };
}
