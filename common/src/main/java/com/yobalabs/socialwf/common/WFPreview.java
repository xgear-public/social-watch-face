package com.yobalabs.socialwf.common;

import android.os.Parcel;

/**
 * Created by yauhen.pelkin on 2/13/15.
 */
public class WFPreview implements Config, android.os.Parcelable {

    protected WearableShape mShape;

    @Override
    public WearableShape getShape() {
        return mShape;
    }

    @Override
    public void setShape(WearableShape shape) {
        mShape = shape;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mShape == null ? -1 : this.mShape.ordinal());
    }

    public WFPreview() {
    }

    private WFPreview(Parcel in) {
        int tmpMShape = in.readInt();
        this.mShape = tmpMShape == -1 ? null : WearableShape.values()[tmpMShape];
    }

}
