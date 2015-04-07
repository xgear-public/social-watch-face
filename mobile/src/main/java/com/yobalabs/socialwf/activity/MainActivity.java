package com.yobalabs.socialwf.activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.yobalabs.socialwf.R;
import com.yobalabs.socialwf.common.CommonConst;
import com.yobalabs.socialwf.common.Const;
import com.yobalabs.socialwf.common.ParcelableUtil;
import com.yobalabs.socialwf.gcm.GCMService;
import com.yobalabs.socialwf.settings.MainSettings;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.companion.WatchFaceCompanion;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;


public class MainActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<DataApi.DataItemResult> {


    @InjectView(R.id.shape)
    ImageView mShape;

    @InjectView(R.id.txt_time)
    TextView mTimeText;

    private GoogleApiClient mGoogleApiClient;

    private GoogleCloudMessaging mGcm;

    private Set<String> mPeerIds = new HashSet<>();

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        String peerId = null;

        peerId = getIntent().getStringExtra(WatchFaceCompanion.EXTRA_PEER_ID);

        if (peerId != null) {
            mPeerIds.add(peerId);
        }

        mShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendFeedInfo();
//                updateWF();

                Intent i = new Intent();
                ComponentName comp = new ComponentName(getPackageName(),
                        GCMService.class.getName());
                i.setComponent(comp);
                startService(i);

//                Intent i = new Intent(MainActivity.this, WearUpdateService.class);
//                getApplication().startService(i);
            }
        });

//        HashMap<Integer, String> urls = new HashMap<>();
//        urls.put(150, "url1");
//        urls.put(306, "url1");
//        urls.put(640, "url1");
//        Image img1 = new Image();
//        img1.setUrls(urls);
//
//        HashMap<Integer, String> urls2 = new HashMap<>();
//        urls2.put(150, "url2");
//        urls2.put(306, "url2");
//        urls2.put(640, "url2");
//        Image img2 = new Image();
//        img2.setUrls(urls2);
//
//        InstagramFeed instagramFeed = new InstagramFeed();
//        instagramFeed.setImages(new Image[]{img1, img2});
//        Feeds feeds = new Feeds();
//        feeds.setInstagramFeed(instagramFeed);
//        Gson gson = new GsonBuilder().create();
//        String s = gson.toJson(feeds);

        initGCM();
    }

    private void initGCM() {
        String regid = getRegistrationId(this);

        if (TextUtils.isEmpty(regid)) {
            registerInBackground();
        }

    }

    private String getRegistrationId(Context context) {
        String registrationId = MainSettings.getInstance().getRegId();
        if (registrationId.isEmpty()) {
            Timber.i("Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
//        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
//        int currentVersion = getAppVersion(context);
//        if (registeredVersion != currentVersion) {
//            Log.i(TAG, "App version changed.");
//            return "";
//        }
        return registrationId;
    }

    private void registerInBackground() {
        new RegisterGCM(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.add_account) {
            LoginActivity.startLoginActivity(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        loadConnectedNodes(new Runnable() {
            @Override
            public void run() {
                getDataFromWear();
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

    private void getDataFromWear() {
        String peerId = choosePeerId(mPeerIds);
        if (!TextUtils.isEmpty(peerId)) {
            Uri.Builder builder = new Uri.Builder();
            Uri uri = builder.scheme("wear").path(Const.PATH_WITH_FEATURE).authority(peerId)
                    .build();
            Wearable.DataApi.getDataItem(mGoogleApiClient, uri).setResultCallback(this);
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
        String peerId = choosePeerId(mPeerIds);
        if (peerId != null) {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Const.PATH_WITH_INFO);
            DataMap dataMap = putDataMapReq.getDataMap();

            com.yobalabs.socialwf.common.Credentials creds
                    = new com.yobalabs.socialwf.common.Credentials("test", "pass",
                    System.currentTimeMillis() + "");

            dataMap.putByteArray(CommonConst.KEY_CONFIG, ParcelableUtil.marshall(creds));
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }
    }

    private void updateWF() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.outWidth = 320;
        options.outHeight = 320;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2, options);

//        Bitmap test = Bitmap.createScaledBitmap(bitmap, 320, 320, true);

        new PrepareBitmapForSendTask(this, bitmap).execute();

//        String peerId = choosePeerId(mPeerIds);
//        if (peerId != null) {
//            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Const.PATH_WITH_FEATURE);
//            DataMap dataMap = putDataMapReq.getDataMap();
//            dataMap.putByteArray(CommonConst.KEY_CONFIG, ParcelableUtil.marshall(mBasicWFPreview));
//            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
//            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
//        }
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

    private void sendImage(Asset mImageAsset) {
        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(Const.PATH_WITH_IMAGES);
        DataMap dataMap = dataMapRequest.getDataMap();
        dataMap.putAsset(CommonConst.KEY_IMAGE, mImageAsset);
        PutDataRequest request = dataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi
                .putDataItem(mGoogleApiClient, request);
    }

    public static class RegisterGCM extends AsyncTask<Void, Void, String> {


        private static final String SENDER_ID = "509581955891";

        private WeakReference<MainActivity> mContextHolder;

        public RegisterGCM(MainActivity mainActivity) {
            mContextHolder = new WeakReference<MainActivity>(mainActivity);
        }

        @Override
        protected String doInBackground(Void... params) {
            MainActivity mainActivity = mContextHolder.get();
            if (mainActivity != null) {

                try {
                    if (mainActivity.mGcm == null) {
                        mainActivity.mGcm = GoogleCloudMessaging.getInstance(mainActivity);
                    }
                    String regid = mainActivity.mGcm.register(SENDER_ID);
                    MainSettings.getInstance().setRegId(regid);
                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.

                }
            }
            return "";
        }
    }

    public static class PrepareBitmapForSendTask extends AsyncTask<Void, Void
            , Asset> {

        private WeakReference<MainActivity> mContextHolder;

        private Bitmap mSource;

        public PrepareBitmapForSendTask(MainActivity mainActivity, Bitmap source) {
            mContextHolder = new WeakReference<MainActivity>(mainActivity);
            mSource = source;
        }

        @Override
        protected Asset doInBackground(Void... params) {
            Asset result = null;
            MainActivity mainActivity = mContextHolder.get();
            if (mainActivity != null) {
                final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                mSource.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
                result = Asset.createFromBytes(byteStream.toByteArray());
            }
            return result;
        }

        @Override
        protected void onPostExecute(Asset asset) {
            super.onPostExecute(asset);
            MainActivity mainActivity = mContextHolder.get();
            if (mainActivity != null) {
                if (asset != null) {
                    mainActivity.sendImage(asset);
                }
            }
        }
    }
}
