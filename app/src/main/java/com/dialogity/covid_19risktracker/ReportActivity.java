package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ReportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_report);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.report_title);
            getSupportActionBar().setSubtitle(R.string.report_subtitle);
        }

        // TODO: fill a form to self report infection
        // TODO: report get healthy
        // TODO: report self quarantine 1w / 2w done
    }

    public void cancelReport(View view) {
        ReportActivity.this.finish();
    }

    public void selfReport(View view) {
        startActivity(new Intent(this, ReportInfectionSelfDiagnoseActivity.class));
        this.finish();
    }

    public void testedReport(View view) {
        startActivity(new Intent(this, ReportInfectionTestedActivity.class));
        this.finish();
    }
}
