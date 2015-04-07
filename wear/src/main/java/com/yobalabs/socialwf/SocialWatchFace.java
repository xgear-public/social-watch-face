/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yobalabs.socialwf;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import com.yobalabs.socialwf.common.AnalogBasicConfig;
import com.yobalabs.socialwf.common.CommonConst;
import com.yobalabs.socialwf.common.Const;
import com.yobalabs.socialwf.common.Credentials;
import com.yobalabs.socialwf.common.ParcelableUtil;
import com.yobalabs.socialwf.common.WFPreview;
import com.yobalabs.socialwf.common.WearDevice;
import com.yobalabs.socialwf.common.WearableShape;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Pair;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SocialWatchFace extends CanvasWatchFaceService {

    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public SocialWfEngine onCreateEngine() {
        return new SocialWfEngine();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class SocialWfEngine extends CanvasWatchFaceService.Engine
            implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        static final int MSG_UPDATE_TIME = 0;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;

        /**
         * Handler to update the time periodically in interactive mode.
         */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        Paint mTextPaint;

        Bitmap mBgImage;

        boolean mAmbient;

        Time mTime;

        float mXOffset;

        float mYOffset;

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(SocialWatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        private boolean mLowBitAmbient;

        private boolean mBurnInProtection;

        private boolean mIsRound;

        private int mChinSize;

        private boolean mRespectToChins;

        private Point mScreenSize;

        private LinkedList<Pair<String, Bitmap>> mCurrentImages = new LinkedList<>();

        private int mCurrentImagePos = -1;

//        private LinkedList<String> mCurrentPostIds = new LinkedList<>();

        private ResultCallback<DataApi.DataItemResult> mConfigResultCallback
                = new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                if (dataItemResult != null && dataItemResult.getStatus().isSuccess()) {
                    DataItem dataItem1 = dataItemResult.getDataItem();
                    if (dataItem1 != null) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem1);
                        DataMap dataMap = dataMapItem.getDataMap();
                        if (dataMap != null) {
                            if (dataItem1.getUri().getPath().equals(Const.PATH_WITH_IMAGES)) {
                                new LoadAssets(SocialWfEngine.this, dataItem1).execute();
//                                handleAsset(dataItem1);
                            }

                            byte[] byteArray = dataMap.getByteArray(CommonConst.KEY_CONFIG);
                            if (byteArray != null && byteArray.length > 0) {
//                                updateUiForConfigDataMap(ParcelableUtil.unmarshall(byteArray, Credentials.CREATOR));
                                Credentials credentials = ParcelableUtil
                                        .unmarshall(byteArray, Credentials.CREATOR);
                                int i = 0;
                                i++;
                            }
                        }
                    } else {
//                        requestImages();
                    }
                }
            }
        };

        private ResultCallback<NodeApi.GetConnectedNodesResult> mConnectedNodesResultResultCallback
                = new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(NodeApi.GetConnectedNodesResult getConnectedNodesResult) {
                if (getConnectedNodesResult != null && getConnectedNodesResult.getStatus()
                        .isSuccess()) {
                    List<Node> nodes = getConnectedNodesResult.getNodes();
                    if (nodes != null && nodes.size() > 0) {
                        String nodeId = nodes.get(0).getId();
                        Uri uri = new Uri.Builder().scheme(PutDataRequest.WEAR_URI_SCHEME)
                                .authority(nodeId).path(Const.PATH_WITH_IMAGES).build();
                        PendingResult<DataApi.DataItemResult> dataItem = Wearable.DataApi
                                .getDataItem(mGoogleApiClient, uri);
                        dataItem.setResultCallback(mConfigResultCallback);

                        sendWearDeviceConfig();
                    }
                }
            }
        };

        private ResultCallback<DataApi.GetFdForAssetResult> mAssetResultResultCallback
                = new ResultCallback<DataApi.GetFdForAssetResult>() {
            @Override
            public void onResult(DataApi.GetFdForAssetResult getFdForAssetResult) {
                InputStream inputStream = getFdForAssetResult.getInputStream();
                if (inputStream != null) {
                    mBgImage = BitmapFactory.decodeStream(inputStream);
                    invalidate();
                }
            }
        };

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            Timber.plant(new Timber.DebugTree());

            setWatchFaceStyle(new WatchFaceStyle.Builder(SocialWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = SocialWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.digital_background));

            mTextPaint = new Paint();
            mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));

            mTime = new Time();
            loadResources();
        }

        private void loadResources() {
            WindowManager wm = (WindowManager) SocialWatchFace.this
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            mScreenSize = new Point();
            display.getRealSize(mScreenSize);
            Timber.d("screen size = %s", mScreenSize.toString());
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);

            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();

                if (mCurrentImages != null && !mCurrentImages.isEmpty()) {
                    if (mCurrentImagePos == -1) {
                        mCurrentImagePos = 0;
                    }
                    mCurrentImagePos++;

                    if (mCurrentImagePos >= mCurrentImages.size()) {
                        mCurrentImagePos = 0;
                    }
                } else {
                    mCurrentImagePos = -1;
                }
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SocialWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SocialWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            mIsRound = insets.isRound();
            mChinSize = insets.getSystemWindowInsetBottom();

            // Load resources that have alternate values for round watches.
            Resources resources = SocialWatchFace.this.getResources();
            mXOffset = resources.getDimension(mIsRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(mIsRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            //TODO show only unread and time
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (mCurrentImages.size() == 0) {
                mBgImage = null;
            } else {
                if (mCurrentImagePos >= mCurrentImages.size() || mCurrentImagePos < 0) {
                    mCurrentImagePos = 0;
                }

                mBgImage = mCurrentImages.get(mCurrentImagePos).second;
            }

            if (mBgImage == null || (mBgImage != null && mBgImage.isRecycled()) || mAmbient) {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBgImage, bounds.centerX() - ((float) mBgImage.getWidth()) / 2,
                        bounds.centerY() - ((float) mBgImage.getHeight()) / 2, null);
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            canvas.drawText(text, mXOffset, mYOffset, mTextPaint);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, SocialWfEngine.this);

            PendingResult<NodeApi.GetConnectedNodesResult> connectedNodes = Wearable.NodeApi
                    .getConnectedNodes(mGoogleApiClient);
            connectedNodes.setResultCallback(mConnectedNodesResultResultCallback);
        }

        private void sendWearDeviceConfig() {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Const.PATH_WITH_FEATURE);
            WFPreview preview = new WFPreview();
            preview.setShape(mIsRound ? WearableShape.ROUND : WearableShape.SQUARE);
            DataMap dataMap = putDataMapReq.getDataMap();
            dataMap
                    .putByteArray(CommonConst.KEY_WEAR_DEVICE_CONFIG, ParcelableUtil
                            .marshall(preview));
            WearDevice wearDevice = new WearDevice(Build.MANUFACTURER, Build.MODEL, mScreenSize.x,
                    mScreenSize.y, mIsRound, mChinSize);
            dataMap.putByteArray(CommonConst.KEY_WEAR_DEVICE_INFO,
                    ParcelableUtil.marshall(wearDevice));
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }

        private void requestImages() {
            PutDataMapRequest putDataMapReq = PutDataMapRequest.create(Const.PATH_WITH_IMAGES);
            putDataMapReq.getDataMap()
                    .putInt(CommonConst.KEY_INSTA_ITEMS_REQUEST, 4);
            PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
        }

        private void updateUiForConfigDataMap(AnalogBasicConfig config) {
//            mAnalogBasicConfig = config;

            invalidate();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            try {
                for (DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    DataItem dataItem = dataEvent.getDataItem();
                    String path = dataItem.getUri().getPath();

                    if (path.equals(Const.PATH_WITH_IMAGES)) {
                        handleAsset(dataItem);

                    } else if (path.equals(Const.PATH_WITH_INFO)) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                        DataMap dataMap = dataMapItem.getDataMap();
                        if (dataMap != null) {
                            byte[] byteArray = dataMap.getByteArray(CommonConst.KEY_CONFIG);
                            if (byteArray != null && byteArray.length > 0) {
                                Credentials unmarshall = ParcelableUtil
                                        .unmarshall(byteArray, Credentials.CREATOR);
                            }
                        }
                    }
                }
            } finally {
                dataEvents.close();
            }
        }

        private void handleAsset(DataItem dataItem) {
            DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);

            DataMap dataMap = dataMapItem.getDataMap();
            ArrayList<String> stringArrayList = dataMap
                    .getStringArrayList(CommonConst.KEY_INSTA_ITEMS);

            if (stringArrayList != null && !stringArrayList.isEmpty()) {
                for (String postId : stringArrayList) {

                    while (mCurrentImages.size() > 2) {
                        Pair<String, Bitmap> first = mCurrentImages.removeFirst();
                        if (first.second != null) {
                            first.second.recycle();
                        }
                    }
                    ;

                    Asset profileAsset = dataMap.getAsset(CommonConst.KEY_IMAGE + "_" + postId);

                    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                            mGoogleApiClient, profileAsset).await().getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);

                    if (bitmap != null) {
                        Pair<String, Bitmap> newImage = new Pair<>(postId, bitmap);
                        mCurrentImages.add(newImage);
                    }
                }
                if (mCurrentImagePos == -1) {
                    mCurrentImagePos = 0;
                }
                invalidate();
            }
        }

        public void loadBitmapFromAsset(Asset asset) {
            if (asset != null) {
                Wearable.DataApi.getFdForAsset(
                        mGoogleApiClient, asset).setResultCallback(mAssetResultResultCallback);
            }
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }


    }

    private static class LoadAssets extends AsyncTask<Void, Void, Void> {

        private WeakReference<SocialWfEngine> mEngineWeakReference;

        private WeakReference<DataItem> mDataItemWeakReference;

        private LoadAssets(SocialWfEngine engine, DataItem dataItem) {
            mEngineWeakReference = new WeakReference<SocialWfEngine>(engine);
            mDataItemWeakReference = new WeakReference<DataItem>(dataItem);
        }

        @Override
        protected Void doInBackground(Void... params) {
            SocialWfEngine socialWfEngine = mEngineWeakReference.get();
            DataItem dataItem = mDataItemWeakReference.get();

            if (socialWfEngine != null && dataItem != null) {

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);

                DataMap dataMap = dataMapItem.getDataMap();
                ArrayList<String> stringArrayList = dataMap
                        .getStringArrayList(CommonConst.KEY_INSTA_ITEMS);

                if (stringArrayList != null && !stringArrayList.isEmpty()) {
                    for (String postId : stringArrayList) {

                        while (socialWfEngine.mCurrentImages.size() > 2) {
                            Pair<String, Bitmap> first = socialWfEngine.mCurrentImages
                                    .removeFirst();
                            if (first.second != null) {
                                first.second.recycle();
                            }
                        }
                        ;

                        Asset profileAsset = dataMap.getAsset(CommonConst.KEY_IMAGE + "_" + postId);

                        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                                socialWfEngine.mGoogleApiClient, profileAsset).await()
                                .getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(assetInputStream);

                        if (bitmap != null) {
                            Pair<String, Bitmap> newImage = new Pair<>(postId, bitmap);
                            socialWfEngine.mCurrentImages.add(newImage);
                        }
                    }
                    if (socialWfEngine.mCurrentImagePos == -1) {
                        socialWfEngine.mCurrentImagePos = 0;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            SocialWfEngine socialWfEngine = mEngineWeakReference.get();
            if (socialWfEngine != null) {
                socialWfEngine.invalidate();
            }
        }
    }
}
