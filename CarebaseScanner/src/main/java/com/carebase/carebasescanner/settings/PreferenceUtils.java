package com.carebase.carebasescanner.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.StringRes;

import com.carebase.carebasescanner.R;

public class PreferenceUtils {
    static public int getConfirmationTimeMs(Context context) {
        return getIntPref(context, R.string.pref_key_confirmation_time_in_manual_search, 1000);
    }

    static private int getIntPref(Context context, @StringRes int prefKeyId, int defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String prefKey = context.getString(prefKeyId);
        return sharedPreferences.getInt(prefKey, defaultValue);
    }
}

