package com.sonicboom.sonicpayvui.config.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.RemoteException;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KeyLoadingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyLoadingFragment extends Fragment {

   private static final String TAG = "KeyLoading";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public KeyLoadingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KeyLoadingFragment.
     */
    public static KeyLoadingFragment newInstance(String param1, String param2) {
        KeyLoadingFragment fragment = new KeyLoadingFragment();
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
        ((ConfigActivity)requireActivity()).UpdateTitle("Key Loading");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_key_loading, container, false);
        try {
            TextInputEditText hostIP = view.findViewById(R.id.text_kl_host_ip);
            hostIP.setText(((ConfigActivity)requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.MasterTerminalIP)));
            TextInputEditText hostPort = view.findViewById(R.id.text_kl_host_port);
            hostPort.setText(((ConfigActivity)requireActivity()).sonicInterface.ReadSharedPref(getString(R.string.MasterTerminalPort)));

            Button btnDownload = view.findViewById(R.id.button_key_download);
            btnDownload.setOnClickListener(view1 -> {
                try {
                    btnDownload.setClickable(false);
                    if (hostIP.getText() != null && !hostIP.getText().toString().equals("") && hostPort.getText() != null && !hostPort.getText().toString().equals("")) {
                        String message = ((ConfigActivity) requireActivity()).sonicInterface.PBBKeyLoading(hostIP.getText().toString(), Integer.parseInt(hostPort.getText().toString()));
                        LogUtils.i(TAG, "PBBKeyLoading: " + message);
                        new AlertDialog.Builder(requireActivity())
                                .setTitle("Result")
                                .setMessage(message)
                                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                .create()
                                .show();
                    }
                    btnDownload.setClickable(true);
                }
                catch(Exception e){
                    LogUtils.e(TAG, "onClick Exception: " + Log.getStackTraceString(e));
                }
            });
        } catch (Exception e) {
            LogUtils.e(TAG, "onCreateView Exception: " + Log.getStackTraceString(e));
        }
        return view;
    }
}