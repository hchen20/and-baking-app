package andbakingapp.udacity.com.andbakingapp;


import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecipeStepInstructionFragment extends Fragment
                implements View.OnClickListener, ExoPlayer.EventListener {
    private static final String TAG = RecipeStepInstructionFragment.class.getSimpleName();

    private static String RECIPE_DETAIL = "details";
    private static String TWO_PANE = "twopane";
    private static String PLAYER_POSITION = "position";
    private static String PLAYER_STATE = "state";
    private static String VIDEO_URL = "video";



    private TextView mStepInstructionView;
    private Button mNextButton;
    private Button mPreviousButton;
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    private String mRecipeStepDetail;
    private int mStepIdx;
    private long mPlayerPosition;
    private boolean mPlayState;

    private JSONArray mRecipeStepsJson;
    private String mVideoUrl;
    private boolean mTwoPane;

    OnButtonClickListener mCallback;

    // OnButtonClickListener interface calls a method in the host activity
    public interface OnButtonClickListener {
        void onButtonClicked(int id, int index);
    }

    public RecipeStepInstructionFragment() {
        // Required empty public constructor
    }



    // Removing buttons for two-pane view
    public void setTwoPane() {
        mTwoPane = true;
    }

    // Override onAttach to make sure that the container host activity has implemented the callback
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnButtonClickListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_recipe_step_instruction, container, false);

        Log.d(TAG, "onCreateView: Step instruction creation");

        if (savedInstanceState != null) {
            mRecipeStepDetail = savedInstanceState.getString(RECIPE_DETAIL);
            mPlayerPosition = savedInstanceState.getLong(PLAYER_POSITION);
            mPlayState = savedInstanceState.getBoolean(PLAYER_STATE);
            mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
            mVideoUrl = savedInstanceState.getString(VIDEO_URL);
        } else {
            mPlayState = true;
            mPlayerPosition = 0;
        }

        // Initialize the player view, textview, and buttons
        mPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.player_view);
        mStepInstructionView = (TextView) rootView.findViewById(R.id.tv_recipe_step_instruction);
        mNextButton = (Button) rootView.findViewById(R.id.btn_next);
        mPreviousButton = (Button) rootView.findViewById(R.id.btn_previous);

        if (mTwoPane) {
            mNextButton.setVisibility(View.GONE);
            mPreviousButton.setVisibility(View.GONE);
        }

        try {
            JSONObject currentStep = mRecipeStepsJson.getJSONObject(mStepIdx);

            // Initialize the video url
            mVideoUrl = currentStep.getString("videoURL");

            // Initialize the Media Session
            initializeMediaSession();

            // Initialize the player
            initializePlayer(mVideoUrl);

            mStepInstructionView.setText(currentStep.getString("description"));
        } catch (Exception e) {
            Log.d(TAG, "onCreateView: " + e.getMessage());
        }

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStepIdx >= mRecipeStepsJson.length()-1) {
                    Toast.makeText(getActivity(), "This is the last step", Toast.LENGTH_LONG).show();
                    return;
                }
                mCallback.onButtonClicked(view.getId(), mStepIdx);
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStepIdx == 0) {
                    Toast.makeText(getActivity(), "This is the first step", Toast.LENGTH_LONG).show();
                    return;
                }
                mCallback.onButtonClicked(view.getId(), mStepIdx);
            }
        });


        return rootView;
    }

    public void setRecipeStepDetail(String detail) {
        this.mRecipeStepDetail = detail;

        try {
            JSONObject theRecipe = new JSONObject(mRecipeStepDetail);
            mRecipeStepsJson = theRecipe.getJSONArray("steps");
        } catch (Exception e) {
            Log.d(TAG, "setRecipeStepDetail: " + e.getMessage());
        }
    }

    public void setStepIdx(int idx) {
        this.mStepIdx = idx;
    }

    private void initializeMediaSession() {
        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(getContext(), TAG);

        // Enable callbacks from the TransportControls
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Do not let MediaButtons restart the player when the app is not visible
        mMediaSession.setMediaButtonReceiver(null);

        // Set an initial PlaybackState with ACTION_PLAY so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                            .setActions(PlaybackStateCompat.ACTION_PLAY |
                                        PlaybackStateCompat.ACTION_PAUSE |
                                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                        PlaybackStateCompat.ACTION_PLAY_PAUSE);

        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback has method that handles callbacks from a media control
        mMediaSession.setCallback(new MySessionCallback());

        // Start the Media Session since the fragment is active
        mMediaSession.setActive(true);
    }

    // Initialize ExoPlayer
    private void initializePlayer(String url) {
        // Check if the player is null
        Log.d(TAG, "initializePlayer: start initializing player");
        if (mExoPlayer == null) {
            // Create a instance of the ExoPlayer
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            mExoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector, loadControl);
            mPlayerView.setPlayer(mExoPlayer);

            // Set the ExoPlayer.EventListener to this fragment
            mExoPlayer.addListener(this);

            // Prepare the MediaSource
            String userAgent = Util.getUserAgent(getContext(), "andbakingapp");
            Log.d(TAG, "initializePlayer: userAgent null? " + (url == null));

            MediaSource mediaSource = new ExtractorMediaSource(
                                        Uri.parse(url),
                                        new DefaultDataSourceFactory(getContext(), userAgent),
                                        new DefaultExtractorsFactory(),
                                        null,
                                        null);

            mExoPlayer.prepare(mediaSource);
            mExoPlayer.seekTo(mPlayerPosition);
            mExoPlayer.setPlayWhenReady(mPlayState);
        }
    }

    // Release ExoPlayer
    private void releasePlayer() {
        // Check if the ExoPlayer is null or not
        if (mExoPlayer != null) {
            mExoPlayer.stop();
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }

    // Release the player when the activity is destroyed
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releasePlayer();
        mMediaSession.setActive(false);
    }

    @Override
    public void onClick(View view) {
        int buttonClicked = view.getId();

        mCallback.onButtonClicked(buttonClicked, mStepIdx);
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if ((playbackState == ExoPlayer.STATE_READY) && playWhenReady) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                    mExoPlayer.getCurrentPosition(), 1f);
        } else if (playbackState == ExoPlayer.STATE_READY) {
            mStateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                    mExoPlayer.getCurrentPosition(), 1f);
        }
        mMediaSession.setPlaybackState(mStateBuilder.build());
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity() {

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(RECIPE_DETAIL, mRecipeStepDetail);
        if (mExoPlayer != null) {
            Log.d(TAG, "onSaveInstanceState: the player is not null");
            outState.putLong(PLAYER_POSITION, mExoPlayer.getCurrentPosition());
            outState.putBoolean(PLAYER_STATE, mExoPlayer.getPlayWhenReady());
        }
        outState.putBoolean(TWO_PANE, mTwoPane);
        outState.putString(VIDEO_URL, mVideoUrl);
    }

//    @Override
//    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
//        super.onViewStateRestored(savedInstanceState);
//        mRecipeStepDetail = savedInstanceState.getString(RECIPE_DETAIL);
//        mPlayerPosition = savedInstanceState.getLong(PLAYER_POSITION);
//        mPlayState = savedInstanceState.getBoolean(PLAYER_STATE);
//        mTwoPane = savedInstanceState.getBoolean(TWO_PANE);
//    }

    // Media Session callbacks for all external clients
    private class MySessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            mExoPlayer.setPlayWhenReady(true);
        }

        @Override
        public void onPause() {
            mExoPlayer.setPlayWhenReady(false);
        }

        @Override
        public void onSkipToPrevious() {
            mExoPlayer.seekTo(0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer(mVideoUrl);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Util.SDK_INT <= 23 || mExoPlayer == null) {
            initializePlayer(mVideoUrl);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }
}
