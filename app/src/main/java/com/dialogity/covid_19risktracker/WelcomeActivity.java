package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    String uuid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Toolbar toolbar = (Toolbar) findViewById(R.id.welcome_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.welcome_title);
            getSupportActionBar().setSubtitle(R.string.welcome_subtitle);
        }

        TextView textViewExplanation = (TextView) findViewById(R.id.textViewExplanation);
        String tmp = getString(R.string.welcome_explanation_html);
        Log.w(TAG, "STRING: " + tmp);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textViewExplanation.setText(Html.fromHtml(tmp, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textViewExplanation.setText(Html.fromHtml(tmp));
        }

        uuid = DataAccess.get(this).getMyUUID();
        //TextView textViewUUID = (TextView) findViewById(R.id.textViewUUID);
        //textViewUUID.setText(uuid);

    }

    public void acceptAndStart(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (this.uuid == null) {
            this.uuid = DataAccess.get(this).getMyUUID();
        }
        editor.putString(MyApplication.MY_UUID_KEY, this.uuid);
        editor.commit();
        WelcomeActivity.this.finish();
    }
}
