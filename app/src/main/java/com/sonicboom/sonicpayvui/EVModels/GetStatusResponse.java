package com.sonicboom.sonicpayvui.EVModels;

import java.util.List;

public class GetStatusResponse {
    public String ComponentCode;
    public String FareChargeText;
    public String DescriptionText;
    public List<Connector> Connectors;

    // Default constructor
    public GetStatusResponse() {
    }
}
