// IAIDLCardCallbackInterface.aidl
package com.sbs.aidl;
import com.sbs.aidl.IAIDLClassInterface;
// Declare any non-default types here with import statements
interface IAIDLCardCallbackInterface
{
        void ReadCardCallback(in ReadCardResult result);
        void ParkingEntryCallback(in ParkingEntryResult result);
        void ParkingExitCallback(in ParkingExitResult result);
        void SalesCallback(in SalesResult result);
        void StatusCallback(in eTerminalState state,in boolean CardPresent);
        void SettlementCallback(in SettlementResult[] result);
        void QRTransactionCallback(in QRTransactionResult result);
        void SalesP1Callback(in SalesResult result);
        void SalesP3Callback(in SalesResult result);
        void SalesP3DebtRecoveryCallback(in SalesResult result, in int orinSystemID);
        void PreAuthCallback(in SalesResult result);
        void SalesCompletionCallback(in SalesCompletionResult result);
        void VoidCallback(in VoidResult result);
        void MaintenanceCallback(in String result);
        void ExecuteQueryCallback(in String result);
        void RefundCallback(in SalesResult result);
        void FareBasedEntryCallback(in FareBasedEntryResult result);
        void FareBasedExitCallback(in FareBasedExitResult result);
        void MaxChargedEntryCallback(in MaxChargedEntryResult result);
        void MaxChargedExitCallback(in MaxChargedExitResult result);
}