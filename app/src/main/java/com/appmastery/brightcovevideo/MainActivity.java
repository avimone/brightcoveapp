package com.appmastery.brightcovevideo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.mediarouter.app.MediaRouteButton;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.brightcove.cast.GoogleCastComponent;
import com.brightcove.cast.GoogleCastEventType;
import com.brightcove.player.analytics.Analytics;
import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.edge.Catalog;
import com.brightcove.player.edge.VideoListener;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.mediacontroller.ThumbnailComponent;
import com.brightcove.player.model.DeliveryType;
import com.brightcove.player.model.Video;
import com.brightcove.player.pictureinpicture.PictureInPictureManager;
import com.brightcove.player.view.BaseVideoView;
import com.brightcove.player.view.BrightcoveExoPlayerVideoView;
import com.brightcove.player.view.BrightcovePlayer;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;

import java.net.URISyntaxException;

public class MainActivity extends BrightcovePlayer {
    public static final String FONT_AWESOME = "fontawesome-webfont.ttf";
    private Button playbackSpeed;
    private Button fforward;
    private Button chromeCast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureThumbnailScrubber(brightcoveVideoView);

        BrightcoveMediaController mediaController = new BrightcoveMediaController(brightcoveVideoView, R.layout.my_media_controller);
        brightcoveVideoView.setMediaController(mediaController);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);


        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();

        playbackSpeed =  findViewById(R.id.playbackSpeed);
        playbackSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayerSpeedDialog();
            }
        });


        Catalog catalog = new Catalog(eventEmitter, getString(R.string.account), getString(R.string.policy));
        catalog.findVideoByID(getString(R.string.videoId), new VideoListener() {

            // Add the video found to the queue with add().
            // Start playback of the video with start().
            @Override
            public void onVideo(Video video) {
                brightcoveVideoView.add(video);
                brightcoveVideoView.start();
            }

            @Override
            public void onError(String s) {
                throw new RuntimeException(s);
            }
        });

        fforward =  findViewById(R.id.skip_ahead);
        fforward.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              int currentTime = brightcoveVideoView.getCurrentPosition();
              Log.d("STATE",currentTime + "");
              brightcoveVideoView.seekTo(currentTime + 15000);

              }
           });
        chromeCast =  findViewById(R.id.cast);
        chromeCast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                eventEmitter.on(GoogleCastEventType.CAST_SESSION_STARTED, event -> {
                    // Connection Started
                });

                eventEmitter.on(GoogleCastEventType.CAST_SESSION_ENDED, event -> {
                    // Connection Ended
                });

                GoogleCastComponent googleCastComponent = new GoogleCastComponent.Builder(eventEmitter, MainActivity.this)
                        .setAutoPlay(true)
                        .build();

                //You can check if there is a session available
                googleCastComponent.isSessionAvailable();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        GoogleCastComponent.setUpMediaRouteButton(this, menu);
        return true;
    }


    public void configureThumbnailScrubber(BaseVideoView brightcoveVideoView) {
        Log.v(TAG, "Thumbnail Scrubbing is enabled, setting up the PreviewThumbnailController");
        ThumbnailComponent thumbnailComponent = new ThumbnailComponent(brightcoveVideoView);
        thumbnailComponent.setupPreviewThumbnailController();
    }
    private void showPlayerSpeedDialog() {
        String[] playerSpeedArrayLabels = {"0.8x", "1.0x", "1.2x", "1.5x", "1.8x", "2.0x"};

        PopupMenu popupMenu = new PopupMenu(MainActivity.this, playbackSpeed);
        for (int i = 0; i < playerSpeedArrayLabels.length; i++) {
            popupMenu.getMenu().add(i, i, i, playerSpeedArrayLabels[i]);
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            CharSequence itemTitle = item.getTitle();
            float playbackSpeed = Float.parseFloat(itemTitle.subSequence(0, 3).toString());
            changePlayerSpeed(playbackSpeed, itemTitle.subSequence(0, 3).toString());
            return false;
        });
        popupMenu.show();
    }
    private void changePlayerSpeed(float speed, String speedLabel) {
        Log.d("STATE",speedLabel);
        // Set playback speed
        ((ExoPlayerVideoDisplayComponent) brightcoveVideoView.getVideoDisplay()).getExoPlayer().setPlaybackParameters(new PlaybackParameters(speed, 1.0f));
        // Set playback speed label
        if(speedLabel.equals("2.0")){
            playbackSpeed.setBackgroundResource(R.drawable.ic_baseline_looks_two_24);
        }
        if(speedLabel.equals("1.0")){
            playbackSpeed.setBackgroundResource(R.drawable.ic_ps1_24);
        }
    }

}
