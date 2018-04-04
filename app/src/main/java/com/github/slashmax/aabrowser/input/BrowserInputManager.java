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
    private WeakReference<View> inputView;
    private WeakReference<View> editable;

    private View getInputView() {
        return inputView == null ? null : inputView.get();
    }

    private View getEditable() {
        return editable == null ? null : editable.get();
    }

    public void setInputView(final View view) {
        inputView = new WeakReference<>(view);
    }

    public void startInput(View editable) {
        this.editable = new WeakReference<View>(editable);

        final View dummyView = getInputView();
        if (dummyView != null) {
            System.out.println("BrowserInputManager.startInput");
            final InputMethodManager inputMethodManager = (InputMethodManager) dummyView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(dummyView, 0 );
        }
    }

    public void stopInput() {
        final View dummyView = getInputView();
        if (dummyView != null) {
            System.out.println("BrowserInputManager.stopInput");
            final InputMethodManager inputMethodManager = (InputMethodManager) dummyView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromInputMethod(dummyView.getWindowToken(), 0);
        }

        editable = null;
    }

    public boolean sendKeyEvent(final KeyEvent event) {
        System.out.println("BrowserInputManager.sendKeyEvent");
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
        System.out.println("BrowserInputManager.deleteSurroundingText");
        final View editable = getEditable();
        if (editable != null) {
            boolean ok = editable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
            ok = ok && editable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            return ok;
        }
        return false;
    }

}
