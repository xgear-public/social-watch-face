package com.yobalabs.socialwf.gcm;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yobalabs.socialwf.common.CommonConst;
import com.yobalabs.socialwf.common.Const;
import com.yobalabs.socialwf.data.DBManager;
import com.yobalabs.socialwf.data.InstaItem;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class WearUpdateService extends Service
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {

    private final IBinder mBinder = new WearUpdateBinder();

    boolean onCreate = false;

    private HandlerThread mHandlerThread;

    private Handler mBatchHandler;

    private GoogleApiClient mGoogleApiClient;

    private Set<String> mPeerIds = new HashSet<>();

    public WearUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onConnected(Bundle bundle) {
        loadConnectedNodes(new Runnable() {
            @Override
            public void run() {
                sendFeedInfo();
            }
        });
    }

    private void loadConnectedNodes(final Runnable runAfterNodesLoaded) {
        PendingResult<NodeApi.GetConnectedNodesResult> connectedNodes = Wearable.NodeApi
                .getConnectedNodes(mGoogleApiClient);
        connectedNodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                for (Node node : nodes.getNodes()) {
                    mPeerIds.add(node.getId());
                }

                if (runAfterNodesLoaded != null) {
                    runAfterNodesLoaded.run();
                }
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(DataApi.DataItemResult dataItemResult) {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            mHandlerThread = new HandlerThread("update");
            mHandlerThread.start();
        }
        mBatchHandler = new Handler(mHandlerThread.getLooper());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        onCreate = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            if (!onCreate) {
                sendFeedInfo();
            }
        }
        onCreate = false;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.getLooper().quit();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private String choosePeerId(Set<String> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            return (String) nodes.toArray()[0];
        } else {
            return null;
        }
    }

    private void sendFeedInfo() {

        DBManager dbManager = DBManager.getInstance();
        SQLiteDatabase database = dbManager.connect();

        final List<InstaItem> unhandled = cupboard()
                .withDatabase(database)
                .query(InstaItem.class)
                .withSelection(InstaItem.HANDLED + " = ?", "" + 0).query().list(true);

        dbManager.disconnect();

        if (unhandled != null && !unhandled.isEmpty()) {

            mBatchHandler.post(new Runnable() {
                @Override
                public void run() {
                    PutDataMapRequest dataMapRequest = PutDataMapRequest
                            .create(Const.PATH_WITH_IMAGES);
                    DataMap dataMap = dataMapRequest.getDataMap();

                    List<Long> idsForUpdate = new ArrayList<>();
                    ArrayList<String> dataMaps = new ArrayList<>();

                    for (InstaItem instaItem : unhandled) {
                        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                        Bitmap bitmap = ImageLoader.getInstance()
                                .loadImageSync(instaItem.getUrls());
                        if (bitmap != null) {

                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 320, 320, true);
                            bitmap.recycle();
                            scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                            Asset result = Asset.createFromBytes(byteStream.toByteArray());

                            dataMap.putAsset(CommonConst.KEY_IMAGE + "_" + instaItem.getPostId(),
                                    result);

                            dataMaps.add(instaItem.getPostId());
                            idsForUpdate.add(instaItem.getId());

                        }

                    }

                    dataMap.putStringArrayList(CommonConst.KEY_INSTA_ITEMS, dataMaps);

                    PutDataRequest request = dataMapRequest.asPutDataRequest();
                    PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                            .putDataItem(mGoogleApiClient, request);

                    if (idsForUpdate.size() > 0) {

                        DBManager dbManager = DBManager.getInstance();
                        SQLiteDatabase database = dbManager.connect();
                        ContentValues values = new ContentValues(1);
                        values.put(InstaItem.HANDLED, true);

                        int update = cupboard().withDatabase(database).update(InstaItem.class,
                                values,
                                BaseColumns._ID + " IN ("+TextUtils
                                        .join(", ", idsForUpdate)+",?)", "");

                        Timber.d("update %d", update);

                        dbManager.disconnect();
                    }
                }
            });
        }
    }

    public class WearUpdateBinder extends Binder {

        public WearUpdateService getService() {
            return WearUpdateService.this;
        }
    }


}
