package de.thaler.baggup.utils;

/*
 * Copyright 2024 Mathias Uebel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static de.thaler.baggup.MainActivity.appContext;
import static de.thaler.baggup.MainActivity.mainActivity;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * @author Mathias Uebel
 *
 * <h1>GrantPermissions</h1>
 * <p>Class that checks whether access to internal storage is permitted.
 * Then opens the required activity.</p>
 * <p><b>mainActivity</b> must be imported from MainActivity as a global variable.
 * import static .....MainActivity.mainActivity;</p>
 *
 */
public class GrantPermissions extends ActivityCompat {
    static String TAG = "myLog GrantPermissions.java";
    public Context context;
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final int STORAGE_PERMISSION_CODE = 23; //23 //100

    public GrantPermissions(Context context) {
        super();
        if (!checkStoragePermissions()) {
            this.requestForStoragePermissions();
        }
    }
    public boolean checkStoragePermissions(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            //Below android 11
            int write = ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(mainActivity.getApplicationContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            return read == PackageManager.PERMISSION_GRANTED
                    && write == PackageManager.PERMISSION_GRANTED;
        }
    }
    private void requestForStoragePermissions() {
        //Android is 11 (R) or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
                intent.setData(uri);
                storageActivityResultLauncher.launch(intent);
            }catch (Exception e){
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                storageActivityResultLauncher.launch(intent);
            }
        }else{
            //Below android 11
            ActivityCompat.requestPermissions(
                    mainActivity,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }
    private final ActivityResultLauncher<Intent> storageActivityResultLauncher =
            mainActivity.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    new ActivityResultCallback<ActivityResult>(){
                        @Override
                        public void onActivityResult(ActivityResult o) {
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                                //Android is 11 (R) or above
                                if(Environment.isExternalStorageManager()){
                                    //Manage External Storage Permissions Granted
                                    Log.d(TAG, "onActivityResult: Manage External Storage Permissions Granted");
                                }else{
                                    Helper.showToast(appContext,"Storage Permissions Denied",2);
                                }
                            }else{
                                //Below android 11
                                Helper.showToast(appContext,"Below android 11",1);
                            }
                        }
                    });


    public void onRequestPermissionsResult(int requestCode,
    @NonNull String[] PERMISSIONS,
    @NonNull int[] grantResults) {
        onRequestPermissionsResult(requestCode, PERMISSIONS, grantResults);
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.length > 0){
                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                if(read && write){
                    Helper.showToast(appContext, "Storage Permissions Granted", 1);
                }else{
                    Helper.showToast(appContext,"Storage Permissions Denied",2);
                }
            }
        }
            /*
            onRequestPermissionsResult(requestCode, permissions, grantResults);
            int REQUEST_CAMERA_PERMISSION = 1;
            if (requestCode == REQUEST_CAMERA_PERMISSION) {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Helper.showToast("REQUEST_CAMERA_PERMISSION okay",1);
                } else {
                    Log.i(TAG,"REQUEST_CAMERA_PERMISSION fail");
                    Helper.showToast("REQUEST_CAMERA_PERMISSION fail",2);
                }
            }
        }
        private boolean hasRequiredPermissions() {
            for (String permission : PERMISSIONS) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                        return true;
                    }
                }
            }
            return false;
        }
    */
    }
}


