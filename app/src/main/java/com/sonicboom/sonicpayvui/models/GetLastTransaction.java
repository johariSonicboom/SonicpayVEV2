package com.sonicboom.sonicpayvui.models;

import com.sbs.aidl.Class.EMVInfo;
import com.sbs.aidl.Class.QRInfo;
import com.sbs.aidl.Class.TNGInfo;

public class GetLastTransaction {
    public class GetLastTransactionRequest{
        public String TxnId;
    }

    public static class GetLastTransactionResponse{
        public String TxnStatus;
        public String SystemId;
        public String PaymentType;
        public String HashedPAN;
        public String CardNo;
        public EMVInfo EMVInfo;
        public TNGInfo TNGInfo;
        public QRInfo QRInfo;
    }
}
