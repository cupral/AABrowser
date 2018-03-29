package com.github.slashmax.aabrowser.input;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.lang.ref.WeakReference;

/**
 * Created by ljannace on 29/03/18.
 */

public class BrowserInputManager {
    private WeakReference<View> dummyView;
    private WeakReference<View> editable;

    private View getDummyView() {
        return dummyView == null ? null : dummyView.get();
    }

    private View getEditable() {
        return editable == null ? null : editable.get();
    }

    public void setDummyView(final View view) {
        dummyView = new WeakReference<>(view);
    }

    public void setEditable(final View view) {
        editable = new WeakReference<>(view);
    }

    public void startInput() {
        final View dummyView = getDummyView();
        if (dummyView != null) {
            ((InputMethodManager) dummyView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(dummyView, 0);
        }
    }

    public void stopInput() {
        final View dummyView = getDummyView();
        if (dummyView != null) {
            ((InputMethodManager) dummyView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(dummyView.getWindowToken(), 0);
        }
    }

    public boolean sendKeyEvent(final KeyEvent event) {
        final View editable = getEditable();
        if (editable != null) {
            boolean ok = editable.dispatchKeyEvent(event);
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                stopInput();
            }
            return ok;
        }
        return false;
    }

    public boolean deleteSurroundingText(final int beforeLength, final int afterLength) {
        final View editable = getEditable();
        if (editable != null) {
            boolean ok = editable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            ok = ok && editable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            return ok;
        }
        return false;
    }

}
