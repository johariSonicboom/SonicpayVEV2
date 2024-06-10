package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.WelcomeFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PlugInToStartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlugInToStartFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    private Timer timer;
    private Timer timeoutTimer;

    private boolean StayOnFragment;

    public PlugInToStartFragment() {
        // Required empty public constructor
    }

    public static PlugInToStartFragment newInstance(String param1, String param2) {
        PlugInToStartFragment fragment = new PlugInToStartFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            StayOnFragment = getArguments().getBoolean("StayOnFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GeneralVariable.CurrentFragment = "PlugInToStartFragment";
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_plug_in_to_start, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
//        Log.i("onViewCreated", "onViewCreated");
        startTimerForTimeout();
        if (StayOnFragment){
//            LogUtils.i("Stay on Plugin Fragment");
        }else{
            startTimerForRedirection();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopTimerForRedirection();
        stopTimerForTimeout();
    }

    // Call this method to start the timer
    public void startTimerForRedirection() {
        timer = new Timer();
        Log.i("Timer Start", "Plugin Fragment Redirect");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Redirect to another fragment here
                redirectToAnotherFragment();
            }
        }, 4000); // 3000 milliseconds = 3 seconds
    }

    public void startTimerForTimeout() {
        timeoutTimer = new Timer();
        Log.i("Timer Start", "Plugin Fragment Redirect");
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Redirect to another fragment here
                redirectToAnotherFragment();
            }
        }, 60000); //
    }

    // Call this method to stop the timer
    public void stopTimerForTimeout() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }

    // Call this method to stop the timer
    public void stopTimerForRedirection() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    // Method to redirect to another fragment
    private void redirectToAnotherFragment() {
        // Perform the fragment redirection here
        if (isAdded() && getActivity() != null) {
            Bundle bundle = new Bundle();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, WelcomeFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
