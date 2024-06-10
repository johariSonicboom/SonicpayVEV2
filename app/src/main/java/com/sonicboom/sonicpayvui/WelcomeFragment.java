package com.sonicboom.sonicpayvui;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgabanner.BGALocalImageSize;

public class WelcomeFragment extends Fragment {

    private static final String TAG = "Welcome";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public WelcomeFragment() {
        // Required empty public constructor
    }

    public static WelcomeFragment newInstance(String param1, String param2) {
        WelcomeFragment fragment = new WelcomeFragment();
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
        GeneralVariable.CurrentFragment = "WelcomeFragment";

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_welcome, container, false);
        try {
            StartBanner(view);
            ((MainActivity)requireActivity()).UpdateTitle(getString(R.string.welcome));
            ((MainActivity) requireActivity()).UpdateTitleColor(R.color.main_blue);
            ((MainActivity)requireActivity()).ShowHideTitle(true);
            requireActivity().findViewById(R.id.btnStartCharge).setVisibility(View.VISIBLE);
            requireActivity().findViewById(R.id.footer).setVisibility(View.VISIBLE);
        } catch (RemoteException e) {
            LogUtils.e(TAG, "onCreateView Exception: " + Log.getStackTraceString(e));
        }
        return view;
    }

    private void StartBanner(View view) throws RemoteException {
        BGABanner mContentBanner = view.findViewById(R.id.logo_banner);
        BGALocalImageSize localImageSize = new BGALocalImageSize(320, 640, 320, 640);

        List<Integer> myList = new ArrayList<>();
        myList.add(R.drawable.paywave);

try {
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsMCCSEnabled)))
        myList.add(R.drawable.mydebit_logo);
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsVisaEnabled)))
        myList.add(R.drawable.visa_logo);
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsMasterEnabled)))
        myList.add(R.drawable.mastercard_logo);
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsAmexEnabled)))
        myList.add(R.drawable.amex_crop_logo);
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsUPIEEnabled)))
        myList.add(R.drawable.unionpay_logo);
    if (((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsTngEnabled)))
        myList.add(R.drawable.tng_logo);
}catch (Exception e){
    LogUtils.i("Start Banner Exception", e);
        }

        int[] resIds = myList.stream().mapToInt(i->i).toArray();

        mContentBanner.setData(localImageSize, ImageView.ScaleType.FIT_CENTER, resIds);
        mContentBanner.setAutoPlayInterval(1500);
        mContentBanner.setAllowUserScrollable(false);
        mContentBanner.setIndicatorVisibility(false);

        mContentBanner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                TextView textView = view.findViewById(R.id.welcomeTitle);
                if(position == 0 && positionOffset == 0.0 || position == resIds.length - 1  && positionOffset >= 0.8)
                    textView.setText("TAP TO PAY");
                else
                    textView.setText("We Accept");
            }

            @Override
            public void onPageSelected(int position) {
//                requireActivity().getSupportFragmentManager().beginTransaction()
//                        .setReorderingAllowed(true)
//                        .replace(R.id.fragmentContainer, QrSelectionFragment.class, null)
//                        .addToBackStack(null)
//                        .commit();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

    }
}