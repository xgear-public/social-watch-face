package com.yobalabs.socialwf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import timber.log.Timber;

public class DBManager {
	private static final String TAG = "DatabaseMng";

	private volatile static DBManager sInstance;
	private SQLiteDatabase mDb;
	private Context mContext;
	private volatile int mCounter;

	public void init(Context context) {
		mContext = context;
	}

	public static DBManager getInstance() {
		DBManager localInstance = sInstance;
		if (localInstance == null) {
			synchronized (DBManager.class) {
				localInstance = sInstance;
				if (localInstance == null) {
					sInstance = localInstance = new DBManager();
				}
			}
		}
		return localInstance;
	}

	private DBManager() {
		this.mCounter = 0;
	}

	public synchronized SQLiteDatabase connect() {
		if (mDb != null) {
			mCounter++;
			return mDb;
		}

		try {
			mDb = new InstaItemSQLiteOpenHelper(mContext).getWritableDatabase();
			mDb.execSQL("PRAGMA foreign_keys=ON;");
		} catch (SQLiteException e) {
			Timber.w(e, "Exception while connect()");
			mDb = null;
			return null;
		}

		mCounter++;
		return mDb;
	}

	public synchronized void disconnect() {
		if ((--mCounter) != 0)
			return;

		if (mDb == null)
			return;
		if (!mDb.isOpen()) {
			mDb = null;
			return;
		}

		try {
			mDb.close();
			mDb = null;
		} catch (Exception e) {
			Timber.w(e, "Exception while disconnect()");
			mDb = null;
		}
	}
}
