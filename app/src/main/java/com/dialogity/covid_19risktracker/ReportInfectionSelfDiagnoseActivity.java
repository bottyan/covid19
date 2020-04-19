package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportInfectionSelfDiagnoseActivity extends AppCompatActivity {

    private static final String TAG = ReportInfectionSelfDiagnoseActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_infection_self_diagnose);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_sd);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.report_s_title);
            getSupportActionBar().setSubtitle(R.string.report_s_subtitle);
        }
    }

    public void submit(View view) {
        RequestQueue mRequestQue = Volley.newRequestQueue(this);
        boolean s1 = ((CheckBox)findViewById(R.id.checkBox1)).isChecked();
        boolean s2 = ((CheckBox)findViewById(R.id.checkBox2)).isChecked();
        boolean s3 = ((CheckBox)findViewById(R.id.checkBox3)).isChecked();
        boolean s4 = ((CheckBox)findViewById(R.id.checkBox4)).isChecked();
        boolean s5 = ((CheckBox)findViewById(R.id.checkBox5)).isChecked();
        boolean s6 = ((CheckBox)findViewById(R.id.checkBox6)).isChecked();
        boolean s7 = ((CheckBox)findViewById(R.id.checkBox7)).isChecked();
        boolean s8 = ((CheckBox)findViewById(R.id.checkBox8)).isChecked();
        try {
            JSONObject json = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject symptoms = new JSONObject();
            JSONObject data2 = new JSONObject();
            data.put("data", data2);
            data2.put("symptoms", symptoms);
            symptoms.put("s1", s1);
            symptoms.put("s2", s2);
            symptoms.put("s3", s3);
            symptoms.put("s4", s4);
            symptoms.put("s5", s5);
            symptoms.put("s6", s6);
            symptoms.put("s7", s7);
            symptoms.put("s8", s8);
            data.put("code", DataAccess.Report.CODE_SELF_DIAGNOSE);
            data.put("timestamp", System.currentTimeMillis());
            data.put("type", "report");

            MyFirebaseMessagingService.sendReport(this, "infected_self_diagnose", data, DataAccess.Report.CODE_SELF_DIAGNOSE);
            this.finish();
        } catch (JSONException e) {
            // never should happen
        }

    }
}
