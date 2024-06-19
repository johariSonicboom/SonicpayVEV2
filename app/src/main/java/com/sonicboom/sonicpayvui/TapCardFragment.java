package com.sonicboom.sonicpayvui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.sbs.aidl.Class.QRResponse;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.models.eQRType;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.ScannerUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.io.IOException;

import pl.droidsonroids.gif.GifImageView;

public class TapCardFragment extends Fragment {

    private static final String TAG = "TapCard";
    private static final String Amount = "Amount";
    private static final String SalesRequest = "SalesRequest";
    private static final String TapCardMsg = "TapCardMsg";

    private String mAmount;
    public String mQRContentType;
    private String mSalesRequest;
    private String mTapCardMsg;
    private String mSelectedChargingStation;

    public TapCardFragment() {
        // Required empty public constructor
    }

    public static TapCardFragment newInstance(String param1, String param2, String param3) {
        TapCardFragment fragment = new TapCardFragment();
        Bundle args = new Bundle();
        args.putString(Amount, param1);
        args.putString(SalesRequest, param2);
        args.putString(TapCardMsg, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAmount = getArguments().getString(Amount);
            mSalesRequest = getArguments().getString(SalesRequest);
            mTapCardMsg = getArguments().getString(TapCardMsg);
//            mSelectedChargingStation = getArguments().getString("chargingStation");
            mSelectedChargingStation =  ((MainActivity)requireActivity()).SelectedChargingStationComponent.ComponentCode;
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        GeneralVariable.CurrentFragment = "TapCardFragment";

        // Inflate the layout for this fragment
        ((MainActivity)requireActivity()).UpdateTitleColor(R.color.main_blue);
        ((MainActivity)requireActivity()).ShowHideTitle(false);
        View view = inflater.inflate(R.layout.fragment_tap_card, container, false);

        TextView qrTitle = view.findViewById(R.id.qrTitle);
        TextView moreQR = view.findViewById(R.id.moreQROptions);
        //Button btnQRPayment = view.findViewById(R.id.btnQRPayment);
        GifImageView imageView = view.findViewById(R.id.tapCard);
        TextView amount = view.findViewById(R.id.tapCard_totalAmount);

        TextView chargingStation = view.findViewById(R.id.selectedChargingStation);
        chargingStation.setText("Charger: " + mSelectedChargingStation);

        if(mAmount != null)
            amount.setText(mAmount);
        else {
            LinearLayout amountLayout = view.findViewById(R.id.tapcard_amountLayout);
            amountLayout.setVisibility(View.GONE);
//            amount.setVisibility(View.INVISIBLE);
//            TextView amountCurrency = view.findViewById(R.id.tapCard_totalAmountCurrency);
//            amountCurrency.setVisibility(View.INVISIBLE);
        }

        boolean IsQRMerchantScanEnabled = false;
        boolean IsQRUserScanEnabled = false;

        try {
            IsQRMerchantScanEnabled = ((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsQRMerchantScanEnabled));
            IsQRUserScanEnabled = ((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(getString(R.string.IsQRUserScanEnabled));
        } catch (RemoteException e) {
            LogUtils.e(TAG, "onCreateView Exception: " + Log.getStackTraceString(e));
        }

        ((MainActivity) requireActivity()).UpdateTitle("Tap to pay");

        if((new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.enable_auto_request_qr)) && IsQRUserScanEnabled) || IsQRMerchantScanEnabled) {
            ((MainActivity) requireActivity()).ShowFooter(false);
            if(IsQRMerchantScanEnabled){
                LinearLayout qrUserScan = view.findViewById(R.id.qrUserScan);
                qrUserScan.setVisibility(View.GONE);
                LinearLayout qrMerchantScan = view.findViewById(R.id.qrMerchantScan);
                qrMerchantScan.setVisibility(View.VISIBLE);
                SurfaceView surfaceView = view.findViewById(R.id.camera_stream);
                try {
                    ((MainActivity)requireActivity()).scannerUtils = new ScannerUtils(requireActivity(), surfaceView, ((MainActivity)requireActivity()).handler);
                    ((MainActivity)requireActivity()).scannerUtils.StartScan((int) (Double.parseDouble(mAmount) * 100));
                } catch (IOException e) {
                    LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
                }
            }
        }
        else{
            ((MainActivity) requireActivity()).ShowFooter(true);
        }

        moreQR.setOnClickListener(view1 -> {
            Bundle bundle = new Bundle();
            QRResponse qrResponse = new Gson().fromJson(((MainActivity) requireActivity()).mStr, QRResponse.class);
            LogUtils.i("QR", qrResponse);
            bundle.putString("QRList", new Gson().toJson(qrResponse.qrList));
            bundle.putString("SalesRequest", mSalesRequest);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, QrSelectionFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        });

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsMCCSEnabled))){
            LinearLayout myDebitImg = view.findViewById(R.id.mydebit_layout);
            myDebitImg.setVisibility(View.GONE);
        }

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsVisaEnabled))){
            LinearLayout visaLayout = view.findViewById(R.id.visa_layout);
            visaLayout.setVisibility(View.GONE);
        }

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsMasterEnabled))){
            LinearLayout masterLayout = view.findViewById(R.id.master_layout);
            masterLayout.setVisibility(View.GONE);
        }

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsAmexEnabled))){
            LinearLayout amexLayout = view.findViewById(R.id.amex_layout);
            amexLayout.setVisibility(View.GONE);
        }

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsUPIEEnabled))){
            LinearLayout unionpayLayout = view.findViewById(R.id.unionpay_layout);
            unionpayLayout.setVisibility(View.GONE);
        }

        if(!new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(getString(R.string.IsTngEnabled))){
            LinearLayout tngLayout = view.findViewById(R.id.tng_layout);
            tngLayout.setVisibility(View.GONE);
        }

        if(mTapCardMsg != null && !mTapCardMsg.isEmpty()){
            TextView tapHereMsg = view.findViewById(R.id.tapHereMsg);
            tapHereMsg.setText(mTapCardMsg);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");
        try {
            ((MainActivity)requireActivity()).ShowHideTitle(false);
            Fragment currentFragment = requireActivity().getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            assert currentFragment != null;
            View view = currentFragment.getView();
            assert view != null;
            if(((MainActivity) requireActivity()).mStr != null) {
                GifImageView imageView = view.findViewById(R.id.tapCard);

                QRResponse qrResponse = new Gson().fromJson(((MainActivity) requireActivity()).mStr, QRResponse.class);
                if(!qrResponse.QRCode.isEmpty()) {
                    TextView gifTitle = view.findViewById(R.id.gifTitle);
                    gifTitle.setText("Scan to pay");
                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }

                TextView qrTitle = view.findViewById(R.id.qrTitle);
                qrTitle.setVisibility(View.VISIBLE);
                qrTitle.setText(qrResponse.QRName);

                if (qrResponse.QRCode.startsWith("http")) {
                    boolean isImage = mQRContentType.equals("image");
                    if (isImage)
                        new Utils.DownloadImageTask(imageView).execute(qrResponse.QRCode);
                    else
                        imageView.setImageBitmap(Utils.generateQRfromStr(requireActivity(), qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue()));
                } else {
                    if(!qrResponse.QRCode.equals(""))
                        imageView.setImageBitmap(Utils.generateQRfromStr(requireActivity(), qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue()));
                }
                if(qrResponse.qrList.length > 0){
                    TextView moreQR = view.findViewById(R.id.moreQROptions);
                    moreQR.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            LogUtils.e(TAG, "onResume Exception: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        ((MainActivity)requireActivity()).ShowHideTitle(true);
    }
}