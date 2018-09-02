package andbakingapp.udacity.com.andbakingapp;


import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
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

    private TextView mStepInstructionView;
    private Button mNextButton;
    private Button mPreviousButton;
    private SimpleExoPlayer mExoPlayer;
    private SimpleExoPlayerView mPlayerView;
    private static MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private NotificationManager mNotificationManager;

    private String mRecipeStepDetail;
    private int mStepIdx;
    private JSONArray mRecipeStepsJson;
    private String mStepDescription;

    OnButtonClickListener mCallback;

    // OnButtonClickListener interface calls a method in the host activity
    public interface OnButtonClickListener {
        void onButtonClicked(int id, int index);
    }

    public RecipeStepInstructionFragment() {
        // Required empty public constructor
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
        
        // Initialize the player view, textview, and buttons
        mPlayerView = (SimpleExoPlayerView) rootView.findViewById(R.id.player_view);
        mStepInstructionView = (TextView) rootView.findViewById(R.id.tv_recipe_step_instruction);
        mNextButton = (Button) rootView.findViewById(R.id.btn_next);
        mPreviousButton = (Button) rootView.findViewById(R.id.btn_previous);

        try {
            JSONObject currentStep = mRecipeStepsJson.getJSONObject(mStepIdx);

            // Initialize the Media Session
            initializeMediaSession();

            // Initialize the player
            initializePlayer(currentStep.getString("videoURL"));

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
        Log.d(TAG, "initializePlayer: " + url);
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
            Log.d(TAG, "initializePlayer: " + url.isEmpty());

            MediaSource mediaSource = new ExtractorMediaSource(
                                        Uri.parse(url),
                                        new DefaultDataSourceFactory(getContext(), userAgent),
                                        new DefaultExtractorsFactory(),
                                        null,
                                        null);
            mExoPlayer.prepare(mediaSource);
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    // Release ExoPlayer
    private void releasePlayer() {
        mExoPlayer.stop();
        mExoPlayer.release();
        mExoPlayer = null;
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
}
