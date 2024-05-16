package com.sonicboom.sonicpayvui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.sonicboom.sonicpayvui.models.AppUpdate;

import java.util.List;

public class CheckboxListAdapter extends BaseAdapter {
    private Context context;
    private List<AppUpdate.ApkInfo> items;
    private LayoutInflater inflater;
    private int checkedPosition = -1;

    public CheckboxListAdapter(Context context, List<AppUpdate.ApkInfo> items) {
        this.context = context;
        this.items = items;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCheckedPosition() {return checkedPosition;}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.checkbox_list_item, parent, false);
        }

        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        //checkBox.setText(items.get(position).appName);
        checkBox.setChecked(position == checkedPosition);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkedPosition != position) {
                    checkedPosition = position;
                    notifyDataSetChanged();
                }
            }
        });

        // Set up additional dynamic views here if needed
        TextView appName = convertView.findViewById(R.id.appName);
        appName.setText(items.get(position).appName);

        TextView appVersion = convertView.findViewById(R.id.appVersion);
        appVersion.setText(items.get(position).versionName);
        return convertView;
    }
}
