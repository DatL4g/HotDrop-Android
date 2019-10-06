package de.datlag.hotdrop.handler;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.Spanned;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import de.datlag.hotdrop.R;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;

public class InfoHandler {

    private Activity activity;

    public InfoHandler(Activity activity) {
        this.activity = activity;
    }

    public void informationPage(){

        new MaterialAlertDialogBuilder(activity)
                .setTitle("Info App / Creator")
                .setMessage("All information...")
                .setPositiveButton(activity.getString(R.string.okay), null)
                .setNeutralButton(activity.getString(R.string.data_protection_title), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        privacyPolicies();
                    }
                }).show();
    }

    public void privacyPolicies(){
        final String mURL = activity.getString(R.string.dsgvo_url)+"?viaJS=true";
        RequestQueue queue = Volley.newRequestQueue(activity);

        Markwon markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, mURL,
                new Response.Listener<String>() {
                    public void onResponse(String response) {
                        final Spanned markdown = markwon.toMarkdown(response);

                        new MaterialAlertDialogBuilder(activity)
                                .setTitle(activity.getString(R.string.data_protection_title))
                                .setMessage(markdown)
                                .setPositiveButton(activity.getString(R.string.okay), null)
                                .setNeutralButton(activity.getString(R.string.open_in_browser), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.dsgvo_url)));
                                        activity.startActivity(browserIntent);
                                    }
                                }).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(stringRequest);
    }



}
