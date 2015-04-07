package com.yobalabs.socialwf;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.yobalabs.socialwf.data.DBManager;
import com.yobalabs.socialwf.network.SocialWatchFaceApi;
import com.yobalabs.socialwf.settings.MainSettings;

import android.app.Application;
import android.os.Environment;

import java.io.File;

import retrofit.RestAdapter;
import timber.log.Timber;

/**
 * Created by Yauhen on 28.03.2015.
 */
public class SocialWfApp extends Application {

    private SocialWatchFaceApi mSocialWatchFaceApi;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://ioba.herokuapp.com/api")
                .build();

        mSocialWatchFaceApi = restAdapter.create(SocialWatchFaceApi.class);

        MainSettings.getInstance().init(this);

        DBManager dbManager = DBManager.getInstance();
        dbManager.init(getApplicationContext());

        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .displayer(new FadeInBitmapDisplayer(300, true, false, false))
                .build();

        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(this);


        final String CACHE_DIR = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/swf";
        builder.denyCacheImageMultipleSizesInMemory()
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiscCache(new File(CACHE_DIR)))
                .defaultDisplayImageOptions(defaultOptions);
        if (BuildConfig.DEBUG) {
            builder.writeDebugLogs();
        }
        ImageLoaderConfiguration config = builder.build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

    }

    public SocialWatchFaceApi getSocialWatchFaceApi() {
        return mSocialWatchFaceApi;
    }
}
