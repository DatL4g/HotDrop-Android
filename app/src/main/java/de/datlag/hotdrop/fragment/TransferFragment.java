package de.datlag.hotdrop.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.datlag.hotdrop.MainActivity;
import de.datlag.hotdrop.R;
import de.datlag.hotdrop.extend.AdvancedActivity;
import de.datlag.hotdrop.p2p.HostTransfer;
import de.datlag.hotdrop.util.FileUtil;


public class TransferFragment extends Fragment {

    private AdvancedActivity activity;
    private HostTransfer hostTransfer;
    private Host host;
    private View rootView;
    private LinearLayoutCompat fileContainer;
    private AppCompatTextView hostName;
    private String defaultPath = null;
    private FloatingActionButton disconnectHost;
    private FloatingActionButton uploadFile;
    private String hostCheckedName;

    public TransferFragment(AdvancedActivity activity, @NotNull Host host) {
        this.activity = activity;
        this.host = host;
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
        fileContainer = rootView.findViewById(R.id.file_container);
        hostName = rootView.findViewById(R.id.host_name);
        disconnectHost = rootView.findViewById(R.id.disconnect_host);
        uploadFile = rootView.findViewById(R.id.upload_file);
    }

    private void initLogic() {
        hostTransfer = new HostTransfer(activity, host);
        hostCheckedName = host.getName().substring(host.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length() +1);
        hostName.setText(hostCheckedName);

        disconnectHost.setOnClickListener((View view) -> {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(hostCheckedName)
                        .setMessage("Are your sure you want to disconnect?")
                        .setPositiveButton(activity.getString(R.string.ok), (DialogInterface dialogInterface, int i) -> {
                                if (activity instanceof MainActivity) {
                                    activity.switchFragment(((MainActivity) activity).getSearchDeviceFragment(), R.id.fragment_view);
                                }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .create().show();
        });

        uploadFile.setOnClickListener((View view) -> {
                FileUtil.chooseAny(activity, new FileUtil.AnyChooseCallback() {
                    @Override
                    public void onChosenFolder(String path, File file) {
                        Toast.makeText(activity, "File chosen", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onChosenFile(String path, File file) {
                        Toast.makeText(activity, "Folder chosen", Toast.LENGTH_LONG).show();
                    }
                });
        });

        fileContainer.setOnClickListener(view -> new MaterialAlertDialogBuilder(activity)
                .setMessage("Sending File or Folder?")
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
                }).create().show());


        fileContainer.setOnDragListener((View view, DragEvent dragEvent) -> {
                if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    Toast.makeText(getContext(), "Dropped", Toast.LENGTH_LONG).show();
                }
                return false;
        });
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}