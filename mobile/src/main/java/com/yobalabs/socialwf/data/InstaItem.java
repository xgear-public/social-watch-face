package com.yobalabs.socialwf.data;

import com.google.gson.annotations.SerializedName;

import android.os.Parcel;
import android.os.Parcelable;

import nl.qbusict.cupboard.annotation.Index;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class InstaItem implements Parcelable {

    //do not change this values of this constants and keep them equal corresponding column values.
    public static final String URLS = "mUrls";

    public static final String POST_ID = "mPostId";

    public static final String HANDLED = "mHandled";


    //do not change this declared names: start
    public Long _id;

    private String mUrls;

    private String mPostId;

    private boolean mHandled;

    public InstaItem() {
    }

    public InstaItem(String postId, String urls) {
        mPostId = postId;
        mUrls = urls;
    }

    public Long getId() {
        return _id;
    }

    public void setId(Long _id) {
        this._id = _id;
    }

    public String getUrls() {
        return mUrls;
    }

    public void setUrls(String urls) {
        mUrls = urls;
    }

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }

    public boolean isHandled() {
        return mHandled;
    }

    public void setHandled(boolean handled) {
        mHandled = handled;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this._id);
        dest.writeString(this.mUrls);
        dest.writeString(this.mPostId);
        dest.writeByte(mHandled ? (byte) 1 : (byte) 0);
    }

    private InstaItem(Parcel in) {
        this._id = (Long) in.readValue(Long.class.getClassLoader());
        this.mUrls = in.readString();
        this.mPostId = in.readString();
        this.mHandled = in.readByte() != 0;
    }

    public static final Parcelable.Creator<InstaItem> CREATOR
            = new Parcelable.Creator<InstaItem>() {
        public InstaItem createFromParcel(Parcel source) {
            return new InstaItem(source);
        }

        public InstaItem[] newArray(int size) {
            return new InstaItem[size];
        }
    };
}
