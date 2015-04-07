package com.yobalabs.socialwf.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

/**
 * Created by yauhen.pelkin on 2/10/15.
 */
public class InstaItemSQLiteOpenHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "wfdata.db";
	private static final int DATABASE_VERSION_1 = 1;//initial

	static {
		cupboard().register(InstaItem.class);
	}

	public InstaItemSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION_1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
            cupboard().withDatabase(db).createTables();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		cupboard().withDatabase(db).upgradeTables();
	}
}
