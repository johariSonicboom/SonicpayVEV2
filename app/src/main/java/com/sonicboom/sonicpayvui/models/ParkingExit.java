package com.sonicboom.sonicpayvui.models;

import com.sbs.aidl.Class.EMVInfo;
import com.sbs.aidl.Class.QRInfo;
import com.sbs.aidl.Class.TNGInfo;

public class ParkingExit {
    public static class ParkingExitRequest{
        public String CardNo;
        public String HashedPAN;
        public int CardType;
        public String EntryTime;
        public int TotalAmount;
        public int FareAmount;
        public int SurchargeAmount;
        public int SurchargeTaxAmount;
        public int TaxAmount;
        public int PreValidationAmount;
        public String TxnId;
    }

    public static class ParkingExitResponse{
        public String SystemId;
        public String CardNo;
        public String HashedPAN;
        public int CardType;
        public String StatusCode;
        public TNGInfo TNGInfo;
        public EMVInfo EMVInfo;
    }
}
