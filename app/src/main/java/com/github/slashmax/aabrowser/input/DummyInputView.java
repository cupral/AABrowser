package com.github.slashmax.aabrowser.input;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;
import android.widget.EditText;

import com.github.slashmax.aabrowser.BrowserApplication;

/**
 * Created by ljannace on 29/03/18.
 */

public class DummyInputView extends WebView {
    public DummyInputView(Context context) {
        super(context);
    }

    public DummyInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DummyInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*
    public DummyInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    */

    @Override
    public boolean onCheckIsTextEditor() {
        System.out.println("DummyInputView.onCheckIsTextEditor");
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(final EditorInfo outAttrs) {
        System.out.println("DummyInputView.onCreateInputConnection");
        return new BaseInputConnection(this, false) {
            public boolean sendKeyEvent(KeyEvent event) {
                return BrowserApplication.getInputManager().sendKeyEvent(event);
            }

            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                return BrowserApplication.getInputManager().deleteSurroundingText(beforeLength, afterLength);
            }
        };
    }
}
