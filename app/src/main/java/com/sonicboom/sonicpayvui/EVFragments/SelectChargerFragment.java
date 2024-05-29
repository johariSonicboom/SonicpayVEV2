package com.sonicboom.sonicpayvui.EVFragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.sonicboom.sonicpayvui.EVModels.Component;
import com.sonicboom.sonicpayvui.R;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectChargerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectChargerFragment extends Fragment {

    private Component[] componentList; // List of Component objects
    private int selectedConnectorIndex;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static class ViewHolder {
        TextView textView1;
        TextView textView11;
    }

    public SelectChargerFragment() {
        // Required empty public constructor
    }

    // Constructor to accept the list of Component objects
    public SelectChargerFragment(Component[] componentList, int selectedConnectorIndex) {
        this.componentList = componentList;
        this.selectedConnectorIndex = selectedConnectorIndex;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectChargerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectChargerFragment newInstance(String param1, String param2) {
        SelectChargerFragment fragment = new SelectChargerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_charger, container, false);
        
        // Get the GridLayout where you want to add the card views
        GridLayout cardContainer = view.findViewById(R.id.cardContainer);


        for (Component component : componentList) {
            // Inflate the card view layout
            View cardView = inflater.inflate(R.layout.card_charger_station, container, false);

            // Set the station name and status in the card view
            TextView chargingStation = cardView.findViewById(R.id.chargingStation);
            chargingStation.setText(component.ComponentName);

            TextView status = cardView.findViewById(R.id.status);
            status.setText(component.Status);

            String connectorStatus = "";
            if(component.Connectors.size() == 1){
                selectedConnectorIndex = 0;
            }

            if ((selectedConnectorIndex - 1) < component.Connectors.size()) {
                connectorStatus = component.Connectors.get(selectedConnectorIndex).Status;
            } else {
                connectorStatus = component.Connectors.get(0).Status;
            }



            if(!(connectorStatus == null)) {
                if (connectorStatus.toUpperCase(Locale.ROOT).equals("STOPCHARGE") || connectorStatus.toUpperCase(Locale.ROOT).equals("BOOTNOTIFICATION") || connectorStatus.toUpperCase(Locale.ROOT).equals("PREPARING")) {
                    status.setText("AVAILABLE");
                } else if (connectorStatus.toUpperCase(Locale.ROOT).equals("STARTCHARGE") || connectorStatus.toUpperCase(Locale.ROOT).equals("CHARGING")) {
                    status.setText("CHARGING");
                } else {
                    status.setText(connectorStatus.toUpperCase(Locale.ROOT));
                }
            }

            if(status.getText() == ""){
                status.setText("OFFLINE");
            }

            ImageView statusIcon = cardView.findViewById(R.id.statusIcon);
            ImageView imageCharger = cardView.findViewById(R.id.imageViewCharger);
            LinearLayout statusLayout = cardView.findViewById(R.id.statusLayout);

            String statusText = status.getText().toString().toUpperCase(Locale.ROOT);
            //Red
//            if (!status.getText().equals("Available")) {
//                int occupiedColor = Color.parseColor("#E02828");
//                status.setTextColor(occupiedColor);
//                statusIcon.setImageTintList(ColorStateList.valueOf(occupiedColor));
//                imageCharger.setImageTintList(ColorStateList.valueOf(occupiedColor));
//            }
//
//            //Charging
//            if (status.getText().equals("Charging")) {
//                int chargingdColor = Color.parseColor("#ff8c00");
//                status.setTextColor(chargingdColor);
//                statusIcon.setImageTintList(ColorStateList.valueOf(chargingdColor));
//                imageCharger.setImageTintList(ColorStateList.valueOf(chargingdColor));
//            }
//
//            //Offline
//            if (status.getText().equals("Offline")) {
//                int offlineColor = Color.parseColor("#C5C5C5");
//                status.setTextColor(offlineColor);
//                statusIcon.setImageTintList(ColorStateList.valueOf(offlineColor));
//                imageCharger.setImageTintList(ColorStateList.valueOf(offlineColor));
//                statusLayout.setPadding(88, 0, 0, 0);
//            }


            int color;

            switch (statusText) {
                case "AVAILABLE":
                    color = Color.parseColor("#008842");
                    break;
                case "OUTOFORDER":
                    color = Color.parseColor("#E02828");
                    break;
                case "CHARGING":
                case "BLOCKED":
                    color = Color.parseColor("#ff8c00");
                    break;
                case "UNKNOWN":
                case "INOPERATIVE":
                    color = Color.parseColor("#C5C5C5");
                    break;
                default:
                    // Set a default color if necessary
                    color = Color.parseColor("#C5C5C5"); // Example default color
                    break;
            }

            status.setTextColor(color);
            statusIcon.setImageTintList(ColorStateList.valueOf(color));
            imageCharger.setImageTintList(ColorStateList.valueOf(color));

//            if (statusText.equals("UNKNOWN") || statusText.equals("INOPERATIVE")) {
//                statusLayout.setPadding(88, 0, 0, 0);
//            }


            // Add the card view to the container
            cardContainer.addView(cardView);
        }

        return view;
    }

}
