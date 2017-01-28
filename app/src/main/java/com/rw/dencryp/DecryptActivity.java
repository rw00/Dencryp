package com.rw.dencryp;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.EditText;

import com.rw.dencryp.crypt.DecryptionUtil;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;

public class DecryptActivity extends Activity {
    private CoordinatorLayout coordinatorLayout;
    private ClipboardManager clipboard;
    private EditText textArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decrypt);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        textArea = (EditText) findViewById(R.id.edittextArea);
    }

    public void pasteContent(View view) {
        if (!clipboard.hasPrimaryClip()) {
            // no data
        } else if (!clipboard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN)) {
            // not plain text data
        } else {
            // the clipboard contains plain text
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            textArea.setText(item.getText().toString());
        }
    }

    public void clearContent(View view) {
        textArea.setText("");
    }

    public void copyContent(View view) {
        ClipData clip = ClipData.newPlainText("DencrypText", textArea.getText().toString());
        clipboard.setPrimaryClip(clip);
        Snackbar snackbar = Snackbar.make(coordinatorLayout, "Text Copied!", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void decryptText(View view) {
        String encrypted = textArea.getText().toString();
        String decrypted = DecryptionUtil.decrypt(encrypted);
        textArea.setText(decrypted);
    }
}
