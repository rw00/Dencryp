package com.rw.dencryp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;

import com.rw.dencryp.crypt.Cryptor;
import com.rw.dencryp.keyboard.DencrypKeyboardService;

public class DencrypApplication extends Application {
    private static final String PREFS = "prefs";
    private static final String DEFAULT_KEY = "Dencryp";

    private static Cryptor cryptor; // TODO: load from SharedPreferences

    @Override
    public void onCreate() {
        super.onCreate();
        String iv = new String(Cryptor.generateInitVector());
        cryptor = new Cryptor(DEFAULT_KEY, iv);
        Intent intent = new Intent(DencrypKeyboardService.class.getName());
        intent.setPackage(this.getPackageName());
        startService(intent);

        SharedPreferences.Editor editor = getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putString("iv", iv);
        editor.putString("sk", DEFAULT_KEY);
        editor.apply();
    }

    public static Cryptor getCryptor() {
        return cryptor;
    }
}
