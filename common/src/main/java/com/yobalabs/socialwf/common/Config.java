package com.yobalabs.socialwf.common;

import android.os.Parcelable;

/**
 * Created by yauhen.pelkin on 2/13/15.
 */
public interface Config extends Parcelable {

    WearableShape getShape();

    void setShape(WearableShape shape);
}
