package com.sonicboom.sonicpayvui.EVFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.MainActivity;
import com.sonicboom.sonicpayvui.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PhoneNumberFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PhoneNumberFragment extends Fragment {

    public IAIDLSonicpayInterface sonicInterface;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PhoneNumberFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PhoneNumberFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PhoneNumberFragment newInstance(String param1, String param2) {
        PhoneNumberFragment fragment = new PhoneNumberFragment();
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

        GeneralVariable.CurrentFragment = "PhoneNumberFragment";

//        try {
//            boolean r = sonicInterface.Abort();
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_phone_number, container, false);
        view.findViewById(R.id.btnPhoneNumber).setOnClickListener( ((MainActivity)requireActivity()));
        return view;
    }
}