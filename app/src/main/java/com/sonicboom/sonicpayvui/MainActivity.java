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
import android.os.RemoteException;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
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
import com.sbs.aidl.Class.RefundResult;
import com.sbs.aidl.Class.SalesCompletionResult;
import com.sbs.aidl.Class.SalesResult;
import com.sbs.aidl.Class.SettlementResult;
import com.sbs.aidl.Class.VoidResult;
import com.sbs.aidl.Class.eCreditCardType;
import com.sbs.aidl.Class.eTerminalState;
import com.sbs.aidl.IAIDLCardCallbackInterface;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.models.GetStatus;
import com.sonicboom.sonicpayvui.models.DisplayInfo;
import com.sonicboom.sonicpayvui.models.SaleP1P2;
import com.sonicboom.sonicpayvui.models.Void;
import com.sonicboom.sonicpayvui.models.ParkingEntry;
import com.sonicboom.sonicpayvui.models.ParkingExit;
import com.sonicboom.sonicpayvui.models.PreAuth;
import com.sonicboom.sonicpayvui.models.ReadCard.ReadCardResponse;
import com.sonicboom.sonicpayvui.models.eQRType;
import com.sonicboom.sonicpayvui.models.eResult;
import com.sonicboom.sonicpayvui.models.Sale;
import com.sonicboom.sonicpayvui.models.SalesCompletion;
import com.sonicboom.sonicpayvui.models.Settlement;
import com.sonicboom.sonicpayvui.models.eStatusCode;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;
import com.sonicboom.sonicpayvui.models.eTngStatusCode;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.ScannerUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.transform.Result;

import cn.bingoogolapple.bgabanner.BGABanner;
import pl.droidsonroids.gif.GifImageView;

@RouterUri(path= {RouterConst.MAIN})
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "MainActivity";

    public Handler handler;
    public String mStr;
    public IAIDLSonicpayInterface sonicInterface;
    private GLTCPServer tcpServer;
    public ScannerUtils scannerUtils;
    private MQTT mqtt;

    private int TotalAmountCharges;
    private boolean IsInSalesP1 = false;

    public final IAIDLCardCallbackInterface callbackInterface = new IAIDLCardCallbackInterface.Stub() {

        @Override
        public void ReadCardCallback(ReadCardResult result) {
            LogUtils.i(TAG, "ReadCardCallback:" + new Gson().toJson(result));

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))){
                TCPGeneralMessage response = new TCPGeneralMessage();
                if(IsInSalesP1){
                    response.Command = "SaleP1";

                    SaleP1P2.SaleP1Response saleP1Response = new SaleP1P2().new SaleP1Response();
                    saleP1Response.IsValidCard = true;
                    ReadCardResponse readCardResp = new ReadCardResponse();
                    readCardResp.CardNo = result.CardNo;
                    readCardResp.CardType = result.CardType.getValue();
                    readCardResp.HashedPAN = result.Token;
                    readCardResp.CardStatus = result.CardStatus;
                    readCardResp.CardUID = result.CardUID;

                    saleP1Response.ReadCardResult =  readCardResp;

                    response.Data = saleP1Response;
                    IsInSalesP1 = false;
                }
                else {
                    response.Command = "ReadCard";

                    ReadCardResponse readCardResp = new ReadCardResponse();
                    readCardResp.CardNo = result.CardNo;
                    readCardResp.CardType = result.CardType.getValue();
                    readCardResp.HashedPAN = result.Token;
                    readCardResp.CardStatus = result.CardStatus;
                    readCardResp.CardUID = result.CardUID;

                    response.Data = readCardResp;
                }
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 :Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void ParkingEntryCallback(ParkingEntryResult result) {
            LogUtils.i(TAG, "ParkingEntryCallback:" + new Gson().toJson(result));

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));

                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");

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
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

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
                boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
                EBeepMode beepMode = isSuccess ? EBeepMode.FREQUENCE_LEVEL_3 : EBeepMode.FREQUENCE_LEVEL_5;
                App.dal.getSys().beep(beepMode, 100);

                if(scannerUtils != null)
                    scannerUtils.releaseRes();
                if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                    TCPGeneralMessage response = new TCPGeneralMessage();

                    if (tcpServer.IsParkingExit) {
                        tcpServer.IsParkingExit = false;
                        String operationType = sonicInterface.ReadSharedPref(getString(R.string.OperationType));
                        operationType = operationType == null || operationType.length() == 0 ? "Parking" : operationType;

                        response.Command = operationType + "Exit";

                        LogUtils.i(TAG, "SalesCallback: ParkingExit response");
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

                    } else {
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

                    String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                    GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                    tcpClient.SendMessage(new Gson().toJson(response));
                }
                Bundle bundle = new Bundle();
                //boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
                bundle.putBoolean("IsSuccess", isSuccess);
                bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
                bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
                if(!isSuccess)
                    bundle.putString("Message", "Error: " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" :eStatusCode.getDescFromCode(result.StatusCode)));
                bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");
                bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }
            catch (Exception e){
                LogUtils.e(TAG, "SalesCallback Exception: " + Log.getStackTraceString(e));
            }
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void StatusCallback(eTerminalState state, boolean cardPresent) {
            LogUtils.i(TAG, "StatusCallback:" + state + "("  + state.getValue() + ")" + " Card Present: " + cardPresent);

            // TODO: is GC and BC handle for SeePhone and Tap Again?
            if(state == eTerminalState.SeePhone || state == eTerminalState.TapAgain || state == eTerminalState.PresentOneCard){
                Bundle bundle = new Bundle();
                bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));
                bundle.putString("SalesRequest", new Gson().toJson(mStr));
                String message = "";
                if(state == eTerminalState.SeePhone)
                    message = "See Phone";
                if(state == eTerminalState.TapAgain)
                    message = "Tap Again";
                if(state == eTerminalState.PresentOneCard)
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
                bundle.putString("TitleText", "Enter PIN\r\nMYR " + String.format("%.2f", (double)TotalAmountCharges/100f));

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }

            if(state == eTerminalState.HostConnecting || state == eTerminalState.ChipCardReading || state == eTerminalState.ContactlessCardReading){
                Bundle bundle = new Bundle();
                bundle.putString("StatusText", state == eTerminalState.HostConnecting ? "Connecting host...": "Processing...");

                getSupportFragmentManager().beginTransaction()
                        .setReorderingAllowed(true)
                        .replace(R.id.fragmentContainer, ProgressFragment.class, bundle)
                        .addToBackStack(null)
                        .commit();
            }

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "GetStatus";

                GetStatus getStatus = new GetStatus();
                getStatus.Stage = String.valueOf(state.getValue());
                getStatus.CardPresent = cardPresent ? 1 : 0;

                response.Data = getStatus;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
        }

        @Override
        public void SettlementCallback(SettlementResult[] result) {
            LogUtils.i(TAG, "SettlementCallback:" + new Gson().toJson(result));
            boolean isSuccess = true;
            String errorCode = "SF";

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "Settlement";

                List<Settlement.SettlementResponse> settlementResponses = new ArrayList<>();
                for(SettlementResult hostResult : result){
                    Settlement.SettlementResponse settlementResponse = new Settlement.SettlementResponse();
                    settlementResponse.BatchNo = hostResult.BatchNo;
                    settlementResponse.StatusCode = hostResult.StatusCode;
                    settlementResponse.BatchCount = hostResult.BatchCount;
                    settlementResponse.HostNo = hostResult.HostNo;
                    settlementResponse.BatchAmount = hostResult.BatchAmount;
                    settlementResponse.RefundCount = hostResult.RefundCount;
                    settlementResponse.RefundAmount = hostResult.RefundAmount;
                    settlementResponses.add(settlementResponse);
                    if(!hostResult.StatusCode.equals(eStatusCode.Approved.getCode()) || !hostResult.StatusCode.equals(eTngStatusCode.No_Error.getCode())) {
                        isSuccess = false;
                        errorCode = hostResult.StatusCode;
                    }
                }

                response.Data = settlementResponses;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
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

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.Status.equals("00");
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putBoolean("IsTng", false);
            if(!isSuccess)
                bundle.putString("Message", "Error " + result.Status  + ": Transaction failed" );
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void SalesP1Callback(SalesResult result) throws RemoteException {
            LogUtils.i(TAG, "SalesP1Callback:" + new Gson().toJson(result));

            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            EBeepMode beepMode = isSuccess ? EBeepMode.FREQUENCE_LEVEL_3 : EBeepMode.FREQUENCE_LEVEL_5;
            App.dal.getSys().beep(beepMode, 100);

            if(scannerUtils != null)
                scannerUtils.releaseRes();

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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
                }
                else {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 :Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }

            Bundle bundle = new Bundle();
            //boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error: " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" : eStatusCode.getDescFromCode(result.StatusCode)));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

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

            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            EBeepMode beepMode = isSuccess ? EBeepMode.FREQUENCE_LEVEL_3 : EBeepMode.FREQUENCE_LEVEL_5;
            App.dal.getSys().beep(beepMode, 100);

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                TCPGeneralMessage response = new TCPGeneralMessage();
                response.Command = "PreAuth";

                PreAuth.PreAuthResponse preAuthResponse = new PreAuth.PreAuthResponse();
                preAuthResponse.StatusCode = result.StatusCode;
                preAuthResponse.SystemId = result.SystemId;
                preAuthResponse.CardNo = result.CardNo;
                preAuthResponse.HashedPAN = result.Token;
                preAuthResponse.ApprovalCode = result.emvInfo.ApprovalCode;
                preAuthResponse.RRN = result.emvInfo.RRN;
                preAuthResponse.TransactionTrace = result.emvInfo.TransactionTrace;
                preAuthResponse.BatchNo = result.emvInfo.BatchNo;
                preAuthResponse.HostNo = result.emvInfo.HostNo;
                preAuthResponse.TerminalId = result.emvInfo.TerminalId;
                preAuthResponse.MerchantId = result.emvInfo.MerchantId;
                preAuthResponse.AID = result.emvInfo.AID;
                preAuthResponse.TC = result.emvInfo.TC;
                preAuthResponse.CardHolderName = result.emvInfo.CardHolderName;
                preAuthResponse.CardType = String.valueOf(result.CardType.getValue());
                preAuthResponse.CardLabel = result.emvInfo.CardLabel;
                preAuthResponse.InvoiceNo = result.emvInfo.InvoiceNo;
                preAuthResponse.TVR = result.emvInfo.TVR;

                response.Data = preAuthResponse;
                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }

            Bundle bundle = new Bundle();
            //boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" :result.StatusCode ));
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void SalesCompletionCallback(SalesCompletionResult result) throws RemoteException {
            LogUtils.i(TAG, "SalesCompletionCallback:" + new Gson().toJson(result));
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Sales completion successful": "Sales completion failed");
            if(!isSuccess)
                bundle.putString("Message", "Error " +  " : " + result.StatusCode);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void VoidCallback(VoidResult result) throws RemoteException {
            LogUtils.i(TAG, "VoidCallback:" + new Gson().toJson(result));
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Transaction is voided successfully": "Transaction fail to void");
            if(!isSuccess)
                bundle.putString("Message", "Error " +  " : " + result.StatusCode);

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
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }

            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Unsuccessful");
            bundle.putString("Message", isSuccess ? "Transaction is refunded successfully": "Transaction fail to refund");
            if(!isSuccess)
                bundle.putString("Message", "Error " +  " : " + result.StatusCode);

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void FareBasedEntryCallback(FareBasedEntryResult result) throws RemoteException {
            LogUtils.i(TAG, "FareBasedEntryCallback:" + new Gson().toJson(result));

            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            EBeepMode beepMode = isSuccess ? EBeepMode.FREQUENCE_LEVEL_3 : EBeepMode.FREQUENCE_LEVEL_5;
            App.dal.getSys().beep(beepMode, 100);

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            //boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        private void FareBasedExitReply(FareBasedExitResult result) throws RemoteException {
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

            String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
            GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
            tcpClient.SendMessage(new Gson().toJson(response));
        }

        @Override
        public void FareBasedExitCallback(FareBasedExitResult result) throws RemoteException {
            LogUtils.i(TAG, "FareBasedExitCallback:" + new Gson().toJson(result));
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            EBeepMode beepMode = isSuccess ? EBeepMode.FREQUENCE_LEVEL_3 : EBeepMode.FREQUENCE_LEVEL_5;
            App.dal.getSys().beep(beepMode, 100);
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                if (isSuccess)
                    FareBasedExitReply(result);
            }
            Bundle bundle = new Bundle();
            //boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();

            // no swap card for status TA
            if(!isSuccess && !result.StatusCode.equals(eStatusCode.Transaction_Aborted.getCode())) {
                // allow swap card if first payment declined
                LogUtils.i(TAG, "ParkingExit Swap Card");
                new Thread(() -> {
                    try {
                        int waitingDuration = 0;
                        boolean ready = false;
                        do {
                            Thread.sleep(500);
                            LogUtils.i(TAG, "GetStatus...");
                            GetStatusResult status = tcpServer.GetStatus();
                            LogUtils.i(TAG, "GetStatus done");
                            if (status != null && status.TerminalState == eTerminalState.Idle)
                                ready = true;

                            waitingDuration += 500;
                            if (waitingDuration == 1500)
                                break;

                        } while (!ready);

                        if (ready) {
                            tcpServer.IsParkingExit = true;
                            tcpServer.Sale();
                        } else {
                            FareBasedExitReply(result);
                        }

                    } catch (Exception e) {}
                }).start();
            }
        }

        @Override
        public void MaxChargedEntryCallback(MaxChargedEntryResult result) throws RemoteException {
            LogUtils.i(TAG, "MaxChargedEntryCallback:" + new Gson().toJson(result));

            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Card Accepted" : "Unsuccessful");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : ": Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }

        @Override
        public void MaxChargedExitCallback(MaxChargedExitResult result) throws RemoteException {
            LogUtils.i(TAG, "MaxChargedExitCallback:" + new Gson().toJson(result));
            if(new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
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

                String controllerIP = new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip)) == null ? "192.168.1.1" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_ip));
                GLTCPClient tcpClient = new GLTCPClient(getApplicationContext(), controllerIP, new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port)) == null ? 9000 : Integer.parseInt(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_controller_port))));
                tcpClient.SendMessage(new Gson().toJson(response));
            }
            Bundle bundle = new Bundle();
            boolean isSuccess = result.StatusCode.equals(eTngStatusCode.No_Error.getCode()) || result.StatusCode.equals(eStatusCode.Approved.getCode());
            bundle.putBoolean("IsSuccess", isSuccess);
            bundle.putString("Title", isSuccess ? "Successful" : "Card Declined");
            bundle.putBoolean("IsTng", result.CardType == eCreditCardType.TNGCard);
            if(!isSuccess)
                bundle.putString("Message", "Error " + (result.CardType == eCreditCardType.TNGCard ? eTngStatusCode.fromCode(String.valueOf(result.StatusCode)) != null ? (Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getCode() + " " + Objects.requireNonNull(eTngStatusCode.fromCode(String.valueOf(result.StatusCode))).getDesc()) : " : Unknown error" :result.StatusCode ));
            bundle.putString("Balance", result.tnginfo != null ? String.format("%.2f", ((double)result.tnginfo.CardBalance/100f)) : "0.00");
            bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.footer).setVisibility(View.VISIBLE);

        Bundle bundleInit = new Bundle();
        bundleInit.putString("TitleText", "Please Wait");
        bundleInit.putString("StatusText", "System Init...");
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragmentContainer, ProgressFragment.class, bundleInit)
                .addToBackStack(null)
                .commit();

        // v1.0.10 temporary fix to prevent screen off after certain period (wait til PAX release new firmware to handle this)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ConnectService();

        handler = new Handler(message -> {
            String input = (String) message.obj;

            if(Objects.equals(input, eResult.NAK.toString())){
                Bundle bundle = new Bundle();
                bundle.putBoolean("IsSuccess", false);
                bundle.putString("Title", "Cancelled");
                bundle.putString("Message", "Transaction is aborted.");

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
                resultFragment = (ResultFragment)fragment;
                resultFragment.stopAutoRedirectionToIdlePage();
            }

            boolean isCommandMatched = true;

            TCPGeneralMessage receiveMessage = new Gson().fromJson(input, TCPGeneralMessage.class);
            switch(receiveMessage.Command){
                case "ReadCard":
                    mStr = null;
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
                    bundle.putString("Amount", String.format("%.2f", (double)preAuthRequest.Amount/100f));

                    mStr = null;
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                    break;
                case "SaleP1":
                    IsInSalesP1 = true;
                case "Sale":
                //case "SaleCompletion": // now only for certification
                    Sale.SaleRequest saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Sale.SaleRequest.class);
                    TotalAmountCharges = saleRequest.Amount;

                    bundle = new Bundle();
                    bundle.putString("Amount", String.format("%.2f", (double)saleRequest.Amount/100f));
                    bundle.putString("SalesRequest", new Gson().toJson(saleRequest));

                    if (tcpServer.IsParkingExit)
                        bundle.putString("TapCardMsg", "Tap Other Card");

                    // clear previous generated QR if any
                    mStr = null;
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
                case "SaleCompletion":
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
                        bundle.putString("Amount", String.format("%.2f", (double)refundRequest.Amount/100f));
                        bundle.putString("SalesRequest", new Gson().toJson(refundRequest));
                        mStr = null;
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
                    bundle.putString("Amount", String.format("%.2f", (double)TotalAmountCharges/100f));

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, TapCardFragment.class, bundle)
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
                        if(currentFragment.getClass() == TapCardFragment.class) {

                            View view = currentFragment.getView();
                            assert view != null;
                            if(!qrResponse.QRCode.isEmpty()) {
                                TextView gifTitle = view.findViewById(R.id.gifTitle);
                                gifTitle.setText("Scan to pay");
                            }

                            TextView qrTitle = view.findViewById(R.id.qrTitle);
                            qrTitle.setVisibility(View.VISIBLE);
                            qrTitle.setText(qrResponse.QRName);

                            //GifImageView imageView = view.findViewById(R.id.tapCard);
                            ImageView imageView = view.findViewById(R.id.tapCard);
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                            ((TapCardFragment)currentFragment).StopSchemeLogoAutoPlay();

                            boolean isQRAvailable = false;
                            if(qrResponse.QRCode.startsWith("http")){
                                boolean isImage = qrResponse.QRCode.endsWith(".png") ||qrResponse.QRCode.endsWith(".jpg") ||qrResponse.QRCode.endsWith(".jpeg");
                                ((TapCardFragment) currentFragment).mQRContentType = isImage ? "image" : "";
                                if(isImage)
                                    new Utils.DownloadImageTask(imageView).execute(qrResponse.QRCode);
                                else
                                    imageView.setImageBitmap(Utils.generateQRfromStr(this, qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue() || qrResponse.QRType == eQRType.MaybankPay.getValue()));

                                isQRAvailable = true;
                            }
                            else {
                                if(!qrResponse.QRCode.equals("")) {
                                    imageView.setImageBitmap(Utils.generateQRfromStr(this, qrResponse.QRCode, qrResponse.QRType == eQRType.DuitNow.getValue() || qrResponse.QRType == eQRType.MaybankPay.getValue()));
                                    isQRAvailable = true;
                                }
                            }
                            if(qrResponse.qrList != null && qrResponse.qrList.length > 1){
                                TextView moreQR = view.findViewById(R.id.moreQROptions);
                                moreQR.setVisibility(View.VISIBLE);
                            }

                            if (!isQRAvailable)
                                ((TapCardFragment)currentFragment).ShowSchemeLogo(view);
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
                    bundle.putString("Title","Maintenance");
                    bundle.putString("Message", "Under maintenance");
                    bundle.putBoolean("StickPage", true);

                    String currentActivity = "";
                    ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                    if (activityManager != null) {
                        ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
                        currentActivity = componentName.getClassName();
                    }
                    if(currentActivity.equals("com.sonicboom.sonicpayvui.MainActivity")) {
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
        LogUtils.d(TAG, "onCreate ended.");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(mConnection);
        LogUtils.d(TAG, "onDestroy");
        tcpServer.stop();
    }

    @Override
    public void onClick(View view) {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtils.d(TAG, "onKeyDown started...");
        LogUtils.d(TAG, "KeyCode: " + keyCode);

        if(keyCode==KeyEvent.KEYCODE_F11 || keyCode==KeyEvent.KEYCODE_MOVE_HOME){ // KEYCODE_F11 is the key value of SERVICE, KEYCODE_MOVE_HOME is the key value of SERVICE for IM30 V2
            Toast.makeText(this, "Back button is clicked", Toast.LENGTH_SHORT).show();
            Router.startUri(this, RouterConst.CONFIG_LOGIN);
        }

        LogUtils.d(TAG, "onKeyDown ended.");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume(){
        super.onResume();
        LogUtils.d(TAG, "onResume");
        try {
            if(sonicInterface != null){
                String SPServiceVersion = sonicInterface.GetSPServiceVersion();
                if(Utils.compareVersionStr(Utils.getServiceMinVersion(), SPServiceVersion.split("_")[0].replace("v","")) > 0) {
                    sonicInterface.SetMaintenanceMode(true, callbackInterface);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title", "UPDATE REQUIRED");
                    bundle.putString("Message", "Please update SonicpayVS version");
                    bundle.putBoolean("StickPage", true);

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .addToBackStack(null)
                            .commit();
                }
                else {
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

    public void UpdateTitle(String title){
        TextView textView = findViewById(R.id.header_title);
        textView.setText(title);
    }

    public void ShowHideTitle(boolean isShow){
        TextView textView = findViewById(R.id.header_title);
        textView.setVisibility(isShow ? View.VISIBLE : View.GONE);
        if(!isShow)
            textView.setText("");
    }

    public void UpdateTitle(String title, int fontSize){
        TextView textView = findViewById(R.id.header_title);
        textView.setTextSize(fontSize);
        if (title != null)
            textView.setText(title);
    }

    public void UpdateTitleColor(@ColorRes int color){
        TextView textView = findViewById(R.id.header_title);
        textView.setBackgroundColor(ContextCompat.getColor(this, color));

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, color));
    }

    public void ShowFooter (boolean show){
        if(show)
            findViewById(R.id.footer).setVisibility(View.VISIBLE);
        else
            findViewById(R.id.footer).setVisibility(View.GONE);
    }

    protected void ConnectService(){
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

                if(tcpServer == null && new SharedPrefUI(getApplicationContext()).ReadSharedPrefBoolean(getString(R.string.enable_tcp))) {
                    tcpServer = new GLTCPServer(getApplicationContext(), handler, sonicInterface, callbackInterface, Integer.parseInt(Objects.equals(new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_listener_port)), "") ? "9000" : new SharedPrefUI(getApplicationContext()).ReadSharedPrefStr(getString(R.string.tcp_listener_port))));
                    tcpServer.start();
                }
                else
                    LogUtils.i(TAG, "TCP is not enabled");

                new Thread(() -> {
                    try {
                        mqtt = new MQTT();
                        mqtt.Init(getApplicationContext(), handler);
                        mqtt.SetServiceCallback(sonicInterface, callbackInterface);
                    }
                    catch(Exception e){
                        LogUtils.e(TAG, "MQTT Exception: " + Log.getStackTraceString(e));
                    }
                }).start();

                if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) != null)
                    getSupportFragmentManager().popBackStack();

                if(Utils.compareVersionStr(Utils.getServiceMinVersion(), SPServiceVersion.split("_")[0].replace("v","")) > 0){
                    sonicInterface.SetMaintenanceMode(true, callbackInterface);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("IsSuccess", false);
                    bundle.putString("Title","UPDATE REQUIRED");
                    bundle.putString("Message", "Please update SonicpayVS version");
                    bundle.putBoolean("StickPage", true);

                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragmentContainer, ResultFragment.class, bundle)
                            .commit();
                }
                else {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .add(R.id.fragmentContainer, WelcomeFragment.class, null)
                            .commit();
                }
            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.d(TAG, "onServiceDisconnected: " + name.getClassName());
            unbindService(mConnection);
            if(tcpServer != null) {
                tcpServer.stop();
                tcpServer = null;
            }

            ConnectService();

            int i = 2;
            while(i > 0) {
                try {
                    Thread.sleep(1000);
                    if(!isServiceRunning())
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
                LogUtils.i(TAG, "IsServiceRunning: true" );
                return true;
            }
        }
        LogUtils.i(TAG, "IsServiceRunning: false" );
        return false;
    }
}