package com.yobalabs.socialwf.model;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class Image {

    @SerializedName("id")
    private String mPostId;

    @SerializedName("urls")
    private Map<Integer, String> mUrls;

    public Map<Integer, String> getUrls() {
        return mUrls;
    }

    public void setUrls(Map<Integer, String> urls) {
        mUrls = urls;
    }

    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }
}
