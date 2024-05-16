package com.sonicboom.sonicpayvui.models;

import com.sbs.aidl.Class.EMVInfo;
import com.sbs.aidl.Class.QRInfo;
import com.sbs.aidl.Class.TNGInfo;

public class Sale {
    public class SaleRequest{
        public int Amount;
        public String TxnId;
        public String Reference;
        public String Reserved;
    }

    public static class SaleResponse{
        public String SystemId;
        public String CardNo;
        public String StatusCode;
        public String PaymentType;
        public String HashedPAN;
        public EMVInfo EMVInfo;
        public TNGInfo TNGInfo;
        public QRInfo QRInfo;
    }
}
