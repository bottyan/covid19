package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ReportInfectionTestedActivity extends AppCompatActivity {

    private static final String TAG = ReportInfectionTestedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_infection_tested);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_inf_tested);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.report_t_title);
            getSupportActionBar().setSubtitle(R.string.report_t_subtitle);
        }
    }

    public void submit(View view) {

        try {

            JSONObject data = new JSONObject();
            JSONObject data2 = new JSONObject();
            data.put("data", data2);
            data.put("code", DataAccess.Report.CODE_INFECTED);
            data.put("timestamp", System.currentTimeMillis());
            data.put("type", "report");
            MyFirebaseMessagingService.sendReport(this, "infected_tested", data, DataAccess.Report.CODE_INFECTED);
            this.finish();
        } catch (JSONException e) {
            // never should happen
        }

    }
}
