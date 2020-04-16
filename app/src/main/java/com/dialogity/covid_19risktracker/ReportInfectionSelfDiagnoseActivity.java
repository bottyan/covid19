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
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
        boolean s1 = ((CheckBox)findViewById(R.id.checkBox1)).isChecked();
        boolean s2 = ((CheckBox)findViewById(R.id.checkBox2)).isChecked();
        boolean s3 = ((CheckBox)findViewById(R.id.checkBox3)).isChecked();
        boolean s4 = ((CheckBox)findViewById(R.id.checkBox4)).isChecked();
        boolean s5 = ((CheckBox)findViewById(R.id.checkBox5)).isChecked();
        boolean s6 = ((CheckBox)findViewById(R.id.checkBox6)).isChecked();
        boolean s7 = ((CheckBox)findViewById(R.id.checkBox7)).isChecked();
        boolean s8 = ((CheckBox)findViewById(R.id.checkBox8)).isChecked();
        try {
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

            SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
            if (sharedPreferences.contains(MyApplication.MY_UUID_KEY)
                && sharedPreferences.contains(MyApplication.MY_TOKEN_KEY)) {
                body.put("token", sharedPreferences.getString(MyApplication.MY_TOKEN_KEY, null));
                body.put("uuid", sharedPreferences.getString(MyApplication.MY_UUID_KEY, null));
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://covid19api.dialogity.com/apiV1/report/";

            MyRequest request = new MyRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.w(TAG, "Response is: " + response);
                            // TODO: proper error handling
                            Toast.makeText(ReportInfectionSelfDiagnoseActivity.this, R.string.report_s_reported, Toast.LENGTH_LONG).show();
                            ReportInfectionSelfDiagnoseActivity.this.finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w(TAG, "Error");
                            Toast.makeText(ReportInfectionSelfDiagnoseActivity.this, R.string.report_s_submit_failed, Toast.LENGTH_LONG).show();
                        }
                    }
            );
            request.setBodyData(body);
            queue.add(request);
        } catch (JSONException e) {
            // never should happen
        }

    }
}
