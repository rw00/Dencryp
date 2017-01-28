package com.rw.dencryp.keyboard.inputmethod;

import android.graphics.drawable.Drawable;

/**
 * InputMethodSettingsInterface is the interface for adding IME related preferences to
 * PreferenceActivity or PreferenceFragment.
 */
interface InputMethodSettingsInterface {
    /**
     * Sets the title for the input method settings category with a resource ID.
     *
     * @param resId the resource ID of the title
     */
    void setInputMethodSettingsCategoryTitle(int resId);

    /**
     * Sets the title for the input method settings category with a CharSequence.
     *
     * @param title the title for this preference
     */
    void setInputMethodSettingsCategoryTitle(CharSequence title);

    /**
     * Sets the title for the input method enabler preference for launching subtype enabler with a
     * resource ID.
     *
     * @param resId the resource ID of the title
     */
    void setSubtypeEnablerTitle(int resId);

    /**
     * Sets the title for the input method enabler preference for launching subtype enabler with a
     * CharSequence.
     *
     * @param title the title for this preference
     */
    void setSubtypeEnablerTitle(CharSequence title);

    /**
     * Sets the icon for the preference for launching subtype enabler with a resource ID.
     *
     * @param resId the resource id of an optional icon for the preference
     */
    void setSubtypeEnablerIcon(int resId);

    /**
     * Sets the icon for the Preference for launching subtype enabler with a Drawable.
     *
     * @param drawable the drawable of an optional icon for the preference
     */
    void setSubtypeEnablerIcon(Drawable drawable);
}
