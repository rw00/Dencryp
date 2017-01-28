package com.rw.dencryp.keyboard;

import android.app.Dialog;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.IBinder;
import android.text.InputType;
import android.text.method.MetaKeyKeyListener;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;

import com.rw.dencryp.DencrypApplication;
import com.rw.dencryp.R;

public class DencrypKeyboardService extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {
    /**
     * Indicates the optional code for performing
     * processing of hard keys in addition to regular text generation
     * from on-screen interaction.  It would be used for input methods that
     * perform language translations (such as converting text entered on
     * a QWERTY keyboard to Chinese), but may not be used for input methods
     * that are primarily intended to be used for on-screen text entry.
     */
    static final boolean PROCESS_HARD_KEYS = true;

    private static final String TAG = DencrypKeyboardService.class.getSimpleName();

    private InputMethodManager inputMethodManager;

    private LatinKeyboardView inputView;

    private StringBuilder composing = new StringBuilder();
    private String prev = "";

    private int lastDisplayWidth;
    private boolean capsLock;
    private long lastShiftTime;
    private long metaState;

    private LatinKeyboard symbolsKeyboard;
    private LatinKeyboard symbolsShiftedKeyboard;
    private LatinKeyboard qwertyKeyboard;
    private LatinKeyboard currentKeyboard;

    private String wordSeparators;

    @Override
    public void onCreate() {
        super.onCreate(); // required
        inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        wordSeparators = getResources().getString(R.string.word_separators);
    }

    /**
     * This is the point where you can do all of your UI initialization.  It
     * is called after creation and any configuration change.
     */
    @Override
    public void onInitializeInterface() {
        if (qwertyKeyboard != null) {
            int displayWidth = getMaxWidth();
            if (displayWidth == lastDisplayWidth) return;
            lastDisplayWidth = displayWidth;
        }
        qwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
        symbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
        symbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);
    }

    @Override
    public View onCreateInputView() { // called to create the Keyboard
        inputView = (LatinKeyboardView) getLayoutInflater().inflate(
                R.layout.input, null);
        inputView.setOnKeyboardActionListener(this);
        inputView.setPreviewEnabled(false);
        setCandidatesViewShown(false);
        setLatinKeyboard(qwertyKeyboard);
        return inputView;
    }

    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
        boolean shouldSupportLanguageSwitchKey =
                inputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        inputView.setKeyboard(nextKeyboard);
    }

    @Override
    public View onCreateCandidatesView() {
        return null; // no candidates
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // reset the state
        prev = "";
        composing.setLength(0);

        if (!restarting) {
            // clear shift states
            metaState = 0;
        }

        // init the state based on the type of text being edited
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                currentKeyboard = symbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                currentKeyboard = symbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                currentKeyboard = qwertyKeyboard;

                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // disable prediction
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {

                }

                updateShiftKeyState(attribute);
                break;

            default:
                // unknown input type
                currentKeyboard = qwertyKeyboard;
                updateShiftKeyState(attribute);
        }

        // update the Enter key label
        currentKeyboard.setInputMethodEditorOptions(getResources(), attribute.imeOptions);
    }

    @Override
    public void onFinishInput() {
        super.onFinishInput();

        // clear current composing text and candidates
        composing.setLength(0);

        currentKeyboard = qwertyKeyboard;
        if (inputView != null) {
            inputView.closing();
        }
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);
        // apply the selected keyboard to the input view
        setLatinKeyboard(currentKeyboard);
        inputView.closing();
    }

    @Override
    public void onCurrentInputMethodSubtypeChanged(InputMethodSubtype subtype) {
        inputView.setSubtypeOnSpaceKey(subtype);
    }

    @Override
    public void onUpdateSelection(int oldSelStart, int oldSelEnd,
                                  int newSelStart, int newSelEnd,
                                  int candidatesStart, int candidatesEnd) {
        // editor moving cursor
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd,
                candidatesStart, candidatesEnd);

        // should clear candidates
        if (composing.length() > 0 && (newSelStart != candidatesEnd
                || newSelEnd != candidatesEnd)) {
            composing.setLength(0);
            InputConnection inputConnection = getCurrentInputConnection();
            if (inputConnection != null) {
                inputConnection.finishComposingText();
            }
        }
    }

    @Override
    public void onDisplayCompletions(CompletionInfo[] completions) {

    }

    /*
     * This translates incoming hard key events in to edit operations on an
     * InputConnection.  It is only needed when using the
     * PROCESS_HARD_KEYS option.
     */
    private boolean translateKeyDown(int keyCode, KeyEvent event) {
        metaState = MetaKeyKeyListener.handleKeyDown(metaState,
                keyCode, event);
        int c = event.getUnicodeChar(MetaKeyKeyListener.getMetaState(metaState));
        metaState = MetaKeyKeyListener.adjustMetaAfterKeypress(metaState);
        InputConnection ic = getCurrentInputConnection();
        if (c == 0 || ic == null) {
            return false;
        }

        boolean dead = false;

        if ((c & KeyCharacterMap.COMBINING_ACCENT) != 0) {
            dead = true;
            c = c & KeyCharacterMap.COMBINING_ACCENT_MASK;
        }

        if (composing.length() > 0) {
            char accent = composing.charAt(composing.length() - 1);
            int composed = KeyEvent.getDeadChar(accent, c);

            if (composed != 0) {
                c = composed;
                composing.setLength(composing.length() - 1);
            }
        }

        onKey(c, null);

        return true;
    }

    /*
     * Monitor key events being delivered to the application.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                // the InputMethodService already takes care of the back key for us
                // to dismiss the input method if it is shown
                if (event.getRepeatCount() == 0 && inputView != null) {
                    if (inputView.handleBack()) {
                        return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_DEL:
                if (composing.length() > 0) {
                    onKey(Keyboard.KEYCODE_DELETE, null);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_ENTER:
                // let the underlying text editor always handle these
                return false;

            default:
                // process and take appropriate action
                if (PROCESS_HARD_KEYS) {
                    if (keyCode == KeyEvent.KEYCODE_SPACE
                            && (event.getMetaState() & KeyEvent.META_ALT_ON) != 0) {
                        // Alt+Space is a shortcut for 'android' in lower case.
                        InputConnection inputConnection = getCurrentInputConnection();
                        if (inputConnection != null) {
                            // first, tell the editor that it is no longer in the
                            // shift state and consume it
                            inputConnection.clearMetaKeyStates(KeyEvent.META_ALT_ON);
                            keyDownUp(KeyEvent.KEYCODE_A);
                            keyDownUp(KeyEvent.KEYCODE_N);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            keyDownUp(KeyEvent.KEYCODE_R);
                            keyDownUp(KeyEvent.KEYCODE_O);
                            keyDownUp(KeyEvent.KEYCODE_I);
                            keyDownUp(KeyEvent.KEYCODE_D);
                            // consume the event
                            return true;
                        }
                    }
                    if (translateKeyDown(keyCode, event)) {
                        return true;
                    }
                }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // If we want to do transformations on text being entered with a hard
        // keyboard, we need to process the up events to update the meta key
        // state we are tracking.
        if (PROCESS_HARD_KEYS) {
            // metaState = MetaKeyKeyListener.handleKeyUp(metaState, keyCode, event);
        }
        return super.onKeyUp(keyCode, event);
    }

    private void commitTyped(InputConnection inputConnection) {
        if (composing.length() > 0) {
            prev = composing.toString();
            String encrypted = ((DencrypApplication) getApplication()).getCryptor().encrypt(prev);
            encrypted = encrypted.substring(0, encrypted.length() - 1);
            inputConnection.commitText(encrypted, encrypted.length());
            composing.setLength(0);
        }
    }

    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && inputView != null && qwertyKeyboard == inputView.getKeyboard()) {
            int caps = 0;
            EditorInfo editorInfo = getCurrentInputEditorInfo();
            if (editorInfo != null && editorInfo.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }
            inputView.setShifted(capsLock || caps != 0);
        }
    }

    private boolean isAlphabet(int code) {
        return Character.isLetter(code);
    }

    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }

    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KeyEvent.KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }

    // Implementation of KeyboardViewListener

    public void onKey(int primaryCode, int[] keyCodes) {
        if (isWordSeparator(primaryCode)) {
            // handle separator
            if (composing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            // show a menu or something
        } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE
                && inputView != null) {
            Keyboard current = inputView.getKeyboard();
            if (current == symbolsKeyboard || current == symbolsShiftedKeyboard) {
                setLatinKeyboard(qwertyKeyboard);
            } else {
                setLatinKeyboard(symbolsKeyboard);
                symbolsKeyboard.setShifted(false);
            }
        } else {
            handleCharacter(primaryCode, keyCodes);
        }
    }

    public void onText(CharSequence text) {
        InputConnection inputConnection = getCurrentInputConnection();
        if (inputConnection == null) return;
        inputConnection.beginBatchEdit();
        if (composing.length() > 0) {
            commitTyped(inputConnection);
        }
        inputConnection.commitText(text, 0);
        inputConnection.endBatchEdit();
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleBackspace() {
        final int length = composing.length();
        if (length > 1) {
            composing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(composing, 1);
        } else if (length > 0) {
            composing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
        } else if (!"".equals(prev)) {
            InputConnection inputConnection = getCurrentInputConnection();
            String text = inputConnection.getExtractedText(new ExtractedTextRequest(), 0).text.toString();
            Log.d(TAG, "text: " + text);
            inputConnection.deleteSurroundingText(100, 100);
            inputConnection.commitText(prev, text.length());
            prev = "";
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }

    private void handleShift() {
        if (inputView == null) {
            return;
        }

        Keyboard currentKeyboard = inputView.getKeyboard();
        if (qwertyKeyboard == currentKeyboard) {
            checkToggleCapsLock();
            inputView.setShifted(capsLock || !inputView.isShifted());
        } else if (currentKeyboard == symbolsKeyboard) {
            symbolsKeyboard.setShifted(true);
            setLatinKeyboard(symbolsShiftedKeyboard);
            symbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == symbolsShiftedKeyboard) {
            symbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(symbolsKeyboard);
            symbolsKeyboard.setShifted(false);
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (inputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (isAlphabet(primaryCode)) {
            composing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(composing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
    }

    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        inputView.closing();
    }

    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }

    private void handleLanguageSwitch() {
        inputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }

    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();
        if (lastShiftTime + 800 > now) {
            capsLock = !capsLock;
            lastShiftTime = 0;
        } else {
            lastShiftTime = now;
        }
    }

    private String getWordSeparators() {
        return wordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char) code));
    }

    public void swipeRight() {
        //TODO: space it
    }

    public void swipeLeft() {
        handleBackspace();
    }

    public void swipeDown() {
        handleClose();
    }

    public void swipeUp() {
    }

    public void onPress(int primaryCode) {
    }

    public void onRelease(int primaryCode) {
    }
}
