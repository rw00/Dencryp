package com.rw.dencryp.keyboard;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.support.v4.content.res.ResourcesCompat;
import android.view.inputmethod.EditorInfo;

import com.rw.dencryp.R;

class LatinKeyboard extends Keyboard {

    private Key enterKey;
    /*
     * Stores the current state of the mode change key. Its width will be dynamically updated to
     * match the region
     */
    private Key modeChangeKey;

    private Key langSwitchKey;
    /**
     * Stores the size and other information of {@link #modeChangeKey} when
     * {@link #langSwitchKey} is visible. This should be immutable and will be used only as a
     * reference size when the visibility of {@link #langSwitchKey} is changed.
     */
    private Key savedModeChangeKey;
    /**
     * Stores the size and other information of {@link #langSwitchKey} when it is visible.
     * This should be immutable and will be used only as a reference size when the visibility of
     * {@link #langSwitchKey} is changed.
     */
    private Key savedLangSwitchKey;

    LatinKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
                                   XmlResourceParser parser) {
        Key key = new LatinKey(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            enterKey = key;
        } else if (key.codes[0] == ' ') {
            //
        } else if (key.codes[0] == Keyboard.KEYCODE_MODE_CHANGE) {
            modeChangeKey = key;
            savedModeChangeKey = new LatinKey(res, parent, x, y, parser);
        } else if (key.codes[0] == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            langSwitchKey = key;
            savedLangSwitchKey = new LatinKey(res, parent, x, y, parser);
        }
        return key;
    }

    /**
     * Dynamically change the visibility of the language switch key (a.k.a. globe key).
     *
     * @param visible <code>true</code> if the language switch key should be visible.
     */
    void setLanguageSwitchKeyVisibility(boolean visible) {
        if (visible) {
            modeChangeKey.width = savedModeChangeKey.width;
            modeChangeKey.x = savedModeChangeKey.x;
            langSwitchKey.width = savedLangSwitchKey.width;
            langSwitchKey.icon = savedLangSwitchKey.icon;
            langSwitchKey.iconPreview = savedLangSwitchKey.iconPreview;
        } else {
            // hide langSwitchKey and update the sizes
            modeChangeKey.width = savedModeChangeKey.width + savedLangSwitchKey.width;
            langSwitchKey.width = 0;
            langSwitchKey.icon = null;
            langSwitchKey.iconPreview = null;
        }
    }

    void setInputMethodEditorOptions(Resources res, int options) { // set Enter key label
        if (enterKey == null) {
            return;
        }

        switch (options & (EditorInfo.IME_MASK_ACTION | EditorInfo.IME_FLAG_NO_ENTER_ACTION)) {
            case EditorInfo.IME_ACTION_GO:
                enterKey.iconPreview = null;
                enterKey.icon = null;
                enterKey.label = res.getText(R.string.label_go_key);
                break;
            case EditorInfo.IME_ACTION_NEXT:
                enterKey.iconPreview = null;
                enterKey.icon = null;
                enterKey.label = res.getText(R.string.label_next_key);
                break;
            case EditorInfo.IME_ACTION_SEARCH:
                enterKey.icon = ResourcesCompat.getDrawable(res, R.drawable.sym_keyboard_search_light, null);
                enterKey.label = null;
                break;
            case EditorInfo.IME_ACTION_SEND:
                enterKey.icon = ResourcesCompat.getDrawable(res, R.drawable.sym_keyboard_send_light, null);
                enterKey.label = null;
                break;
            default:
                enterKey.icon = ResourcesCompat.getDrawable(res, R.drawable.sym_keyboard_return_light, null);
                enterKey.label = null;
                break;
        }
    }

    private static class LatinKey extends Keyboard.Key {

        LatinKey(Resources res, Keyboard.Row parent, int x, int y,
                 XmlResourceParser parser) {
            super(res, parent, x, y, parser);
        }

        @Override
        public boolean isInside(int x, int y) { // reduce target area of close keyboard key
            return super.isInside(x, codes[0] == KEYCODE_CANCEL ? y - 10 : y);
        }
    }
}
