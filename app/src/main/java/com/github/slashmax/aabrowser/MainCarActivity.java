package com.github.slashmax.aabrowser;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.car.Car;
import android.support.car.CarConnectionCallback;
import android.support.car.media.CarAudioManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.apps.auto.sdk.CarActivity;
import com.google.android.apps.auto.sdk.CarUiController;
import com.google.android.apps.auto.sdk.DayNightStyle;
import com.google.android.apps.auto.sdk.SearchCallback;
import com.google.android.apps.auto.sdk.SearchItem;
import com.google.android.gms.car.input.CarEditable;
import com.google.android.gms.car.input.CarEditableListener;

import static android.graphics.Bitmap.Config.ALPHA_8;
import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.support.car.media.CarAudioManager.CAR_AUDIO_USAGE_DEFAULT;
import static android.view.MotionEvent.ACTION_UP;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class MainCarActivity extends CarActivity implements CarEditable , View.OnTouchListener
{
    private static final String TAG = "MainCarActivity";

    private static final String DEFAULT_HOME = "https://www.google.com";
    private static final String DEFAULT_SEARCH = "https://www.google.com/search?q=";

    private static final String DEFAULT_LINUX_AGENT     = "Mozilla/5.0 (Linux;) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Safari/537.36";
    private static final String DEFAULT_WINDOWS_AGENT   = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.71 Safari/537.36";

    private static final int DISPLAY_MIN_SIZE = 180;

    private Car             m_Car;

    private DrawerLayout    m_DrawerLayout;
    private LinearLayout    m_TaskBarDrawer;
    private FrameLayout     m_Fullscreen;
    private View            m_WebViewLayout;
    private WebView         m_WebView;
    private WebChromeClient m_WebChromeClient;
    private WebViewClient   m_WebViewClient;
    private Bitmap          m_DefaultVideoPoster;
    private ProgressBar     m_ProgressBar;

    private View            m_CurrentEditable;

    private String          m_HomeUrl;
    private String          m_LastUrl;
    private String          m_OriginalUserAgent;
    private int             m_UserAgentIndex;

    @Override
    public void onCreate(Bundle bundle)
    {
        Log.d(TAG, "onCreate: " + (bundle != null ? bundle.toString() : "null"));
        setTheme(R.style.AppTheme);
        super.onCreate(bundle);
        setContentView(R.layout.activity_car_main);

        InitCarUiController(getCarUiController());
        setIgnoreConfigChanges(0xFFFF);

        InitWebChromeClient();
        InitWebViewClient();

        m_DefaultVideoPoster = Bitmap.createBitmap(1, 1, ALPHA_8);

        m_DrawerLayout = (DrawerLayout)findViewById(R.id.m_DrawerLayout);
        m_TaskBarDrawer = (LinearLayout)findViewById(R.id.m_TaskBarDrawer);
        m_Fullscreen = (FrameLayout)findViewById(R.id.m_Fullscreen);
        m_WebViewLayout = findViewById(R.id.m_WebViewLayout);
        m_ProgressBar = (ProgressBar) findViewById(R.id.m_ProgressBar);

        m_WebView = (WebView)findViewById(R.id.m_WebView);

        m_WebView.getSettings().setJavaScriptEnabled(true);
        m_WebView.getSettings().setDomStorageEnabled(true);
        m_WebView.getSettings().setDatabaseEnabled(true);
        m_WebView.getSettings().setGeolocationEnabled(true);

        m_WebView.getSettings().setUseWideViewPort(true);
        m_WebView.getSettings().setLoadWithOverviewMode(true);
        m_WebView.getSettings().setLoadsImagesAutomatically(true);
        m_WebView.getSettings().setMediaPlaybackRequiresUserGesture(false);

        m_WebView.getSettings().setSupportZoom(true);
        m_WebView.getSettings().setBuiltInZoomControls(true);
        m_WebView.getSettings().setDisplayZoomControls(false);

        m_WebView.setFocusable(true);
        m_WebView.setFocusableInTouchMode(true);

        m_WebView.setWebChromeClient(m_WebChromeClient);
        m_WebView.setWebViewClient(m_WebViewClient);

        CookieManager.getInstance().setAcceptThirdPartyCookies(m_WebView, true);

        m_WebView.setOnTouchListener(this);

        m_OriginalUserAgent = m_WebView.getSettings().getUserAgentString();
        Log.d(TAG, "m_OriginalUserAgent: " + m_OriginalUserAgent);

        LoadSharedPreferences();
        UpdateConfiguration(getResources().getConfiguration());
        InitButtonsActions();

        if (bundle == null)
            goLast();

        m_Car = Car.createCar(this, new CarConnectionCallback()
        {
            @Override
            public void onConnected(Car car)
            {
                Log.d(TAG, "onConnected");
                RequestAudioFocus();
            }

            @Override
            public void onDisconnected(Car car)
            {
                Log.d(TAG, "onDisconnected");
                AbandonAudioFocus();
            }
        });
        m_Car.connect();
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if (m_Car.isConnected())
            m_Car.disconnect();
    }

    @Override
    public void onRestoreInstanceState(Bundle bundle)
    {
        Log.d(TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(bundle);
        m_WebView.restoreState(bundle);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        Log.d(TAG, "onSaveInstanceState");
        super.onSaveInstanceState(bundle);
        m_WebView.saveState(bundle);
        SaveSharedPreferences();
    }

    @Override
    public void onStart()
    {
        Log.d(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onStop()
    {
        Log.d(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onPause()
    {
        Log.d(TAG, "onPause");
        super.onPause();
        m_WebView.onPause();
    }

    @Override
    public void onResume()
    {
        Log.d(TAG, "onResume");
        super.onResume();
        m_WebView.onResume();
    }

    @Override
    public void onPostResume()
    {
        Log.d(TAG, "onPostResume");
        super.onPostResume();
    }

    @Override
    public void onWindowFocusChanged(boolean b, boolean b1)
    {
        Log.d(TAG, "onWindowFocusChanged");
        super.onWindowFocusChanged(b, b1);

        if (!a().isInputActive()){
            stopInput();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration configuration)
    {
        Log.d(TAG, "onConfigurationChanged: " + (configuration != null ? configuration.toString() : "null"));
        super.onConfigurationChanged(configuration);
        UpdateConfiguration(configuration);
    }

    private void InitCarUiController(CarUiController controller)
    {
        Log.d(TAG, "InitCarUiController");
        controller.getStatusBarController().setTitle("");
        controller.getStatusBarController().hideAppHeader();
        controller.getStatusBarController().setAppBarAlpha(0.0f);
        controller.getStatusBarController().setAppBarBackgroundColor(Color.WHITE);
        controller.getStatusBarController().setDayNightStyle(DayNightStyle.AUTO);
        controller.getMenuController().hideMenuButton();

        controller.getSearchController().setSearchCallback(new SearchCallback()
        {
            @Override
            public void onSearchItemSelected(SearchItem searchItem)
            {
                Log.d(TAG, "onSearchItemSelected");
            }

            @Override
            public void onSearchStart()
            {
                Log.d(TAG, "onSearchStart");
                super.onSearchStart();
            }

            @Override
            public void onSearchStop()
            {
                Log.d(TAG, "onSearchStop");
                super.onSearchStop();
            }

            @Override
            public boolean onSearchSubmitted(String s)
            {
                Log.d(TAG, "onSearchSubmitted: " + (s != null ? s : "null"));
                doSearch(s);
                return true;
            }

            @Override
            public void onSearchTextChanged(String s)
            {
                Log.d(TAG, "onSearchTextChanged: " + (s != null ? s : "null"));
            }
        });
    }

    private void InitWebChromeClient()
    {
        Log.d(TAG, "InitWebChromeClient");
        m_WebChromeClient = new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                m_ProgressBar.setProgress(newProgress);
                m_ProgressBar.setVisibility(newProgress<100 ? View.VISIBLE : View.INVISIBLE);
                Log.d(TAG, "onProgressChanged: " + newProgress);
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onReceivedTitle(WebView view, String title)
            {
                Log.d(TAG, "onReceivedTitle: " + title);
                super.onReceivedTitle(view, title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon)
            {
                Log.d(TAG, "onReceivedIcon");
                super.onReceivedIcon(view, icon);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                Log.d(TAG, "onShowCustomView");
                m_Fullscreen.setBackgroundColor(Color.BLACK);
                m_Fullscreen.addView(view);
                m_Fullscreen.bringToFront();
                m_TaskBarDrawer.bringToFront();
            }

            @Override
            public void onHideCustomView()
            {
                Log.d(TAG, "onHideCustomView");
                m_Fullscreen.removeAllViewsInLayout();
                m_WebViewLayout.bringToFront();
                m_TaskBarDrawer.bringToFront();
            }

            @Override
            public Bitmap getDefaultVideoPoster()
            {
                Log.d(TAG, "getDefaultVideoPoster");
                return m_DefaultVideoPoster;
            }

            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        };
    }

    private void InitWebViewClient()
    {
        Log.d(TAG, "InitWebViewClient");
        m_WebViewClient = new WebViewClient();
    }

    private void InitButtonsActions()
    {
        Log.d(TAG, "InitButtonsActions");

        ImageView back = (ImageView)findViewById(R.id.m_Back);
        if (back != null)
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Back.onClick");
                    m_WebView.goBack();
                }
            });

        ImageView reload = (ImageView)findViewById(R.id.m_Reload);
        if (reload != null)
            reload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Reload.onClick");
                    m_WebView.reload();
                    m_DrawerLayout.closeDrawers();
                }
            });

        ImageView home = (ImageView)findViewById(R.id.m_Home);
        if (home != null)
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_HomeUrl.onClick");
                    goHome();
                    m_DrawerLayout.closeDrawers();
                }
            });


        ImageView browse = (ImageView)findViewById(R.id.m_Browse);
        if (browse != null)
            browse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Browse.onClick");
                    getCarUiController().getSearchController().startSearch("");
                    m_DrawerLayout.closeDrawers();
                }
            });

        ImageView desktop =(ImageView)findViewById(R.id.m_DesktopMode);
        if (desktop != null)
            desktop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_DesktopMode.onClick");

                    if (m_UserAgentIndex == 0)
                        SetUserAgentIndex(1);
                    else
                        SetUserAgentIndex(0);

                    m_WebView.reload();
                }
            });

        ImageView keyboard = (ImageView)findViewById(R.id.m_Keyboard);
        if (keyboard != null)
            keyboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "m_Keyboard.onClick");
                    startInput(m_WebView, 0);
                    m_DrawerLayout.closeDrawers();
                }
            });
    }

    private void go(String url)
    {
        Log.d(TAG, "go: " + url);
        m_WebView.loadUrl(url);
    }

    private void goHome()
    {
        Log.d(TAG, "goHome");
        go(m_HomeUrl);
    }

    private void goLast()
    {
        Log.d(TAG, "goCurrent");
        if (m_LastUrl == null || m_LastUrl.isEmpty())
            goHome();
        else
            go(m_LastUrl);
    }

    private void doSearch(String query)
    {
        Log.d(TAG, "doSearch : " + (query != null ? query : "null"));
        go(DEFAULT_SEARCH + query);
    }

    @ColorInt
    private int getColorCompat(@ColorRes int id)
    {
        if (Build.VERSION.SDK_INT >= 23)
            return getColor(id);
        else
            return getResources().getColor(id);
    }

    private void UpdateConfiguration(Configuration configuration)
    {
        if (configuration == null)
            return;

        Log.d(TAG, "UpdateConfiguration: " + configuration.toString());
        int backgroundColor;
        if ((configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
            backgroundColor = getColorCompat(R.color.colorCarBackgroundNight);
        else
            backgroundColor = getColorCompat(R.color.colorCarBackgroundDay);

        m_TaskBarDrawer.setBackgroundColor(backgroundColor);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (event != null && event.getAction() == ACTION_UP)
        {
            WebView.HitTestResult hitTest = m_WebView.getHitTestResult();
            if (hitTest != null && hitTest.getType() == WebView.HitTestResult.EDIT_TEXT_TYPE)
            {
                startInput(m_WebView, event.getY());
            }
            else
            {
                stopInput();
            }
        }
        return false;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo editorInfo)
    {
        Log.d(TAG, "onCreateInputConnection");
        InputConnection result = null;
        if (m_CurrentEditable != null)
        {
            result = new BaseInputConnection(m_CurrentEditable, false)
            {
                public boolean sendKeyEvent(KeyEvent event)
                {
                    boolean ok = m_CurrentEditable.dispatchKeyEvent(event);
                    if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                        stopInput();
                    return ok;
                }

                public boolean deleteSurroundingText(int beforeLength, int afterLength)
                {
                    boolean ok = m_CurrentEditable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
                    ok = ok && m_CurrentEditable.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                    return ok;
                }
            };
        }
        return result;
    }

    @Override
    public void setCarEditableListener(CarEditableListener carEditableListener)
    {
        Log.d(TAG, "setCarEditableListener");
    }

    @Override
    public void setInputEnabled(boolean b)
    {
        Log.d(TAG, "setInputEnabled: " + b);
    }

    private void startInput(View view, float pos)
    {
        Log.d(TAG, "startInput");
        m_CurrentEditable = view;

        if (m_WebView.getLayoutParams().height == MATCH_PARENT) {
            m_CurrentEditable.getLayoutParams().height = DISPLAY_MIN_SIZE;
            m_CurrentEditable.requestLayout();
            if (pos > DISPLAY_MIN_SIZE) {
                m_WebView.scrollBy(0, (int) pos - 100);
            }
        }

        a().startInput(this);
    }

    private void stopInput()
    {
        Log.d(TAG, "stopInput");
        if (a().isInputActive())
            a().stopInput();
        m_CurrentEditable = null;

        if (m_WebView.getLayoutParams().height != MATCH_PARENT)
        {
            m_WebView.getLayoutParams().height = MATCH_PARENT;
            m_WebView.requestLayout();
        }
    }

    private void SetUserAgentIndex(int index)
    {
        Log.d(TAG, "SetUserAgentIndex: " + index);
        m_UserAgentIndex = index;
        ImageView desktop =(ImageView)findViewById(R.id.m_DesktopMode);
        if (m_UserAgentIndex == 0)
        {
            m_WebView.getSettings().setUserAgentString(m_OriginalUserAgent);
            desktop.setImageDrawable(getDrawable(R.drawable.ic_tablet_android_black));
        }
        else if (m_UserAgentIndex == 1)
        {
            m_WebView.getSettings().setUserAgentString(DEFAULT_LINUX_AGENT);
            desktop.setImageDrawable(getDrawable(R.drawable.ic_desktop_windows_black));
        }
    }

    private void RequestAudioFocus()
    {
        Log.d(TAG, "RequestAudioFocus");
        try
        {
            CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
            carAM.requestAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT), AUDIOFOCUS_GAIN, 0);
        }
        catch (Exception e)
        {
            Log.d(TAG, "RequestAudioFocus exception: " + e.toString());
        }
    }

    private void AbandonAudioFocus()
    {
        Log.d(TAG, "AbandonAudioFocus");
        try
        {
            CarAudioManager carAM = m_Car.getCarManager(CarAudioManager.class);
            carAM.abandonAudioFocus(null, carAM.getAudioAttributesForCarUsage(CAR_AUDIO_USAGE_DEFAULT));
        }
        catch (Exception e)
        {
            Log.d(TAG, "AbandonAudioFocus exception: " + e.toString());
        }
    }

    private void LoadSharedPreferences()
    {
        Log.d(TAG, "LoadSharedPreferences");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        m_HomeUrl = sharedPref.getString("m_HomeUrl", DEFAULT_HOME);
        m_LastUrl = sharedPref.getString("m_LastUrl", DEFAULT_HOME);
        m_UserAgentIndex = sharedPref.getInt("m_UserAgentIndex", 0);

        SetUserAgentIndex(m_UserAgentIndex);
    }

    private void SaveSharedPreferences()
    {
        Log.d(TAG, "SaveSharedPreferences");

        m_LastUrl = m_WebView.getUrl();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("m_HomeUrl", m_HomeUrl);
        editor.putString("m_LastUrl", m_LastUrl);
        editor.putInt("m_UserAgentIndex", m_UserAgentIndex);
        editor.apply();
    }
}
