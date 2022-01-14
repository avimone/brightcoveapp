package com.appmastery.brightcovevideo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import com.brightcove.cast.DefaultSessionManagerListener;
import com.brightcove.cast.GoogleCastComponent;
import com.brightcove.cast.model.SplashScreen;
import com.brightcove.cast.util.BrightcoveChannelUtil;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.Session;

public class MainActivity2 extends AppCompatActivity {
Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        CastContext castContext = CastContext.getSharedInstance();
        if (castContext != null) {
            castContext.getSessionManager().addSessionManagerListener(defaultSessionManagerListener);
        }

        b = findViewById(R.id.button2);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity2.this,MainActivity.class);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CastContext castContext = CastContext.getSharedInstance();
        if (castContext != null) {
            castContext.getSessionManager().removeSessionManagerListener(defaultSessionManagerListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        GoogleCastComponent.setUpMediaRouteButton(this, menu);
        return true;
    }
    private DefaultSessionManagerListener defaultSessionManagerListener = new DefaultSessionManagerListener() {
        @Override
        public void onSessionStarted(Session castSession, String s) {
            super.onSessionStarted(castSession, s);
            String src = "https://dev.acquia.com/sites/default/files/blog/brightcove-logo-horizontal-grey-new.png";
            BrightcoveChannelUtil.castSplashScreen((CastSession) castSession, new SplashScreen(src));
        }
    };
}