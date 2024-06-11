package com.sonicboom.sonicpayvui;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.utils.LogUtils;

public class ResultFragment extends Fragment {

    private static final String TAG = "ResultFragment";
    private static final String Title = "Title";
    private static final String Message = "Message";
    private static final String IsSuccess = "IsSuccess";
    private static final String IsTng = "IsTng";
    private static final String Amount = "Amount";
    private static final String Balance = "Balance";
    private static final String StickPage = "StickPage";

    private boolean mStickPage;
    private String mTitle;
    private String mMsg;
    private boolean mIsSuccess;
    private boolean mIsTng;
    private String mAmount;
    private String mBalance;

    private Handler handler;
    private Runnable runnable;

    public ResultFragment() {
        // Required empty public constructor
    }

    public static ResultFragment newInstance(String param1, String param2, boolean param3, boolean param4, String param5, String param6) {
        ResultFragment fragment = new ResultFragment();
        Bundle args = new Bundle();
        args.putString(Title, param1);
        args.putString(Message, param2);
        args.putBoolean(IsSuccess, param3);
        args.putBoolean(IsTng, param4);
        args.putString(Amount, param5);
        args.putString(Balance, param6);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTitle = getArguments().getString(Title);
            mMsg = getArguments().getString(Message);
            mIsSuccess = getArguments().getBoolean(IsSuccess);
            mIsTng = getArguments().getBoolean(IsTng);
            mAmount = getArguments().getString(Amount);
            mBalance = getArguments().getString(Balance);
            mStickPage = getArguments().getBoolean(StickPage);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GeneralVariable.CurrentFragment = "ResultFragment";
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        ((MainActivity) requireActivity()).UpdateTitle(mTitle == null ? "Error" : mTitle);
        ((MainActivity)requireActivity()).ShowFooter(true);
        ((MainActivity) requireActivity()).btnStartCharge.setVisibility(View.GONE);

        TextView errMsg = view.findViewById(R.id.resultMessage);
        LinearLayout resultSummary = view.findViewById(R.id.resultSummary);
        ImageView resultIcon = view.findViewById(R.id.resultIcon);

        if(mIsSuccess) {
            ((MainActivity) requireActivity()).UpdateTitleColor(R.color.success_green);
            resultIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.check_leaf_shadow));

            if(mMsg != null){
                errMsg.setText(mMsg);
                errMsg.setTextColor(ContextCompat.getColor(requireActivity(), R.color.black));
            }
            else
                errMsg.setVisibility(View.GONE);

            if(mIsTng) {
                if(mAmount != null) {
                    TextView amount = view.findViewById(R.id.result_amount);
                    amount.setText(mAmount);
                }
                else {
                    LinearLayout amountLayout = view.findViewById(R.id.amount_layout);
                    amountLayout.setVisibility(View.GONE);
                }
                if(mBalance != null) {
                    TextView balance = view.findViewById(R.id.result_balance);
                    balance.setText(mBalance);
                }
                else{
                    LinearLayout balanceLayout = view.findViewById(R.id.balance_layout);
                    balanceLayout.setVisibility(View.GONE);
                }
                if(mAmount == null && mBalance == null){
                    resultSummary.setVisibility(View.INVISIBLE);
                }
            }
            else{
                LinearLayout balanceLayout = view.findViewById(R.id.balance_layout);
                balanceLayout.setVisibility(View.GONE);

                if(mAmount != null) {
                    TextView amount = view.findViewById(R.id.result_amount);
                    amount.setText(mAmount);
                    amount.setTextColor(Color.BLACK);

                    TextView amountCurrency = view.findViewById(R.id.result_amount_currency);
                    amountCurrency.setText("MYR");
                    amountCurrency.setTextColor(Color.BLACK);
                }
                else {
                    resultSummary.setVisibility(View.GONE);
                }
            }

        }
        else{
            ((MainActivity) requireActivity()).UpdateTitleColor(R.color.fail_red);
            errMsg.setText(mMsg == null ? "Something went wrong" : mMsg);
            resultSummary.setVisibility(View.GONE);
            resultIcon.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.cross_fat));
        }

        return view;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        ((MainActivity)requireActivity()).UpdateTitleColor(R.color.main_blue);
        if(handler != null  && runnable != null)
            handler.removeCallbacks(runnable);
        LogUtils.d(TAG, "onDestroy");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        LogUtils.d(TAG, "onViewCreated");

        if (mStickPage)
            return;

//        if(GeneralVariable.CurrentFragment.equals(""))
        if(!((MainActivity)requireActivity()).preAuthSuccess) {
            startAutoRedirectionToIdlePage();
        }else{
            ((MainActivity)requireActivity()).preAuthSuccess = false;
        }
    }

    public void startAutoRedirectionToIdlePage() {
        // Declare a Handler object
        handler = new Handler();

        // Define the delay duration in milliseconds
        int delayMillis = 3000;

        // Create a Runnable to be executed after the delay
        runnable = new Runnable() {
            @Override
            public void run() {
                LogUtils.d(TAG, "Timer triggered");
                handler.removeCallbacks(this);

                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

                fragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                        .addToBackStack(null)
                        .commit();

                if(fragmentManager.getBackStackEntryCount() > 1)
                    fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

            }
        };

        // Start the timer by posting the Runnable with the specified delay
        handler.postDelayed(runnable, delayMillis);
    }

    public void stopAutoRedirectionToIdlePage() {
        LogUtils.d(TAG, "stopAutoRedirectionToIdlePage");
        if(handler != null  && runnable != null)
            handler.removeCallbacks(runnable);
    }
}