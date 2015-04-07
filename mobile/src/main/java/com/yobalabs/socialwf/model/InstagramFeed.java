package com.yobalabs.socialwf.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Yauhen on 23.03.2015.
 */
public class InstagramFeed {

    @SerializedName("images")
    private List<Image> mImages;

    public List<Image> getImages() {
        return mImages;
    }

    public void setImages(List<Image> images) {
        mImages = images;
    }
}
