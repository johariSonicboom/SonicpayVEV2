package com.sonicboom.sonicpayvui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.pax.dal.entity.EBeepMode;
import com.pax.gl.commhelper.IComm;
import com.pax.gl.commhelper.IServer;
import com.pax.gl.commhelper.exception.CommException;
import com.pax.gl.commhelper.impl.GLCommDebug;
import com.pax.gl.commhelper.impl.PaxGLComm;
import com.sbs.aidl.Class.GetStatusResult;
import com.sbs.aidl.Class.ParkingEntryRequest;
import com.sbs.aidl.Class.ParkingExitRequest;
import com.sbs.aidl.Class.QRResponse;
import com.sbs.aidl.Class.ReadCardResult;
import com.sbs.aidl.Class.SalesResult;
import com.sbs.aidl.Class.eAction;
import com.sbs.aidl.Class.eCreditCardType;
import com.sbs.aidl.IAIDLCardCallbackInterface;
import com.sbs.aidl.IAIDLSonicpayInterface;
import com.sonicboom.sonicpayvui.models.DisplayInfo;
import com.sonicboom.sonicpayvui.models.GetLastTransaction;
import com.sonicboom.sonicpayvui.models.GetStatus;
import com.sonicboom.sonicpayvui.models.ParkingEntry;
import com.sonicboom.sonicpayvui.models.ParkingExit;
import com.sonicboom.sonicpayvui.models.PreAuth;
import com.sonicboom.sonicpayvui.models.ReadCard;
import com.sonicboom.sonicpayvui.models.SaleP1P2;
import com.sonicboom.sonicpayvui.models.eResult;
import com.sonicboom.sonicpayvui.models.Sale;
import com.sonicboom.sonicpayvui.models.SalesCompletion;
import com.sonicboom.sonicpayvui.models.SerialNo;
import com.sonicboom.sonicpayvui.models.Settlement;
import com.sonicboom.sonicpayvui.models.TCPGeneralMessage;
import com.sonicboom.sonicpayvui.models.Void;
import com.sonicboom.sonicpayvui.utils.LogUtils;
import com.sonicboom.sonicpayvui.utils.Utils;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class GLTCPServer {
    private static final String TAG = "GLTCPServer";

    private IServer iServer;
    private final Context context;

    private Handler scHandler;
    private IAIDLSonicpayInterface spServiceInterface;
    private IAIDLCardCallbackInterface spServiceCallback;
    private ParkingExit.ParkingExitRequest parkingExitReq = null;

    public boolean IsParkingExit = false;

    public GLTCPServer(Context context, Handler handler, IAIDLSonicpayInterface sonicpayInterface, IAIDLCardCallbackInterface callbackInterface, int port){
        //GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_ALL);
        GLCommDebug.setDebugLevel(GLCommDebug.EDebugLevel.DEBUG_LEVEL_E);
        scHandler = handler;
        spServiceInterface = sonicpayInterface;
        spServiceCallback = callbackInterface;
        this.context = context;

        if (iServer==null) {
            iServer = PaxGLComm.getInstance(context).createTcpServer(port, 5, new IServer.ITcpServerListener() {

                @Override
                public void onStarted(List<String> localAddresses, int port) {
                    LogUtils.d(TAG, "onStarted: " + String.join(",", localAddresses) + ":" + port);
                }

                @Override
                public void onPeerConnected(IComm comm, Socket socket) {
                    LogUtils.i(TAG, "onPeerConnected:" + socket.getInetAddress().getHostAddress());
                    try {
                        new Thread(() -> {
                            String receiveMsg = receive(comm);

                            TCPGeneralMessage receiveMessage = new Gson().fromJson(receiveMsg, TCPGeneralMessage.class);

                            TCPGeneralMessage response = new TCPGeneralMessage();
                            response.Command = receiveMessage.Command;

                            Utils.LinkedTreeCastDoubleToInt((LinkedTreeMap<String, Object>) receiveMessage.Data);

                            String dataToHashed = receiveMessage.Command + (receiveMessage.Data == null ? receiveMessage.Result == null ? "" : receiveMessage.Result : new Gson().toJson(receiveMessage.Data));
                            LogUtils.i(TAG, "Data to be hashed: " + dataToHashed);

                            String hash = Utils.md5(dataToHashed);
                            LogUtils.i(TAG, "Expected checksum: " + hash);

                            String currentActivity = "";
                            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                            if (activityManager != null) {
                                ComponentName componentName = activityManager.getRunningTasks(1).get(0).topActivity;
                                currentActivity = componentName.getClassName();
                            }

                            if (!hash.equals(receiveMessage.Checksum)) {
                                response.Result = eResult.NAK.toString();
                                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));
                                send(comm, new Gson().toJson(response));
                                disconnect(comm);

                                //Notify UI to change to Welcome screen
                                Message message = handler.obtainMessage();
                                message.obj = "NAK";
                                handler.sendMessage(message);

                                return;
                            }

                            if(!currentActivity.equals("com.sonicboom.sonicpayvui.MainActivity")) {//|| (inProcess && !receiveMessage.Command.equals("Abort"))){
                                response.Result = eResult.NAK.toString();
                                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));
                                send(comm, new Gson().toJson(response));
                                disconnect(comm);
                                return;
                            }

                            IsParkingExit = false;

                            assert sonicpayInterface != null;
                            switch (receiveMessage.Command) {
                                case "GetStatus":
                                    try {
                                        GetStatusResult result = sonicpayInterface.getStatus();
                                        GetStatus getStatus = new GetStatus();
                                        getStatus.Stage = String.valueOf(result.TerminalState.getValue());
                                        getStatus.CardPresent = result.IsCardPresent ? 1 :0;
                                        response.Data = getStatus;

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "GetStatus Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "GetSerialNo":
                                    SerialNo serialNo = new SerialNo();
                                    serialNo.SerialNo = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.serial_number));
                                    response.Data = serialNo;
                                    break;
                                case "Settlement":
                                    try {
                                        //Get host number
                                        Settlement.SettlementRequest settlementRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Settlement.SettlementRequest.class);
                                        LogUtils.i(TAG, "Settlement for Host: " + settlementRequest.HostNo);

                                        boolean result = sonicpayInterface.Settlement(settlementRequest.HostNo, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();
                                        if(result){
                                            //Notify UI to change to Processing screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "Settlement Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "ReadCard":
                                    try {
                                        ReadCard.ReadCardRequest readCardRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), ReadCard.ReadCardRequest.class);

                                        boolean result = sonicpayInterface.ReadCard(readCardRequest.EmvPAN, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();
                                        if(result){
                                            //Notify UI to change to TapCard screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "ReadCard Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "PreAuth":
                                    try {
                                        PreAuth.PreAuthRequest preAuthRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), PreAuth.PreAuthRequest.class);
                                        boolean result = sonicpayInterface.PreAuth(preAuthRequest.Amount, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

                                        if(result){
                                            //Notify UI to change to Progress screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "PreAuth Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "SaleCompletion":
                                    try {
                                        SalesCompletion.SalesCompletionRequest salesCompletionRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), SalesCompletion.SalesCompletionRequest.class);
                                        boolean result = sonicpayInterface.SalesCompletion(salesCompletionRequest.Amount, salesCompletionRequest.TransactionTrace, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

                                        if(result){
                                            //Notify UI to change to Progress screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "SalesCompletion Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "Sale":
                                    try {
                                        Sale.SaleRequest saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Sale.SaleRequest.class);
                                        //TODO: remove QRReferenceNumber
                                        boolean result = sonicpayInterface.Sales(saleRequest.Amount, "", saleRequest.TxnId, saleRequest.Reference, saleRequest.Reserved, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

                                        if(result) {
                                            //Notify UI to change to Tap Card screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                        if (result && new SharedPrefUI(context).ReadSharedPrefBoolean(context.getString(R.string.enable_auto_request_qr)) && sonicpayInterface.ReadSharedPrefBoolean(context.getString(R.string.IsQRUserScanEnabled))) {
                                            new Thread(()->{
                                                try {
                                                    QRResponse qrResponse = sonicpayInterface.QRRequest(saleRequest.Amount, 0, callbackInterface);
                                                    LogUtils.i(TAG, "QRRequest Response:" + new Gson().toJson(qrResponse));

                                                    TCPGeneralMessage qrResponseMsg = new TCPGeneralMessage();
                                                    qrResponseMsg.Command = "QRRequest";
                                                    qrResponseMsg.Data = qrResponse;

                                                    //Notify UI to change to Tap Card screen
                                                    Message message = handler.obtainMessage();
                                                    message.obj = new Gson().toJson(qrResponseMsg);
                                                    handler.sendMessage(message);

                                                } catch (Exception e) {
                                                    LogUtils.e(TAG, "QRRequest Exception: " + Log.getStackTraceString(e));
                                                }
                                            }).start();
                                        }

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "Sale Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "SaleP1":
                                    try {
                                        Sale.SaleRequest saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Sale.SaleRequest.class);
                                        boolean result = sonicpayInterface.SalesP1(saleRequest.Amount, saleRequest.TxnId, saleRequest.Reference, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

                                        if(result) {
                                            //Notify UI to change to Tap Card screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "SaleP1 Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "SaleP2":
                                    try {
                                        SaleP1P2.SaleP2Request saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), SaleP1P2.SaleP2Request.class);
                                        boolean result = sonicpayInterface.SalesP2(eAction.fromId(saleRequest.Action));
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "SaleP2 Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "Abort":
                                    try{
                                        boolean result = sonicpayInterface.Abort();
                                        response.Result = result ? eResult.ACK.toString(): eResult.NAK.toString();
                                        if(result){
                                            //Notify UI to change to Result screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch(Exception e){
                                        LogUtils.e(TAG, "Abort Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "Void":
                                    try{
                                        Void.VoidRequest voidRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Void.VoidRequest.class);

                                        boolean result = sonicpayInterface.Void(voidRequest.Amount, voidRequest.TransactionTrace, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString(): eResult.NAK.toString();
                                        if(result) {//
                                            //Notify UI to change to Process screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch(Exception e){
                                        LogUtils.e(TAG, "Void Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "Refund":
                                    try{
                                        Void.VoidRequest refundRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Void.VoidRequest.class);
                                        boolean result = sonicpayInterface.Refund(refundRequest.Amount, refundRequest.TransactionTrace, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString(): eResult.NAK.toString();
                                        if(result) {//
                                            //Notify UI to change to Tap Card/Process screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch(Exception e){
                                        LogUtils.e(TAG, "Refund Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "GetLastTransaction":
                                    try{
                                        GetLastTransaction.GetLastTransactionRequest getLastTransactionRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), GetLastTransaction.GetLastTransactionRequest.class);
                                        SalesResult salesResult = sonicpayInterface.getLastTransaction(getLastTransactionRequest.TxnId);
                                        GetLastTransaction.GetLastTransactionResponse getLastTransactionResponse = new GetLastTransaction.GetLastTransactionResponse();
                                        getLastTransactionResponse.TxnStatus = salesResult.StatusCode;
                                        getLastTransactionResponse.SystemId = salesResult.SystemId;
                                        getLastTransactionResponse.PaymentType = String.valueOf(salesResult.CardType.getValue());
                                        getLastTransactionResponse.HashedPAN = salesResult.Token;
                                        getLastTransactionResponse.CardNo = salesResult.CardNo;
                                        getLastTransactionResponse.EMVInfo = salesResult.emvInfo;
                                        getLastTransactionResponse.QRInfo = salesResult.qrInfo;
                                        getLastTransactionResponse.TNGInfo = salesResult.tnginfo;

                                        response.Data = getLastTransactionResponse;
                                    } catch(Exception e){
                                        LogUtils.e(TAG, "GetLastTransaction Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "ParkingEntry":
                                    try {
                                        //TODO: can't just return NAK, ui screen is hang at please tap
                                        String operationType = sonicpayInterface.ReadSharedPref(context.getString(R.string.OperationType));
                                        if(!operationType.equalsIgnoreCase("Parking")){
                                            response.Result = eResult.NAK.toString();
                                            response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));
                                            send(comm, new Gson().toJson(response));
                                            disconnect(comm);
                                            return;
                                        }

                                        ParkingEntry.ParkingEntryRequest parkingEntryRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), ParkingEntry.ParkingEntryRequest.class);
                                        ReadCardResult readCardResult = new ReadCardResult();
                                        readCardResult.CardNo = parkingEntryRequest.CardNo;
                                        Optional<eCreditCardType> creditCardType = Arrays.stream(eCreditCardType.values()).filter(x-> x.getValue() == Integer.parseInt(parkingEntryRequest.CardType)).findFirst();
                                        creditCardType.ifPresent(eCreditCardType -> readCardResult.CardType = eCreditCardType);
                                        readCardResult.Token = parkingEntryRequest.HashedPAN;

                                        ParkingEntryRequest request = new ParkingEntryRequest(readCardResult, Integer.parseInt(parkingEntryRequest.MinTngBalance), parkingEntryRequest.EntryTime);

                                        boolean result = false;

                                        String fareMode = new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.fare_mode));
                                        if(fareMode == null || fareMode.equals(""))
                                            fareMode = "FareBased";

                                        if(fareMode.equals("FareBased")){
                                            result = sonicpayInterface.FareBasedEntry(new Gson().toJson(request), callbackInterface);
                                        }

                                        if(fareMode.equals("MaxCharged")){
                                            result = sonicpayInterface.MaxChargedEntry(new Gson().toJson(request), callbackInterface);
                                        }

                                        //result = sonicpayInterface.ParkingEntry(readCardResult, Integer.parseInt(parkingEntryRequest.MinTngBalance), parkingEntryRequest.EntryTime, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();
                                        if(result){
                                            //Notify UI to change to Process screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        LogUtils.e(TAG, "ParkingEntry Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "ParkingExit":
                                    try{
                                        if(!sonicpayInterface.ReadSharedPref(context.getString(R.string.OperationType)).equals("Parking")){
                                            response.Result = eResult.NAK.toString();
                                            response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));
                                            send(comm, new Gson().toJson(response));
                                            disconnect(comm);
                                            return;
                                        }

                                        ParkingExit.ParkingExitRequest parkingExitRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), ParkingExit.ParkingExitRequest.class);
                                        parkingExitReq = parkingExitRequest; // keep for swap card usage

                                        ReadCardResult readCardResult = new ReadCardResult();
                                        readCardResult.CardNo = parkingExitRequest.CardNo;
                                        Optional<eCreditCardType> creditCardType = Arrays.stream(eCreditCardType.values()).filter(x-> x.getValue() == parkingExitRequest.CardType).findFirst();
                                        creditCardType.ifPresent(eCreditCardType -> readCardResult.CardType = eCreditCardType);
                                        readCardResult.Token = parkingExitRequest.HashedPAN;

                                        ParkingExitRequest request = new ParkingExitRequest();
                                        request.ReadCardResult = readCardResult;
                                        request.TotalAmount = parkingExitRequest.TotalAmount;
                                        request.FareAmount = parkingExitRequest.FareAmount;
                                        request.SurchargeAmount = parkingExitRequest.SurchargeAmount;
                                        request.SurchargeTaxAmount = parkingExitRequest.SurchargeTaxAmount;
                                        request.TaxAmount = parkingExitRequest.TaxAmount;
                                        request.PreValidationAmount = parkingExitRequest.PreValidationAmount;
                                        request.EntryTime = parkingExitRequest.EntryTime;
                                        request.ExitTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                                        request.TxnId = parkingExitRequest.TxnId;

                                        boolean result = false;
                                        if(new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.fare_mode)).equals("FareBased")){
                                            result = sonicpayInterface.FareBasedExit(new Gson().toJson(request), callbackInterface);
                                        }

                                        if(new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.fare_mode)).equals("MaxCharged")){
                                            result = sonicpayInterface.MaxChargedExit(new Gson().toJson(request), callbackInterface);
                                        }

                                        //boolean result = sonicpayInterface.ParkingExit(readCardResult, parkingExitRequest.TotalAmount, parkingExitRequest.FareAmount, parkingExitRequest.SurchargeAmount, parkingExitRequest.TaxAmount, parkingExitRequest.SurchargeTaxAmount, parkingExitRequest.PreValidationAmount, parkingExitRequest.EntryTime, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()), parkingExitRequest.TxnId, callbackInterface);
                                        response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();
                                        if(result){
                                            //Notify UI to change to Process screen
                                            Message message = handler.obtainMessage();
                                            message.obj = receiveMsg;
                                            handler.sendMessage(message);
                                        }

                                    } catch(Exception e){
                                        LogUtils.e(TAG, "ParkingExit Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                case "DisplayInfo":
                                    try{
                                        //Notify UI to change to Result screen
                                        Message message = handler.obtainMessage();
                                        message.obj = receiveMsg;
                                        handler.sendMessage(message);

                                        // v1.0.4 added beep sound
                                        DisplayInfo displayInfo = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), DisplayInfo.class);
                                        if (displayInfo.IsSuccess)
                                            App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_3, 100);
                                        else
                                            App.dal.getSys().beep(EBeepMode.FREQUENCE_LEVEL_5, 100);

                                        response.Result = eResult.ACK.toString();
                                    } catch(Exception e){
                                        LogUtils.e(TAG, "DisplayInfo Exception: " + Log.getStackTraceString(e));
                                    }
                                    break;
                                default:
                                    response.Result = eResult.NAK.toString();
                                    response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));
                                    send(comm, new Gson().toJson(response));
                                    disconnect(comm);
                                    return;
                            }
                            if(response.Data != null)
                                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Data));
                            else
                                response.Checksum = Utils.md5(response.Command + new Gson().toJson(response.Result));

                            send(comm, new Gson().toJson(response));
                            disconnect(comm);

                        }).start();
                    } catch (Exception e) {
                        LogUtils.e(TAG, "onPeerConnected Exception: " + Log.getStackTraceString(e));
                    }
                }

                @Override
                public void onShuttingDown() {
                    LogUtils.d(TAG, "onShuttingDown");
                }

                @Override
                public void onStopped() {
                    LogUtils.d(TAG, "onStopped");
                }

                @Override
                public void onError(IServer.EServerError error) {
                    LogUtils.d(TAG, "onError:" + error.name());
                }
            });

        }
    }

    public void start() {
        if (iServer!=null){
            LogUtils.d(TAG,"startServer");
            iServer.start();
        }
    }

    public void disconnect(IComm mComm) {
        if (mComm!=null) {
            try {
                mComm.disconnect();
                LogUtils.d(TAG,"disconnect");
            } catch (CommException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    public void send(IComm mComm, String input) {
        if (mComm!=null) {
            byte[] sendContent = input.getBytes();
            try {
                mComm.send(sendContent);
                LogUtils.i(TAG, "send success:" + new String(sendContent, StandardCharsets.UTF_8));
            } catch (CommException e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
    }

    public String receive(IComm mComm) {
        String message = "";
        if (mComm!=null) {
            try {
                mComm.setRecvTimeout(Integer.parseInt(new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_listener_receive_timeout)).equals("") ? "5000" : new SharedPrefUI(context).ReadSharedPrefStr(context.getString(R.string.tcp_listener_receive_timeout))));
                while(message.equals("")) {
                    byte[] recvContent = mComm.recvNonBlocking();
                    message = new String(recvContent, StandardCharsets.UTF_8);
                }
                LogUtils.i(TAG, "receive success:" + message);

            } catch (Exception e) {
                LogUtils.e(TAG, "Exception: " + Log.getStackTraceString(e));
            }
        }
        return message;
    }

    public void stop() {
        if (iServer!=null){
            LogUtils.d(TAG,"stopServer");
            iServer.stop();
        }
    }

    public void Sale() {
        TCPGeneralMessage receiveMessage = new TCPGeneralMessage();
        TCPGeneralMessage response = new TCPGeneralMessage();
        try
        {
            receiveMessage.Command = "Sale";
            JsonObject content = new JsonObject();
            content.addProperty("Amount", parkingExitReq.TotalAmount);
            content.addProperty("QRReferenceNumber", "");
            content.addProperty("TxnId", parkingExitReq.TxnId);
            content.addProperty("Reference", "");
            content.addProperty("Reserved", "");
            receiveMessage.Data = content;

            Sale.SaleRequest saleRequest = new Gson().fromJson(new Gson().toJson(receiveMessage.Data), Sale.SaleRequest.class);
            boolean result = spServiceInterface.Sales(saleRequest.Amount, "", saleRequest.TxnId, saleRequest.Reference, saleRequest.Reserved, spServiceCallback);
            response.Result = result ? eResult.ACK.toString() : eResult.NAK.toString();

            if(result) {
                //Notify UI to change to Tap Card screen
                Message message = scHandler.obtainMessage();
                message.obj = new Gson().toJson(receiveMessage, TCPGeneralMessage.class);
                scHandler.sendMessage(message);
            }

        } catch (Exception e) {
            LogUtils.e(TAG, "CallServiceCommand Error: " + Log.getStackTraceString(e));
        }
    }

    public GetStatusResult GetStatus() {
        GetStatusResult result = null;
        try {
            result = spServiceInterface.getStatus();
        } catch (Exception e) {
            LogUtils.e(TAG, "serviceGetStatus Error: " + Log.getStackTraceString(e));
        }
        return result;
    }
}
