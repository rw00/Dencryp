package com.rw.dencryp.keyboard.inputmethod;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public abstract class InputMethodSettingsFragment extends PreferenceFragment
        implements InputMethodSettingsInterface {
    private final InputMethodSettingsImpl settings = new InputMethodSettingsImpl();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(context));
        settings.init(context, getPreferenceScreen());
    }

    @Override
    public void setInputMethodSettingsCategoryTitle(int resId) {
        settings.setInputMethodSettingsCategoryTitle(resId);
    }

    @Override
    public void setInputMethodSettingsCategoryTitle(CharSequence title) {
        settings.setInputMethodSettingsCategoryTitle(title);
    }

    @Override
    public void setSubtypeEnablerTitle(int resId) {
        settings.setSubtypeEnablerTitle(resId);
    }

    @Override
    public void setSubtypeEnablerTitle(CharSequence title) {
        settings.setSubtypeEnablerTitle(title);
    }

    @Override
    public void setSubtypeEnablerIcon(int resId) {
        settings.setSubtypeEnablerIcon(resId);
    }

    @Override
    public void setSubtypeEnablerIcon(Drawable drawable) {
        settings.setSubtypeEnablerIcon(drawable);
    }

    @Override
    public void onResume() {
        super.onResume();
        settings.updateSubtypeEnabler();
    }
}
