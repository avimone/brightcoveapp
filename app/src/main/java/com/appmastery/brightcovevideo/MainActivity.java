package com.appmastery.brightcovevideo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.mediarouter.app.MediaRouteButton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.brightcove.cast.DefaultSessionManagerListener;
import com.brightcove.cast.GoogleCastComponent;
import com.brightcove.cast.GoogleCastEventType;
import com.brightcove.cast.model.BrightcoveCastCustomData;
import com.brightcove.cast.model.CustomData;
import com.brightcove.cast.model.SplashScreen;
import com.brightcove.cast.util.BrightcoveChannelUtil;
import com.brightcove.player.analytics.Analytics;
import com.brightcove.player.captioning.BrightcoveCaptionFormat;
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
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.Session;
import com.google.android.gms.cast.framework.SessionProvider;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends BrightcovePlayer {
    public static final String FONT_AWESOME = "fontawesome-webfont.ttf";
    private Button playbackSpeed;
    private Button fforward;
    private Button play;
    private Button captions;
    List<String> captionsLanguages;
    List<String> audioLanguages;

    int selectedId = R.id.rb1x;
    int defaultCaptionId = 777;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configureThumbnailScrubber(brightcoveVideoView);
        CastContext castContext = CastContext.getSharedInstance(this);
        if (castContext != null) {
            castContext.getSessionManager().addSessionManagerListener(defaultSessionManagerListener);
        }
        BrightcoveMediaController mediaController = new BrightcoveMediaController(brightcoveVideoView, R.layout.my_media_controller);
        brightcoveVideoView.setMediaController(mediaController);
        brightcoveVideoView = (BrightcoveExoPlayerVideoView) findViewById(R.id.brightcove_video_view);



        EventEmitter eventEmitter = brightcoveVideoView.getEventEmitter();

        captions =  findViewById(R.id.captions_custom);
        captions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaptionsDialog();
            }
        });


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


                Log.d("STATE","captions "+ video.getProperties().get(Video.Fields.CAPTION_SOURCES));
                Log.d("STATE","captions "+ video.getProperties().get(Video.Fields.HEADERS));


                brightcoveVideoView.add(video);
                brightcoveVideoView.start();
            }

            @Override
            public void onError(String s) {
                throw new RuntimeException(s);
            }
        });






        play =  findViewById(R.id.play);

        fforward =  findViewById(R.id.skip_ahead);
        fforward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentTime = brightcoveVideoView.getCurrentPosition();
                Log.d("STATE",currentTime + "");
                brightcoveVideoView.seekTo(currentTime + 15000);
                DefaultTrackSelector trackSelector = new DefaultTrackSelector();
                trackSelector.setParameters(
                        trackSelector.getParameters().buildUpon().setMaxVideoSizeSd()
                                .setPreferredTextLanguage("en")
                                .setPreferredAudioLanguage("en").build());
            }
        });



        CustomData customData = new BrightcoveCastCustomData.Builder(this)
                .setAccountId(getString(R.string.account))
                // Set your account’s policy key
                .setPolicyKey(getString(R.string.policy))
                // Optional: Set your Edge Playback Authorization (EPA) JWT token here
                // Note that if you set the EPA token, you will not need to set the Policy Key
                .setBrightcoveAuthorizationToken(null)
                // Optional: For SSAI videos, set your adConfigId here
                // Set your Analytics application ID here
                .build();


        eventEmitter.on(EventType.CAPTIONS_LANGUAGES, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Map<String,Object> langs = event.getProperties();
                captionsLanguages = (List<String>) langs.get("languages");
                Log.d("STATE","captions " + event.getProperties());
                Log.d("STATE","captions " + langs.get("languages"));

                // React to the event
            }
        });


        eventEmitter.on(EventType.AUDIO_TRACKS, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","AUDIO " + event.getProperties());

                // React to the event
            }
        });





        eventEmitter.on(EventType.PAUSE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","Paused");
                // React to the event
                play.setBackgroundResource(R.drawable.play);
            }
        });

        eventEmitter.on(EventType.PLAY, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","Playing");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);

            }
        });
        eventEmitter.on(EventType.SEEK_TO, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","Paused");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);

            }
        });
        eventEmitter.on(EventType.DID_EXIT_PICTURE_IN_PICTURE_MODE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","EXIT PIP");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);
            }
        });
        eventEmitter.on(EventType.DID_ENTER_PICTURE_IN_PICTURE_MODE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","EXIT PIP");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);
            }
        });
        eventEmitter.on(EventType.EXIT_PICTURE_IN_PICTURE_MODE, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","EXIT PIP");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);
            }
        });
        eventEmitter.on(EventType.REWIND, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.d("STATE","Paused");
                // React to the event
                play.setBackgroundResource(R.drawable.pause);
            }
        });
        eventEmitter.on(GoogleCastEventType.CAST_SESSION_STARTED, event -> {
            // Connection Started
        });

        eventEmitter.on(GoogleCastEventType.CAST_SESSION_ENDED, event -> {
            // Connection Ended
        });
        GoogleCastComponent googleCastComponent = new GoogleCastComponent.Builder(eventEmitter, this)
                .setAutoPlay(true)
                .setEnableCustomData(true)
                .setCustomData(customData)
                .build();

        //You can check if there is a session available
        googleCastComponent.isSessionAvailable();

    }
    //Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        GoogleCastComponent.setUpMediaRouteButton(MainActivity.this, menu);
        return true;
    }

    @Override
    public void onPictureInPictureModeChanged (boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        PictureInPictureManager.getInstance().onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        play.setBackgroundResource(R.drawable.pause);

    }

    public void configureThumbnailScrubber(BaseVideoView brightcoveVideoView) {
        Log.v(TAG, "Thumbnail Scrubbing is enabled, setting up the PreviewThumbnailController");
        ThumbnailComponent thumbnailComponent = new ThumbnailComponent(brightcoveVideoView);
        thumbnailComponent.setupPreviewThumbnailController();
    }
    private void showPlayerSpeedDialog() {
        String[] playerSpeedArrayLabels = {"0.8x", "1.0x", "1.2x", "1.5x", "1.8x", "2.0x"};
/*
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
*/

        ////////////////////////// popup window ////////////////////////
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.playbackspeed_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        ImageButton close =(ImageButton)popupView.findViewById(R.id.close_playbackspeed_window);
        RadioGroup radioGroup = (RadioGroup)popupView.findViewById(R.id.rg);
        RadioButton selectedBtn = (RadioButton) popupView.findViewById(selectedId);
        selectedBtn.toggle();
        if(radioGroup.getCheckedRadioButtonId() == -1){


        }

        close.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton selected = (RadioButton) popupView.findViewById(selectedId);
                float playbackSpeed = Float.parseFloat(selected.getText().subSequence(0, 3).toString());
                changePlayerSpeed(playbackSpeed, selected.getText().subSequence(0, 3).toString());

                popupWindow.dismiss();
            }
        });


        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //      popupWindow.dismiss();

                return true;
            }
        });

    }


    @SuppressLint("ResourceType")
    private void showCaptionsDialog() {

        ////////////////////////// popup window ////////////////////////
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.captions_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        ImageButton close =(ImageButton)popupView.findViewById(R.id.close_playbackspeed_window);
        RadioGroup radioGroup = (RadioGroup)popupView.findViewById(R.id.rg);
        ////////////////////// create radio buttons ////////////////
        for (int i = 0; i < captionsLanguages.size(); i++) {
            Log.d("STATE",   captionsLanguages.get(i) + "  ");

            RadioButton radioButton1 = new RadioButton(this);
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            int marginStart = (int) getResources().getDisplayMetrics().density * 30; // 1dp to pixels
            buttonLayoutParams.setMarginStart(marginStart);
            radioButton1.setLayoutParams(buttonLayoutParams);
            radioButton1.setText(captionsLanguages.get(i));
            radioButton1.setTextSize(20);
            radioButton1.setTextColor(Color.parseColor("#FFFFFF"));
            radioButton1.setId(i);
            radioGroup.addView(radioButton1);

            View viewDivider = new View(this);
            int dividerHeight = (int) getResources().getDisplayMetrics().density * 1; // 1dp to pixels
            LinearLayout.LayoutParams dividerLayoutParams =    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight);
            dividerLayoutParams.setMarginStart(marginStart);
            viewDivider.setLayoutParams(dividerLayoutParams);
            viewDivider.setBackgroundColor(Color.parseColor("#AAAAAA"));
            radioGroup.addView(viewDivider);
        }
        RadioButton defaultRadioButton = new RadioButton(this);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginStart = (int) getResources().getDisplayMetrics().density * 30; // 1dp to pixels
        buttonLayoutParams.setMarginStart(marginStart);
        defaultRadioButton.setLayoutParams(buttonLayoutParams);
        defaultRadioButton.setText("None");
        defaultRadioButton.setTextSize(20);
        defaultRadioButton.setTextColor(Color.parseColor("#FFFFFF"));
        defaultRadioButton.setId(777);
        radioGroup.addView(defaultRadioButton);

        RadioButton selectedBtn = (RadioButton) popupView.findViewById(defaultCaptionId);
        selectedBtn.toggle();
        if(radioGroup.getCheckedRadioButtonId() == -1){


        }

        close.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Log.d("STATE",    radioGroup.getCheckedRadioButtonId() + " selected caption index " );
                defaultCaptionId = radioGroup.getCheckedRadioButtonId();
                RadioButton selected = (RadioButton) popupView.findViewById(defaultCaptionId);
                Log.d("STATE",    selected.getText() + " selected caption " );
                if(defaultCaptionId == 777)
                {
                    brightcoveVideoView.setClosedCaptioningEnabled(false);

                }
                else
                {
                    brightcoveVideoView.setClosedCaptioningEnabled(true);
                    selectCaption(brightcoveVideoView.getCurrentVideo(),selected.getText().toString());
                }

                popupWindow.dismiss();
            }
        });


        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //      popupWindow.dismiss();



                return true;
            }
        });

    }

    private void changePlayerSpeed(float speed, String speedLabel) {
        Log.d("STATE",speedLabel
                + speed);
        // Set playback speed
        ((ExoPlayerVideoDisplayComponent) brightcoveVideoView.getVideoDisplay()).getExoPlayer().setPlaybackParameters(new PlaybackParameters(speed, 1.0f));
        // Set playback speed label

        playbackSpeed.setText(speedLabel+"x");

    }


    private void selectCaption(Video video, String language) {
        Pair<Uri, BrightcoveCaptionFormat> pair = getCaptionsForLanguageCode(video, language);

        if (pair != null && !pair.first.equals(Uri.EMPTY)) {
            // BrightcoveCaptionFormat.BRIGHTCOVE_SCHEME indicates that is not a URL we need to load with the LoadCaptionsService, but instead we'll be enabled through a different component.
            if (!pair.first.toString().startsWith(BrightcoveCaptionFormat.BRIGHTCOVE_SCHEME)) {
                brightcoveVideoView.getClosedCaptioningController().getLoadCaptionsService().loadCaptions(pair.first, pair.second.type());
            }
            Map<String, Object> properties = new HashMap<>();
            properties.put(Event.CAPTION_FORMAT, pair.second);
            properties.put(Event.CAPTION_URI, pair.first);
            brightcoveVideoView.getEventEmitter().emit(EventType.SELECT_CLOSED_CAPTION_TRACK, properties);
        }
    }



    private Pair<Uri, BrightcoveCaptionFormat> getCaptionsForLanguageCode(Video video, String languageCode) {
        Object payload = video == null ? null : video.getProperties().get(Video.Fields.CAPTION_SOURCES);

        if (payload instanceof List) {
            @SuppressWarnings("unchecked")
            List<Pair<Uri, BrightcoveCaptionFormat>> pairs =
                    (List<Pair<Uri, BrightcoveCaptionFormat>>) payload;

            for (Pair<Uri, BrightcoveCaptionFormat> pair : pairs) {
                if (pair.second.language().equals(languageCode)) {
                    return pair;
                }
            }
        }
        return null;
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
