package com.dialogity.covid_19risktracker;

import android.util.Log;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MyRequest extends StringRequest {


    JSONObject jsonBody = null;

    public MyRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public void setBodyData(JSONObject data) {
        jsonBody = data;
    }

    public void addBodyData(String key, String value) {
        if (jsonBody == null) {
            jsonBody = new JSONObject();
        }
        try {
            jsonBody.put(key, value);
        } catch (Exception e) {
            // never should happen
            // TODO: proper error handling
        }
    }

    @Override
    public String getBodyContentType() {
        return "application/json; charset=utf-8";
        //return "application/json; charset=utf-8";
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        String mRequestBody = null;
        try {
            if (jsonBody != null) {
                mRequestBody = jsonBody.toString();
                Log.e(MyRequest.class.getSimpleName(), "POST url: " + getUrl());
                Log.e(MyRequest.class.getSimpleName(), "POST body: " + mRequestBody);
            }
            return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
            return null;
        }
    }
}
