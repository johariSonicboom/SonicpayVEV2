package com.sonicboom.sonicpayvui.models;

public enum eStatusCode {
    Approved ("00", "Approved"),
    Refer_To_Card_Issuer ("01", "Refer to card issuer"),
    Refer_To_Card_Issuer_Special_Condition ("02", "Refer to card issuer's special condition"),
    Invalid_Merchant("03", "Invalid Merchant"),
    Pickup_Card("04", "Pickup Card"),
    Declined("05", "Declined"),
    Transaction_Error("06", "Transaction error"),
    Pickup_Card_Special_Condition("07", "Pickup card, special condition"),
    Verify_Id_And_Sign("08", "Verify ID and sign"),
    Invalid_Transaction("12", "Invalid transaction"),
    Invalid_Amount("13", "Invalid amount"),
    Invalid_Card_Number("14", "Invalid card number"),
    Invalid_Issuer("15", "Invalid issuer"),
    Reversal_Error("18", "Reversal error"),
    Reenter_Transaction("19", "Re-enter transaction"),
    Invalid_Response("20", "Invalid response"),
    No_Transaction("21", "No transaction"),
    Unable_To_Locate_Record_On_File("25", "Unable to locate record on file"),
    Format_Error("30", "Format error"),
    Bank_Not_Supported_By_Switch("31", "Bank not supported by switch"),
    Please_Call_Bank("38", "PIN tries exceeded"),
    No_Credit_Account("39", "No credit account"),
    Lost_Card("41", "Lost card"),
    Stolen_Card_Pick_Up("43", "Stolen card pick up"),
    Not_Sufficient_Funds("51", "Not sufficient funds"),
    No_Cheque_Account("52", "No cheque account"),
    Invalid_Saving_Account("53", "Invalid saving account"),
    Expired_Card("54", "Expired card"),
    Incorrect_Pin("55", "Incorrect PIN"),
    No_Card_Record("56", "No card record"),
    Transaction_Not_Permitted("57", "Transaction not permitted"),
    Transaction_Not_Permitted_In_Terminal("58", "Transaction not permitted in terminal"),
    Amount_Limit_Exceeded("61", "Amount limit exceeded"),
    Security_Violation("63", "Security violation"),
    Pin_Tries_Exceeded("75", "PIN tries exceeded"),
    Invalid_Product_Codes("76", "Invalid product codes"),
    Reconcile_Error("77", "Reconcile error"),
    Trace_Not_Found("78", "Trace# not found"),
    Declined_CVV2("79", "Declined CVV2"),
    Batch_Number_Not_Found("80", "Batch number not found"),
    Exceed_REDP_Limit ("81", "Exceed REDP limit"),
    No_Closed_SOC_Slot("82", "No closed SOC slot"),
    No_Susp_SOC_Slot("83", "No susp SOC slot"),
    Insuffient_Pts("84", "Insufficient PTS"),
    Batch_Not_Found("85", "Batch not found"),
    Bad_Terminal_ID("89", "Bad terminal ID"),
    Issuer_Switch_Inoperative("91", "Issuer/switch inoperative"),
    Transaction_Cant_Be_Completed("93", "Transaction can't be completed"),
    Duplicate_Transmission("94", "Duplicate transmission"),
    Batch_Upload("95", "Batch upload"),
    System_Malfunction("96", "System malfunction"),
    Transaction_Approved_Offline_By_Card("Y1", "Transaction approved offline by card"),
    Transaction_Approved_Offline_By_Card2("Y3", "Transaction approved offline by card"),
    Transaction_Declined_By_Card("Z1", "Transaction declined by card"),
    Transaction_Declined_By_Card2("Z3", "Transaction declined by card"),
    Checksum_Error("M01", "Checksum error"),
    Key_Expired("M02", "Key expired"),
    Merchant_Terminal_Invalid("M03", "Merchant terminal invalid (MID/TID/UID)"),
    Card_Deny("M04", "Card deny"),
    Merchant_Terminal_Inactive("M05", "Merchant terminal inactive"),
    System_Error_Failure("M99", "System error/failure"),
    Terminal_Full("SE", "Terminal full"),
    Pin_Entry_Error("PE", "Pin entry error"),
    Invalid_Card_Or_Card_Not_Supported("IC", "Invalid card or card not supported"),
    Card_Expired("EC", "Card expired"),
    Zero_Amount_Settlement_Or_No_Transaction("ZE", "Zero amount settlement or no transaction"),
    Batch_Not_Found_During_Settlement_On_Host("BU", "Batch not found during settlement on host"),
    Comms_Error_Or_Connection_Timeout("CE", "Comms error or connection timeout"),
    Record_Error_TraceNo_Not_Found_During_Void_C201OrSaleCompC220("RE", "Record error trace number not found"),
    Host_Number_Error("HE", "Host number error"),
    Line_Error("LE", "Line error"),
    Transaction_Already_Voided("VB", "Transaction already voided"),
    File_Error_Empty("FE", "File error empty"),
    Card_Number_Not_Matched("WC", "Card number not matched"),
    Transaction_Aborted("TA", "Transaction aborted"),
    Amount_Not_Matched("AE", "Amount not matched"),
    Terminal_Busy("TB", "Terminal busy"),
    Settlement_Failed("SF", "Settlement failed"),
    Batch_Upload_Failed("BF", "Batch upload failed"),
    Pending_Settlement("PS", "Pending settlement"),
    Record_Not_Found("RN", "Record not found"),
    Reversal_Failed("RF", "Reversal failed");

    private final String code;
    private final String desc;

    private eStatusCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public static String getDescFromCode(String code) {
        for (eStatusCode b : eStatusCode.values()) {
            if (b.code.equalsIgnoreCase(code)) {
                return b.desc;
            }
        }
        return code;
    }
}
