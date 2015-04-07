package com.yobalabs.socialwf.common;

/**
 * Created by yauhen.pelkin on 2/12/15.
 */
public enum WearableShape {
    SQUARE(1),
    ROUND(2),
    BOTH(3),
    UNDEFINED(4);

    private int value;

    private WearableShape(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static WearableShape getByValue(int value) {
        for (WearableShape wearableShape : WearableShape.values()) {
            if (wearableShape.getValue() == value) {
                return wearableShape;
            }
        }
        return UNDEFINED;
    }
}
