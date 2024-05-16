// IAIDLSonicpayInterface.aidl
package com.sbs.aidl;

// Declare any non-default types here with import statements

import com.sbs.aidl.IAIDLClassInterface;
import com.sbs.aidl.IAIDLCardCallbackInterface;
interface IAIDLSonicpayInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    String GetSPServiceVersion();
    GetStatusResult getStatus();
    SalesResult getLastTransaction(String TxnId);
    boolean ReadCard(boolean EmvPan,IAIDLCardCallbackInterface callback);
    //boolean ReadCard(IAIDLCardCallbackInterface callback);
    boolean Sales(int Amount, String QRReferenceNumber, String TxnId, String SalesReference, String Reserved, IAIDLCardCallbackInterface callback);
    boolean PreAuth(int Amount, IAIDLCardCallbackInterface callback);
    boolean SalesCompletion(int Amount,String TraceId, IAIDLCardCallbackInterface callback);
    boolean Void(int Amount, String TraceId, IAIDLCardCallbackInterface callback);
    boolean Settlement(int HostNo,IAIDLCardCallbackInterface callback);
    //boolean ParkingEntry(in ReadCardResult readCardResult ,int MinBalance,String EntryTime ,IAIDLCardCallbackInterface callback);
    //boolean ParkingExit(in ReadCardResult readCardResult ,int TotalAmount,int FareAmount,int SurchargeAmount,int TaxAmount,int SurChargeTaxAmount,int PreValidationAmount ,String EntryTime ,String ExitTime ,String TxnId  ,IAIDLCardCallbackInterface callback);
    QRResponse QRRequest(int Amount,int QRType,IAIDLCardCallbackInterface callback);
    boolean QRCancel(String RefNo,int Amount);
    boolean Abort();

    int DownloadConfig(); // 0-Failed, 1-Success, 2-No Update
    int DataUpload(); // 0-Failed, 1-Success, 2-No data to upload
    String PBBKeyLoading(String deviceIP, int devicePort);
    boolean ClearReversal();
    boolean ClearSettlement();

    void WriteSharedPref(String key,String Value);
    String ReadSharedPref(String key);
    boolean ReadSharedPrefBoolean(String key);
    void WriteSharedPrefBoolean(String key,in boolean Value);

    boolean SetSalesP3Callback(IAIDLCardCallbackInterface callback);
    boolean SalesP1(int amount, String txnId, String salesReference, IAIDLCardCallbackInterface callback);
    boolean SalesP2(in eAction action);

    boolean FileUpload(String startDate, String endDate, boolean logFileOnly);

    // 1-VisaMaster, 2-Amex, 3-MyDebit, 4-UnionPay
    String RemoteKeyLoading(int scheme);
    String RenewTEPIN(String newPIN);
    boolean Refund(int Amount, String TraceId, IAIDLCardCallbackInterface callback);

    boolean SetMaintenanceMode(boolean isOn, IAIDLCardCallbackInterface callback);
    int GetPendingTransactionCount();
    boolean ExecuteQuery(String request, IAIDLCardCallbackInterface callback);
    boolean QRMerchantScan(int Amount, String QRCode, IAIDLCardCallbackInterface callback);
    boolean FareBasedEntry(String json ,IAIDLCardCallbackInterface callback);
    boolean FareBasedExit(String json  ,IAIDLCardCallbackInterface callback);
    boolean MaxChargedEntry(String json ,IAIDLCardCallbackInterface callback);
    boolean MaxChargedExit(String json  ,IAIDLCardCallbackInterface callback);
    int PerformAction(String action,String param);
}