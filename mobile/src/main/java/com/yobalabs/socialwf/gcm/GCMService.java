package com.yobalabs.socialwf.gcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yobalabs.socialwf.data.DBManager;
import com.yobalabs.socialwf.data.InstaItem;
import com.yobalabs.socialwf.model.Feeds;
import com.yobalabs.socialwf.model.Image;
import com.yobalabs.socialwf.model.InstagramFeed;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import timber.log.Timber;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class GCMService extends IntentService {

    public GCMService() {
        super("GCMService");
    }

    String testJson = "{\n"
            + "    \"instaFeed\": {\n"
            + "        \"images\": [{\n"
            + "            \"id\": \"950803062406535229_223031557\",\n"
            + "            \"urls\": {\n"
            + "                \"640\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/e15/10852576_1623829024515550_1902186406_n.jpg\",\n"
            + "                \"306\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/s306x306/e15/10852576_1623829024515550_1902186406_n.jpg\",\n"
            + "                \"150\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/s150x150/e15/10852576_1623829024515550_1902186406_n.jpg\"\n"
            + "            }\n"
            + "        }, {\n"
            + "            \"id\": \"950792401752447737_223031557\",\n"
            + "            \"urls\": {\n"
            + "                \"640\": \"https://scontent.cdninstagram.com/hphotos-xfa1/t51.2885-15/e15/11101982_1615575731991974_613609225_n.jpg\",\n"
            + "                \"306\": \"https://scontent.cdninstagram.com/hphotos-xfa1/t51.2885-15/s306x306/e15/11101982_1615575731991974_613609225_n.jpg\",\n"
            + "                \"150\": \"https://scontent.cdninstagram.com/hphotos-xfa1/t51.2885-15/s150x150/e15/11101982_1615575731991974_613609225_n.jpg\"\n"
            + "            }\n"
            + "        }, {\n"
            + "            \"id\": \"950775758563819226_31209078\",\n"
            + "            \"urls\": {\n"
            + "                \"640\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/e15/11056023_1412318632416327_492937208_n.jpg\",\n"
            + "                \"306\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/s306x306/e15/11056023_1412318632416327_492937208_n.jpg\",\n"
            + "                \"150\": \"https://scontent.cdninstagram.com/hphotos-xaf1/t51.2885-15/s150x150/e15/11056023_1412318632416327_492937208_n.jpg\"\n"
            + "            }\n"
            + "        }]\n"
            + "    }\n"
            + "}";

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            Timber.d("GCM onHandleIntent action = %s", action);

//            HashMap<Integer, String> urls = new HashMap<>();
//            urls.put(150,
//                    "https://igcdn-photos-g-a.akamaihd.net/hphotos-ak-xfa1/t51.2885-15/10948280_1534480306835182_272302951_n.jpg");
//            urls.put(306,
//                    "https://igcdn-photos-g-a.akamaihd.net/hphotos-ak-xfa1/t51.2885-15/10948280_1534480306835182_272302951_n.jpg");
//            urls.put(640,
//                    "https://igcdn-photos-g-a.akamaihd.net/hphotos-ak-xfa1/t51.2885-15/10948280_1534480306835182_272302951_n.jpg");
//            Image img1 = new Image();
//            img1.setUrls(urls);
//            img1.setPostId("test4");
//
//            InstagramFeed instagramFeed = new InstagramFeed();
//            instagramFeed.setImages(Arrays.asList(img1));
//            Feeds feeds = new Feeds();
//            feeds.setInstagramFeed(instagramFeed);

            Bundle extras = intent.getExtras();
            String images = extras.getString("images");

            Gson gson = new GsonBuilder().create();
            Type collectionType = new TypeToken<List<Image>>() {
            }.getType();
            List<Image> imageList = gson.fromJson(images, collectionType);

            InstagramFeed instagramFeed = new InstagramFeed();
            instagramFeed.setImages(imageList);

            Feeds feeds = new Feeds();
            feeds.setInstagramFeed(instagramFeed);

            handleNewData(feeds);
        }
    }


    private void handleNewData(Feeds feeds) {
        if (feeds != null) {
            InstagramFeed instagramFeed = feeds.getInstagramFeed();
            if (instagramFeed != null) {
                List<Image> images = instagramFeed.getImages();
                if (images != null && !images.isEmpty()) {
                    DBManager dbManager = DBManager.getInstance();
                    SQLiteDatabase database = dbManager.connect();
                    for (Image image : images) {
                        Map<Integer, String> urls = image.getUrls();
                        SortedSet<Integer> keys = new TreeSet<>(urls.keySet());
                        Integer last = keys.last();
                        String urlToLoad = urls.get(last);
                        ImageLoader.getInstance().loadImageSync(urlToLoad);
                        InstaItem instaItem = new InstaItem(image.getPostId(), urlToLoad);

                        List<InstaItem> storedInDbList = cupboard()
                                .withDatabase(database)
                                .query(InstaItem.class)
                                .withSelection(InstaItem.POST_ID + " = ?", image.getPostId()).query().list(true);


                        InstaItem storedInDb = storedInDbList.isEmpty() ? null : storedInDbList.get(0);

                        if (storedInDb != null) {
                            ContentValues vals = new ContentValues(2);
                            vals.put(InstaItem.HANDLED, false);

                            vals.put(BaseColumns._ID, storedInDb.getId());

                            int update = cupboard()
                                    .withDatabase(database)
                                    .update(InstaItem.class, vals);

                            Timber.d("Updated items = %d", update);
                        } else {
                            cupboard()
                                    .withDatabase(database).put(instaItem);
                        }
                    }
                    dbManager.disconnect();


                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sendBroadcast(new Intent("com.yobalabs.socialwf.UPDATE"));
    }

    private void runImageOnWearUpdate() {
        DBManager dbManager = DBManager.getInstance();
        SQLiteDatabase database = dbManager.connect();

        List<InstaItem> unhandled = cupboard()
                .withDatabase(database)
                .query(InstaItem.class)
                .withSelection(InstaItem.HANDLED + " = ?", "" + 0).query().list(true);

        List<Integer> idsForUpdate = new ArrayList<>();

        dbManager.disconnect();
    }

    public static class RootObject{
        Map <String,ChoiceEntry> choice;
    }

    public static class ChoiceEntry{
        String id;
        String label;
        int status;
    }
}
