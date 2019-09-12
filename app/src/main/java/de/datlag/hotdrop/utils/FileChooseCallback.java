package de.datlag.hotdrop.utils;

import java.io.File;

public interface FileChooseCallback {
    void onChosen(String path, File file);
}
