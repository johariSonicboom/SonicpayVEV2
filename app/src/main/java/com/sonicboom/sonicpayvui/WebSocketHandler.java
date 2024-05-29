package com.sonicboom.sonicpayvui;


import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.sbs.aidl.Class.SalesCompletionResult;
import com.sbs.aidl.Class.SalesResult;
import com.sonicboom.sonicpayvui.EVModels.Component;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.EVModels.GetCharPointStatusRequest;
import com.sonicboom.sonicpayvui.EVModels.GetStatusNotificationResponse;
import com.sonicboom.sonicpayvui.EVModels.GetStatusResponse;
import com.sonicboom.sonicpayvui.EVModels.SharedResource;
import com.sonicboom.sonicpayvui.EVModels.StartSales;
import com.sonicboom.sonicpayvui.EVModels.StartTransaction;
import com.sonicboom.sonicpayvui.EVModels.eChargePointStatus;
import com.sonicboom.sonicpayvui.EVModels.vSalesCompletionResult;
import com.sonicboom.sonicpayvui.EVModels.StopChargeTapCard;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.gustavoavila.websocketclient.WebSocketClient;

public class WebSocketHandler {
    private WebSocketClient webSocketClient;
    private SharedResource sharedResource;
    private String InitResul = "";
    //    public Component component;
//    public Component selectedComponent;
    MainActivity mainActivity;

    public WebSocketHandler(MainActivity a) {
        mainActivity = a;
        mainActivity.UpdateChargePointStatus(eChargePointStatus.Init);
    }

    public void connect() {
        URI uri;

        try {
            mainActivity.UpdateChargePointStatus(eChargePointStatus.Init);
            sharedResource = new SharedResource();
            String ws = new SharedPrefUI(mainActivity).ReadSharedPrefStr(mainActivity.getString(R.string.WebSocketURL));

            String ClientCode = new SharedPrefUI(mainActivity).ReadSharedPrefStr(mainActivity.getString(R.string.client_code));

            String ComponentCode = new SharedPrefUI(mainActivity).ReadSharedPrefStr(mainActivity.getString(R.string.ComponentCode));
            uri = new URI(ws + "/" + ClientCode + "/" + ComponentCode);
            Log.d("URI", uri.toString());

        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }
        webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen() {
                LogUtils.i("onOpen");
                System.out.println("onOpen");
                //webSocketClient.send("Hello, World!");
                new Thread(() -> {
                    Init();
                }).start();
            }

            @Override
            public void onTextReceived(String message) {
                System.out.println("onTextReceived :" + message);
                LogUtils.i("onTextReceived :" + message);
                String[] Received = message.split("\\|");
                switch (Received[1]) {
                    case "StatusNotification":
                        StatusNotificationReceived(Received[2]);
                        break;
                    case "GetCharPointStatus":
                        NotificationReceived(Received[2]);
                        sharedResource.setCondition();
                        break;
                    case "SalesCompletion":
                        //SalesCompletionReceived(Reveied[2]);
                        SalesCompletionReceived(Received[2]);
                        break;
                    case "initialization":
                        InitResul = message;
                        sharedResource.setCondition();
                        break;
                    case "StopChargeTapCardError":
                        LogUtils.i("StopChargeTapCardError :" + Received[2]);
                        mainActivity.StopChargeTapCardErrorReceived(Received[2]);
                        break;
                    case "StartSales":
                        StartSalesReceived(Received[2]);
                        sharedResource.setCondition();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onBinaryReceived(byte[] data) {
                System.out.println("onBinaryReceived");
                LogUtils.i("onBinaryReceived");
            }

            @Override
            public void onPingReceived(byte[] data) {
                System.out.println("onPingReceived");
                LogUtils.i("onPingReceived");
            }

            @Override
            public void onPongReceived(byte[] data) {
                System.out.println("onPongReceived");
                LogUtils.i("onPongReceived");
            }

            @Override
            public void onException(Exception e) {
                System.out.println(e.getMessage());
                // Print the stack trace to standard error
                e.printStackTrace();
                // Log the exception using your logging utility
                LogUtils.e(e);
                // Update the charge point status
                mainActivity.UpdateChargePointStatus(eChargePointStatus.Disconnected);
            }

            @Override
            public void onCloseReceived(int reason, String description) {
                LogUtils.e("onCloseReceived", "onCloseReceived");
                mainActivity.UpdateChargePointStatus(eChargePointStatus.Disconnected);
            }


        };

        webSocketClient.setConnectTimeout(10000);
        // webSocketClient.setReadTimeout(60000);
        webSocketClient.addHeader("Origin", "http://developer.example.com");
        webSocketClient.enableAutomaticReconnection(5000);
        webSocketClient.connect();
    }

    public StartSales startSales;

    private void StartSalesReceived(String notificationResponseMsg) {
        startSales = new Gson().fromJson(notificationResponseMsg, StartSales.class);


    }


    public static Component[] componentList;

    void Init() {
        String uniqId = RandomString(16);
        String InitMessage = String.format("%s|%s|%s", uniqId, "initialization", "");

        new Thread(() -> {
            LogUtils.e("Init ", InitMessage);
            webSocketClient.send(InitMessage);

        }).start();


        try {
            sharedResource.waitForCondition();

            String[] Reveied = InitResul.split("\\|");
            Gson gson = new Gson();

            componentList = gson.fromJson(Reveied[2], Component[].class);

            if (componentList.length > 0) {
                mainActivity.SelectedChargingStationComponent = componentList[0];
                mainActivity.selectedConnectorIndex = 0;
            } else {
                mainActivity.UpdateChargePointStatus(eChargePointStatus.NotFound);
                return;
            }
            mainActivity.UpdateChargePointStatus(eChargePointStatus.Idle);
            //if(component.Status.equalsIgnoreCase("charging")){
//            GetStatus(component.ComponentCode);
            //}

            for (Component component : componentList) {
                GetStatus(component.ComponentCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void GetStatus(String ComponentCode) throws InterruptedException {
        String uniqId = RandomString(16);
        GetCharPointStatusRequest charPointStatusRequest = new GetCharPointStatusRequest();

        charPointStatusRequest.ComponentCode = ComponentCode;
        Gson gson = new Gson();

        String GetChargePointMessage = String.format("%s|%s|%s", uniqId, "GetCharPointStatus", gson.toJson(charPointStatusRequest));
        //new Thread(() -> {
        sharedResource = new SharedResource();
        webSocketClient.send(GetChargePointMessage);
        sharedResource.waitForCondition();
        LogUtils.e("GetStatus ", GetChargePointMessage);
        //}).start();

    }

    int Txid = 0;

    public String startTransactionTrace;

    StartSales SalesResultResponse(SalesResult PreAuthResponse, String PhoneNumber) {
        String uniqId = RandomString(16);
        StartTransaction startTransaction = new StartTransaction();


        startTransaction.ComponentCode = mainActivity.SelectedChargingStationComponent.ComponentCode;
        startTransaction.TransactionTrace = PreAuthResponse.emvInfo.TransactionTrace;
        startTransaction.CardNumber = PreAuthResponse.CardNo;
        startTransaction.HashPAN = PreAuthResponse.Token;
        startTransaction.SystemPaymentId = Integer.parseInt(PreAuthResponse.SystemId);
        startTransaction.CardType = PreAuthResponse.CardType.getValue();
        startTransaction.PhoneNumber = PhoneNumber;
        startTransaction.MID = PreAuthResponse.emvInfo.MerchantId;

        startTransaction.TID = PreAuthResponse.emvInfo.TerminalId;
        startTransaction.AuthCode = PreAuthResponse.emvInfo.ApprovalCode;
        startTransaction.AID = PreAuthResponse.emvInfo.AID;
        startTransaction.RNN = PreAuthResponse.emvInfo.RRN;
        startTransaction.Connector = mainActivity.getConnectorIDByIndex(mainActivity.SelectedChargingStationComponent, mainActivity.selectedConnectorIndex);
        Gson gson = new Gson();
        String StartTransactionMessage = String.format("%s|%s|%s", uniqId, "StartSales", gson.toJson(startTransaction));

        startTransactionTrace = PreAuthResponse.emvInfo.TransactionTrace;

        webSocketClient.send(StartTransactionMessage);
        LogUtils.e("SalesResultResponse ", StartTransactionMessage);
        sharedResource = new SharedResource();

        try {
            sharedResource.waitForCondition();
            return startSales;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


//        try {
//            sharedResource.waitForCondition();
//
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return null;
    }

    void SalesCompletionResultResponse(SalesCompletionResult result) {
        String uniqId = RandomString(16);
        vSalesCompletionResult SalesCompletion = new vSalesCompletionResult();

        SalesCompletion.ComponentCode = mainActivity.SelectedChargingStationComponent.ComponentCode;
        SalesCompletion.TransactionTrace = result.TransactionTrace;

        SalesCompletion.TxId = Txid;
        SalesCompletion.SalesResponse = result.StatusCode;
        SalesCompletion.MID = result.MerchantId;
        SalesCompletion.TID = result.TerminalId;
        SalesCompletion.AuthCode = result.InvoiceNo;
        SalesCompletion.AID = "";
        SalesCompletion.RNN = result.RRN;
        SalesCompletion.PaymentId = Integer.parseInt(result.SystemId);
        Gson gson = new Gson();
        String SalesCompletionMessage = String.format("%s|%s|%s", uniqId, "SalesCompletionResult", gson.toJson(SalesCompletion));

        new Thread(() -> {

            webSocketClient.send(SalesCompletionMessage);
            LogUtils.e("SalesCompletionResultResponse ", SalesCompletionMessage);
        }).start();


//        try {
//            sharedResource.waitForCondition();
//
//
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    public void StatusNotificationReceived(String statusNotificationResponseMsg) {
        if (!statusNotificationResponseMsg.isEmpty()) {

            GetStatusNotificationResponse statusNotificationResponse = new Gson().fromJson(statusNotificationResponseMsg, GetStatusNotificationResponse.class);
            Log.d("Status Notification Response", statusNotificationResponse.toString());
            Component component = mainActivity.GetSelectedComponentbyComponentCode(statusNotificationResponse.ComponentCode, componentList);
            Log.d("Notification Response", "GetStatusResponse");

            String connectorStatus;
            Component component2 = mainActivity.GetSelectedComponentbyComponentCode(mainActivity.SelectedChargingStationComponent.ComponentCode, componentList);

            if (component2.Status == null) {
                connectorStatus = "Offline";
            } else {
                connectorStatus = statusNotificationResponse.Status;
            }

            switch (connectorStatus.toLowerCase(Locale.ROOT)) {
                case "preparing":

                    break;
                case "startcharge":
                    Component selectedComponent = mainActivity.GetSelectedComponentbyComponentCode(mainActivity.SelectedChargingStationComponent.ComponentCode, componentList);
//                        mainActivity.UpdateAllConnectorStatus(notificationResponse.Status, selectedComponent);

                case "charging":
                    if (!statusNotificationResponse.Description.isEmpty()) {

//                            mainActivity.StartCharging(notificationResponse.Description);
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

                        try {
                            mainActivity.SelectedChargingStationComponent.StartChargeTime = format.parse(statusNotificationResponse.Description);
                        } catch (Exception ex) {
                            LogUtils.e("NotificationReceived ", ex);
                        }
                    } else if (mainActivity.SelectedChargingStationComponent.StartChargeTime != null) {
                        mainActivity.UpdateChargePointStatus(eChargePointStatus.Charging);
                    }
                    new Date().getTime();
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMddHHmmss");
                    String formattedDate = format2.format(new Date());
                    mainActivity.StartCharging(formattedDate);
                    break;
                case "available":
                    break;
                case "offline":
                    break;
            }

            mainActivity.UpdateStatus(statusNotificationResponse.Status);
            GeneralVariable.ChargePointStatus = statusNotificationResponse.Status;

            mainActivity.UpdateConnectorStatus(statusNotificationResponse.Status, component2, statusNotificationResponse.ConnectorId);
            mainActivity.SelectedChargingStationComponent.Status = statusNotificationResponse.Status;
            mainActivity.replaceComponent(componentList, mainActivity.SelectedChargingStationComponent);

        }

    }

    public void NotificationReceived(String notificationResponseMsg) {
        if (!notificationResponseMsg.isEmpty()) {
            GetStatusResponse notificationResponse = new Gson().fromJson(notificationResponseMsg, GetStatusResponse.class);
            Log.d("Notification Response", notificationResponse.toString());

            Component component = mainActivity.GetSelectedComponentbyComponentCode(notificationResponse.ComponentCode, componentList);

            mainActivity.replaceComponentConnectors(componentList, notificationResponse.ComponentCode, notificationResponse.Connectors);
//            mainActivity.UpdateConnectorStatus(component.Connectors.get(component.SelectedConnector).Status, component, notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).ConnectorId);
//                String connectorStatus = notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Status;

//            mainActivity.replaceComponent(componentList, component);
            String connectorStatus;

            if (component.Connectors.isEmpty()) {
                connectorStatus = "Offline";
            } else {
                connectorStatus = notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Status;
            }

            component.FareChargeText = notificationResponse.FareChargeText;
            component.FareChargeDescription = notificationResponse.DescriptionText;
            mainActivity.replaceComponent(componentList, component);

            switch (connectorStatus.toLowerCase(Locale.ROOT)) {
//                switch (notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Status.toLowerCase(Locale.ROOT)) {
                case "preparing":

                    break;
                case "startcharge":
                    Component selectedComponent = mainActivity.GetSelectedComponentbyComponentCode(notificationResponse.ComponentCode, componentList);
//                        mainActivity.UpdateAllConnectorStatus(notificationResponse.Status, selectedComponent);
                    new Date().getTime();
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMddHHmmss");
                    String formattedDate = format2.format(new Date());
                    mainActivity.StartCharging(formattedDate);
                case "charging":
                    if (!notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Description.isEmpty()) {
                        //charging fragment
//                            mainActivity.StartCharging(notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Description);
                        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");


                        try {
                            mainActivity.SelectedChargingStationComponent = mainActivity.GetSelectedComponentbyComponentCode(notificationResponse.ComponentCode, componentList);
                            mainActivity.SelectedChargingStationComponent.StartChargeTime = format.parse(notificationResponse.Connectors.get(mainActivity.selectedConnectorIndex).Description);
                            mainActivity.replaceComponent(componentList, mainActivity.SelectedChargingStationComponent);
                        } catch (Exception ex) {
                            LogUtils.e(ex);
                        }
                    } else if (mainActivity.SelectedChargingStationComponent.StartChargeTime != null) {
                        mainActivity.UpdateChargePointStatus(eChargePointStatus.Charging);
                    }

                    break;
                case "available":
//                        mainActivity.UpdateChargePointStatus(eChargePointStatus.Idle);
                    component.FareChargeText = notificationResponse.FareChargeText;
                    component.FareChargeDescription = notificationResponse.DescriptionText;
                    mainActivity.replaceComponent(componentList, component);
                    break;
                case "offline":
//                        mainActivity.UpdateChargePointStatus(eChargePointStatus.Disconnected);
                    break;
            }
        }
    }

    public SalesCompletion salesCompletionResult;

    public void SalesCompletionReceived(String StopChargeMsg) {
        salesCompletionResult = new Gson().fromJson(StopChargeMsg, SalesCompletion.class);

        if (salesCompletionResult.CustumErrorMessage != null) {
            // mainActivity.SalesCompletionError(salesCompletion.CustumErrorMessage);
            try {
                Thread.sleep(3000);
                mainActivity.SalesCompletion(salesCompletionResult.Amount, salesCompletionResult.TransactionTrace, String.format("Total Chargin time %02d Hours %02d Minutes", 0, 0));
                LogUtils.i("custumErrorMessage", "custumErrorMessage is Not Null : " + salesCompletionResult.CustumErrorMessage);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            try {
                LogUtils.i("custumErrorMessage is Null", "custumErrorMessage is Null");
                Txid = salesCompletionResult.TxId;
                long diff = new Date().getTime() - mainActivity.SelectedChargingStationComponent.StartChargeTime.getTime();
                long seconds = diff / 1000;
                long minutes = seconds / 60;
                long hours = minutes / 60;
                long days = hours / 24;
                long m = minutes % 60;
                String TimeUse = String.format("Total Charging Time %02d Hours %02d Minutes", hours, m);
                mainActivity.SalesCompletion(salesCompletionResult.Amount, salesCompletionResult.TransactionTrace, TimeUse);

                try {
                    Thread.sleep(4000);
                    mainActivity.UpdateChargePointStatus(eChargePointStatus.Idle);
                    mainActivity.UpdateStatus("Available");
                    GeneralVariable.ChargePointStatus = "Available";

                    Component component = mainActivity.GetSelectedComponentbyComponentCode(salesCompletionResult.ComponentCode, componentList);
//                    mainActivity.UpdateAllConnectorStatus("Available", mainActivity.SelectedChargingStationComponent);
                    mainActivity.SelectedChargingStationComponent.Status = "Available";
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (Exception ex) {
                mainActivity.SalesCompletion(salesCompletionResult.Amount, salesCompletionResult.TransactionTrace, String.format("Total Chargin time %02d Hours %02d Minutes", 0, 0));
            }
        }
    }

    String RandomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }

    public void StopChargeTapCardResultResponse(StopChargeTapCard result) {

        String uniqId = RandomString(16);


        Gson gson = new Gson();
        String StopChargeTapCardMessage = String.format("%s|%s|%s", uniqId, "StopChargeTapCard", gson.toJson(result));

        new Thread(() -> {

            webSocketClient.send(StopChargeTapCardMessage);
            LogUtils.i("StopChargeTapCardResultResponse ", StopChargeTapCardMessage);
        }).start();
    }

}

