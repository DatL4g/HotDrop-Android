package de.datlag.hotdrop.fragment

import android.content.DialogInterface
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.adroitandroid.near.model.Host
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.datlag.hotdrop.MainActivity
import de.datlag.hotdrop.R
import de.datlag.hotdrop.extend.AdvancedActivity
import de.datlag.hotdrop.p2p.HostTransfer
import de.datlag.hotdrop.util.FileUtil
import java.io.File

class TransferFragment : Fragment() {
    private var hostTransfer: HostTransfer? = null
    private lateinit var rootView: View
    private lateinit var hostName: AppCompatTextView
    private var defaultPath: String? = null
    private lateinit var disconnectHost: FloatingActionButton
    private lateinit var uploadFile: FloatingActionButton
    private lateinit var hostCheckedName: String
    private var animatable: Animatable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_transfer, container, false)
        init()
        initLogic()
        return rootView
    }

    private fun init() {
        hostName = rootView.findViewById(R.id.host_name)
        disconnectHost = rootView.findViewById(R.id.disconnect_host)
        uploadFile = rootView.findViewById(R.id.upload_file)

        if(advancedActivity is MainActivity) {
            animatable = (advancedActivity as MainActivity).backgroundImage.drawable as Animatable
        }
        if(animatable != null && animatable!!.isRunning) {
            animatable!!.stop()
        }
    }

    private fun initLogic() {
        hostTransfer = HostTransfer(advancedActivity, transferHost)
        hostCheckedName = transferHost.name.substring(1)
        hostName.text = hostCheckedName
        disconnectHost.setOnClickListener {
            advancedActivity.applyDialogAnimation(MaterialAlertDialogBuilder(advancedActivity)
                    .setTitle(hostCheckedName)
                    .setMessage("Are your sure you want to disconnect?")
                    .setPositiveButton(advancedActivity.getString(R.string.ok)) { _: DialogInterface?, _: Int -> onStop() }
                    .setNegativeButton(advancedActivity.getString(R.string.cancel), null)
                    .create()).show()
        }
        uploadFile.setOnClickListener {
            advancedActivity.applyDialogAnimation(MaterialAlertDialogBuilder(advancedActivity)
                    .setTitle("Sending File or Folder")
                    .setMessage("Folders are archived in a ZIP file and temporarily stored in the cache")
                    .setPositiveButton("File") { _: DialogInterface?, _: Int ->
                        FileUtil.chooseFile(advancedActivity, null, defaultPath, object: FileUtil.FileChooseCallback{
                            override fun onChosen(path: String?, file: File?) {
                                hostTransfer!!.startTransfer(file)
                                defaultPath = path
                            }

                        })
                    }.setNegativeButton("Folder") { _: DialogInterface?, _: Int ->
                        FileUtil.chooseFolder(advancedActivity, defaultPath, object: FileUtil.FolderChooseCallback {
                            override fun onChosen(path: String?, file: File?) {
                                hostTransfer!!.startTransfer(FileUtil.folderToFile(advancedActivity, file!!))
                                defaultPath = path
                            }
                        })
                    }.setNeutralButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.cancel() }
                    .create()).show()
        }
    }

    override fun onStop() {
        super.onStop()
        hostTransfer!!.stopTransferAndDisconnect()
        onDestroy()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (advancedActivity is MainActivity) {
            advancedActivity.switchFragment((activity as MainActivity?)!!.searchDeviceFragment, R.id.fragment_view)
        }
    }

    companion object {
        private lateinit var advancedActivity: AdvancedActivity
        private lateinit var transferHost: Host

        @JvmStatic
        fun newInstance(activity: AdvancedActivity, host: Host): TransferFragment {
            advancedActivity = activity
            transferHost = host
            return TransferFragment()
        }
    }
}