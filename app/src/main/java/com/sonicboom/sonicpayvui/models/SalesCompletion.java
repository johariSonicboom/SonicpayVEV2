package com.sonicboom.sonicpayvui.models;

public class SalesCompletion {
    public class SalesCompletionRequest{
        public int Amount;
        public String TransactionTrace;
    }

    public static class SalesCompletionResponse{
        public String StatusCode;
        public String SystemId;
        public String RRN;
        public String TransactionTrace;
        public int BatchNo;
        public int HostNo;
        public String TerminalId;
        public String MerchantId;
        public String InvoiceNo;
    }
}
