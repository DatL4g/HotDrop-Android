package de.datlag.hotdrop;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;
import de.datlag.hotdrop.utils.FileUtil;
import de.datlag.hotdrop.utils.HostTransfer;


public class TransferFragment extends Fragment {

    private Activity activity;
    private HostTransfer hostTransfer;
    private Host host;
    private View rootView;
    private LinearLayoutCompat fileContainer;
    private AppCompatTextView hostName;
    private String defaultPath = null;
    private FloatingActionButton disconnectHost;
    private FloatingActionButton uploadFile;
    private String hostCheckedName;

    public TransferFragment(Activity activity, @NotNull Host host) {
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

        disconnectHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(activity)
                        .setTitle(hostCheckedName)
                        .setMessage("Are your sure you want to disconnect?")
                        .setPositiveButton(activity.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (activity instanceof MainActivity) {
                                    ((MainActivity) activity).switch2Fragment(((MainActivity) activity).searchDeviceFragment);
                                }
                            }
                        })
                        .setNegativeButton(activity.getString(R.string.cancel), null)
                        .create().show();
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        fileContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(activity)
                        .setMessage("Sending File or Folder?")
                        .setPositiveButton("File", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FileUtil.chooseFile(activity, defaultPath, new FileUtil.FileChooseCallback() {
                                    @Override
                                    public void onChosen(String path, File file) {
                                        hostTransfer.startTransfer(file);
                                        defaultPath = path;
                                    }
                                });
                            }
                        })
                        .setNegativeButton("Folder", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FileUtil.chooseFolder(activity, defaultPath, new FileUtil.FolderChooseCallback() {
                                    @Override
                                    public void onChosen(String path, File file) {
                                        hostTransfer.startTransfer(FileUtil.folderToFile(activity, file));

                                        defaultPath = path;
                                    }
                                });
                            }
                        }).create().show();
            }
        });


        fileContainer.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    Toast.makeText(getContext(), "Dropped", Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
