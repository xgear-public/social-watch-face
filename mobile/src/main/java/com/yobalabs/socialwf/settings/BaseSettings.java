package com.yobalabs.socialwf.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by yauhen.pelkin on 2/20/15.
 */
public abstract class BaseSettings {

	public void registerListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		read().registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		read().unregisterOnSharedPreferenceChangeListener(listener);
	}

	protected SharedPreferences.Editor write() {
		return read().edit();
	}

	protected SharedPreferences read() {
		return getAppContext().getSharedPreferences(getPrefsFileName(), Context.MODE_PRIVATE);
	}

	public abstract String getPrefsFileName();
	public abstract Context getAppContext();
}
