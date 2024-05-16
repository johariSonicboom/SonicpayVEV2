package com.sonicboom.sonicpayvui;

import android.icu.text.CaseMap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.sbs.aidl.Class.QRResponse;
import com.sonicboom.sonicpayvui.models.eQRType;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import pl.droidsonroids.gif.GifImageView;

public class ProgressFragment extends Fragment {

    private static final String TitleText = "TitleText";
    private static final String StatusText = "StatusText";
    private static final String TotalAmount = "TotalAmount";

    private String mTitleText;
    private String mStatusText;
    private String mTotalAmount;

    public ProgressFragment() {
        // Required empty public constructor
    }

    public static ProgressFragment newInstance(String param1, String param2, String param3) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putString(StatusText, param1);
        args.putString(TotalAmount, param2);
        args.putString(TitleText, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStatusText = getArguments().getString(StatusText);
            mTotalAmount = getArguments().getString(TotalAmount);
            mTitleText = getArguments().getString(TitleText);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((MainActivity)requireActivity()).UpdateTitleColor(R.color.main_blue);
        if (mTitleText != null && !mTitleText.equalsIgnoreCase(""))
            ((MainActivity)requireActivity()).UpdateTitle(mTitleText, 33);
        else
            ((MainActivity)requireActivity()).UpdateTitle("Please wait");
        requireActivity().findViewById(R.id.footer).setVisibility(View.VISIBLE);

        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        TextView statusText = view.findViewById(R.id.status_text);
        statusText.setText(mStatusText);
        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
        // reset to default text size
        ((MainActivity)requireActivity()).UpdateTitle(null,40);
    }
}