package com.sonicboom.sonicpayvui.models;

public enum eTngStatusCode {
    No_Error("00", "No error"),
    Failed_To_Read_The_Card ("01", "Fail to read the card"),
    Authentication_Failed("02", "Authentication failed"),
    Failed_To_Get_The_Key_From_SAM ("03", "Failed to get the key from the SAM"),
    Failed_To_Run_The_Command("04", "Failed to run the command"),
    Failed_To_Backup_Data("05", "Failed to backup data"),
    Unable_To_Backup_Data("06", "Unable to backup data"),
    The_Terminal_Is_Busy("07", "The terminal is busy"),
    Failed_To_Initialize_The_SAM("08", "Failed to initialize the SAM"),
    The_Kernel_Is_Missing_Tag("09", "The kernel is missing the tag"),
    Failed_To_Authenticate_With_The_Host("10", "Failed to authenticate with the host"),
    Failed_To_Authenticate_With_The_SAM("11", "Failed to authenticate with the SAM"),
    Failed_To_Write_To_The_Database("12", "Failed to write to the database"),
    The_Sales_Amount_Is_Zero("13", "The sales amount is zero"),
    The_Key_Version_Is_Invalid("20", "The key version is invalid"),
    The_Block_Number_Is_Invalid("21", "The block number is invalid"),
    The_Application_Directory_Is_Invalid("22", "The application directory is invalid"),
    The_CBKL_Is_Invalid("23", "The CBKL is invalid"),
    The_IBLK_Is_Invalid("24", "The IBLK is invalid"),
    The_Luhn_Check_Digit_Is_Invalid("25", "The Luhn check digit is invalid"),
    Failed_To_Initialize_The_Sales_Transaction("26", "Failed to initialize the sales transaction"),
    Failed_To_Initialize_The_Reload_Transaction("27", "Failed to initialize the reload transaction"),
    The_Prefix_Is_Invalid("28","The prefix is invalid"),
    The_Card_Has_Expired("29", "The card has expired"),
    The_SBLK_Reload_Is_Invalid("30", "The SBLK reload is invalid"),
    The_SBLK_Sales_Is_Invalid("31", "The SBLK sales is invalid"),
    The_VDSP_Is_Invalid("32", "The VDSP is invalid"),
    The_Transaction_Number_Is_Invalid("33", "The transaction number is invalid"),
    The_Transaction_Is_Not_Complete("50", "The transaction is not complete"),
    The_Amount_Is_Insufficient("51", "The amount is insufficient"),
    The_Exit_Is_Invalid("52","The exit is invalid"),
    Failed_To_Calculate_The_Fare("53", "Failed to calculate the fare"),
    The_Parameter_Status_Has_Expired("54", "The parameter status has expired"),
    The_Entry_Failed("55","The entry failed"),
    The_Refund_Is_Negative("56", "The refund is negative"),
    The_Bus_Journey_Must_Be_At_Least_One_Hour_Long("60", "The bus journey must be at least one hour long");

    private final String code;
    private final String desc;

    private eTngStatusCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public String getDesc(){ return this.desc;}

    public static eTngStatusCode fromCode(String text) {
        for (eTngStatusCode b : eTngStatusCode.values()) {
            if (b.code.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
