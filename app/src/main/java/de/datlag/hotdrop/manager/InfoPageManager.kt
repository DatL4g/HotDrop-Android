package de.datlag.hotdrop.manager

import android.animation.Animator
import android.content.DialogInterface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.view.helper.MaterialSnackbar.configSnackbar
import io.codetail.animation.ViewAnimationUtils
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import kotlin.math.hypot
import kotlin.math.max

class InfoPageManager(private val activity: AdvancedActivity,
                      private val mainLayout: FrameLayout,
                      private val infoLayout: FrameLayout) {
    lateinit var githubIcon: AppCompatImageView
    lateinit var codeIcon: AppCompatImageView
    lateinit var helpIcon: AppCompatImageView

    fun init() {
        githubIcon.setOnClickListener { activity.browserIntent(activity.getString(R.string.github_repo)) }
        codeIcon.setOnClickListener {
            activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setTitle(activity.getString(R.string.dependencies))
                    .setItems(activity.resources.getStringArray(R.array.dependencies)) {
                        _: DialogInterface?,
                        which: Int -> activity.browserIntent(activity.resources.getStringArray(R.array.dependencies_link)[which])
                    }
                    .setPositiveButton(activity.getString(R.string.okay), null)
                    .create())
                    .show()
        }
        helpIcon.setOnClickListener { informationDialog() }
    }

    fun start(isReverse: Boolean) {
        if (isReverse) {
            infoLayout.visibility = View.GONE
            mainLayout.visibility = View.VISIBLE
        } else {
            infoReveal()
        }
    }

    private fun infoReveal() {
        val dx = max(mainLayout.right, infoLayout.width - mainLayout.right)
        val dy = max(0, infoLayout.height)
        val finalRadius = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val animator = ViewAnimationUtils.createCircularReveal(infoLayout, mainLayout.right, 0, 0f, finalRadius)
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.duration = duration
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                mainLayout.visibility = View.GONE
                infoLayout.visibility = View.VISIBLE
            }

            override fun onAnimationCancel(animation: Animator) {
                mainLayout.visibility = View.VISIBLE
                infoLayout.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animator) {}
        })
        infoLayout.visibility = View.VISIBLE
        animator.start()
    }

    fun informationDialog() {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle("Info App / Creator")
                .setMessage("All information...")
                .setPositiveButton(activity.getString(R.string.close), null)
                .setNeutralButton(activity.getString(R.string.data_protection_title)) { _: DialogInterface?, _: Int -> privacyPolicies() }
                .create()).show()
    }

    private fun privacyPolicies() {
        val mURL = activity.getString(R.string.dsgvo_url) + "?viaJS=true"
        val queue = Volley.newRequestQueue(activity)
        val markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build()
        val stringRequest = StringRequest(Request.Method.GET, mURL,
                Response.Listener { response: String? ->
                    val markdown = markwon.toMarkdown(response!!)
                    activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                            .setTitle(activity.getString(R.string.data_protection_title))
                            .setMessage(markdown)
                            .setPositiveButton(activity.getString(R.string.okay), null)
                            .setNeutralButton(activity.getString(R.string.open_in_browser)) { _: DialogInterface?, _: Int -> activity.browserIntent(activity.getString(R.string.dsgvo_url)) }
                            .create()).show()
                }, Response.ErrorListener {
            val snackbar = Snackbar.make(activity.findViewById(R.id.coordinator), "Error fetching Privacy Policies", Snackbar.LENGTH_LONG)
            configSnackbar(activity, snackbar)
            snackbar.show()
        })
        queue.add(stringRequest)
    }

    companion object {
        private const val duration: Long = 1000
    }

}