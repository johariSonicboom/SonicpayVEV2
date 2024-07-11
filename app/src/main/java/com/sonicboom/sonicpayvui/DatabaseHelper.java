package com.sonicboom.sonicpayvui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;
import com.sonicboom.sonicpayvui.EVModels.TransactionTableDB;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.ArrayList;

//- SalesCompletion Status:
//        - "I", Initial value
//        - "S", Success
//        - "F", Failed
//        - "E", No of retries exceeded

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String TRANSACTION_TABLE = "TRANSACTION_TABLE";
    public static final String TRANSACTION_TRACE = "TransactionTrace";
    public static final String COMPONENT_CODE = "ComponentCode";
    public static final String CARD_NUMBER = "CardNumber";
    public static final String HASH_PAN = "HashPAN";
    public static final String SYSTEM_PAYMENT_ID = "SystemPaymentId";
    public static final String CARD_TYPE = "CardType";
    public static final String PHONE_NUMBER = "PhoneNumber";
    public static final String MID = "MID";
    public static final String TID = "TID";
    public static final String AUTH_CODE = "AuthCode";
    public static final String AID = "AID";
    public static final String RNN = "RNN";
    public static final String CONNECTOR = "Connector";
    public static final String AMOUNT = "Amount";
    public static final String TX_ID = "TxId";
    public static final String CUSTUM_ERROR_MESSAGE = "CustumErrorMessage";
    public static final String CHARGING_PERIOD = "ChargingPeriod";
    public static final String STATUS = "Status";
    public static final String NO_OF_RETRIES = "NoOfRetries";
    public static final String RECEIVE_SALES_COMPLETION_DATE_TIME = "ReceiveSalesCompletionDateTime";

    public DatabaseHelper(@Nullable Context context) {
        super(context, "ev.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + TRANSACTION_TABLE + " (" +
                COMPONENT_CODE + " TEXT, " +
                TRANSACTION_TRACE + " TEXT PRIMARY KEY, " +
                CARD_NUMBER + " TEXT, " +
                HASH_PAN + " TEXT, " +
                SYSTEM_PAYMENT_ID + " INTEGER, " +
                CARD_TYPE + " INTEGER, " +
                PHONE_NUMBER + " TEXT, " +
                MID + " TEXT, " +
                TID + " TEXT, " +
                AUTH_CODE + " TEXT, " +
                AID + " TEXT, " +
                RNN + " TEXT, " +
                CONNECTOR + " INTEGER, " +
                AMOUNT + " REAL, " +
                TX_ID + " INTEGER, " +
                CUSTUM_ERROR_MESSAGE + " TEXT, " +
                CHARGING_PERIOD + " TEXT, " +
                STATUS + " TEXT, " +
                NO_OF_RETRIES + " INTEGER, " +
                RECEIVE_SALES_COMPLETION_DATE_TIME + " TEXT)"
                ;

        db.execSQL(createTableStatement);
    }



    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean insertData(TransactionTableDB transactionTableDB) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COMPONENT_CODE, transactionTableDB.ComponentCode);
        cv.put(TRANSACTION_TRACE, transactionTableDB.TransactionTrace);
        cv.put(CARD_NUMBER, transactionTableDB.CardNumber);
        cv.put(HASH_PAN, transactionTableDB.HashPAN);
        cv.put(SYSTEM_PAYMENT_ID, transactionTableDB.SystemPaymentId);
        cv.put(CARD_TYPE, transactionTableDB.CardType);
        cv.put(PHONE_NUMBER, transactionTableDB.PhoneNumber);
        cv.put(MID, transactionTableDB.MID);
        cv.put(TID, transactionTableDB.TID);
        cv.put(AUTH_CODE, transactionTableDB.AuthCode);
        cv.put(AID, transactionTableDB.AID);
        cv.put(RNN, transactionTableDB.RNN);
        cv.put(CONNECTOR, transactionTableDB.Connector);
        cv.put(AMOUNT, transactionTableDB.Amount);
        cv.put(TX_ID, transactionTableDB.TxId);
        cv.put(CUSTUM_ERROR_MESSAGE, transactionTableDB.CustumErrorMessage);
        cv.put(CHARGING_PERIOD, transactionTableDB.ChargingPeriod);
        cv.put(STATUS, transactionTableDB.Status);
        cv.put(NO_OF_RETRIES, transactionTableDB.NoOfRetries);
        cv.put(RECEIVE_SALES_COMPLETION_DATE_TIME, transactionTableDB.ReceiveSalesCompletionDateTime);

        long insert = db.insert(TRANSACTION_TABLE, null, cv);

        if (insert == -1) {
            LogUtils.i("Failed to add to DB");
            return false;
        } else {
            LogUtils.i("Successfully added to DB");
            return true;
        }
    }

    public boolean updateData(TransactionTableDB transactionTableDB) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COMPONENT_CODE, transactionTableDB.ComponentCode);
        cv.put(TRANSACTION_TRACE, transactionTableDB.TransactionTrace);
        cv.put(CARD_NUMBER, transactionTableDB.CardNumber);
        cv.put(HASH_PAN, transactionTableDB.HashPAN);
        cv.put(SYSTEM_PAYMENT_ID, transactionTableDB.SystemPaymentId);
        cv.put(CARD_TYPE, transactionTableDB.CardType);
        cv.put(PHONE_NUMBER, transactionTableDB.PhoneNumber);
        cv.put(MID, transactionTableDB.MID);
        cv.put(TID, transactionTableDB.TID);
        cv.put(AUTH_CODE, transactionTableDB.AuthCode);
        cv.put(AID, transactionTableDB.AID);
        cv.put(RNN, transactionTableDB.RNN);
        cv.put(CONNECTOR, transactionTableDB.Connector);
        cv.put(AMOUNT, transactionTableDB.Amount);
        cv.put(TX_ID, transactionTableDB.TxId);
        cv.put(CUSTUM_ERROR_MESSAGE, transactionTableDB.CustumErrorMessage);
        cv.put(CHARGING_PERIOD, transactionTableDB.ChargingPeriod);
        cv.put(STATUS, transactionTableDB.Status);
        cv.put(NO_OF_RETRIES, transactionTableDB.NoOfRetries);
        cv.put(RECEIVE_SALES_COMPLETION_DATE_TIME, transactionTableDB.ReceiveSalesCompletionDateTime);

        // Updating the record with the specified Trace
        int update = db.update(TRANSACTION_TABLE, cv, TRANSACTION_TRACE + " = ?", new String[]{String.valueOf(transactionTableDB.TransactionTrace)});

        if (update == -1) {
            LogUtils.i("Failed to update DB");
            return false;
        } else {
            LogUtils.i("Successfully updated DB");
            return true;
        }
    }
//
public TransactionTableDB getTransactionByTrace(String trace) {
    SQLiteDatabase db = this.getReadableDatabase();
    String query = "SELECT * FROM " + TRANSACTION_TABLE + " WHERE " + TRANSACTION_TRACE + " = ?";
    Cursor cursor = db.rawQuery(query, new String[]{trace});

    TransactionTableDB transactionTableDB = null;

    if (cursor.moveToFirst()) {
        String componentCode = cursor.getString(cursor.getColumnIndexOrThrow(COMPONENT_CODE));
        String transactionTrace = cursor.getString(cursor.getColumnIndexOrThrow(TRANSACTION_TRACE));
        String cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(CARD_NUMBER));
        String hashPAN = cursor.getString(cursor.getColumnIndexOrThrow(HASH_PAN));
        int systemPaymentId = cursor.getInt(cursor.getColumnIndexOrThrow(SYSTEM_PAYMENT_ID));
        int cardType = cursor.getInt(cursor.getColumnIndexOrThrow(CARD_TYPE));
        String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(PHONE_NUMBER));
        String mid = cursor.getString(cursor.getColumnIndexOrThrow(MID));
        String tid = cursor.getString(cursor.getColumnIndexOrThrow(TID));
        String authCode = cursor.getString(cursor.getColumnIndexOrThrow(AUTH_CODE));
        String aid = cursor.getString(cursor.getColumnIndexOrThrow(AID));
        String rnn = cursor.getString(cursor.getColumnIndexOrThrow(RNN));
        int connector = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTOR));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(AMOUNT));
        int txId = cursor.getInt(cursor.getColumnIndexOrThrow(TX_ID));
        String customErrorMessage = cursor.getString(cursor.getColumnIndexOrThrow(CUSTUM_ERROR_MESSAGE));
        String chargingPeriod = cursor.getString(cursor.getColumnIndexOrThrow(CHARGING_PERIOD));
        String status = cursor.getString(cursor.getColumnIndexOrThrow(STATUS));
        int noOfRetries = cursor.getInt(cursor.getColumnIndexOrThrow(NO_OF_RETRIES));
        String receiveSalesCompletionDateTime = cursor.getString(cursor.getColumnIndexOrThrow(RECEIVE_SALES_COMPLETION_DATE_TIME));

        transactionTableDB = new TransactionTableDB();
        transactionTableDB.ComponentCode = componentCode;
        transactionTableDB.TransactionTrace = transactionTrace;
        transactionTableDB.CardNumber = cardNumber;
        transactionTableDB.HashPAN = hashPAN;
        transactionTableDB.SystemPaymentId = systemPaymentId;
        transactionTableDB.CardType = cardType;
        transactionTableDB.PhoneNumber = phoneNumber;
        transactionTableDB.MID = mid;
        transactionTableDB.TID = tid;
        transactionTableDB.AuthCode = authCode;
        transactionTableDB.AID = aid;
        transactionTableDB.RNN = rnn;
        transactionTableDB.Connector = connector;
        transactionTableDB.Amount = amount;
        transactionTableDB.TxId = txId;
        transactionTableDB.CustumErrorMessage = customErrorMessage;
        transactionTableDB.ChargingPeriod = chargingPeriod;
        transactionTableDB.Status = status;
        transactionTableDB.NoOfRetries = noOfRetries;
        transactionTableDB.ReceiveSalesCompletionDateTime = receiveSalesCompletionDateTime;
    }
    cursor.close();
    LogUtils.i("Get Based on Trace:", transactionTableDB.toString());
    return transactionTableDB;
}

    public ArrayList<TransactionTableDB> getTransactionsByStatus(String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TransactionTableDB> transactionsList = new ArrayList<>();

        String query = "SELECT * FROM " + TRANSACTION_TABLE + " WHERE " + STATUS + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{status});

        if (cursor.moveToFirst()) {
            do {
                String componentCode = cursor.getString(cursor.getColumnIndexOrThrow(COMPONENT_CODE));
                String transactionTrace = cursor.getString(cursor.getColumnIndexOrThrow(TRANSACTION_TRACE));
                String cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(CARD_NUMBER));
                String hashPAN = cursor.getString(cursor.getColumnIndexOrThrow(HASH_PAN));
                int systemPaymentId = cursor.getInt(cursor.getColumnIndexOrThrow(SYSTEM_PAYMENT_ID));
                int cardType = cursor.getInt(cursor.getColumnIndexOrThrow(CARD_TYPE));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(PHONE_NUMBER));
                String mid = cursor.getString(cursor.getColumnIndexOrThrow(MID));
                String tid = cursor.getString(cursor.getColumnIndexOrThrow(TID));
                String authCode = cursor.getString(cursor.getColumnIndexOrThrow(AUTH_CODE));
                String aid = cursor.getString(cursor.getColumnIndexOrThrow(AID));
                String rnn = cursor.getString(cursor.getColumnIndexOrThrow(RNN));
                int connector = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTOR));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(AMOUNT));
                int txId = cursor.getInt(cursor.getColumnIndexOrThrow(TX_ID));
                String customErrorMessage = cursor.getString(cursor.getColumnIndexOrThrow(CUSTUM_ERROR_MESSAGE));
                String chargingPeriod = cursor.getString(cursor.getColumnIndexOrThrow(CHARGING_PERIOD));
                String retrievedStatus = cursor.getString(cursor.getColumnIndexOrThrow(STATUS));
                int noOfRetries = cursor.getInt(cursor.getColumnIndexOrThrow(NO_OF_RETRIES));
                String receiveSalesCompletionDateTime = cursor.getString(cursor.getColumnIndexOrThrow(RECEIVE_SALES_COMPLETION_DATE_TIME));

                TransactionTableDB transaction = new TransactionTableDB();
                transaction.ComponentCode = componentCode;
                transaction.TransactionTrace = transactionTrace;
                transaction.CardNumber = cardNumber;
                transaction.HashPAN = hashPAN;
                transaction.SystemPaymentId = systemPaymentId;
                transaction.CardType = cardType;
                transaction.PhoneNumber = phoneNumber;
                transaction.MID = mid;
                transaction.TID = tid;
                transaction.AuthCode = authCode;
                transaction.AID = aid;
                transaction.RNN = rnn;
                transaction.Connector = connector;
                transaction.Amount = amount;
                transaction.TxId = txId;
                transaction.CustumErrorMessage = customErrorMessage;
                transaction.ChargingPeriod = chargingPeriod;
                transaction.Status = retrievedStatus;
                transaction.NoOfRetries = noOfRetries;
                transaction.ReceiveSalesCompletionDateTime = receiveSalesCompletionDateTime;

                transactionsList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionsList;
    }


    public ArrayList<TransactionTableDB> getFailedSalesCompletionTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TransactionTableDB> transactionsList = new ArrayList<>();

        String query = "SELECT * FROM " + TRANSACTION_TABLE + " WHERE " + STATUS + " = 'F'";
        Cursor cursor = db.rawQuery(query, new String[]{});

        if (cursor.moveToFirst()) {
            do {
                String componentCode = cursor.getString(cursor.getColumnIndexOrThrow(COMPONENT_CODE));
                String transactionTrace = cursor.getString(cursor.getColumnIndexOrThrow(TRANSACTION_TRACE));
                String cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(CARD_NUMBER));
                String hashPAN = cursor.getString(cursor.getColumnIndexOrThrow(HASH_PAN));
                int systemPaymentId = cursor.getInt(cursor.getColumnIndexOrThrow(SYSTEM_PAYMENT_ID));
                int cardType = cursor.getInt(cursor.getColumnIndexOrThrow(CARD_TYPE));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(PHONE_NUMBER));
                String mid = cursor.getString(cursor.getColumnIndexOrThrow(MID));
                String tid = cursor.getString(cursor.getColumnIndexOrThrow(TID));
                String authCode = cursor.getString(cursor.getColumnIndexOrThrow(AUTH_CODE));
                String aid = cursor.getString(cursor.getColumnIndexOrThrow(AID));
                String rnn = cursor.getString(cursor.getColumnIndexOrThrow(RNN));
                int connector = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTOR));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(AMOUNT));
                int txId = cursor.getInt(cursor.getColumnIndexOrThrow(TX_ID));
                String customErrorMessage = cursor.getString(cursor.getColumnIndexOrThrow(CUSTUM_ERROR_MESSAGE));
                String chargingPeriod = cursor.getString(cursor.getColumnIndexOrThrow(CHARGING_PERIOD));
                String retrievedStatus = cursor.getString(cursor.getColumnIndexOrThrow(STATUS));
                int noOfRetries = cursor.getInt(cursor.getColumnIndexOrThrow(NO_OF_RETRIES));
                String receiveSalesCompletionDateTime = cursor.getString(cursor.getColumnIndexOrThrow(RECEIVE_SALES_COMPLETION_DATE_TIME));

                TransactionTableDB transaction = new TransactionTableDB();
                transaction.ComponentCode = componentCode;
                transaction.TransactionTrace = transactionTrace;
                transaction.CardNumber = cardNumber;
                transaction.HashPAN = hashPAN;
                transaction.SystemPaymentId = systemPaymentId;
                transaction.CardType = cardType;
                transaction.PhoneNumber = phoneNumber;
                transaction.MID = mid;
                transaction.TID = tid;
                transaction.AuthCode = authCode;
                transaction.AID = aid;
                transaction.RNN = rnn;
                transaction.Connector = connector;
                transaction.Amount = amount;
                transaction.TxId = txId;
                transaction.CustumErrorMessage = customErrorMessage;
                transaction.ChargingPeriod = chargingPeriod;
                transaction.Status = retrievedStatus;
                transaction.NoOfRetries = noOfRetries;
                transaction.ReceiveSalesCompletionDateTime = receiveSalesCompletionDateTime;

                transactionsList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionsList;
    }

    public ArrayList<TransactionTableDB> getExceededSalesCompletionTransactions() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TransactionTableDB> transactionsList = new ArrayList<>();

        String query = "SELECT * FROM " + TRANSACTION_TABLE + " WHERE " + STATUS + " = 'E'";
        Cursor cursor = db.rawQuery(query, new String[]{});

        if (cursor.moveToFirst()) {
            do {
                String componentCode = cursor.getString(cursor.getColumnIndexOrThrow(COMPONENT_CODE));
                String transactionTrace = cursor.getString(cursor.getColumnIndexOrThrow(TRANSACTION_TRACE));
                String cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(CARD_NUMBER));
                String hashPAN = cursor.getString(cursor.getColumnIndexOrThrow(HASH_PAN));
                int systemPaymentId = cursor.getInt(cursor.getColumnIndexOrThrow(SYSTEM_PAYMENT_ID));
                int cardType = cursor.getInt(cursor.getColumnIndexOrThrow(CARD_TYPE));
                String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(PHONE_NUMBER));
                String mid = cursor.getString(cursor.getColumnIndexOrThrow(MID));
                String tid = cursor.getString(cursor.getColumnIndexOrThrow(TID));
                String authCode = cursor.getString(cursor.getColumnIndexOrThrow(AUTH_CODE));
                String aid = cursor.getString(cursor.getColumnIndexOrThrow(AID));
                String rnn = cursor.getString(cursor.getColumnIndexOrThrow(RNN));
                int connector = cursor.getInt(cursor.getColumnIndexOrThrow(CONNECTOR));
                double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(AMOUNT));
                int txId = cursor.getInt(cursor.getColumnIndexOrThrow(TX_ID));
                String customErrorMessage = cursor.getString(cursor.getColumnIndexOrThrow(CUSTUM_ERROR_MESSAGE));
                String chargingPeriod = cursor.getString(cursor.getColumnIndexOrThrow(CHARGING_PERIOD));
                String retrievedStatus = cursor.getString(cursor.getColumnIndexOrThrow(STATUS));
                int noOfRetries = cursor.getInt(cursor.getColumnIndexOrThrow(NO_OF_RETRIES));
                String receiveSalesCompletionDateTime = cursor.getString(cursor.getColumnIndexOrThrow(RECEIVE_SALES_COMPLETION_DATE_TIME));

                TransactionTableDB transaction = new TransactionTableDB();
                transaction.ComponentCode = componentCode;
                transaction.TransactionTrace = transactionTrace;
                transaction.CardNumber = cardNumber;
                transaction.HashPAN = hashPAN;
                transaction.SystemPaymentId = systemPaymentId;
                transaction.CardType = cardType;
                transaction.PhoneNumber = phoneNumber;
                transaction.MID = mid;
                transaction.TID = tid;
                transaction.AuthCode = authCode;
                transaction.AID = aid;
                transaction.RNN = rnn;
                transaction.Connector = connector;
                transaction.Amount = amount;
                transaction.TxId = txId;
                transaction.CustumErrorMessage = customErrorMessage;
                transaction.ChargingPeriod = chargingPeriod;
                transaction.Status = retrievedStatus;
                transaction.NoOfRetries = noOfRetries;
                transaction.ReceiveSalesCompletionDateTime = receiveSalesCompletionDateTime;

                transactionsList.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionsList;
    }

}
