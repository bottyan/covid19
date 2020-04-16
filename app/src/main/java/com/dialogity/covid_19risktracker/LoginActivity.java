package com.dialogity.covid_19risktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_login);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.login_title);
            getSupportActionBar().setSubtitle(R.string.login_subtitle);
        }
    }

    public void login(View view) {

        String password = ((EditText)findViewById(R.id.editText_pswd)).getText().toString();
        String uuid = null;
        final SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        if (sharedPreferences.contains(MyApplication.MY_UUID_KEY)) {
            uuid = sharedPreferences.getString(MyApplication.MY_UUID_KEY, null);
        }

        if (uuid != null) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://covid19api.dialogity.com/apiV1/token/";

            MyRequest stringRequest = new MyRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // Display the first 500 characters of the response string.
                            Log.w(TAG, "Response is: " + response);
                            if (response.contains("token")) {
                                try {
                                    JSONObject jObject = new JSONObject(response);
                                    String token = jObject.getString("token");
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(MyApplication.MY_TOKEN_KEY, token);
                                    editor.commit();
                                    LoginActivity.this.finish();
                                    return;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error while parsing JSON response.", e);
                                }
                            }
                            Log.e(TAG, "Error, no token in response.");
                            Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w(TAG, "Error");
                            Toast.makeText(LoginActivity.this, R.string.login_error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
            stringRequest.addBodyData("uuid", uuid);
            stringRequest.addBodyData("password", password);

            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } else {
            Toast.makeText(LoginActivity.this, R.string.login_no_account_found, Toast.LENGTH_LONG).show();
        }
    }
}
