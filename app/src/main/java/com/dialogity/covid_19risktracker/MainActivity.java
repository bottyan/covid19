package com.dialogity.covid_19risktracker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = AppCompatActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;

    private Menu menu = null;
    private String currentFragmenName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.main_title);
        }
        //toolbar.setSubtitle("Test Subtitle");
        //toolbar.inflateMenu(R.menu.main);
        toolbar.inflateMenu(R.menu.main_menu);

        checkAndRequestPermissions();
        checkState();

        // TODO: bluetooth tracking
        // TODO: status page for self reported infection w/ report recovery


        // TODO 2: link to explanation site
        // TODO 2: stop tracking
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //checkState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkState();
    }

    public void loadStatus() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://covid19api.dialogity.com/apiV1/status/";

        MyRequest request = new MyRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.w(TAG, "Response is: " + response);
                        if (response.contains("ok")) {
                            updateStatusView(0);
                        } else if (response.contains("infected")) {
                            updateStatusView(3);
                        } else if (response.contains("recovered")) {
                            updateStatusView(0);
                        } else if (response.contains("warning")) {
                            updateStatusView(1);
                        } else if (response.contains("danger")) {
                            updateStatusView(2);
                        }
                        // TODO: implement proper status selection
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.w(TAG, "Error");
                        Toast.makeText(MainActivity.this, R.string.main_status_failed, Toast.LENGTH_LONG).show();
                    }
                }
        );

        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(MyApplication.MY_UUID_KEY)
                && sharedPreferences.contains(MyApplication.MY_TOKEN_KEY)) {
            request.addBodyData("token", sharedPreferences.getString(MyApplication.MY_TOKEN_KEY, null));
            request.addBodyData("uuid", sharedPreferences.getString(MyApplication.MY_UUID_KEY, null));
        }
        queue.add(request);
    }

    private void updateStatusView(int status) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        if (status == 1) {
            currentFragmenName = "warning";
            Log.w(TAG, "Loading fragment WARNING");
            fragment = new StatusWarningFragment();
        } else if (status == 0) {
            currentFragmenName = "OK";
            Log.w(TAG, "Loading fragment OK");
            fragment = new StatusOkFragment();
        } else if (status == 2) {
            currentFragmenName = "danger";
            Log.w(TAG, "Loading fragment DANGER");
            fragment = new StatusDangerFragment();
        } else if (status == 3) {
            currentFragmenName = "infected";
            Log.w(TAG, "Loading fragment INFECTED");
            fragment = new StatusInfectedFragment();
        }
        if (currentFragmenName == null) {
            fragmentTransaction.add(R.id.ll_main_content, fragment);
        } else {
            try {
                fragmentTransaction.replace(R.id.ll_main_content, fragment);
            } catch (Exception e) {
                fragmentTransaction.add(R.id.ll_main_content, fragment);
            }
        }
        fragmentTransaction.commit();
    }

    private void checkState() {
        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(MyApplication.MY_UUID_KEY)) {
            if (sharedPreferences.contains(MyApplication.MY_TOKEN_KEY)) {
                if (menu != null) {
                    menu.findItem(R.id.mi_log_out).setVisible(true);
                    menu.findItem(R.id.mi_log_in).setVisible(false);
                    this.invalidateOptionsMenu();
                }
                // TODO: render main screen
            } else {
                startActivityForResult(new Intent(this, LoginActivity.class), 111);
                if (menu != null) {
                    menu.findItem(R.id.mi_log_out).setVisible(false);
                    menu.findItem(R.id.mi_log_in).setVisible(true);
                    this.invalidateOptionsMenu();
                }
            }
        } else {
            startActivityForResult(new Intent(this, WelcomeActivity.class), 111);
        }
        //updateStatusView((int)(Math.random()*3));
        loadStatus();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (this.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("This app needs background location access");
                            builder.setMessage("Please grant location access so this app can detect other phones running the same app in the background. "
                                    + "This is the basis of contact tracking, without this the app can not work properly.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @TargetApi(23)
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                                            PERMISSION_REQUEST_BACKGROUND_LOCATION);
                                }

                            });
                            builder.show();
                        }
                        else {
                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Functionality limited");
                            builder.setMessage("Since background location access has not been granted, this app will not be able to discover other users in the background.  Please go to Settings -> Applications -> Permissions and grant background location access to this app.");
                            builder.setPositiveButton(android.R.string.ok, null);
                            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                }

                            });
                            builder.show();
                        }
                    }
                }
            } else {
                if (!this.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            PERMISSION_REQUEST_FINE_LOCATION
                    );
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover other users.  Please go to Settings -> Applications -> Permissions and grant location access to this app.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_FINE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "fine location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover other users which is the basic of contact tracking.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
            case PERMISSION_REQUEST_BACKGROUND_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since background location access has not been granted, this app will not be able to discover other users when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private void logout() {
        // TODO: stop tracking
        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(MyApplication.MY_TOKEN_KEY)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(MyApplication.MY_TOKEN_KEY);
            editor.commit();
        }
        startActivityForResult(new Intent(this, LoginActivity.class), 111);
    }

    private void deleteAll() {
        // TODO: get approval
        new AlertDialog.Builder(this)
                .setTitle(R.string.main_confirm_dialog_title)
                .setMessage(R.string.main_confirm_dialog_text)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Toast.makeText(MainActivity.this, "Yaay", Toast.LENGTH_SHORT).show();
                        MainActivity.this.deleteAllFinalize();
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }
    private void deleteAllFinalize() {
        // TODO: implement server side delete
        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(MyApplication.MY_UUID_KEY)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(MyApplication.MY_UUID_KEY);
            editor.commit();
        }
        startActivityForResult(new Intent(this, WelcomeActivity.class), 111);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.w(TAG, "Menu item selected...");
        switch (item.getItemId()) {
            case R.id.mi_report:
                startActivityForResult(new Intent(this, ReportActivity.class), 111);
                return true;
            case R.id.mi_log_in:
                startActivityForResult(new Intent(this, LoginActivity.class), 111);
                return true;
            case R.id.mi_log_out:
                this.logout();
                return true;
            case R.id.mi_delete_all:
                this.deleteAll();
                return true;
        }
        return false;
}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        refreshMenu();

        return true;
    }

    private void refreshMenu() {
        if (menu != null) {
            SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
            if (sharedPreferences.contains(MyApplication.MY_TOKEN_KEY)) {
                menu.findItem(R.id.mi_log_out).setVisible(true);
                menu.findItem(R.id.mi_log_in).setVisible(false);
            } else {
                menu.findItem(R.id.mi_log_out).setVisible(false);
                menu.findItem(R.id.mi_log_in).setVisible(true);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // handle go back
        super.onActivityResult(requestCode, resultCode, data);
        this.refreshMenu();
        this.checkState();

    }

    public void unreport(View view) {
        try {
            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            body.put("data", data);
            data.put("recovered", true);

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
                            Toast.makeText(MainActivity.this, R.string.recovery_reported, Toast.LENGTH_LONG).show();
                            MainActivity.this.loadStatus();
                            // TODO: parent reload status
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w(TAG, "Error");
                            Toast.makeText(MainActivity.this, R.string.report_s_submit_failed, Toast.LENGTH_LONG).show();
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
