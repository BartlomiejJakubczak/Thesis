package com.example.bartomiejjakubczak.thesis.interfaces;

import android.content.Context;

public interface SharedPrefs {
    void putStringToSharedPrefs(Context context, String label, String string);
    String loadStringFromSharedPrefs(Context context, String label);
}
