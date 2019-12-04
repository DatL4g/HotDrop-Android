package de.datlag.hotdrop.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.p2p.HostTransfer;
import de.datlag.hotdrop.util.FileUtil;


public class TransferFragment extends Fragment {

    private static AdvancedActivity activity;
    private HostTransfer hostTransfer;
    private static Host host;
    private View rootView;
    private AppCompatTextView hostName;
    private String defaultPath = null;
    private FloatingActionButton disconnectHost;
    private FloatingActionButton uploadFile;
    private String hostCheckedName;

    @NotNull
    @Contract("_, _ -> new")
    public static TransferFragment newInstance(AdvancedActivity activity, Host host) {
        TransferFragment.activity = activity;
        TransferFragment.host = host;
        return new TransferFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_transfer, container, false);
        init();
        initLogic();
        return rootView;
    }

    private void init() {
        hostName = rootView.findViewById(R.id.host_name);
        disconnectHost = rootView.findViewById(R.id.disconnect_host);
        uploadFile = rootView.findViewById(R.id.upload_file);
    }

    private void initLogic() {
        hostTransfer = new HostTransfer(activity, host);
        hostCheckedName = host.getName().substring(host.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length());
        hostName.setText(hostCheckedName);

        disconnectHost.setOnClickListener((View view) -> {
                activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                        .setTitle(hostCheckedName)
                        .setMessage("Are your sure you want to disconnect?")
                        .setPositiveButton(activity.getString(R.string.ok), (DialogInterface dialogInterface, int i) -> {
                                onStop();
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .create()).show();
        });

        uploadFile.setOnClickListener((View view) -> {
            activity.applyDialogAnimation(new MaterialAlertDialogBuilder(activity)
                    .setTitle("Sending File or Folder")
                    .setMessage("Folders are archived in a ZIP file and temporarily stored in the cache")
                    .setPositiveButton("File", (DialogInterface dialogInterface, int i) -> {
                        FileUtil.chooseFile(activity, defaultPath, (String path, File file) -> {
                            hostTransfer.startTransfer(file);
                            defaultPath = path;
                        });
                    }).setNegativeButton("Folder", (DialogInterface dialogInterface, int i) -> {
                        FileUtil.chooseFolder(activity, defaultPath, (String path, File file) -> {
                            hostTransfer.startTransfer(FileUtil.folderToFile(activity, file));
                            defaultPath = path;
                        });
                    }).setNeutralButton("Cancel", (dialog, which) -> dialog.cancel())
                    .create()).show();
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        hostTransfer.stopTransferAndDisconnect();
        onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activity instanceof MainActivity) {
            activity.switchFragment(((MainActivity) activity).getSearchDeviceFragment(), R.id.fragment_view);
        }
    }
}