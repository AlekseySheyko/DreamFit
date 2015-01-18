package ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

import java.util.Timer;
import java.util.TimerTask;

import bellamica.tech.dreamteenfitness.R;
import helpers.Constants;
import ui.activities.SummaryActivity;

public class MapPane extends Fragment
        implements OnClickListener {

    // Callback to update session in RunActivity
    OnWorkoutStateChanged mCallback;

    // Time counter
    private TimerTask timerTask;
    private int elapsedSeconds = 0;

    // Control buttons
    private Button mStartButton;
    private Button mPauseButton;
    private Button mFinishButton;

    // User's settings
    private SharedPreferences sharedPrefs;
    private TextView mDurationCounter;
    private TextView mDistanceCounter;
    private TextView mDistanceUnitsLabel;

    private TextView mStartButtonLabel;
    private TextView mPauseButtonLabel;
    private TextView mFinishButtonLabel;

    private static final int WORKOUT_START = 1;
    private static final int WORKOUT_PAUSE = 2;
    private static final int WORKOUT_FINISH = 3;

    public interface OnWorkoutStateChanged {
        public void onWorkoutStateChanged(int state);
    }

    // Required empty constructor
    public MapPane() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Session state change listener
        mCallback = (OnWorkoutStateChanged) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        disableMapUiControls(
                getFragmentManager().findFragmentById(R.id.map));

        mStartButton.setOnClickListener(this);
    }

    private void initializeViews(View rootView) {
        mStartButton = (Button) rootView.findViewById(R.id.startButton);
        mPauseButton = (Button) rootView.findViewById(R.id.pauseButton);
        mFinishButton = (Button) rootView.findViewById(R.id.finishButton);

        mStartButtonLabel = (TextView) rootView.findViewById(R.id.start_button_label);
        mPauseButtonLabel = (TextView) rootView.findViewById(R.id.pause_button_label);
        mFinishButtonLabel = (TextView) rootView.findViewById(R.id.finish_button_label);

        mDurationCounter = (TextView) rootView.findViewById(R.id.duration_counter);
        mDistanceCounter = (TextView) rootView.findViewById(R.id.distance_counter);
        mDistanceUnitsLabel = (TextView) rootView.findViewById(R.id.distanceUnitsLabel);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startButton:
                mCallback.onWorkoutStateChanged(WORKOUT_START);

                updateUiOnStart();

                startUiStopwatch();

                break;
            case R.id.pauseButton:
                mCallback.onWorkoutStateChanged(WORKOUT_PAUSE);

                updateUiOnPause();

                stopUiStopwatch(Constants.Timer.JUST_PAUSE);

                break;
            case R.id.finishButton:
                mCallback.onWorkoutStateChanged(WORKOUT_FINISH);

                // TODO:
                // 1. Save duration
                // 2. Save time stamp
                startActivity(new Intent(this.getActivity(), SummaryActivity.class));
                break;
        }
    }

    private void updateUiOnStart() {
        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar().
                    setTitle(getString(R.string.running_label));

        mStartButton.setVisibility(View.GONE);
        mStartButtonLabel.setVisibility(View.GONE);

        mPauseButton.setOnClickListener(this);
        mPauseButton.setVisibility(View.VISIBLE);
        mPauseButtonLabel.setVisibility(View.VISIBLE);

        mFinishButton.setOnClickListener(this);
        mFinishButton.setVisibility(View.GONE);
        mFinishButtonLabel.setVisibility(View.GONE);

        // Set distance units
        if (sharedPrefs.getString("pref_units", "1").equals("1"))
            mDistanceUnitsLabel.setText("miles");
    }

    private void updateUiOnPause() {
        if (getActivity() != null && getActivity().getActionBar() != null)
            getActivity().getActionBar().
                    setTitle(getString(R.string.pause_label));

        mPauseButton.setVisibility(View.GONE);
        mPauseButtonLabel.setVisibility(View.GONE);

        mStartButton.setVisibility(View.VISIBLE);
        mStartButtonLabel.setVisibility(View.VISIBLE);
        mStartButtonLabel.setText(R.string.resume_run_button_label);

        mFinishButton.setVisibility(View.VISIBLE);
        mFinishButtonLabel.setVisibility(View.VISIBLE);
    }

    // Stopwatch to show duration
    public void startUiStopwatch() {
        final Handler handler = new Handler();
        Timer mTimer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        mDurationCounter.setText(
                                convertSecondsToHMmSs(elapsedSeconds));
                        elapsedSeconds++;
                    }
                });
            }
        };
        mTimer.schedule(timerTask, 0, 1000);
    }

    private String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public void stopUiStopwatch(int pauseOrStop) {
        timerTask.cancel();
        timerTask = null;

        if (pauseOrStop == Constants.Timer.STOP)
            elapsedSeconds = 0;
    }

    private void disableMapUiControls(Fragment fragment) {
        GoogleMap map = ((MapFragment) fragment).getMap();
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
    }
}

