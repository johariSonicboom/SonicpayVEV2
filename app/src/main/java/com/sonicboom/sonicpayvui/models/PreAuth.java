package com.sonicboom.sonicpayvui.models;

public class PreAuth {
    public class PreAuthRequest{
        public int Amount;
    }

    public static class PreAuthResponse{
        public String StatusCode;
        public String SystemId;
        public String CardNo;
        public String HashedPAN;
        public String ApprovalCode;
        public String RRN;
        public String TransactionTrace;
        public int BatchNo;
        public int HostNo;
        public String TerminalId;
        public String MerchantId;
        public String AID;
        public String TC;
        public String CardHolderName;
        public String CardType;
        public String CardLabel;
        public String InvoiceNo;
        public String TVR;

    }
}
