package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChargingRateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChargingRateFragment extends Fragment {

    public IAIDLSonicpayInterface sonicInterface;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String Amount = "Amount";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String mSelectedChargingStation;
    private String mAmount;
    private String mFareChargeText;
    private String mFareChargeDescription;


    public ChargingRateFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChargingRateFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChargingRateFragment newInstance(String param1, String param2) {
        ChargingRateFragment fragment = new ChargingRateFragment();
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
            mAmount = getArguments().getString(Amount);
            mSelectedChargingStation = getArguments().getString("chargingStation");
            mFareChargeText = getArguments().getString("FareChargeText");
            mFareChargeDescription = getArguments().getString("FareChargeDescription");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GeneralVariable.CurrentFragment = "ChargingRateFragment";

//        try {
//            boolean r = sonicInterface.Abort();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_charging_rate, container, false);
        TextView FareChargeText = view.findViewById(R.id.chargingRate);
        TextView FareChargeDescription = view.findViewById(R.id.rateDescription);

        if (mFareChargeText != null ) {
            FareChargeText.setText(mFareChargeText);
            FareChargeDescription.setText(mFareChargeDescription);
        } else {
            FareChargeText.setText("No Fare Set");
            FareChargeDescription.setText("No Description");
        }
        return view;
    }
}