package com.yobalabs.socialwf.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class Feeds {

    @SerializedName("instaFeed")
    private InstagramFeed mInstagramFeed;

    public InstagramFeed getInstagramFeed() {
        return mInstagramFeed;
    }

    public void setInstagramFeed(InstagramFeed instagramFeed) {
        mInstagramFeed = instagramFeed;
    }
}
