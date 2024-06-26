package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.MainActivity;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.WelcomeFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ChargingFragment extends Fragment {

    private static final String ARG_PARAM1 = "StartChargeTime";
    private static final String ARG_PARAM2 = "param2";

    private String StartChargeTime = "";
    private String mParam2;
    private String HideStopButton = "false";

    private TextView txtChargeingTime;

    private Timer t;
    private Timer timer;

    public ChargingFragment() {
        // Required empty public constructor
    }

    public static ChargingFragment newInstance(String param1, String param2) {
        ChargingFragment fragment = new ChargingFragment();
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
            StartChargeTime = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            HideStopButton = getArguments().getString("HideStopButton");
        }
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        GeneralVariable.CurrentFragment = "ChargingFragment";
//
//        View view = inflater.inflate(R.layout.fragment_charging, container, false);
//        txtChargeingTime = view.findViewById(R.id.chargeTime_text);
//
//        Button stopChargeButton = view.findViewById(R.id.btnStopCharge);
//        if ("true".equals(HideStopButton)) {
//            stopChargeButton.setVisibility(View.GONE);
//        } else {
//            stopChargeButton.setVisibility(View.VISIBLE);
//        }
//
//        try {
//            ((MainActivity) requireActivity()).UpdateTitleColor(R.color.main_blue);
//            ((MainActivity) requireActivity()).ShowHideTitle(true);
//            ((MainActivity) requireActivity()).UpdateTitle("Charging");
//
//            // Initialize and schedule the timer
//            t = new Timer();
//            t.schedule(new TimerTask() {
//                @Override
//                public void run() {
//                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//                    try {
//                        Date date = format.parse(StartChargeTime);
//                        long diff = new Date().getTime() - date.getTime();
//                        long seconds = diff / 1000;
//                        long minutes = seconds / 60;
//                        long hours = minutes / 60;
//                        long days = hours / 24;
//                        long m = minutes % 60;
//                        // Ensure we are updating the UI on the main thread
//                        new Handler(Looper.getMainLooper()).post(() -> {
//                            if (isAdded()) {
//                                txtChargeingTime.setText(String.format("Charging Time %02d:%02d", hours, m));
//                            }
//                        });
//                    } catch (ParseException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }, 1000, 1000);
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            stopTimerForRedirection();
//        }
//
//        return view;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GeneralVariable.CurrentFragment = "ChargingFragment";

        LogUtils.i("Current Fragment", "ChargingFragment");

        View view = inflater.inflate(R.layout.fragment_charging, container, false);
        txtChargeingTime = view.findViewById(R.id.chargeTime_text);

        Button stopChargeButton = view.findViewById(R.id.btnStopCharge);
        if ("true".equals(HideStopButton)) {
            stopChargeButton.setVisibility(View.GONE);
        } else {
            stopChargeButton.setVisibility(View.VISIBLE);
        }

        try {
            ((MainActivity) requireActivity()).UpdateTitleColor(R.color.main_blue);
            ((MainActivity) requireActivity()).ShowHideTitle(true);
            ((MainActivity) requireActivity()).UpdateTitle("Charging");

            // Check if StartChargeTime is valid
            if (StartChargeTime != null && !StartChargeTime.isEmpty()) {
                // Initialize and schedule the timer
                t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
                        try {
                            Date date = format.parse(StartChargeTime);
                            long diff = new Date().getTime() - date.getTime();
                            long seconds = diff / 1000;
                            long minutes = seconds / 60;
                            long hours = minutes / 60;
                            long m = minutes % 60;
                            // Ensure we are updating the UI on the main thread
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (isAdded()) {
                                    txtChargeingTime.setText(String.format("Charging Time %02d:%02d", hours, m));
                                }
                            });
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }, 1000, 1000);
            } else {
                Log.e(TAG, "StartChargeTime is null or empty. Cannot start timer.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            stopTimerForRedirection();
        }

        return view;
    }


    private static final String TAG = "Charging";

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        // Guard against null reference
        if (t != null) {
            t.cancel();
            t = null;
        }
        stopTimerForRedirection();
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button stopChargeButton = view.findViewById(R.id.btnStopCharge);
//        LogUtils.i("isOneConnector in Charging Fragment", ((MainActivity) requireActivity()).isOneConnector);
        LogUtils.i("EnableTapCardStopCharge", new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.EnableTapCardStopCharge)));
//            if(new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.EnableTapCardStopCharge))){
//                stopChargeButton.setVisibility(View.VISIBLE);
//            } else {
//                stopChargeButton.setVisibility(View.INVISIBLE);
//            }

        if (!((MainActivity) requireActivity()).isOneConnector) {
            startTimerForRedirection();
            LogUtils.i("Charging Fragment", "Timer is activated");
        } else {
            if (new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.EnableTapCardStopCharge))) {
                stopChargeButton.setVisibility(View.VISIBLE);
            } else {
                stopChargeButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    // Call this method to start the timer
    public void startTimerForRedirection() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Redirect to another fragment here
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        redirectToAnotherFragment();
                    }
                });
            }
        }, 6000); // 6000 milliseconds = 6 seconds
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
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                .addToBackStack(null)
                .commit();
    }
}
