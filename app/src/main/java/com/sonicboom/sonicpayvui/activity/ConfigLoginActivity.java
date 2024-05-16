package com.sonicboom.sonicpayvui.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.sankuai.waimai.router.Router;
import com.sankuai.waimai.router.annotation.RouterUri;
import com.sonicboom.sonicpayvui.R;
import com.sonicboom.sonicpayvui.RouterConst;
import com.sonicboom.sonicpayvui.WelcomeFragment;
import com.sonicboom.sonicpayvui.utils.LogUtils;

import java.util.Objects;

@RouterUri(path= {RouterConst.CONFIG_LOGIN})
public class ConfigLoginActivity extends AppCompatActivity {
    private final String TAG = "ConfigLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.d(TAG, "onCreate started...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_login);

        Toolbar toolbar = findViewById(R.id.config_login_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Maintenance");

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        LogUtils.d(TAG, "onCreate ended.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onKeyBackDown();
        }
        return true;
    }

    protected void onKeyBackDown() {
        super.onBackPressed();
    }

    public void onConfirm(View view){
        LogUtils.d(TAG, "onConfirm started...");
        EditText password = findViewById(R.id.maintenance_password);

        if(password.getText().length() == 0) {
            password.setError("Required");
            return;
        }

        if(password.getText().toString().equals("841367")){
            LogUtils.i(TAG, "onConfirm: Password matched");
            Router.startUri(this, RouterConst.CONFIG_MAIN);

        }
        else{
            Toast.makeText(this, "Wrong Password", Toast.LENGTH_SHORT).show();
        }
        password.getText().clear();
        LogUtils.d(TAG, "onConfirm ended.");
    }

}