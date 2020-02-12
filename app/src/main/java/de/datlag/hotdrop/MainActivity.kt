package de.datlag.hotdrop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.fragment.ChooseDeviceFragment
import de.datlag.hotdrop.fragment.ChooseDeviceFragment.OnFragmentInteractionListener
import de.datlag.hotdrop.fragment.SearchDeviceFragment
import de.datlag.hotdrop.manager.InfoPageManager
import de.datlag.hotdrop.manager.PermissionManager
import de.datlag.hotdrop.manager.SettingsManager
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.html.HtmlPlugin
import de.datlag.hotdrop.fragment.SearchDeviceFragment.SearchCallback
import de.datlag.hotdrop.p2p.*
import de.datlag.hotdrop.util.FileUtil
import java.io.File

class MainActivity : AdvancedActivity(), OnFragmentInteractionListener, SearchCallback {
    private val activity: AdvancedActivity = this@MainActivity
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    val backgroundImage: AppCompatImageView by bindView(R.id.background_image)
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
    private lateinit var adRequest: AdRequest
    private lateinit var markwon: Markwon

    lateinit var searchDeviceFragment: SearchDeviceFragment
    var chooseDeviceFragment: ChooseDeviceFragment? = null
    var showingChooseFragment = false
    private lateinit var hostDiscovery: HostDiscovery
    private lateinit var fileTransfer: FileTransfer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initImportant()
        initViewLogic()
        initLogic()
        initListener()
        initReceive()
    }

    private fun initImportant() {
        createShortcuts()
        checkUpdate()

        permissionManager = PermissionManager(activity)
        permissionManager.check()
        MobileAds.initialize(activity, getString(R.string.admob_app_id))
        adRequest = AdRequest.Builder().build()
        hostDiscovery = HostDiscovery(activity)
    }

    private fun initViewLogic() {
        adView.loadAd(adRequest)
        settingsManager = SettingsManager(activity)
    }

    private fun initLogic() {
        setSupportActionBar(toolbar)
        checkConfiguration()
        markwon = Markwon.builder(activity)
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(HtmlPlugin.create())
                .build()
        searchDeviceFragment = SearchDeviceFragment.newInstance(activity)
        switchFragment(searchDeviceFragment, R.id.fragment_view)
        infoPageManager = InfoPageManager(activity, mainLayout, infoLayout)
        infoPageManager.codeIcon = codeIcon
        infoPageManager.githubIcon = githubIcon
        infoPageManager.helpIcon = helpIcon
        infoPageManager.init()

        fileTransfer = FileTransfer(activity)
    }

    private fun initListener() {
        revealButton.setOnClickListener { infoPageManager.start(false) }
        reverseRevealButton.setOnClickListener { infoPageManager.start(true) }

        tuneButton.setOnClickListener { settingsManager.open() }
    }

    private fun initReceive() {
        val intent = activity.intent
        val action = intent.action
        val type = intent.type
        if (action == Intent.ACTION_SEND && type != null) {
            TODO()
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

    override fun onChooseFragmentInteraction(host: Host) {
        /*
            activity.applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                    .setTitle(host.name)
                    .setMessage("Request Connection with ${host.name}")
                    .setPositiveButton("Request") { _, _ ->
                        host.send("START_TRANSFER".toByteArray())
                    }
                    .setNeutralButton("Cancel", null)
                    .create()).show()
         */
        applyDialogAnimation(MaterialAlertDialogBuilder(activity)
                .setTitle(host.name)
                .setMessage("Do you want to send a File or a Folder to this device?\nFolders will be packed in a ZIP-Archiv and then sending the .zip file.")
                .setPositiveButton("File") { _, _ ->
                    FileUtil.chooseFile(activity, null, null, object: FileUtil.FileChooseCallback{
                        override fun onChosen(path: String?, file: File?) {
                            fileTransfer.send(host, file!!)
                        }
                    })
                }
                .setNegativeButton("Folder") { _, _ ->
                    FileUtil.chooseFolder(activity, null, object: FileUtil.FolderChooseCallback {
                        override fun onChosen(path: String?, file: File?) {
                            fileTransfer.send(host, FileUtil.folderToFile(activity, file!!))
                        }
                    })
                }
                .setNeutralButton(R.string.cancel, null)
                .create()).show()
    }

    override fun onStop() {
        super.onStop()
        hostDiscovery.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        hostDiscovery.stop()
        activity.finishAffinity()
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
        }
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 1337
    }

    override fun onSearchChanged(search: Boolean) {
        if (search) {
            hostDiscovery.start(object: DiscoveryCallback {
                override fun onHostsFound(hosts: Set<Host>) {
                    if (hosts.isEmpty()) {
                        switchSearchAndChoose(false)
                        return
                    }

                    if (chooseDeviceFragment == null) {
                        chooseDeviceFragment = ChooseDeviceFragment.newInstance(hosts)
                        switchFragment(chooseDeviceFragment!!, R.id.fragment_view)
                    } else {
                        chooseDeviceFragment!!.setHosts(hosts)
                    }
                    switchSearchAndChoose(true)
                }
            })
        } else {
            hostDiscovery.stop()
            switchSearchAndChoose(false)
        }
    }

    private fun switchSearchAndChoose(showChooseFragment: Boolean) {
        if (showChooseFragment && !showingChooseFragment) {
            chooseDeviceFragment?.let {
                switchFragment(it, R.id.fragment_view)
                showingChooseFragment = true
            }
        } else {
            switchFragment(searchDeviceFragment, R.id.fragment_view)
            showingChooseFragment = false
        }
    }
}