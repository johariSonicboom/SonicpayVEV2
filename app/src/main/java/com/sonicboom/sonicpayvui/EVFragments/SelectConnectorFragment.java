package com.sonicboom.sonicpayvui.EVFragments;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.sonicboom.sonicpayvui.EVModels.Component;
import com.sonicboom.sonicpayvui.EVModels.Connector;
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.WelcomeFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectConnectorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectConnectorFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Component component; // List of Component objects

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Timer timeoutTimer;

    public SelectConnectorFragment() {
        // Required empty public constructor
    }

    public SelectConnectorFragment(Component component) {
        this.component = component;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SelectConnectorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SelectConnectorFragment newInstance(String param1, String param2) {
        SelectConnectorFragment fragment = new SelectConnectorFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    //Current implementation does not use the select connector fragment
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

        GeneralVariable.CurrentFragment = "SelectConnectorFragment";
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_select_connector, container, false);

        // Get the GridLayout where you want to add the card views
        GridLayout cardContainer2 = view.findViewById(R.id.cardConnectorContainer);

        for (Connector connector : component.Connectors) {
            // Inflate the card view layout
            if (connector.Status != "REMOVED") {
                View cardView2 = inflater.inflate(R.layout.card_connector, container, false);

                // Set the station name and status in the card view
                TextView connectorText = cardView2.findViewById(R.id.connector);
                connectorText.setText(String.valueOf(connector.ConnectorId));

                TextView status = cardView2.findViewById(R.id.status);
                status.setText(connector.Status.toUpperCase(Locale.ROOT));

                String connectorStatus = connector.Status.toUpperCase(Locale.ROOT);

                if (connector.Status != null && !connector.Status.isEmpty()) {
                    if (connectorStatus.equals("STOPCHARGE") || connectorStatus.equals("BOOTNOTIFICATION")) {
                        status.setText("AVAILABLE");
                    } else if (connectorStatus.equals("STARTCHARGE") || connectorStatus.equals("CHARGING")) {
                        status.setText("CHARGING");
                    } else if (connectorStatus.equals("PREPARING")) {
                        status.setText("PREPARING");
                    }
                } else {
                    status.setText("OFFLINE");
                }

                if(status.getText() == ""){
                    status.setText("OFFLINE");
                }

                ImageView statusIcon = cardView2.findViewById(R.id.statusIcon);
                ImageView imageCharger = cardView2.findViewById(R.id.imageViewConnector);
                LinearLayout statusLayout = cardView2.findViewById(R.id.statusLayout);

                String statusText = status.getText().toString().toUpperCase(Locale.ROOT);

                int color;

                switch (statusText) {
                    case "PREPARING":
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

//                if (statusText.equals("UNKNOWN") || statusText.equals("INOPERATIVE")) {
//                    statusLayout.setPadding(88, 0, 0, 0);
//                }

                // Add the card view to the container
                cardContainer2.addView(cardView2);
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
        startTimerForTimeout();
    }

    public void startTimerForTimeout() {
        timeoutTimer = new Timer();
        int GoBackToWelcomeTimeOutDuration = 300000;
        GoBackToWelcomeTimeOutDuration = Integer.parseInt( new SharedPrefUI(requireContext()).ReadSharedPrefStr(getString(R.string.GoBackToWelcomeTimeOutDuration)));
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                redirectToAnotherFragment();
            }
        }, GoBackToWelcomeTimeOutDuration); //
    }


    public void stopTimerForTimeout() {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }
    }



    @Override
    public void onPause() {
        super.onPause();
        stopTimerForTimeout();
    }


    // Method to redirect to another fragment
    private void redirectToAnotherFragment() {
        LogUtils.i("Fragment Inactive, redirecting to Welcome Fragment", GeneralVariable.CurrentFragment);
        // Perform the fragment redirection here
        if (isAdded() && getActivity() != null) {
            Bundle bundle = new Bundle();
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, WelcomeFragment.class, bundle)
                    .addToBackStack(null)
                    .commit();
        }
    }


}
