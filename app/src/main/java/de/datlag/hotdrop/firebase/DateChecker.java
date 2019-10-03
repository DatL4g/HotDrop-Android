package de.datlag.hotdrop.firebase;

import android.app.Activity;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.Timestamp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.datlag.hotdrop.R;

public class DateChecker {

    private Activity activity;

    public DateChecker(Activity activity) {
        this.activity = activity;
    }

    public void getDate(DateCheckerCallback dateCheckerCallback) {
        final String mURL = "http://worldclockapi.com/api/json/utc/now";
        RequestQueue queue = Volley.newRequestQueue(activity);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
                        String dateString = jsonObject.get("currentDateTime").getAsString();
                        Log.e("Date", dateString);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        boolean dateValid = true;
                        Date date = null;
                        try {
                            date = dateFormat.parse(dateString);
                        } catch (ParseException e) {
                            dateValid = false;
                            date = new Date();
                        }

                        Timestamp timestamp = new Timestamp(date);

                        if (dateValid) {
                            dateCheckerCallback.onSuccess(timestamp);
                        } else {
                            dateCheckerCallback.onFailure(timestamp);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Date date = new Date();
                Timestamp timestamp = new Timestamp(date);
                dateCheckerCallback.onFailure(timestamp);
            }
        });
        queue.add(stringRequest);
    }

    public interface DateCheckerCallback {
        void onSuccess(Timestamp timestamp);

        void onFailure(Timestamp timestamp);
    }
}
