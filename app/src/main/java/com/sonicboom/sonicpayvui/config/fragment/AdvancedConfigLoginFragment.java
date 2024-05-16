package com.sonicboom.sonicpayvui.config.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.activity.ConfigActivity;
import com.sonicboom.sonicpayvui.utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdvancedConfigLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdvancedConfigLoginFragment extends Fragment {

    private static final String TAG = "AdvancedConfigLogin";

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "Path";
    private static final String ARG_PARAM2 = "param2";

    private String path;
    private String mParam2;

    public AdvancedConfigLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdvancedConfigLoginFragment.
     */
    public static AdvancedConfigLoginFragment newInstance(String param1, String param2) {
        AdvancedConfigLoginFragment fragment = new AdvancedConfigLoginFragment();
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
            path = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_advanced_config_login, container, false);

        Button btnConfirm = view.findViewById(R.id.advanced_config_btnConfirm);
        btnConfirm.setOnClickListener(view1 -> onConfirm(view));

        EditText password= view.findViewById(R.id.advanced_config_password);
        password.requestFocus();

        password.setOnFocusChangeListener((v, hasFocus) -> {
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (hasFocus)
                imm.showSoftInput(password, InputMethodManager.SHOW_IMPLICIT);
            else
                imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
        });
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        ((ConfigActivity)requireActivity()).UpdateTitle("Advanced Operation");
    }

    public void onConfirm(View view) {
        LogUtils.d(TAG, "onConfirm started...");

        EditText password = view.findViewById(R.id.advanced_config_password);

        InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(password.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        if(password.getText().length() == 0) {
            password.setError("Required");
            return;
        }

        if(password.getText().toString().equals("260972")){
            LogUtils.i(TAG, "onConfirm: Password matched");

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.config_container, new SPServiceConfigFragment.AdvancedOperationFragment())
                    .addToBackStack(null)
                    .commit();
        }
        else{
            Toast.makeText(getContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
        }
        password.getText().clear();
        LogUtils.d(TAG, "onConfirm ended.");
    }
}