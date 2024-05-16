package com.sonicboom.sonicpayvui;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.sbs.aidl.Class.QRList;
import com.sbs.aidl.Class.QRResponse;
import com.sonicboom.sonicpayvui.models.Sale;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.io.File;

public class QrSelectionFragment extends Fragment {

    private static final String TAG = "QRSelection";
    private static final String QRList = "QRList";
    private static final String SalesRequest = "SalesRequest";

    private String mQRListStr;
    private String mSalesRequest;
    private QRList[] mQRList;

    public QrSelectionFragment() {
        // Required empty public constructor
    }

    public static QrSelectionFragment newInstance(String param1, String param2) {
        QrSelectionFragment fragment = new QrSelectionFragment();
        Bundle args = new Bundle();
        args.putString(QRList, param1);
        args.putString(SalesRequest, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mQRListStr = getArguments().getString(QRList);
            mQRList = new Gson().fromJson(mQRListStr, QRList[].class);
            mSalesRequest = getArguments().getString(SalesRequest);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_qr_selection, container, false);
        ((MainActivity)requireActivity()).UpdateTitle("Select QR");
        requireActivity().findViewById(R.id.footer).setVisibility(View.GONE);

        view.findViewById(R.id.qrBack).setOnClickListener(view1 -> {
            requireActivity().onBackPressed();
        });

        if(mQRList != null) {
            int noOfItem = mQRList.length;
            LinearLayout qrList = view.findViewById(R.id.qrList);

            int noOfRows = noOfItem / 2 + noOfItem % 2;
            int itemIndex = 0;
            for (int i = 0; i < noOfRows; i++) {
                LinearLayout qrItem = new LinearLayout(requireActivity());
                qrItem.setLayoutParams(new LinearLayout.LayoutParams((i == noOfRows - 1 ? noOfItem % 2 == 0 ? LinearLayout.LayoutParams.MATCH_PARENT : 360 : LinearLayout.LayoutParams.MATCH_PARENT), 360));
                qrItem.setOrientation(LinearLayout.HORIZONTAL);

                for (int j = 0; j < (i == noOfRows - 1 ? noOfItem % 2 == 0 ? 2 : 1 : 2); j++) {
                    ImageButton qrImg = new ImageButton(requireActivity());
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.gravity = Gravity.CENTER;
                    layoutParams.weight = 0.5f;
                    qrImg.setLayoutParams(layoutParams);
                    qrImg.setScaleType(ImageView.ScaleType.FIT_CENTER);

                    //TODO: verify
                    String fileName = mQRList[itemIndex].QRProviderID + ".jpg";
                    //File file = new File(Environment.getExternalStorageDirectory().getPath() + "/QRImages/" + fileName);
                    //Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                    Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getPath() + "/QRImages/" + fileName);
                    qrImg.setImageBitmap(bitmap);
                    qrImg.setContentDescription(String.valueOf(mQRList[itemIndex].QRProviderID));
                    qrImg.setBackgroundTintList(ColorStateList.valueOf(requireActivity().getColor(R.color.white)));
                    qrImg.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LogUtils.d(TAG, "Selected QR: " + Integer.parseInt(view.getContentDescription().toString()));
                            try {
                                boolean isAborted = ((MainActivity)requireActivity()).sonicInterface.Abort();
                                if(isAborted){
                                    ((MainActivity) requireActivity()).mStr = null;
                                    Sale.SaleRequest saleRequest = new Gson().fromJson(mSalesRequest, Sale.SaleRequest.class);
                                    //TODO: remove QRReferenceNumber
                                    boolean result = ((MainActivity) requireActivity()).sonicInterface.Sales(saleRequest.Amount, "abc", saleRequest.TxnId, saleRequest.Reference, saleRequest.Reserved, ((MainActivity) requireActivity()).callbackInterface);

                                    if(result) {
                                        TCPGeneralMessage saleRequestMsg = new TCPGeneralMessage();
                                        saleRequestMsg.Command = "Sale";
                                        saleRequestMsg.Data = saleRequest;

                                        //Notify UI to change to Tap Card screen
                                        Message message = ((MainActivity)requireActivity()).handler.obtainMessage();
                                        message.obj = new Gson().toJson(saleRequestMsg);
                                        ((MainActivity)requireActivity()).handler.sendMessage(message);
                                    }

                                    if (result && new SharedPrefUI(requireActivity()).ReadSharedPrefBoolean(requireActivity().getString(R.string.enable_auto_request_qr)) && ((MainActivity) requireActivity()).sonicInterface.ReadSharedPrefBoolean(requireActivity().getString(R.string.IsQRUserScanEnabled))) {
                                        new Thread(()->{
                                            try {
                                                QRResponse qrResponse = ((MainActivity) requireActivity()).sonicInterface.QRRequest(saleRequest.Amount, Integer.parseInt(view.getContentDescription().toString()), ((MainActivity) requireActivity()).callbackInterface);
                                                LogUtils.i(TAG, "QRRequest Response:" + new Gson().toJson(qrResponse));

                                                TCPGeneralMessage qrResponseMsg = new TCPGeneralMessage();
                                                qrResponseMsg.Command = "QRRequest";
                                                qrResponseMsg.Data = qrResponse;

                                                //Notify UI to change to Tap Card screen
                                                Message message = ((MainActivity)requireActivity()).handler.obtainMessage();
                                                message.obj = new Gson().toJson(qrResponseMsg);
                                                ((MainActivity)requireActivity()).handler.sendMessage(message);

                                            } catch (Exception e) {
                                                LogUtils.e(TAG, "QRRequest Exception: " + Log.getStackTraceString(e));
                                            }
                                        }).start();
                                    }
                                }
                            } catch (RemoteException e) {
                                LogUtils.e(TAG, "onClick Exception: " + Log.getStackTraceString(e));
                            }
                        }
                    });
                    qrItem.addView(qrImg);
                    itemIndex++;
                }

                qrList.addView(qrItem);
            }
        }

        return view;
    }

}