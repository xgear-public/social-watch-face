package com.yobalabs.socialwf.common;

import android.os.Parcel;


/**
 * Created by yauhen.pelkin on 2/13/15.
 */
public class AnalogBasicWFPreview extends WFPreview
        implements AnalogBasicConfig, android.os.Parcelable {

    private int mInstaUnread;

    @Override
    public int getInstaUnread() {
        return mInstaUnread;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mInstaUnread);
        dest.writeInt(this.mShape == null ? -1 : this.mShape.ordinal());
    }

    public AnalogBasicWFPreview() {
    }

    private AnalogBasicWFPreview(Parcel in) {
        this.mInstaUnread = in.readInt();
        int tmpMShape = in.readInt();
        this.mShape = tmpMShape == -1 ? null : WearableShape.values()[tmpMShape];
    }

    public static final Creator<AnalogBasicWFPreview> CREATOR
            = new Creator<AnalogBasicWFPreview>() {
        public AnalogBasicWFPreview createFromParcel(Parcel source) {
            return new AnalogBasicWFPreview(source);
        }

        public AnalogBasicWFPreview[] newArray(int size) {
            return new AnalogBasicWFPreview[size];
        }
    };
}
