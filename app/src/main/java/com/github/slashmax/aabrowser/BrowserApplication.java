package com.github.slashmax.aabrowser;

import android.app.Application;

import com.github.slashmax.aabrowser.input.BrowserInputManager;

/**
 * Created by ljannace on 29/03/18.
 */

public class BrowserApplication extends Application{

    private static BrowserApplication INSTANCE;

    private BrowserInputManager browserInputManager;

    @Override
    public void onCreate() {
        INSTANCE = this;

        super.onCreate();

        browserInputManager = new BrowserInputManager();
    }

    public static BrowserInputManager getInputManager() {
        return INSTANCE.browserInputManager;
    }
}
