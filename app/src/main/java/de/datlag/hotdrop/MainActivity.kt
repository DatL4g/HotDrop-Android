package de.datlag.hotdrop

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.adroitandroid.near.model.Host
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.firebase.StorageManager
import de.datlag.hotdrop.fragment.ChooseDeviceFragment.OnFragmentInteractionListener
import de.datlag.hotdrop.fragment.SearchDeviceFragment
import de.datlag.hotdrop.manager.InfoPageManager
import de.datlag.hotdrop.manager.PermissionManager
import de.datlag.hotdrop.manager.SettingsManager
import de.datlag.hotdrop.p2p.DiscoverHost
import de.interaapps.firebasemanager.auth.*
import de.interaapps.firebasemanager.core.FirebaseManager
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import java.util.*

class MainActivity : AdvancedActivity(), OnFragmentInteractionListener {
    private val activity: AdvancedActivity = this@MainActivity
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    val backgroundImage: AppCompatImageView by bindView(R.id.background_image)
    private val speedDialView: SpeedDialView by bindView(R.id.speedDial)
    private val tuneButton: AppCompatImageView by bindView(R.id.tune_button)
    private val revealButton: AppCompatImageView by bindView(R.id.reveal_button)
    private val reverseRevealButton: AppCompatImageView by bindView(R.id.reverse_reveal)
    private val infoLayout: FrameLayout by bindView(R.id.info_layout)
    private val mainLayout: FrameLayout by bindView(R.id.frame)
    private val githubIcon: AppCompatImageView by bindView(R.id.github_icon)
    private val codeIcon: AppCompatImageView by bindView(R.id.code_icon)
    private val helpIcon: AppCompatImageView by bindView(R.id.help_icon)
    private val adView: AdView by bindView(R.id.adView)


    private lateinit var permissionManager: PermissionManager
    private lateinit var infoPageManager: InfoPageManager
    private lateinit var settingsManager: SettingsManager
    private lateinit var firebaseManager: FirebaseManager
    private lateinit var storageManager: StorageManager
    private lateinit var adRequest: AdRequest
    private lateinit var googleAuth: GoogleAuth
    private lateinit var emailAuth: EmailAuth
    private lateinit var githubAuth: OAuth
    private lateinit var anonymousAuth: AnonymousAuth
    private lateinit var markwon: Markwon

    lateinit var searchDeviceFragment: SearchDeviceFragment

    private val menuItems = ArrayList<SpeedDialActionItem>()
    private var adLoaded = false
    private var speedDialMoved = false
    private lateinit var discoverHost: DiscoverHost


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImportant()
        initViewLogic()
        initLogic()
        initListener()
        initDeeplink()
        initReceive()
    }

    private fun initImportant() {
        createShortcuts()
        checkUpdate()

        permissionManager = PermissionManager(activity)
        permissionManager.check()
        MobileAds.initialize(activity, getString(R.string.admob_app_id))
        adRequest = AdRequest.Builder().build()
        firebaseManager = FirebaseManager(activity)
        googleAuth = GoogleAuth(activity, getString(R.string.default_web_client_id))
        emailAuth = EmailAuth(activity)
        githubAuth = OAuth(activity, OAuthEnum.GITHUB)
        anonymousAuth = AnonymousAuth(activity)
        firebaseManager.authManager.addLogin(googleAuth)
        firebaseManager.authManager.addLogin(emailAuth)
        firebaseManager.authManager.addLogin(githubAuth)
        firebaseManager.authManager.addLogin(anonymousAuth)
        settingsManager = SettingsManager(activity, firebaseManager)
        storageManager = StorageManager(activity, firebaseManager, settingsManager)
        discoverHost = DiscoverHost(activity)
    }

    private fun initViewLogic() {
        menuItems.add(SpeedDialActionItem.Builder(downloadID, R.drawable.ic_cloud_download_white_24dp)
                .setFabImageTintColor(Color.WHITE)
                .create())
        menuItems.add(SpeedDialActionItem.Builder(uploadID, R.drawable.ic_cloud_upload_white_24dp)
                .setFabImageTintColor(Color.WHITE)
                .create())
        speedDialView.addAllActionItems(menuItems)
        adView.loadAd(adRequest)
    }

    private fun initLogic() {
        setSupportActionBar(toolbar)
        checkConfiguration()
        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build()
        searchDeviceFragment = SearchDeviceFragment.newInstance(activity, discoverHost)
        switchFragment(searchDeviceFragment, R.id.fragment_view)
        infoPageManager = InfoPageManager(activity, mainLayout, infoLayout)
        infoPageManager.codeIcon = codeIcon
        infoPageManager.githubIcon = githubIcon
        infoPageManager.helpIcon = helpIcon
        infoPageManager.init()
    }

    private fun initListener() {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                adLoaded = true
                moveSpeedDial(true)
            }

            override fun onAdClosed() {
                super.onAdClosed()
                adLoaded = false
                moveSpeedDial(false)
            }

            override fun onAdFailedToLoad(i: Int) {
                super.onAdFailedToLoad(i)
                adLoaded = false
                moveSpeedDial(false)
            }
        }
        revealButton.setOnClickListener { infoPageManager.start(false) }
        reverseRevealButton.setOnClickListener { infoPageManager.start(true) }

        speedDialView.setOnActionSelectedListener { actionItem: SpeedDialActionItem ->
            if (actionItem.id == uploadID) {
                storageManager.upload()
            } else if (actionItem.id == downloadID) {
                val inflater = activity.layoutInflater
                val linearLayoutCompat = LinearLayoutCompat(activity)
                inflater.inflate(R.layout.download_dialog, linearLayoutCompat)
                val appCompatEditText: AppCompatEditText = linearLayoutCompat.findViewById(R.id.download_edittext)
                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                        .setTitle("Download file")
                        .setView(linearLayoutCompat)
                        .setPositiveButton(activity.getString(R.string.okay)) { _: DialogInterface?, _: Int -> storageManager.download(appCompatEditText.text.toString()) }
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .create()).show()
            }
            false
        }

        tuneButton.setOnClickListener { settingsManager.open() }
    }

    private fun initDeeplink() {
        val data = activity.intent.data
        if (data != null && data.isHierarchical) {
            val url = activity.intent.dataString
            if (isURLValid(url!!)) {
                askDownload(url)
            }
        }
    }

    private fun initReceive() {
        val intent = activity.intent
        val action = intent.action
        val type = intent.type
        if (action == Intent.ACTION_SEND && type != null) {
            if (type == "text/plain") {
                val url = intent.getStringExtra(Intent.EXTRA_TEXT)
                if (isURLValid(url)) {
                    askDownload(url)
                }
            } else {
                TODO()
            }
        }
    }

    private fun askDownload(url: String?) {
        activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle("Cloud File")
                .setMessage("Do you want to download the file from this link:\n\n$url")
                .setPositiveButton(R.string.okay) { _: DialogInterface?, _: Int -> storageManager.download(url) }
                .setNegativeButton(R.string.cancel) { dialog: DialogInterface, _: Int -> dialog.cancel() }
                .setNeutralButton("Copy Link") { _: DialogInterface?, _: Int -> activity.copyText("HotDrop Download URL", url!!) }
                .create()).show()
    }

    private fun isURLValid(url: String?): Boolean {
        if (url != null) {
            return url.contains("gs://hotdrop-420.appspot.com")
        }
        return false
    }

    private fun moveSpeedDial(up: Boolean) {
        if (up) {
            if (!speedDialMoved && adLoaded) {
                val params = speedDialView.layoutParams as MarginLayoutParams
                params.setMargins(params.marginStart, params.topMargin, params.marginEnd, params.bottomMargin + actionBarHeight)
                speedDialView.layoutParams = params
                speedDialMoved = true
            }
        } else {
            if (speedDialMoved) {
                val params = speedDialView.layoutParams as MarginLayoutParams
                params.setMargins(params.marginStart, params.topMargin, params.marginEnd, params.bottomMargin - actionBarHeight)
                speedDialView.layoutParams = params
                speedDialMoved = false
            }
        }
    }

    private fun checkUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(activity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener {
            if(it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(it, AppUpdateType.IMMEDIATE, activity, UPDATE_REQUEST_CODE)
            }
        }
    }

    override fun onChooseFragmentInteraction(host: Host?) {
        host?.let {
            activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setTitle(it.name)
                    .setMessage("Request Connection with ${host.name}")
                    .setPositiveButton("Request") { _, _ ->
                        discoverHost.send(host, DiscoverHost.MESSAGE_REQUEST_START_TRANSFER.toByteArray())
                    }
                    .setNeutralButton("Cancel", null)
                    .create()).show()
        }
    }

    override fun onOrientationLandscape() {
        moveSpeedDial(false)
    }

    override fun onOrientationPortrait() {
        moveSpeedDial(true)
    }

    override fun onStart() {
        super.onStart()
        firebaseManager.authManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        moveSpeedDial(false)
        discoverHost.stopDiscovery()
    }

    override fun onResume() {
        super.onResume()
        moveSpeedDial(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        activity.finishAffinity()
        discoverHost.stopDiscovery()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == UPDATE_REQUEST_CODE) {
            if(resultCode != Activity.RESULT_OK) {
                activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                        .setTitle("Update")
                        .setMessage("Update flow failed!\nTry again or update in Google Play Store")
                        .setPositiveButton("Try again"){
                            _, _ -> checkUpdate()
                        }
                        .setNegativeButton("Google Play") {
                            _, _ -> browserIntent("https://play.google.com/store/apps/details?id=de.datlag.hotdrop")
                        }
                        .create()).show()
            }
        } else googleAuth.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        private val downloadID = ViewCompat.generateViewId()
        private val uploadID = ViewCompat.generateViewId()
        private const val UPDATE_REQUEST_CODE = 1337
    }
}