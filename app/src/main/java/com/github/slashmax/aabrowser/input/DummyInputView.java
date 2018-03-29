package com.github.slashmax.aabrowser.input;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.github.slashmax.aabrowser.BrowserApplication;

/**
 * Created by ljannace on 29/03/18.
 */

public class DummyInputView extends View {
    public DummyInputView(Context context) {
        super(context);
    }

    public DummyInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DummyInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DummyInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(final EditorInfo outAttrs) {
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
