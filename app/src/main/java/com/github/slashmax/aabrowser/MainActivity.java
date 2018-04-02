package com.github.slashmax.aabrowser;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BrowserApplication.getInputManager().setDummyView(findViewById(R.id.m_DummyView));

        findViewById(R.id.m_DummyView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BrowserApplication.getInputManager().startInput(null);
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

}
