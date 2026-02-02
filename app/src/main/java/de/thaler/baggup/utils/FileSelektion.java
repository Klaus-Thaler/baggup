package de.thaler.baggup.utils;

import static de.thaler.baggup.MainActivity.rootFile;

import android.content.Context;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FileSelektion {
    private static final String TAG = "myLOG FileSelection";
    Context context;
    Map<String, ?> dirs = new HashMap<>();
    Map<String, ?> types = new HashMap<>();

    public FileSelektion(Context context) {
        this.context = context;
    }
    private List<String> inDirs () {
        Map<String, ?> prefsKeys = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        List<String> allFiles = new ArrayList<>();
        for (Map.Entry<String, ?> entry : prefsKeys.entrySet()) {
            if (entry.getKey().contains("dir_")                     // alle dirs in prefs
                    && entry.getValue().equals(true)) {             // mit true in prefs
                allFiles.add(entry.getKey().substring(4));
            }
        }       //Log.v(TAG, "all files " + allFiles);
        return allFiles;
    }
    private List<String> inTypes () {
        Map<String, ?> prefsKeys = PreferenceManager.getDefaultSharedPreferences(context).getAll();
        List<String> allMimes = new ArrayList<>();
        for (Map.Entry<String, ?> entry : prefsKeys.entrySet()) {
            if (entry.getKey().contains("mime_")                    // alle mimes in prefs
                    && entry.getValue().equals(true)) {             // mit true in prefs
                allMimes.add(entry.getKey().substring(5));
            }
        }       //Log.v(TAG, "all mimes " + allMimes);
        return allMimes;
    }
    public List<File> fileList () {
        List<File> fileList = new ArrayList<>();
        for (String dirs : inDirs()) {                      // file walk durch alle main dirs
            List<File> files = Helper.walkInDir(context,rootFile + File.separator + dirs);
            for (File f : files) {
                for (String type : inTypes()) {              // mit mime typen vergleichen
                    if (Objects.equals(Helper.getMimeType(String.valueOf(f)).split("/")[0], type)) {
                        if (f.isFile())
                            fileList.add(f);
                    }
                }
            }
        }       // Log.d(TAG, "-> fileList " + fileList);
        return fileList;
    }
}
