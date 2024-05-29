package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.WelcomeFragment;

import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DisconnectChargerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisconnectChargerFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DisconnectChargerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DisconnectChargerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisconnectChargerFragment newInstance(String param1, String param2) {
        DisconnectChargerFragment fragment = new DisconnectChargerFragment();
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
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        startTimerForRedirection();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_disconnect_charger, container, false);

    }

    private Timer timer;

    // Call this method to start the timer
    public void startTimerForRedirection() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Redirect to another fragment here
                redirectToAnotherFragment();
            }
        }, 5000); // 3000 milliseconds = 3 seconds
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
        // For example:
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