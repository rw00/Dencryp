package com.rw.dencryp.keyboard.inputmethod;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import java.util.List;

/* package private */ class InputMethodSettingsImpl implements InputMethodSettingsInterface {
    private Preference subtypeEnablerPreference;
    private int inputMethodSettingsCategoryTitleRes;
    private int subtypeEnablerTitleRes;
    private CharSequence subtypeEnablerTitle;
    private int subtypeEnablerIconRes;
    private Drawable subtypeEnablerIcon;
    private InputMethodManager inputMethodManager;
    private InputMethodInfo inputMethodInfo;
    private Context context;

    private static InputMethodInfo getInputMethodInfo(Context context, InputMethodManager imm) {
        final List<InputMethodInfo> inputMethodInfos = imm.getInputMethodList();
        for (InputMethodInfo imi : inputMethodInfos) {
            if (imi.getPackageName().equals(context.getPackageName())) {
                return imi;
            }
        }
        return null;
    }

    private static String getEnabledSubtypesLabel(
            Context context, InputMethodManager inputMethodManager, InputMethodInfo inputMethodInfo) {
        if (context == null || inputMethodManager == null || inputMethodInfo == null) return null;
        final List<InputMethodSubtype> subtypes = inputMethodManager.getEnabledInputMethodSubtypeList(inputMethodInfo, true);
        final StringBuilder sb = new StringBuilder();
        final int N = subtypes.size();
        for (int i = 0; i < N; ++i) {
            final InputMethodSubtype subtype = subtypes.get(i);
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(subtype.getDisplayName(context, inputMethodInfo.getPackageName(),
                    inputMethodInfo.getServiceInfo().applicationInfo));
        }
        return sb.toString();
    }

    /**
     * Init internal states of this object
     *
     * @param context    the context for this application
     * @param prefScreen a PreferenceScreen of PreferenceActivity or PreferenceFragment
     * @return true if this application is an IME and has two or more subtypes, false otherwise
     */
    boolean init(final Context context, final PreferenceScreen prefScreen) {
        this.context = context;
        inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodInfo = getInputMethodInfo(context, inputMethodManager);
        if (inputMethodInfo == null || inputMethodInfo.getSubtypeCount() <= 1) {
            return false;
        }
        subtypeEnablerPreference = new Preference(context);
        subtypeEnablerPreference
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        final CharSequence title = getSubtypeEnablerTitle(context);
                        final Intent intent =
                                new Intent(Settings.ACTION_INPUT_METHOD_SUBTYPE_SETTINGS);
                        intent.putExtra(Settings.EXTRA_INPUT_METHOD_ID, inputMethodInfo.getId());
                        if (!TextUtils.isEmpty(title)) {
                            intent.putExtra(Intent.EXTRA_TITLE, title);
                        }
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(intent);
                        return true;
                    }
                });
        prefScreen.addPreference(subtypeEnablerPreference);
        updateSubtypeEnabler();
        return true;
    }

    @Override
    public void setInputMethodSettingsCategoryTitle(int resId) {
        inputMethodSettingsCategoryTitleRes = resId;
        updateSubtypeEnabler();
    }

    @Override
    public void setInputMethodSettingsCategoryTitle(CharSequence title) {
        inputMethodSettingsCategoryTitleRes = 0;
        CharSequence inputMethodSettingsCategoryTitle = title;
        updateSubtypeEnabler();
    }

    @Override
    public void setSubtypeEnablerTitle(int resId) {
        subtypeEnablerTitleRes = resId;
        updateSubtypeEnabler();
    }

    @Override
    public void setSubtypeEnablerTitle(CharSequence title) {
        subtypeEnablerTitleRes = 0;
        subtypeEnablerTitle = title;
        updateSubtypeEnabler();
    }

    @Override
    public void setSubtypeEnablerIcon(int resId) {
        subtypeEnablerIconRes = resId;
        updateSubtypeEnabler();
    }

    @Override
    public void setSubtypeEnablerIcon(Drawable drawable) {
        subtypeEnablerIconRes = 0;
        subtypeEnablerIcon = drawable;
        updateSubtypeEnabler();
    }

    private CharSequence getSubtypeEnablerTitle(Context context) {
        if (subtypeEnablerTitleRes != 0) {
            return context.getString(subtypeEnablerTitleRes);
        } else {
            return subtypeEnablerTitle;
        }
    }

    void updateSubtypeEnabler() {
        if (subtypeEnablerPreference != null) {
            if (subtypeEnablerTitleRes != 0) {
                subtypeEnablerPreference.setTitle(subtypeEnablerTitleRes);
            } else if (!TextUtils.isEmpty(subtypeEnablerTitle)) {
                subtypeEnablerPreference.setTitle(subtypeEnablerTitle);
            }
            final String summary = getEnabledSubtypesLabel(context, inputMethodManager, inputMethodInfo);
            if (!TextUtils.isEmpty(summary)) {
                subtypeEnablerPreference.setSummary(summary);
            }
            if (subtypeEnablerIconRes != 0) {
                subtypeEnablerPreference.setIcon(subtypeEnablerIconRes);
            } else if (subtypeEnablerIcon != null) {
                subtypeEnablerPreference.setIcon(subtypeEnablerIcon);
            }
        }
    }
}
