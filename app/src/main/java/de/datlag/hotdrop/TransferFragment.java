package de.datlag.hotdrop;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;

import com.adroitandroid.near.model.Host;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import de.datlag.hotdrop.utils.FileUtil;
import de.datlag.hotdrop.utils.HostTransfer;


public class TransferFragment extends Fragment {

    private Activity activity;
    private HostTransfer hostTransfer;
    private Host host;
    private View rootView;
    private LinearLayoutCompat fileContainer;
    private AppCompatImageView folderIcon;
    private AppCompatTextView hostName;
    private String defaultPath = null;

    public TransferFragment(Activity activity, Host host) {
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
        folderIcon = rootView.findViewById(R.id.folder_icon);
        hostName = rootView.findViewById(R.id.host_name);
    }

    private void initLogic() {
        hostTransfer = new HostTransfer(activity, host);

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
                                        Log.e("Path", path);
                                        Log.e("File", file.getName());

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

        hostName.setText(host.getName().substring(host.getName().indexOf(activity.getPackageName()) + activity.getPackageName().length() +1));

        fileContainer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    ImageViewCompat.setImageTintList(folderIcon, ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.primaryDarkColor)));
                } else {
                    ImageViewCompat.setImageTintList(folderIcon, ColorStateList.valueOf(ContextCompat.getColor(activity, R.color.primaryColor)));
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();

    }
}
