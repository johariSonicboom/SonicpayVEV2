package com.sonicboom.sonicpayvui.models;

public class SaleP1P2 {
    public class SaleP1Response{
        public boolean IsValidCard;
        public ReadCard.ReadCardResponse ReadCardResult;
        public Sale.SaleResponse SalesResult;
    }

    public class SaleP2Request{
        public int Action;
    }
}
