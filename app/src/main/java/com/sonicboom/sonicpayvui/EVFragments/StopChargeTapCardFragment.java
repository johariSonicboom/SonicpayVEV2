package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.MainActivity;
import com.sonicboom.sonicpayvui.R;

import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StopChargeTapCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StopChargeTapCardFragment extends Fragment {

    MainActivity mainActivity;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mStartChargeTime;

    public StopChargeTapCardFragment() {
        // Required empty public constructor
    }

    public StopChargeTapCardFragment(MainActivity a) {
        this.mainActivity = a;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StopChargeTapCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StopChargeTapCardFragment newInstance(String param1, String param2) {
        StopChargeTapCardFragment fragment = new StopChargeTapCardFragment();
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
            mStartChargeTime = getArguments().getString("startChargeTime");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GeneralVariable.CurrentFragment = "StopChargeTapCardFragment";
        // Inflate the layout for this fragment
        ((MainActivity)requireActivity()).UpdateTitleColor(R.color.main_blue);
        ((MainActivity)requireActivity()).ShowHideTitle(false);

        View view = inflater.inflate(R.layout.fragment_stop_charge_tap_card, container, false);
        TextView txtChargeTime = view.findViewById(R.id.txtChargeTime);
        if(mStartChargeTime != null){
            txtChargeTime.setText(calculateTotalChargeTime());
        } else{
            txtChargeTime.setText("TAP CARD TO STOP CHARGE");
        }

        return view; // Return the inflated view
    }

    // Method to calculate the total charge time
    private String calculateTotalChargeTime() {
        if (mainActivity != null && mainActivity.SelectedChargingStationComponent != null) {
            long diff = new Date().getTime() - mainActivity.SelectedChargingStationComponent.StartChargeTime.getTime();

            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long m = minutes % 60;
            String timeUse = String.format("Total Charging time %02d Hours %02d Minutes", hours, m);

            // Format the total charge time string
            return timeUse;
        } else {
            return ""; // Handle the case when mainActivity or selectedChargingStationComponent is null
        }
    }
}