package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
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
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            body.put("data", data);
            data.put("s1", s1);
            data.put("s2", s2);
            data.put("s3", s3);
            data.put("s4", s4);
            data.put("s5", s5);
            data.put("s6", s6);
            data.put("s7", s7);
            data.put("s8", s8);

            String[] allOtherTokens = new String[] {"token1", "token2"};

            json.put("to", "/topics/" + MyFirebaseMessagingService.TOPIC_NAME);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title", "REPORT");
            notificationObj.put("body", allOtherTokens);
            //replace notification with data when went send data
            json.put("notification", notificationObj);
            // TODO: check from here https://firebase.google.com/docs/cloud-messaging/android/topic-messaging?authuser=0#rest

            String URL = "https://fcm.googleapis.com/fcm/send";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.e(TAG, response.toString());
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "key=" + getString(R.string.fcm_api_key));
                    return header;
                }
            };

            mRequestQue.add(request);

            // TODO: save alert to the DB

        } catch (JSONException e) {
            // never should happen
        }

    }
}
