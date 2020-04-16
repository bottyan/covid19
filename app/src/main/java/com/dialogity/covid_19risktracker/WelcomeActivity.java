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

    UUID _UUID = null;

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

        _UUID = getUUID();
        TextView textViewUUID = (TextView) findViewById(R.id.textViewUUID);
        textViewUUID.setText(_UUID.toString());

    }

    private UUID getUUID() {
        return  UUID.randomUUID();
    }

    private void saveData(UUID uuid, String token) {
        SharedPreferences sharedPreferences = getSharedPreferences(MyApplication.MY_PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(MyApplication.MY_UUID_KEY, uuid.toString());
        editor.putString(MyApplication.MY_TOKEN_KEY, token);
        editor.commit();
    }

    public void acceptAndStart(View view) {
        // TODO: send UUID and password to the server
        // TODO: proper error handling
        // TODO: save password too - UUID is public in a sense that it's broadcast by bluetooth

        EditText etpswd1 = (EditText) findViewById(R.id.editTextPassword);
        EditText etpswd2 = (EditText) findViewById(R.id.editText_pswd2);
        final String pswd1 = etpswd1.getText().toString();
        String pswd2 = etpswd2.getText().toString();
        if (pswd1.equals(pswd2)) {

            RequestQueue queue = Volley.newRequestQueue(this);
            String url ="https://covid19api.dialogity.com/apiV1/register/";

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
                                    saveData(_UUID, token);
                                    WelcomeActivity.this.finish();
                                    return;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error while parsing JSON response.", e);
                                }
                            }
                            Log.e(TAG, "Error, no token in response.");
                            Toast.makeText(WelcomeActivity.this, R.string.welcome_error, Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w(TAG, "Error");
                            Toast.makeText(WelcomeActivity.this, R.string.welcome_error, Toast.LENGTH_LONG).show();
                        }
                    }
                );
            stringRequest.addBodyData("uuid", _UUID.toString());
            stringRequest.addBodyData("password", pswd1);

            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        } else {
            Toast.makeText(this, R.string.welcome_notification_two_pswd_dif, Toast.LENGTH_LONG).show();
        }
    }
}
