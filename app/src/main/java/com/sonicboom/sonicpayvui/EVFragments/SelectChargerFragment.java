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
import com.sonicboom.sonicpayvui.EVModels.GeneralVariable;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.SharedPrefUI;
import com.sonicboom.sonicpayvui.WelcomeFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer timeoutTimer;

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

        GeneralVariable.CurrentFragment = "SelectChargerFragment";
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_charger, container, false);
        
        // Get the GridLayout where you want to add the card views
        GridLayout cardContainer = view.findViewById(R.id.cardContainer);

        // Sort the componentList based on the componentName
        Arrays.sort(componentList, new Comparator<Component>() {
            @Override
            public int compare(Component c1, Component c2) {
                return c1.ComponentName.compareToIgnoreCase(c2.ComponentName);
            }
        });


        for (Component component : componentList) {
            // Inflate the card view layout
            View cardView = inflater.inflate(R.layout.card_charger_station, container, false);

            // Set the station name and status in the card view
            TextView chargingStation = cardView.findViewById(R.id.chargingStation);
            chargingStation.setText(component.ComponentName);
            TextView status = cardView.findViewById(R.id.status);

            ImageView statusIcon = cardView.findViewById(R.id.statusIcon);
            ImageView imageCharger = cardView.findViewById(R.id.imageViewCharger);
            LinearLayout statusLayout = cardView.findViewById(R.id.statusLayout);
            int color;

            if (component.Connectors.size() <= 1) {
                status.setText(component.Connectors.get(0).Status.toUpperCase(Locale.ROOT));
            } else{
                status.setText("AVAILABLE");
                status.setVisibility(View.INVISIBLE);
                statusIcon.setVisibility(View.INVISIBLE);
            }

            String statusText = status.getText().toString().toUpperCase(Locale.ROOT);


            if(status.getText() == ""){
                status.setText("OFFLINE");
            }

            switch (statusText) {
                case "PREPARING":
                case "AVAILABLE":
                    color = Color.parseColor("#008842");
                    break;
                case "OUTOFORDER":
                    color = Color.parseColor("#E02828");
                    break;
                case "CHARGING":
                case "STARTCHARGE":
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
//
            status.setTextColor(color);
            statusIcon.setImageTintList(ColorStateList.valueOf(color));
            imageCharger.setImageTintList(ColorStateList.valueOf(color));

//            // Add the card view to the container
            cardContainer.addView(cardView);
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
        Log.i("Timer Start", "Plugin Fragment Redirect");
        int GoBackToWelcomeTimeOutDuration = 3000;
        GoBackToWelcomeTimeOutDuration = Integer.parseInt( new SharedPrefUI(requireContext()).ReadSharedPrefStr(getString(R.string.GoBackToWelcomeTimeOutDuration)));
        timeoutTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Redirect to another fragment here
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
        // Perform the fragment redirection here
        LogUtils.i("Fragment Inactive, redirecting to Welcome Fragment", GeneralVariable.CurrentFragment);
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

