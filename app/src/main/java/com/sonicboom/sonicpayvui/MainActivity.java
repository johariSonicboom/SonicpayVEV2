package com.sonicboom.sonicpayvui;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.gson.Gson;
import com.pax.dal.entity.EBeepMode;
import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.annotation.RouterUri;
import com.sbs.aidl.Class.FareBasedEntryResult;
import com.sbs.aidl.Class.FareBasedExitResult;
import com.sbs.aidl.Class.GetStatusResult;
import com.sbs.aidl.Class.MaxChargedEntryResult;
import com.sbs.aidl.Class.MaxChargedExitResult;
import com.sbs.aidl.Class.ParkingEntryResult;
import com.sbs.aidl.Class.ParkingExitResult;
import com.sbs.aidl.Class.QRResponse;
import com.sbs.aidl.Class.QRTransactionResult;
import com.sbs.aidl.Class.ReadCardResult;
import com.sbs.aidl.Class.SalesCompletionResult;
import com.sbs.aidl.Class.SalesResult;
import com.sbs.aidl.Class.SettlementResult;
import com.sbs.aidl.Class.VoidResult;
import com.sbs.aidl.Class.eCreditCardType;
import com.sbs.aidl.Class.eTerminalState;
import com.sbs.aidl.IAIDLCardCallbackInterface;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.EVFragments.ChargingFragment;
import com.sonicboom.sonicpayvui.EVFragments.ChargingRateFragment;
import com.sonicboom.sonicpayvui.EVFragments.DisconnectChargerFragment;
import com.sonicboom.sonicpayvui.EVFragments.PhoneNumberFragment;
import com.sonicboom.sonicpayvui.EVFragments.PlugInToStartFragment;
import com.sonicboom.sonicpayvui.EVFragments.SelectChargerFragment;
import com.sonicboom.sonicpayvui.EVFragments.SelectConnectorFragment;
import com.sonicboom.sonicpayvui.EVFragments.StopChargeTapCardFragment;
import com.sonicboom.sonicpayvui.EVModels.Component;
import com.sonicboom.sonicpayvui.EVModels.Connector;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.EVModels.StartSales;
import com.sonicboom.sonicpayvui.EVModels.StopChargeTapCardError;
import com.sonicboom.sonicpayvui.EVModels.eChargePointStatus;
import com.sonicboom.sonicpayvui.models.DisplayInfo;
import com.sonicboom.sonicpayvui.models.GetStatus;
import com.sonicboom.sonicpayvui.models.ParkingEntry;
import com.sonicboom.sonicpayvui.models.ParkingExit;
import com.sonicboom.sonicpayvui.models.PreAuth;
import com.sonicboom.sonicpayvui.models.Sale;
import com.sonicboom.sonicpayvui.models.SaleP1P2;
import com.sonicboom.sonicpayvui.models.SalesCompletion;
import com.sonicboom.sonicpayvui.models.Settlement;
import com.sonicboom.sonicpayvui.EVModels.StopChargeTapCard;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;
import com.sonicboom.sonicpayvui.models.Void;
import com.sonicboom.sonicpayvui.models.eQRType;
import com.sonicboom.sonicpayvui.models.eResult;
import com.sonicboom.sonicpayvui.models.eStatusCode;
import com.sonicboom.sonicpayvui.models.eTngStatusCode;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.ScannerUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pl.droidsonroids.gif.GifImageView;

@RouterUri(path = {RouterConst.MAIN})
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    public Handler handler;
    public String mStr;
    public IAIDLSonicpayInterface sonicInterface;

    public ScannerUtils scannerUtils;
    private MQTT mqtt;
    private String phNumber;
    private int TotalAmountCharges;
    private boolean IsInSalesP1 = false;
    private boolean IsStopChargeTapCard;
    private String SelectedChargingStation;
    public Component SelectedChargingStationComponent;
    public int selectedConnectorIndex;
    public boolean isOneConnector;
    public List<com.sonicboom.sonicpayvui.SalesCompletion> SalesCompletionQueue = new ArrayList<>();
    public boolean runSettlement;
    //Used to skip the Result Fragment if the SalesCompletion returns error to avoid showing two result fragments.
    public boolean IsSkipSalesCompletionResultFragment;


    public boolean IsCharging;
    TextView txtStatus;
    public final IAIDLCardCallbackInterface callbackInterface = new IAIDLCardCallbackInterface.Stub() {

        @Override
        public void ReadCardCallback(ReadCardResult result) {
            LogUtils.i(TAG, "ReadCardCallback:" + new Gson().toJson(result));


            if (!result.CardType.name().equals("Unknown")) {
                if (IsStopChargeTapCard) {

                    StopChargeTapCard stopChargeTapCardResponse = new StopChargeTapCard();
                    stopChargeTapCardResponse.CardNo = result.CardNo;
                    stopChargeTapCardResponse.HashedPAN = result.Token;
                    stopChargeTapCardResponse.ComponentCode = SelectedChargingStationComponent.ComponentCode;

//
                    stopChargeTapCardResponse.ConnectorId = getConnectorIDByIndex(SelectedChargingStationComponent, SelectedChargingStationComponent.SelectedConnector);
                    wbs.StopChargeTapCardResultResponse(stopChargeTapCardResponse);

                    UpdateTitle("Stop Charge");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, DisconnectChargerFragment.class, null)
                            .addToBackStack(null)
                            .commit();

                }
            } else {
                if (!isOneConnector) {
                    Bundle bundle = new Bundle();
                    boolean isSuccess = false;
                    bundle.putBoolean("IsSuccess", isSuccess);
                    bundle.putString("Title", "Unknown Card");
                    bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
                    bundle.putString("Message", "Error: Unknown Card Type. Try Tapping The Card Again");


                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                }
            }

        }

        @SuppressLint("DefaultLocale")
        @Override
        public void ParkingEntryCallback(ParkingEntryResult result) {
            LogUtils.i(TAG, "ParkingEntryCallback:" + new Gson().toJson(result));

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "ParkingEntry";

                ParkingEntry.ParkingEntryResponse parkingEntryResponse = new ParkingEntry.ParkingEntryResponse();
                parkingEntryResponse.CardNo = result.CardNo;
                parkingEntryResponse.HashedPAN = result.Token;
                parkingEntryResponse.CardType = result.CardType.getValue();
                parkingEntryResponse.StatusCode = result.StatusCode;
                parkingEntryResponse.TNGInfo = result.tnginfo;
                parkingEntryResponse.EMVInfo = result.emvInfo;

                response.Data = parkingEntryResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void ParkingExitCallback(ParkingExitResult result) {
            LogUtils.i(TAG, "ParkingExitCallback:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "ParkingExit";

                ParkingExit.ParkingExitResponse parkingExitResponse = new ParkingExit.ParkingExitResponse();
                parkingExitResponse.SystemId = result.SystemId;
                parkingExitResponse.CardNo = result.CardNo;
                parkingExitResponse.HashedPAN = result.Token;
                parkingExitResponse.CardType = result.CardType.getValue();
                parkingExitResponse.StatusCode = result.StatusCode;
                parkingExitResponse.TNGInfo = result.tnginfo;
                parkingExitResponse.EMVInfo = result.emvInfo;

                response.Data = parkingExitResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void SalesCallback(SalesResult result) {
            LogUtils.i(TAG, "SalesCallback:" + new Gson().toJson(result));

            try {
                App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
                if (scannerUtils != null)
                    scannerUtils.releaseRes();
                if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                    TCPGeneralMessage response = new TCPGeneralMessage();
                    response.Command = "Sale";

                    Sale.SaleResponse saleResponse = new Sale.SaleResponse();
                    saleResponse.CardNo = result.CardNo;
                    saleResponse.HashedPAN = result.Token;
                    saleResponse.PaymentType = String.valueOf(result.CardType.getValue());
                    saleResponse.StatusCode = result.StatusCode;
                    saleResponse.SystemId = result.SystemId;
                    saleResponse.EMVInfo = result.emvInfo;
                    saleResponse.TNGInfo = result.tnginfo;
                    saleResponse.QRInfo = result.qrInfo;

                    response.Data = saleResponse;
                    response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


                }
                Bundle bundle = new Bundle();
                boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
                bundle.putBoolean("IsSuccess", isSuccess);
                bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
                bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
                if (!isSuccess)
                    bundle.putString("Message", "Error: " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : eStatusCode.getDescFromCode(result.StatusCode)));
                bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");
                bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                LogUtils.e(TAG, "SalesCallback Exception: " + Log.getStackTraceString(e));
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void StatusCallback(eTerminalState state, boolean cardPresent) {
            LogUtils.i(TAG, "StatusCallback:" + state + "(" + state.getValue() + ")" + " Card Present: " + cardPresent);

            if (state == eTerminalState.SeePhone || state == eTerminalState.TapAgain || state == eTerminalState.PresentOneCard) {
                Bundle bundle = new Bundle();
                bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));
                bundle.putString("SalesRequest", new Gson().toJson(mStr));
                String message = "";
                if (state == eTerminalState.SeePhone)
                    message = "See Phone";
                if (state == eTerminalState.TapAgain)
                    message = "Tap Again";
                if (state == eTerminalState.PresentOneCard)
                    message = "Present 1 Card";
                bundle.putString("TapCardMsg", message);

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }

            if (state == eTerminalState.PINEntering) {
                Bundle bundle = new Bundle();
                bundle.putString("StatusText", "Processing...");
                bundle.putString("TitleText", "Enter PIN\r\nMYR " + String.format("%.2f", (double) TotalAmountCharges / 100f));

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }

            if (state == eTerminalState.HostConnecting || state == eTerminalState.ChipCardReading || state == eTerminalState.ContactlessCardReading) {
                Bundle bundle = new Bundle();
                bundle.putString("StatusText", state == eTerminalState.HostConnecting ? "Connecting host..." : "Processing...");

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "GetStatus";

                GetStatus getStatus = new GetStatus();
                getStatus.Stage = String.valueOf(state.getValue());
                getStatus.CardPresent = cardPresent ? 1 : 0;

                response.Data = getStatus;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
        }

        @Override
        public void SettlementCallback(SettlementResult[] result) {
            LogUtils.i(TAG, "SettlementCallback:" + new Gson().toJson(result));
            boolean isSuccess = true;
            String errorCode = "SF";

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "Settlement";

                List<Settlement.SettlementResponse> settlementResponses = new ArrayList<>();
                for (SettlementResult hostResult : result) {
                    Settlement.SettlementResponse settlementResponse = new Settlement.SettlementResponse();
                    settlementResponse.BatchNo = hostResult.BatchNo;
                    settlementResponse.StatusCode = hostResult.StatusCode;
                    settlementResponse.BatchCount = hostResult.BatchCount;
                    settlementResponse.HostNo = hostResult.HostNo;
                    settlementResponse.BatchAmount = hostResult.BatchAmount;
                    settlementResponse.RefundCount = hostResult.RefundCount;
                    settlementResponse.RefundAmount = hostResult.RefundAmount;
                    settlementResponses.add(settlementResponse);
                    if (!hostResult.StatusCode.equals(eStatusCode.Approved.getCode()) || !hostResult.StatusCode.equals(eTngStatusCode.No_Error.getCode())) {
                        isSuccess = false;
                        errorCode = hostResult.StatusCode;
                    }
                }

                response.Data = settlementResponses;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

            }
            Bundle bundle = new Bundle();
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Close batch successfully" : eStatusCode.getDescFromCode(errorCode));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void QRTransactionCallback(QRTransactionResult result) {
            LogUtils.i(TAG, "QRTransactionCallback:" + new Gson().toJson(result));

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "Sale";

                Sale.SaleResponse saleResponse = new Sale.SaleResponse();
                saleResponse.CardNo = "";
                saleResponse.HashedPAN = "";
                saleResponse.PaymentType = String.valueOf(result.QRType);
                saleResponse.StatusCode = result.Status;
                saleResponse.SystemId = String.valueOf(result.SystemId);
                saleResponse.EMVInfo = null;
                saleResponse.TNGInfo = null;
                saleResponse.QRInfo = result.qrInfo;

                response.Data = saleResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.Status.equals("00");
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putBoolean("IsTng", false);
            if (!isSuccess)
                bundle.putString("Message", "Error " + result.Status + ": Transaction failed");
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void SalesP1Callback(SalesResult result) throws RemoteException {
            LogUtils.i(TAG, "SalesP1Callback:" + new Gson().toJson(result));
            App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
            if (scannerUtils != null)
                scannerUtils.releaseRes();

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                SaleP1P2.SaleP1Response saleP1Response = new SaleP1P2().new SaleP1Response();
                if (IsInSalesP1) {
                    response.Command = "SaleP1";
                    saleP1Response.IsValidCard = false;
                    Sale.SaleResponse saleResponse = new Sale.SaleResponse();
                    saleResponse.SystemId = result.SystemId;
                    saleResponse.StatusCode = result.StatusCode;
                    saleResponse.PaymentType = String.valueOf(result.CardType.getValue());
                    saleResponse.HashedPAN = result.Token;
                    saleResponse.CardNo = result.CardNo;
                    saleResponse.QRInfo = result.qrInfo;
                    saleResponse.EMVInfo = result.emvInfo;
                    saleResponse.TNGInfo = result.tnginfo;

                    saleP1Response.SalesResult = saleResponse;

                    response.Data = saleP1Response;
                    IsInSalesP1 = false;
                } else {
                    response.Command = "SaleP2";
                    Sale.SaleResponse saleResponse = new Sale.SaleResponse();
                    saleResponse.SystemId = result.SystemId;
                    saleResponse.StatusCode = result.StatusCode;
                    saleResponse.PaymentType = String.valueOf(result.CardType.getValue());
                    saleResponse.HashedPAN = result.Token;
                    saleResponse.CardNo = result.CardNo;
                    saleResponse.QRInfo = result.qrInfo;
                    saleResponse.EMVInfo = result.emvInfo;
                    saleResponse.TNGInfo = result.tnginfo;

                    response.Data = saleResponse;
                }

                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error: " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : eStatusCode.getDescFromCode(result.StatusCode)));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void SalesP3Callback(SalesResult result) throws RemoteException {
            LogUtils.i(TAG, "SalesP3Callback:" + new Gson().toJson(result));
        }

        @Override
        public void SalesP3DebtRecoveryCallback(SalesResult result, int orinSystemID) throws RemoteException {
            LogUtils.i(TAG, "SalesP3DebtRecoveryCallback:" + new Gson().toJson(result) + ", OriSysId: " + orinSystemID);
        }


        @SuppressLint("DefaultLocale")
        @Override
        public void PreAuthCallback(SalesResult result) throws RemoteException {
            LogUtils.i(TAG, "PreAuthCallback:" + new Gson().toJson(result));

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess) {
                String message = "Error ";
                if (result.CardType == eCreditCardType.TNGCard) {
                    eTngStatusCode tngStatusCode = eTngStatusCode.fromCode(String.valueOf(result.StatusCode));
                    message += tngStatusCode != null ? (tngStatusCode.getCode() + " " + tngStatusCode.getDesc()) : " : Unknown error";
                } else {
                    message += result.StatusCode;
                }
                bundle.putString("Message", message);
            }
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();

            if (isSuccess) {
                StartSales startSales = wbs.SalesResultResponse(result, phNumber);
                if (startSales != null) {


                    LogUtils.i("startSales in PreAuth", startSales.isSuccess);
                    if (startSales.isSuccess) {
//                        try {
//                            new Date().getTime();
//                            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//                            String formattedDate = format.format(new Date());
//
//                            Thread.sleep(3000);
//                            ShowHideTitle(true);
//                            UpdateTitle("Charging");
//                            StartCharging(formattedDate);

//                        if (wbs.componentList.length == 1 && SelectedChargingStationComponent.Connectors.size() > 1) {
//                            bundle.putBoolean("stayOnFragment", true);
//                        }else{
//                            bundle.putBoolean("stayOnFragment", false);
//                        }
//
//                        try {
//                        Thread.sleep(3000);
//                        UpdateTitle("Plug In To Charge");
//                        UpdateTitleColor(R.color.main_blue);
//                        ShowHideTitle(true);
//                        btnStartCharge.setVisibility(View.GONE);
//                        getSupportFragmentManager().beginTransaction()
//                                .setReorderingAllowed(true)
//                                .replace(R.id.fragmentContainer, PlugInToStartFragment.class, bundle)
//                                .addToBackStack(null)
//                                .commit();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        try {
                            Thread.sleep(4000);
                            ChangeToPluginFragment();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    } else {
                        new Thread(() -> {
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            SalesCompletion(0, wbs.startTransactionTrace, String.format("Total Charging time %02d Hours %02d Minutes", 0, 0));
                        }).start();

                        bundle = new Bundle();
                        isSuccess = false;
                        bundle.putBoolean("IsSuccess", false);
                        bundle.putString("Title", "ERROR");
                        bundle.putString("Message", startSales.CustomError);

                        IsSkipSalesCompletionResultFragment = true;
                        LogUtils.i("IsSkipSalesCompletionResultFragment", "Set to True");

                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    }
                } else {
                    LogUtils.e(TAG, "startSales is null");
                    bundle = new Bundle();
                    isSuccess = false;
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "ERROR");
                    bundle.putString("Message", "Failed to initiate sales process");

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                }
            }
        }

        @Override
        public void SalesCompletionCallback(SalesCompletionResult result) throws
                RemoteException {
            LogUtils.i(TAG, "SalesCompletionCallback:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "SaleCompletion";

                SalesCompletion.SalesCompletionResponse salesCompletionResponse = new SalesCompletion.SalesCompletionResponse();
                salesCompletionResponse.TransactionTrace = result.TransactionTrace;
                salesCompletionResponse.RRN = result.RRN;
                salesCompletionResponse.HostNo = result.HostNo;
                salesCompletionResponse.TerminalId = result.TerminalId;
                salesCompletionResponse.MerchantId = result.MerchantId;
                salesCompletionResponse.BatchNo = result.BatchNo;
                salesCompletionResponse.InvoiceNo = result.InvoiceNo;
                salesCompletionResponse.StatusCode = result.StatusCode;
                salesCompletionResponse.SystemId = result.SystemId;

                response.Data = salesCompletionResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            if (wbs.salesCompletionResult != null && wbs.salesCompletionResult.CustumErrorMessage != null) {
                Bundle bundle = new Bundle();
                boolean isSuccess = false;
                bundle.putBoolean("IsSuccess", false);
                bundle.putString("Title", "Invalid Card");
                bundle.putString("Message", wbs.salesCompletionResult.CustumErrorMessage);


//                LogUtils.i("StopChargeTapCardErrorReceived", "StopChargeTapCardErrorReceived");
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            } else {
                Bundle bundle = new Bundle();
                boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
                bundle.putBoolean("IsSuccess", isSuccess);
                bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
                bundle.putString("Message", isSuccess ? SalesCompletionResult : "Sales completion failed");
                bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));
                Log.i(TAG, "SalesCompletionCallback: " + String.format("%.2f", (double) TotalAmountCharges / 100f));
                LogUtils.i("IsSkipSalesCompletionResultFragment", "Set to False");
                if (!isSuccess)
                    bundle.putString("Message", "Error " + " : " + result.StatusCode);


                if (IsSkipSalesCompletionResultFragment == false) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                }
                IsSkipSalesCompletionResultFragment = false;
                if (isSuccess) {
                    wbs.SalesCompletionResultResponse(result);
                }
            }
        }

        @Override
        public void VoidCallback(VoidResult result) throws RemoteException {
            LogUtils.i(TAG, "VoidCallback:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "Void";

                Void.VoidResponse voidResponse = new Void.VoidResponse();
                voidResponse.SystemId = result.SystemId;
                voidResponse.StatusCode = result.StatusCode;
                voidResponse.RRN = result.RRN;
                voidResponse.Amount = result.Amount;
                voidResponse.ApprovalCode = result.ApprovalCode;
                voidResponse.TransactionTrace = result.TransactionTrace;

                response.Data = voidResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Transaction is voided successfully" : "Transaction fail to void");
            if (!isSuccess)
                bundle.putString("Message", "Error " + " : " + result.StatusCode);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void MaintenanceCallback(String result) throws RemoteException {
            LogUtils.i(TAG, "MaintenanceCallback:" + result);
        }

        @Override
        public void ExecuteQueryCallback(String result) throws RemoteException {
            LogUtils.i(TAG, "ExecuteQueryCallback:" + result);
            MQTT.Payload payload = new MQTT().new Payload();
            payload.MessageId = mqtt.queryPayload.MessageId;
            payload.ClientId = mqtt.queryPayload.ClientId;
            payload.Command = mqtt.queryPayload.Command;
            payload.SerialNo = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getApplicationContext().getString(R.string.serial_number));
            payload.Value = result;
            payload.IsSuccess = true;

            mqtt.Publish("Response/" + mqtt.queryPayload.ClientId, new Gson().toJson(payload));
        }

        @Override
        public void RefundCallback(SalesResult result) throws RemoteException {
            LogUtils.i(TAG, "RefundResult:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "Refund";

                Sale.SaleResponse saleResponse = new Sale.SaleResponse();
                saleResponse.CardNo = result.CardNo;
                saleResponse.HashedPAN = result.Token;
                saleResponse.PaymentType = String.valueOf(result.CardType.getValue());
                saleResponse.StatusCode = result.StatusCode;
                saleResponse.SystemId = result.SystemId;
                saleResponse.EMVInfo = result.emvInfo;
                saleResponse.TNGInfo = result.tnginfo;
                saleResponse.QRInfo = result.qrInfo;

                response.Data = saleResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Transaction is refunded successfully" : "Transaction fail to refund");
            if (!isSuccess)
                bundle.putString("Message", "Error " + " : " + result.StatusCode);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void FareBasedEntryCallback(FareBasedEntryResult result) throws RemoteException {
            LogUtils.i(TAG, "FareBasedEntryCallback:" + new Gson().toJson(result));

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();

                String operationType = sonicInterface.ReadSharedPref(getString(R.string.OperationType));
                operationType = operationType == null || operationType.length() == 0 ? "Parking" : operationType;

                response.Command = operationType + "Entry";

                ParkingEntry.ParkingEntryResponse parkingEntryResponse = new ParkingEntry.ParkingEntryResponse();
                parkingEntryResponse.CardNo = result.CardNo;
                parkingEntryResponse.HashedPAN = result.Token;
                parkingEntryResponse.CardType = result.CardType.getValue();
                parkingEntryResponse.StatusCode = result.StatusCode;
                parkingEntryResponse.TNGInfo = result.tnginfo;
                parkingEntryResponse.EMVInfo = result.emvInfo;

                response.Data = parkingEntryResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));

            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void FareBasedExitCallback(FareBasedExitResult result) throws RemoteException {
            LogUtils.i(TAG, "FareBasedExitCallback:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();

                String operationType = sonicInterface.ReadSharedPref(getString(R.string.OperationType));
                operationType = operationType == null || operationType.length() == 0 ? "Parking" : operationType;

                response.Command = operationType + "Exit";

                ParkingExit.ParkingExitResponse parkingExitResponse = new ParkingExit.ParkingExitResponse();
                parkingExitResponse.SystemId = result.SystemId;
                parkingExitResponse.CardNo = result.CardNo;
                parkingExitResponse.HashedPAN = result.Token;
                parkingExitResponse.CardType = result.CardType.getValue();
                parkingExitResponse.StatusCode = result.StatusCode;
                parkingExitResponse.TNGInfo = result.tnginfo;
                parkingExitResponse.EMVInfo = result.emvInfo;

                response.Data = parkingExitResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void MaxChargedEntryCallback(MaxChargedEntryResult result) throws
                RemoteException {
            LogUtils.i(TAG, "MaxChargedEntryCallback:" + new Gson().toJson(result));

            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();

                String operationType = sonicInterface.ReadSharedPref(getString(R.string.OperationType));
                operationType = operationType == null || operationType.length() == 0 ? "Parking" : operationType;

                response.Command = operationType + "Entry";

                ParkingEntry.ParkingEntryResponse parkingEntryResponse = new ParkingEntry.ParkingEntryResponse();
                parkingEntryResponse.CardNo = result.CardNo;
                parkingEntryResponse.HashedPAN = result.Token;
                parkingEntryResponse.CardType = result.CardType.getValue();
                parkingEntryResponse.StatusCode = result.StatusCode;
                parkingEntryResponse.TNGInfo = result.tnginfo;
                parkingEntryResponse.EMVInfo = result.emvInfo;

                response.Data = parkingEntryResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void MaxChargedExitCallback(MaxChargedExitResult result) throws RemoteException {
            LogUtils.i(TAG, "MaxChargedExitCallback:" + new Gson().toJson(result));
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();

                String operationType = sonicInterface.ReadSharedPref(getString(R.string.OperationType));
                operationType = operationType == null || operationType.length() == 0 ? "Parking" : operationType;

                response.Command = operationType + "Exit";

                ParkingExit.ParkingExitResponse parkingExitResponse = new ParkingExit.ParkingExitResponse();
                parkingExitResponse.SystemId = result.SystemId;
                parkingExitResponse.CardNo = result.CardNo;
                parkingExitResponse.HashedPAN = result.Token;
                parkingExitResponse.CardType = result.CardType.getValue();
                parkingExitResponse.StatusCode = result.StatusCode;
                parkingExitResponse.TNGInfo = result.tnginfo;
                parkingExitResponse.EMVInfo = result.emvInfo;

                response.Data = parkingExitResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));


            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if (!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : result.StatusCode));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double) result.tnginfo.CardBalance / 100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double) TotalAmountCharges / 100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }
    };
    public AppCompatButton btnStartCharge;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    WebSocketHandler wbs = null;
    private Handler handlerTimer;
    private Runnable runnable;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.footer).setVisibility(View.VISIBLE);

        ConnectService();

        handler = new Handler(message -> {
            String input = (String) message.obj;

            if (Objects.equals(input, eResult.NAK.toString())) {
                Bundle bundle = new Bundle();
                bundle.putBoolean("IsSuccess", false);
                bundle.putString("Title", "Cancelled");
                bundle.putString("Message", "Transaction is aborted.");

                LogUtils.i("onCreate", "onCreate");
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
                return false;


            }

            // To cancel ResultFragment auto redirection to Idle page timer if received new command
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            ResultFragment resultFragment = null;
            if (fragment.getClass() == ResultFragment.class && fragment != null && fragment.isVisible()) {
                LogUtils.i(TAG, "Cancel ResultPage auto redirection to idle page timer");
                resultFragment = (ResultFragment) fragment;
                resultFragment.stopAutoRedirectionToIdlePage();
            }

            boolean isCommandMatched = true;

            TCPGeneralMessage receiveMessage = new Gson().fromJson(input, TCPGeneralMessage.class);
            switch (receiveMessage.Command) {
                case "ReadCard":
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, TapCardFragment.class, null)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "Settlement":
                    Bundle bundle = new Bundle();
                    bundle.putString("StatusText", "Uploading...");

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();

                    break;
                case "PreAuth":
                    PreAuth.PreAuthRequest preAuthRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), PreAuth.PreAuthRequest.class);
                    TotalAmountCharges = preAuthRequest.Amount;

                    bundle = new Bundle();
                    bundle.putString("Amount", String.format("%.2f", (double) preAuthRequest.Amount / 100f));

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "SaleP1":
                    IsInSalesP1 = true;
                case "Sale":
                case "SaleCompletion": // now only for certification
                    Sale.SaleRequest saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Sale.SaleRequest.class);
                    TotalAmountCharges = saleRequest.Amount;

                    bundle = new Bundle();
                    bundle.putString("Amount", String.format("%.2f", (double) saleRequest.Amount / 100f));
                    bundle.putString("SalesRequest", new Gson().toJson(saleRequest));
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "Abort":
                    bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "Cancelled");
                    bundle.putString("Message", "Transaction is aborted.");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                //case "SaleCompletion":
                case "Void":
                case "ParkingEntry":
                    bundle = new Bundle();
                    bundle.putString("StatusText", "Processing...");

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "Refund":
                    Void.VoidRequest refundRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Void.VoidRequest.class);
                    TotalAmountCharges = refundRequest.Amount;

                    bundle = new Bundle();
                    if (refundRequest.TransactionTrace == null || refundRequest.TransactionTrace.equalsIgnoreCase("")) {
                        bundle.putString("Amount", String.format("%.2f", (double) refundRequest.Amount / 100f));
                        bundle.putString("SalesRequest", new Gson().toJson(refundRequest));
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        bundle.putString("StatusText", "Processing...");
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    }
                    break;
                case "ParkingExit":
                    ParkingExit.ParkingExitRequest parkingExitRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), ParkingExit.ParkingExitRequest.class);
                    TotalAmountCharges = parkingExitRequest.TotalAmount;

                    bundle = new Bundle();
                    bundle.putString("StatusText", "Processing...");

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "DisplayInfo":
                    DisplayInfo displayInfo = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), DisplayInfo.class);

                    bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", displayInfo.IsSuccess);
                    bundle.putString("Title", displayInfo.IsSuccess ? "Successful" : "Unsuccessful");
                    bundle.putString("Message", displayInfo.Message);

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "QRRequest":
                    try {
                        QRResponse qrResponse = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), QRResponse.class);

                        mStr = new Gson().toJson(qrResponse);
                        // Get the FragmentManager instance
                        FragmentManager fragmentManager = getSupportFragmentManager();

                        // Get the current fragment
                        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
                        assert currentFragment != null;
                        if (currentFragment.getClass() == TapCardFragment.class) {

                            View view = currentFragment.getView();
                            assert view != null;
                            if (!qrResponse.QRCode.isEmpty()) {
                                TextView gifTitle = view.findViewById(R.id.gifTitle);
                                gifTitle.setText("Scan to pay");
                            }

                            TextView qrTitle = view.findViewById(R.id.qrTitle);
                            qrTitle.setVisibility(View.VISIBLE);
                            qrTitle.setText(qrResponse.QRName);

                            GifImageView imageView = view.findViewById(R.id.tapCard);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            if (qrResponse.QRCode.startsWith("http")) {
                                boolean isImage = qrResponse.QRCode.endsWith(".png") || qrResponse.QRCode.endsWith(".jpg") || qrResponse.QRCode.endsWith(".jpeg");
                                ((TapCardFragment) currentFragment).mQRContentType = isImage ? "image" : "";
                                if (isImage)
                                    new Utils.DownloadImageTask(imageView).execute(qrResponse.QRCode);
                                else
                                    imageView.setImageBitmap(Utils.generateQRfromStr(this, qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue()));
                            } else {
                                if (!qrResponse.QRCode.equals(""))
                                    imageView.setImageBitmap(Utils.generateQRfromStr(this, qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue()));
                            }
                            if (qrResponse.qrList != null && qrResponse.qrList.length > 0) {
                                TextView moreQR = view.findViewById(R.id.moreQROptions);
                                moreQR.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {
                        LogUtils.e(TAG, "QR Display Exception: " + Log.getStackTraceString(e));
                    }
                    break;
                case "QRMerchantScan":
                    try {
                        sonicInterface.QRMerchantScan(TotalAmountCharges, (String) receiveMessage.Data, callbackInterface);

                        bundle = new Bundle();
                        bundle.putString("StatusText", "Processing...");
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    } catch (RemoteException e) {
                        LogUtils.e(TAG, "QR Merchant Scan Exception: " + Log.getStackTraceString(e));
                    }
                    break;
                case "TriggerMaintenance":
                    bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "Maintenance");
                    bundle.putString("Message", "Under maintenance");
                    bundle.putBoolean("StickPage", true);

                    String currentActivity = "";
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    if (activityManager != null) {
                        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
                        currentActivity = componentName.getClassName();
                    }
                    if (currentActivity.equals("com.sonicboom.sonicpayvui.MainActivity")) {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    }

                    break;
                case "ClearMaintenance":
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                            .addToBackStack(null)
                            .commit();
                    break;
                default:
                    isCommandMatched = false;
                    break;
            }

            if (!isCommandMatched && resultFragment != null) {
                resultFragment.startAutoRedirectionToIdlePage();
            }

            return false;
        });
        btnStartCharge = findViewById(R.id.btnStartCharge);
        btnStartCharge.setText("");
        btnStartCharge.setOnClickListener(this);
        //findViewById(R.id.btnPhoneNumber).setOnClickListener(this);
        txtStatus = findViewById(R.id.txtstatus);
        wbs = new WebSocketHandler(this);

// Initialize the handler
        handlerTimer = new Handler();

// Define the runnable
        runnable = new Runnable() {
            @Override
            public void run() {
                // Execute the function
                if (SalesCompletionQueue != null && !SalesCompletionQueue.isEmpty()) {
                    if (GeneralVariable.CurrentFragment.equals("WelcomeFragment")) {
                        com.sonicboom.sonicpayvui.SalesCompletion salesCompletionResult = SalesCompletionQueue.get(0); // Get the first item
                        Component salesCompletionComponent = GetSelectedComponentbyComponentCode(salesCompletionResult.ComponentCode, wbs.componentList);

                        long diff = new Date().getTime() - salesCompletionComponent.StartChargeTime.getTime();

                        long seconds = diff / 1000;
                        long minutes = seconds / 60;
                        long hours = minutes / 60;
                        long days = hours / 24;
                        long m = minutes % 60;
                        String timeUse = String.format("Total Charging time %02d Hours %02d Minutes", hours, m);

                        SalesCompletion(salesCompletionResult.Amount, salesCompletionResult.TransactionTrace, timeUse);
                        SalesCompletionQueue.remove(0); // Remove the first item
                    }
                }

                if (runSettlement) {
                    StartSettlement();
                    runSettlement = false;
                }

                // Schedule the runnable to run again after 5 seconds
                handler.postDelayed(this, 5000);
            }
        };

// Start the runnable for the first time
        handlerTimer.post(runnable);


        LogUtils.d(TAG, "onCreate ended.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        LogUtils.d(TAG, "onDestroy");

    }


    @Override
    public void onClick(View view) {
        Log.i(TAG, "onClick: " + view.getId());
        SelectChargerFragment selectChargerFragment = new SelectChargerFragment(wbs.componentList, selectedConnectorIndex);
        final SelectConnectorFragment[] selectConnectorFragment = new SelectConnectorFragment[1];


        Bundle bundle = new Bundle();
        switch (view.getId()) {
            case R.id.btnStartCharge:
                // handle button A click;
                selectedConnectorIndex = 0;
                isOneConnector = false;

                UpdateTitle("Loading");
                btnStartCharge.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, LoadingFragment.class, null)
                        .addToBackStack(null)
                        .commit();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());

                // Declare futureTask as an array to make it effectively final
                final Future<?>[] futureTask = new Future<?>[1];

                // Define the timeout Runnable
                Runnable timeoutRunnable = new Runnable() {
                    @Override
                    public void run() {
                        // Cancel the task if it's running
                        if (futureTask[0] != null && !futureTask[0].isDone()) {
                            futureTask[0].cancel(true);
                            LogUtils.e("TimeoutError", "Loading took longer than 5 seconds");
                            Toast.makeText(MainActivity.this, "Loading timeout. Please try again.", Toast.LENGTH_SHORT).show();
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                                    .addToBackStack(null)
                                    .commit();
                            btnStartCharge.setVisibility(View.VISIBLE);
                        }
                    }
                };

                int LoadingTimeOutDuration = 3000;
                LoadingTimeOutDuration = Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.LoadingTimeOutDuration)));

                // Post the timeout Runnable with a 5-second delay
                handler.postDelayed(timeoutRunnable, LoadingTimeOutDuration);

                selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                SelectConnectorFragment finalSelectConnectorFragment = selectConnectorFragment[0];
                SelectChargerFragment finalSelectChargerFragment = selectChargerFragment;
                futureTask[0] = executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            for (Component component : wbs.componentList) {
                                // Check for interruption
                                if (Thread.currentThread().isInterrupted()) {
                                    return;
                                }

                                wbs.GetStatus(component.ComponentCode);
                                if (component.Connectors != null && !component.Connectors.isEmpty()) {
                                    LogUtils.i("Start Component Status :" + component.ComponentCode, component.Connectors.get(0).Status);
                                } else {
                                    LogUtils.i("Start Component Status :", "Status is null or connectors are empty for component: " + component.ComponentCode);
                                    return;
                                }
                            }

                            // After fetching statuses, handle the UI update on the main thread
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    // Cancel the timeout Runnable
                                    handler.removeCallbacks(timeoutRunnable);

                                    try {
                                        // One charge station
                                        if (wbs.componentList.length == 1) {
                                            SelectedChargingStationComponent = wbs.componentList[0];
                                            SelectedChargingStation = SelectedChargingStationComponent.ComponentName;

                                            // One charge station, One Connector
                                            if (SelectedChargingStationComponent.Connectors.size() <= 1) {
                                                if (SelectedChargingStationComponent.Connectors.get(0).Status.toUpperCase(Locale.ROOT).equals("BLOCKED")) {
                                                    Toast.makeText(MainActivity.this, "Please unplug charger", Toast.LENGTH_SHORT).show();
                                                    getSupportFragmentManager().beginTransaction()
                                                            .setReorderingAllowed(true)
                                                            .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                                                            .addToBackStack(null)
                                                            .commit();
                                                    btnStartCharge.setVisibility(View.VISIBLE);
                                                } else {
                                                    btnStartCharge.setVisibility(View.GONE);

                                                    if (SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT).equals("STARTCHARGE") || SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT).equals("CHARGING")) {
                                                        try {
                                                            boolean result = sonicInterface.ReadCard(true, callbackInterface);
                                                            IsStopChargeTapCard = true;

                                                            if (result) {
                                                                // First parse the original date string
                                                                SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                                                                Date date = originalFormat.parse(String.valueOf(SelectedChargingStationComponent.StartChargeTime));

                                                                // Then format it to the desired format
                                                                SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                                                                String formattedDate = targetFormat.format(date);

                                                                bundle.putString("StartChargeTime", formattedDate);
                                                                bundle.putString("HideStopButton", "false");
                                                                isOneConnector = true;
                                                                getSupportFragmentManager().beginTransaction()
                                                                        .replace(R.id.fragmentContainer, ChargingFragment.class, bundle)
                                                                        .addToBackStack(null)
                                                                        .commit();
                                                            }
                                                        } catch (RemoteException e) {
                                                            LogUtils.e(TAG, "ReadCard Exception: " + Log.getStackTraceString(e));
                                                        }
                                                    } else if (SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT).equals("AVAILABLE") || SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT).equals("PREPARING") || SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT).equals("FINISHING")) {
                                                        IsStopChargeTapCard = false;
                                                        IsCharging = true;
                                                        isOneConnector = true;

                                                        if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.IsSkipFare))) {
                                                            ShowHideTitle(true);
                                                            UpdateTitle("Key in Phone No");
                                                            getSupportFragmentManager().beginTransaction()
                                                                    .setReorderingAllowed(true)
                                                                    .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
                                                                    .addToBackStack(null)
                                                                    .commit();
                                                        } else {
                                                            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.IsSkipFare))) {
                                                                ShowHideTitle(true);
                                                                UpdateTitle("Key in Phone No");
                                                                getSupportFragmentManager().beginTransaction()
                                                                        .setReorderingAllowed(true)
                                                                        .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
                                                                        .addToBackStack(null)
                                                                        .commit();
                                                            } else {
                                                                UpdateTitle("Fare Rate");
                                                                bundle.putString("FareChargeText", SelectedChargingStationComponent.FareChargeText);
                                                                bundle.putString("FareChargeDescription", SelectedChargingStationComponent.FareChargeDescription);
                                                                getSupportFragmentManager().beginTransaction()
                                                                        .setReorderingAllowed(true)
                                                                        .replace(R.id.fragmentContainer, ChargingRateFragment.class, bundle)
                                                                        .addToBackStack(null)
                                                                        .commit();
                                                            }
                                                        }
                                                    } else {
                                                        Bundle bundle = new Bundle();
                                                        boolean isSuccess = false;
                                                        bundle.putBoolean("IsSuccess", isSuccess);
                                                        bundle.putString("Title", "CHARGE POINT ERROR");
                                                        bundle.putString("Message", "The current Charge Point status is : " + SelectedChargingStationComponent.Connectors.get(SelectedChargingStationComponent.SelectedConnector).Status.toUpperCase(Locale.ROOT));

                                                        getSupportFragmentManager().beginTransaction()
                                                                .setReorderingAllowed(true)
                                                                .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                                                                .addToBackStack(null)
                                                                .commit();
                                                    }
                                                }
                                                // One Charge Station, Multiple Connectors
                                            } else {
                                                btnStartCharge.setVisibility(View.GONE);
                                                UpdateTitle("Connectors");

                                                selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                                                getSupportFragmentManager().beginTransaction()
                                                        .setReorderingAllowed(true)
                                                        .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                                        .addToBackStack(null)
                                                        .commit();
                                            }

                                            // Multiple Charge Stations
                                        } else {
                                            // Multiple Charge Station will always go to Select Charger Fragment
                                            selectedConnectorIndex = 0;

                                            btnStartCharge.setVisibility(View.GONE);
                                            UpdateTitle("Select Charger");
                                            getSupportFragmentManager().beginTransaction()
                                                    .setReorderingAllowed(true)
                                                    .replace(R.id.fragmentContainer, finalSelectChargerFragment, null)
                                                    .addToBackStack(null)
                                                    .commit();
                                        }
                                    } catch (Exception e) {
                                        LogUtils.e("ConditionError", e);
                                    }
                                }
                            });
                        } catch (InterruptedException e) {
                            LogUtils.e(TAG, "Task interrupted: " + Log.getStackTraceString(e));
                        }
                    }
                });
                break;


            case R.id.btnPhoneNumber:
                phNumber = ((EditText) findViewById(R.id.phone_noCountryCode)).getText().toString() + ((EditText) findViewById(R.id.phone_no)).getText().toString();
                try {
                    IsStopChargeTapCard = false;

                    btnStartCharge.setVisibility(View.GONE);
                    PreAuth.PreAuthRequest preAuthRequest = new PreAuth.PreAuthRequest();
                    preAuthRequest.Amount = ((int) SelectedChargingStationComponent.PreAuthAmount) * 100;
                    boolean result = sonicInterface.PreAuth(preAuthRequest.Amount, callbackInterface);

                    if (result) {
//                        //Notify UI to change to Progress screen
                        bundle.putString("Amount", String.format("%.2f", (double) preAuthRequest.Amount / 100f));

                        bundle.putString("chargingStation", SelectedChargingStation);
                        Log.d("Charging Station : ", SelectedChargingStation);

                        TotalAmountCharges = preAuthRequest.Amount;

                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    }
                } catch (Exception e) {
                    LogUtils.e(TAG, "PreAuth Exception: " + Log.getStackTraceString(e));
                }

                break;
            //Phone Fragment Cancel Button
            case R.id.btnCancel:
                selectedConnectorIndex = 0;

                if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.IsSkipFare))) {
                    if (wbs.componentList.length == 1) {
                        if (SelectedChargingStationComponent.Connectors.size() > 1) {
                            btnStartCharge.setVisibility(View.GONE);
                            UpdateTitle("Connectors");
                            selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                                    .addToBackStack(null)
                                    .commit();
                            btnStartCharge.setVisibility(View.VISIBLE);
                            ShowHideTitle(true);
                        }
                    } else {
                        if (SelectedChargingStationComponent.Connectors.size() > 1) {
                            btnStartCharge.setVisibility(View.GONE);
                            UpdateTitle("Connectors");
                            selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                    .addToBackStack(null)
                                    .commit();
                        } else {
//                        selectChargerFragment = new SelectChargerFragment(wbs.componentList, selectedConnectorIndex);
                            ShowHideTitle(true);
                            UpdateTitle("Select Charger");
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, selectChargerFragment)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }
                } else {
                    UpdateTitle("Fare Rate");
                    bundle.putString("FareChargeText", SelectedChargingStationComponent.FareChargeText);
                    bundle.putString("FareChargeDescription", SelectedChargingStationComponent.FareChargeDescription);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ChargingRateFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                }
                break;
            //Select Charger Fragment Back button
            case R.id.btnBack:
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                        .addToBackStack(null)
                        .commit();
                btnStartCharge.setVisibility(View.VISIBLE);

                break;

            //Tapping the charger card
            case R.id.chargingCard:

                TextView chargingStation = view.findViewById(R.id.chargingStation);

                TextView chargingStationStatus = view.findViewById(R.id.status);
                SelectedChargingStation = chargingStation.getText().toString().toUpperCase(Locale.ROOT);

                selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);

                SelectedChargingStationComponent = GetSelectedComponent(SelectedChargingStation, wbs.componentList);
                if (chargingStationStatus.getText().toString().equals("AVAILABLE") || chargingStationStatus.getText().toString().equals("PREPARING")) {


                    if (SelectedChargingStationComponent != null &&
                            SelectedChargingStationComponent.Connectors != null &&
                            SelectedChargingStationComponent.Connectors.size() < 2) {
                        selectedConnectorIndex = 0;
                        if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.IsSkipFare))) {
                            ShowHideTitle(true);
                            UpdateTitle("Key in Phone No");
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
                                    .addToBackStack(null)
                                    .commit();
                        } else {
                            UpdateTitle("Fare Rate");
                            bundle.putString("FareChargeText", SelectedChargingStationComponent.FareChargeText);
                            bundle.putString("FareChargeDescription", SelectedChargingStationComponent.FareChargeDescription);
                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, ChargingRateFragment.class, bundle)
                                    .addToBackStack(null)
                                    .commit();
                        }

                    } else {
                        btnStartCharge.setVisibility(View.GONE);
                        UpdateTitle("Connectors");
                        selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                .addToBackStack(null)
                                .commit();
                    }
//                }else{
//                    Toast.makeText(this, "Charging Station Not Available", Toast.LENGTH_SHORT).show();
//                }

//                    //Used for skipping Select Connector
////                    btnStartCharge.setVisibility(View.GONE);
////                    UpdateTitle("Key in Phone No");
////                    getSupportFragmentManager().beginTransaction()
////                            .setReorderingAllowed(true)
////                            .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
////                            .addToBackStack(null)
////                            .commit();
//
//
                } else if (chargingStationStatus.getText().toString().equals("CHARGING")) {
                    try {
                        if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.EnableTapCardStopCharge))) {
                            boolean result = sonicInterface.ReadCard(true, callbackInterface);

                            IsStopChargeTapCard = true;

                            if (result) {

                                Log.d("startChargeTime : ", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                                bundle.putString("startChargeTime", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                                StopChargeTapCardFragment fragment = new StopChargeTapCardFragment(this);

                                getSupportFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(R.id.fragmentContainer, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }else {
                            Toast.makeText(this, "Please unplug charger to stop charging", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        LogUtils.e(TAG, "ReadCard Exception: " + Log.getStackTraceString(e));
                    }
                } else if (chargingStationStatus.getText().toString().equals("BLOCKED")) {
                    Toast.makeText(this, "Please unplug charger", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Charging Station Not Available", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.connectorCard:
                IsStopChargeTapCard = false;

                TextView connector = view.findViewById(R.id.connector);

                TextView connectorStatus = view.findViewById(R.id.status);
                String connectorStatusText = connectorStatus.getText().toString().toUpperCase(Locale.ROOT);

                if (connectorStatusText.equals("AVAILABLE") || connectorStatusText.equals("PREPARING")) {
                    selectedConnectorIndex = getConnectorIndexByID(SelectedChargingStationComponent, Integer.valueOf(connector.getText().toString()));
//                selectedConnectorIndex = Integer.valueOf(connector.getText().toString()) - 1;

                    SelectedChargingStationComponent.SelectedConnector = selectedConnectorIndex;
                    replaceComponent(wbs.componentList, SelectedChargingStationComponent);

                    if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.IsSkipFare))) {
                        ShowHideTitle(true);
                        UpdateTitle("Key in Phone No");
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
                                .addToBackStack(null)
                                .commit();
                    } else {
                        UpdateTitle("Fare Rate");
                        bundle.putString("FareChargeText", SelectedChargingStationComponent.FareChargeText);
                        bundle.putString("FareChargeDescription", SelectedChargingStationComponent.FareChargeDescription);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, ChargingRateFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    }
                } else if (connectorStatusText.equals("BLOCKED")) {
                    Toast.makeText(this, "Please unplug charger", Toast.LENGTH_SHORT).show();
                } else if (connectorStatusText.equals("CHARGING")) {
                    try {
                        if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.EnableTapCardStopCharge))) {
                            boolean result = sonicInterface.ReadCard(true, callbackInterface);
                            IsStopChargeTapCard = true;
                            if (result) {

                                Log.d("startChargeTime : ", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                                bundle.putString("startChargeTime", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                                StopChargeTapCardFragment fragment = new StopChargeTapCardFragment(this);

                                getSupportFragmentManager().beginTransaction()
                                        .setReorderingAllowed(true)
                                        .replace(R.id.fragmentContainer, fragment)
                                        .addToBackStack(null)
                                        .commit();
                            }
                        }else {
                            Toast.makeText(this, "Please unplug charger to stop charging", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        LogUtils.e(TAG, "ReadCard Exception: " + Log.getStackTraceString(e));
                    }
                } else {
                    Toast.makeText(this, "Connector Not Available", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnNextChargingRate:
                ShowHideTitle(true);
                UpdateTitle("Key in Phone No");
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, PhoneNumberFragment.class, null)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.btnRateBack:
                if (wbs.componentList.length == 1) {
                    if (SelectedChargingStationComponent.Connectors.size() > 1) {
                        btnStartCharge.setVisibility(View.GONE);
                        UpdateTitle("Connectors");
                        selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                .addToBackStack(null)
                                .commit();
                    } else {
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                                .addToBackStack(null)
                                .commit();
                        btnStartCharge.setVisibility(View.VISIBLE);
                        ShowHideTitle(true);
                    }
                } else {
                    if (SelectedChargingStationComponent.Connectors.size() > 1) {
                        btnStartCharge.setVisibility(View.GONE);
                        UpdateTitle("Connectors");
                        selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                .addToBackStack(null)
                                .commit();
                    } else {
//                        selectChargerFragment = new SelectChargerFragment(wbs.componentList, selectedConnectorIndex);
                        ShowHideTitle(true);
                        UpdateTitle("Select Charger");
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, selectChargerFragment)
                                .addToBackStack(null)
                                .commit();
                    }
                }


                break;
            case R.id.btnStopChargeTapCardBack:
                try {
                    boolean r = sonicInterface.Abort();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (wbs.componentList.length == 1) {
                    if (isOneConnector) {
                        SimpleDateFormat originalFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
                        Date date = null;
                        try {
                            date = originalFormat.parse(String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        // Then format it to the desired format
                        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                        String formattedDate = targetFormat.format(date);

                        bundle.putString("StartChargeTime", formattedDate);
                        bundle.putString("HideStopButton", "false");
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, ChargingFragment.class, bundle)
                                .addToBackStack(null)
                                .commit();
                    } else {
//                            ShowHideTitle(true);
//                            getSupportFragmentManager().beginTransaction()
//                                    .setReorderingAllowed(true)
//                                    .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
//                                    .addToBackStack(null)
//                                    .commit();
                        ShowHideTitle(true);
                        UpdateTitle("Connectors");
                        selectConnectorFragment[0] = new SelectConnectorFragment(SelectedChargingStationComponent);
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, selectConnectorFragment[0])
                                .addToBackStack(null)
                                .commit();
                    }

                } else {
                    ShowHideTitle(true);
                    UpdateTitle("Select Charger");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, selectChargerFragment)
                            .addToBackStack(null)
                            .commit();
                }

                break;

            case R.id.btnBackConnector:
//                selectedConnectorIndex = 0;
                if (wbs.componentList.length == 1) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                            .addToBackStack(null)
                            .commit();
                    btnStartCharge.setVisibility(View.VISIBLE);
                } else {
//                    selectChargerFragment = new SelectChargerFragment(wbs.componentList, selectedConnectorIndex);
                    UpdateTitle("Select Charger");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, selectChargerFragment)
                            .addToBackStack(null)
                            .commit();
                }


                break;

            case R.id.btnStopCharge:
                try {
                    boolean result = sonicInterface.ReadCard(true, callbackInterface);

                    IsStopChargeTapCard = true;

//                    if (result) {

                    Log.d("startChargeTime : ", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                    bundle.putString("startChargeTime", String.valueOf(SelectedChargingStationComponent.StartChargeTime));
                    StopChargeTapCardFragment fragment = new StopChargeTapCardFragment(this);

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit();
//                    }
                } catch (RemoteException e) {
                    LogUtils.e(TAG, "ReadCard Exception: " + Log.getStackTraceString(e));
                }
                break;
            default:
                throw new RuntimeException("Unknown button ID");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d(TAG, "onKeyDown started...");
        LogUtils.d(TAG, "KeyCode: " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_F11) { // KEYCODE_F11 is the key value of SERVICE
            Toast.makeText(this, "Back button is clicked", Toast.LENGTH_SHORT).show();
            Router.startUri(this, RouterConst.CONFIG_LOGIN);
        }

        LogUtils.d(TAG, "onKeyDown ended.");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.d(TAG, "onResume");
        try {
            if (sonicInterface != null) {
                String SPServiceVersion = sonicInterface.GetSPServiceVersion();
                if (Utils.compareVersionStr(Utils.getServiceMinVersion(), SPServiceVersion.split("_")[0].replace("v", "")) > 0) {
                    sonicInterface.SetMaintenanceMode(true, callbackInterface);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "UPDATE REQUIRED");
                    bundle.putString("Message", "Please update SonicpayVS version");
                    bundle.putBoolean("StickPage", true);

                    LogUtils.i("onResume", "onResume");
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                } else {
                    sonicInterface.SetMaintenanceMode(false, callbackInterface);
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                            .addToBackStack(null)
                            .commit();
                }
            }
        } catch (RemoteException e) {
            LogUtils.e(TAG, "onResume Exception: " + Log.getStackTraceString(e));
        }
    }

    public void UpdateTitle(String title) {
        TextView textView = findViewById(R.id.header_title);
        textView.setText(title);
    }

    public void ShowHideTitle(boolean isShow) {
        TextView textView = findViewById(R.id.header_title);
        textView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        if (!isShow)
            textView.setText("");
    }

    public void UpdateTitle(String title, int fontSize) {
        TextView textView = findViewById(R.id.header_title);
        textView.setTextSize(fontSize);
        if (title != null)
            textView.setText(title);
    }

    public void UpdateTitleColor(@ColorRes int color) {
        TextView textView = findViewById(R.id.header_title);
        textView.setBackgroundColor(ContextCompat.getColor(this, color));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, color));
    }

    public void ShowFooter(boolean show) {
        if (show)
            findViewById(R.id.footer).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.footer).setVisibility(View.GONE);
    }

    public void UpdateStatus(String status) {
        Log.i(TAG, "UpdateStatus: ");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                txtStatus.setText(status);
            }
        });
    }

    public void UpdateChargePointStatus(eChargePointStatus status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (status) {
                    case Init:
                        btnStartCharge.setText("Connecting");
                        btnStartCharge.setClickable(false);
                        GeneralVariable.Status = "Connecting";
                        break;
                    case Idle:
                        btnStartCharge.setText("START");
                        btnStartCharge.setClickable(true);
                        btnStartCharge.setVisibility(View.VISIBLE);
                        GeneralVariable.Status = "Connected";
                        break;
                    case Disconnected:
                        btnStartCharge.setText("Disconnected");
                        btnStartCharge.setClickable(false);
                        btnStartCharge.setVisibility(View.VISIBLE);
                        GeneralVariable.Status = "Disconnected";

                        // Clear the back stack
                        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        // Replace the current fragment with the WelcomeFragment
                        getSupportFragmentManager().beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.fragmentContainer, WelcomeFragment.class, null)
                                .commit();

                        break;

                    case Charging:
                        UpdateTitle("Charging");
                        btnStartCharge.setVisibility(View.GONE);
                        GeneralVariable.Status = "Charging";
                        break;
                    case NotFound:
                        UpdateTitle("Component Not Found");
                        btnStartCharge.setVisibility(View.VISIBLE);
                        GeneralVariable.Status = "Not Found";
                        break;

                }
            }
        });


    }

    protected void ConnectService() {
        LogUtils.d(TAG, "ConnectService started...");
        Intent serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName(Utils.getServiceAppCode(), Utils.getServiceClassName()));

        bindService(serviceIntent, mConnection, BIND_AUTO_CREATE);

        LogUtils.d(TAG, "ConnectService ended.");
    }

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogUtils.d(TAG, "onServiceConnected");
            try {
                sonicInterface = IAIDLSonicpayInterface.Stub.asInterface(service);

                String SPServiceVersion = sonicInterface.GetSPServiceVersion();
                new SharedPrefUI(getApplicationContext()).WriteSharedPrefStr(getString(R.string.service_app_version), SPServiceVersion);
                LogUtils.i(TAG, "SP5 Service Version: " + SPServiceVersion);

                TextView spuiVersion = findViewById(R.id.spui_version);
                spuiVersion.setText("SonicpayVUI v" + new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.ui_app_version)).split("_")[0]);
                TextView spserviceVersion = findViewById(R.id.spservice_version);
                spserviceVersion.setText("SonicpayVS v" + SPServiceVersion.split("_")[0]);


                new Thread(() -> {
                    try {
                        mqtt = new MQTT();
                        mqtt.Init(getApplicationContext(), handler);
                        mqtt.SetServiceCallback(sonicInterface, callbackInterface);
                    } catch (Exception e) {
                        LogUtils.e(TAG, "MQTT Exception: " + Log.getStackTraceString(e));
                    }
                }).start();

                if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null)
                    getSupportFragmentManager().popBackStack();

                if (Utils.compareVersionStr(Utils.getServiceMinVersion(), SPServiceVersion.split("_")[0].replace("v", "")) > 0) {
                    sonicInterface.SetMaintenanceMode(true, callbackInterface);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "UPDATE REQUIRED");
                    bundle.putString("Message", "Please update SonicpayVS version");
                    bundle.putBoolean("StickPage", true);

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragmentContainer, WelcomeFragment.class, null)
                            .commit();
                }
                AutoSettlementHandler();
                new Thread(() -> {
                    wbs.connect();
                }).start();
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected: " + name.getClassName());
            unbindService(mConnection);


            ConnectService();

            int i = 2;
            while (i > 0) {
                try {
                    Thread.sleep(1000);
                    if (!isServiceRunning())
                        ConnectService();
                    i--;
                } catch (Exception e) {
                    LogUtils.e(TAG, "onServiceDisconnected Exception: " + Log.getStackTraceString(e));
                }
            }
        }
    };

    private boolean isServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : runningServices) {
            if ((serviceInfo.service.getClassName()).equals(Utils.getServiceClassName())) {
                LogUtils.i(TAG, "IsServiceRunning: true");
                return true;
            }
        }
        LogUtils.i(TAG, "IsServiceRunning: false");
        return false;
    }

    public String StartChargeTime;

    public void StartCharging(String time, String hideStopButton) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Bundle bundle = new Bundle();
                bundle.putString("StartChargeTime", time);
                bundle.putString("HideStopButton", hideStopButton);

//                StartChargeTime = time;
//                UpdateTitle("Charging");
//                UpdateTitleColor(R.color.main_blue);
//                ShowHideTitle(true);
//                btnStartCharge.setVisibility(View.GONE);
//                getSupportFragmentManager().beginTransaction()
//                        .setReorderingAllowed(true)
//                        .replace(R.id.fragmentContainer, PlugInToStartFragment.class, bundle)
//                        .addToBackStack(null)
//                        .commit();

                StartChargeTime = time;
                UpdateTitle("Charging");
                UpdateTitleColor(R.color.main_blue);
                ShowHideTitle(true);
                btnStartCharge.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ChargingFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();

            }
        });

    }

    public void ChangeToPluginFragment() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Bundle bundle = new Bundle();

//                LogUtils.i("wbs.componentList.length", wbs.componentList.length);
//                LogUtils.i("wbs.componentList[0].Connectors.size()", wbs.componentList[0].Connectors.size());

                if (wbs.componentList.length == 1 && wbs.componentList[0].Connectors.size() <= 1) {
                    LogUtils.i("StayOnFragment", "true");
                    bundle.putBoolean("StayOnFragment", true);
                } else {
                    LogUtils.i("StayOnFragment", "false");
                    bundle.putBoolean("StayOnFragment", false);
                }

//                    try {
//                        Thread.sleep(3000);
                UpdateTitle("Plug In To Charge");
                UpdateTitleColor(R.color.main_blue);
                ShowHideTitle(true);
                btnStartCharge.setVisibility(View.GONE);
                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, PlugInToStartFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

            }
        });

    }

    String SalesCompletionResult = "";

    public void SalesCompletion(double amount, String TransactionTrace, String TimeUse) {
        try {

            TotalAmountCharges = (int) (amount * 100);
            SalesCompletionResult = TimeUse;
            boolean SalesCompletionIsSuceess = sonicInterface.SalesCompletion((int) (amount * 100), TransactionTrace, callbackInterface);
            LogUtils.i("SalesCompletion Success", SalesCompletionIsSuceess);

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void AutoSettlementHandler() throws RemoteException {
        try {
            ArrayList<Date> settlementList = new ArrayList<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
            SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyMMdd HHmmss");
            if (new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.EnableAutoSettlement))) {
                String AutoSettlementTime = sonicInterface.ReadSharedPref(getString(R.string.AutoSettlementTime));
                if (!AutoSettlementTime.isEmpty()) {
                    String[] settlementTimeSchedule = AutoSettlementTime.split("\\|");
                    for (String st : settlementTimeSchedule) {

                        // check setting format validity
                        if (st.isEmpty() || st.length() < 6) {
                            // skip to next schedule setup
                            continue;
                        }

                        Date currentTime = new Date();
                        Date stTime = null;

                        stTime = datetimeFormat.parse(dateFormat.format(currentTime) + " " + st);

                        Calendar calSTTime = Calendar.getInstance();
                        calSTTime.setTime(stTime);

                        LogUtils.i(TAG, "[CheckSchedule] Settlement Setting: " + datetimeFormat.format(calSTTime.getTime()));

                        // add 1 day if schedule time is earlier than current time
                        if (calSTTime.getTime().compareTo(currentTime) < 0)
                            calSTTime.add(Calendar.DATE, 1);

                        LogUtils.i(TAG, "[CheckSchedule] Settlement Schedule: " + datetimeFormat.format(calSTTime.getTime()));

                        settlementList.add(calSTTime.getTime());
                        // only create the timer if not created yet


                    }
                }
                Date NextSettlementTime = getSmallestDate(settlementList);
                Timer settlementTimer = new Timer();
//                Date date= new Date();
//                date.setTime(date.getTime() + 10000);
                LogUtils.i(TAG, "[CheckSchedule] Next Settlement Schedule: " + NextSettlementTime);
                settlementTimer.schedule(new MyTimerTask(), NextSettlementTime);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public boolean IsSettlementRunning = false;

    public boolean TriggerSettlementHandler() {
        try {


            Date date = new Date();
            date.setTime(date.getTime() + 2000);
            Timer settlementTimer = new Timer();
            LogUtils.i(TAG, "[CheckSchedule1] Next Settlement Schedule: " + date);

            settlementTimer.schedule(new MyTimerTaskWithFixHost(), date);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }

    private Date getSmallestDate(List<Date> dateList) {
        if (dateList.isEmpty()) {
            throw new IllegalArgumentException("List of dates is empty");
        }

        Date smallestDate = dateList.get(0);

        for (Date date : dateList) {
            if (date.before(smallestDate)) {
                smallestDate = date;
            }
        }

        return smallestDate;
    }

    public class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Your task logic goes here

            IsSettlementRunning = true;
            LogUtils.i("SettlementTimer Running");
            try {
                GetStatusResult result = sonicInterface.getStatus();
                if (GeneralVariable.CurrentFragment.equals("WelcomeFragment")) {
                    StartSettlement();
                } else {
                    runSettlement = true;
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public class MyTimerTaskWithFixHost extends TimerTask {
        @Override
        public void run() {
            // Your task logic goes here
            IsSettlementRunning = true;
            LogUtils.i("SettlementTimer Running");
            try {
                GetStatusResult result = sonicInterface.getStatus();
                if (result.TerminalState == eTerminalState.Idle || result.TerminalState == eTerminalState.WaitingForCard) {
                    if (result.TerminalState == eTerminalState.WaitingForCard) {
                        boolean r = sonicInterface.Abort();
                    }
                } else {
                    StartSettlement();
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    public void StartSettlement() {

        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putString("StatusText", "Settlement...");

                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });

                    sonicInterface.Settlement(0, callbackInterface);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Bundle bundle = new Bundle();
                            bundle.putString("StatusText", "Settlement...");

                            getSupportFragmentManager().beginTransaction()
                                    .setReorderingAllowed(true)
                                    .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                                    .addToBackStack(null)
                                    .commit();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        };

        thread.start();


    }

    public void StopChargeTapCardErrorReceived(String StopChargeMsg) {
        StopChargeTapCardError stopChargeTapCardError = new Gson().fromJson(StopChargeMsg, StopChargeTapCardError.class);
        try {
            boolean r = sonicInterface.Abort();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        Bundle bundle = new Bundle();
        boolean isSuccess = false;
        bundle.putBoolean("IsSuccess", false);
        bundle.putString("Title", "Invalid Card");
        bundle.putString("Message", stopChargeTapCardError.CustumError);


//        LogUtils.i("StopChargeTapCardErrorReceived", "StopChargeTapCardErrorReceived");
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                .addToBackStack(null)
                .commit();
    }

    public void SalesCompletionError(String custumErrorMessage) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("IsSuccess", false);
        bundle.putString("Title", "ERROR");
        bundle.putString("Message", custumErrorMessage);

        LogUtils.i("SalesCompletionError", "SalesCompletionError");
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                .addToBackStack(null)
                .commit();
    }


    public Component GetSelectedComponent(String componentName, Component[] componentList) {
        for (Component component : componentList) {
            if (component.ComponentName.toUpperCase(Locale.ROOT).equals(componentName)) {
                return component;
            }
        }
        return null;
    }

    public Component[] replaceComponent(Component[] componentList, Component newComponent) {
        for (int i = 0; i < componentList.length; i++) {
            if (componentList[i].ComponentId == newComponent.ComponentId) { // Assuming getId() returns a unique identifier for each component
                componentList[i] = newComponent; // Replace the component with the new one
                break; // Exit the loop since we found and replaced the component
            }
        }
        return componentList;
    }

    public Component[] replaceComponentConnectors(Component[] componentList, String componentCode, List<Connector> newConnectors) {
        for (int i = 0; i < componentList.length; i++) {
            if (componentList[i].ComponentCode.equals(componentCode)) { // Assuming getId() returns a unique identifier for each component
                componentList[i].Connectors = newConnectors; // Replace the component with the new one
                break; // Exit the loop since we found and replaced the component
            }
        }
        return componentList;
    }

    public void UpdateConnectorStatus(String status, Component component, int connectorID) {
        // Check if the component is null to avoid NullPointerException
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        // Check if the Connectors list is null to avoid NullPointerException
        if (component.Connectors == null) {
            throw new IllegalArgumentException("Component's Connectors list cannot be null");
        }

        if (!component.Connectors.isEmpty()) {
            for (Connector connector : component.Connectors) {
                if (connectorID == connector.ConnectorId) {
                    connector.Status = status;
                    replaceComponent(wbs.componentList, component);
                    break;
                }
            }
        }
    }

    public void UpdateAllConnectorStatus(String status, Component component) {
        // Check if the component is null to avoid NullPointerException
        if (component == null) {
            throw new IllegalArgumentException("Component cannot be null");
        }

        // Check if the Connectors list is null to avoid NullPointerException
        if (component.Connectors == null) {
            throw new IllegalArgumentException("Component's Connectors list cannot be null");
        }

        if (!component.Connectors.isEmpty()) {
            for (Connector connector : component.Connectors) {
                if (!(connector.Status == null)) {
                    connector.Status = status;
                    replaceComponent(wbs.componentList, component);
                }
            }
        }
    }

    public int getConnectorIDByIndex(Component component, int connectorIndex) {
        return component.Connectors.get(connectorIndex).ConnectorId;
    }

    public int getConnectorIndexByID(Component component, int connectorID) {
        if (!component.Connectors.isEmpty()) {
            for (int i = 0; i < component.Connectors.size(); i++) {
                if (component.Connectors.get(i).ConnectorId == connectorID) {
                    return i;
                }
            }
        }
        return -1; // Return -1 if no matching ConnectorId is found
    }


    public Component GetSelectedComponentbyComponentCode(String componentCode, Component[] componentList) {
        for (Component component : componentList) {
            if (component.ComponentCode.equals(componentCode)) {
                return component;
            }
        }
        return null;
    }

}

