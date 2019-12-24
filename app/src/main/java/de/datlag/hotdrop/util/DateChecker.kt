package de.datlag.hotdrop.util

import android.app.Activity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.datlag.hotdrop.R
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateChecker(private val activity: Activity) {
    private var currentDate = Date()
    private var dateValid = false

    fun getDate(dateCheckerCallback: DateCheckerCallback) {
        val queue = Volley.newRequestQueue(activity)
        val stringRequest = StringRequest(Request.Method.GET, activity.getString(R.string.utc_date_url),
                Response.Listener { response: String? ->
                    val jsonObject = Gson().fromJson(response, JsonObject::class.java)
                    val dateString = jsonObject["utc_datetime"].asString
                    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                    dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                    try {
                        currentDate = dateFormat.parse(dateString)!!
                        dateValid = true
                    } catch (e: ParseException) {
                        dateValid = false
                    }
                    val timestamp = Timestamp(currentDate)
                    if (dateValid) {
                        dateCheckerCallback.onSuccess(timestamp)
                    } else {
                        dateCheckerCallback.onFailure(timestamp)
                    }
                }, Response.ErrorListener {
            val timestamp = Timestamp(currentDate)
            dateCheckerCallback.onFailure(timestamp)
        })
        queue.add(stringRequest)
    }

    interface DateCheckerCallback {
        fun onSuccess(timestamp: Timestamp?)
        fun onFailure(timestamp: Timestamp?)
    }

}