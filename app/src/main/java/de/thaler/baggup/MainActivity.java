package de.thaler.baggup;

import static de.thaler.baggup.R.id.nav_host_fragment_content_main;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.thaler.baggup.databinding.ActivityMainBinding;
import de.thaler.baggup.utils.FileSelektion;
import de.thaler.baggup.utils.GrantPermissions;
import de.thaler.baggup.utils.Helper;
import fi.iki.elonen.NanoHTTPD;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "myLOG Main";
    public static MainActivity mainActivity;
    public static Context appContext;

    public static List<String> MimeType;
    public static List<String> AllDirs;
    public static String baggupFileName = ".baggup";
    public httpServer mHttpServer;
    public static File rootFile = new File("/storage/emulated/0");
    public static SharedPreferences mPreference;
    public FileSelektion fileSelektion;
    private AppBarConfiguration mAppBarConfiguration;
    private FloatingActionButton fab;
    private ActivityMainBinding binding;
    public static int LoginLen = 6;
    public static int PasswordLen = 6;
    public static int defaultPort = 8443;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Global Vars
        mainActivity = this;
        appContext = getApplicationContext();

        // grant all permissions
        GrantPermissions grant = new GrantPermissions(this);
        grant.checkAll();
        //String los = grant.checkWifiName();

        // Get the device policy manager and the admin component
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = new ComponentName(appContext, MainActivity.class);
        // Check if the app has been granted device admin privileges
        if (devicePolicyManager.isAdminActive(componentName)) {
            Helper.showToast(this, "The app has already been granted device admin privileges",2);
            devicePolicyManager.lockNow();
            finish();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // kein wischen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // immer an

        mPreference = MainActivity.appContext.getSharedPreferences("MyPref", 0);

        // TODO: 02.02.26  nur für tests
        //mPreference.edit().putString("login", "Login33").apply();
        //mPreference.edit().putString("password", "Login33").apply();
        // todo end

        int port = mPreference.getInt("port", defaultPort);
        try {
            mHttpServer = new httpServer(port);
        } catch (IOException | UnrecoverableKeyException | CertificateException |
                 KeyStoreException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        // search files
        fileSelektion = new FileSelektion(this);
        Log.d(TAG, "size " + fileSelektion.fileList().size());
        // check all prefs
        Log.i(TAG, "getDefaultPref: " + fileSelektion.fileList());

        MimeType = new ArrayList<>();
        MimeType.add("image");
        MimeType.add("video");
        MimeType.add("audio");
        MimeType.add("text");
        MimeType.add("application");
        MimeType.add("message");
        MimeType.add("multipart");

        AllDirs = new ArrayList<>();
        AllDirs.addAll(Helper.listDirNames(Objects.requireNonNull(rootFile.listFiles())));
        // TODO: 16.01.26 ordner Android macht Probleme?
        // Android raus, macht Probleme
        AllDirs.remove("Android");

        //
        setSupportActionBar(binding.appBarMain.toolbar);
        // fab floating button
        fab = binding.appBarMain.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //web server an und aus
                if (mHttpServer.isAlive()) {
                    mHttpServer.stop();
                    fab.setImageResource(R.drawable.baseline_cached_24);
                    Helper.showToast(appContext, getString(R.string.backupStoped), 1);
                } else {
                    try {
                        mHttpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false);
                        mHttpServer.determineResult();
                    } catch (IOException e) {
                        Log.e(TAG, "server stert faild" + e);
                        throw new RuntimeException(e);
                    }
                    fab.setImageResource(R.drawable.baseline_do_not_disturb_alt_24);
                    Helper.showToast(appContext, getString(R.string.backupNow)
                            + " " + fileSelektion.fileList().size() + " Files", 1);

                    Log.d(TAG, "getDefaultPref: " + fileSelektion.fileList().size());
                }
            }
        });

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_folder, R.id.nav_mime)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        SettingDialogs dialog = new SettingDialogs(this);

        if (item.getItemId() == R.id.action_settings1) {
            dialog.DialogDescription();
        }

        if (item.getItemId() == R.id.action_settings2) {
            dialog.DialogPasswd();
            mHttpServer.stop();
            fab.setImageResource(R.drawable.baseline_cached_24);
        }
        if (item.getItemId() == R.id.action_settings3) {
            dialog.DialogOverThis();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * refresh also all relevant TextViews
     */
    @SuppressLint("SetTextI18n")
    public void httpResultSize () {
        // text in der Appbar refresh
        TextView textView = binding.appBarMain.customProgressInfo1;
        textView.setText(this.getString(R.string.customProgress_DataSize) +
                fileSelektion.fileList().size() + " " + this.getString(R.string.string) + ".");
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
        //mHttpServer.stop();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mHttpServer.stop();
    }
    @Override
    protected void onResume() {
        super.onResume();
        appContext = getApplicationContext();
    }

    /*
    // https://mohitsingh2002.medium.com/background-location-permission-in-android-11-and-above-1ab7399ec861
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // Background Location Permission is granted so do your work here
                } else {
                    // Ask for Background Location Permission
                    askPermissionForBackgroundUsage();
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
            askForLocationPermission();
        }
    }

    private void askForLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed!")
                    .setMessage("Location Permission Needed!")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Permission is denied by the user
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void askPermissionForBackgroundUsage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed!")
                    .setMessage("Background Location Permission Needed!, tap \"Allow all time in the next screen\"")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // User declined for Background Location Permission.
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted location permission
                // Now check if android version >= 11, if >= 11 check for Background Location Permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        // Background Location Permission is granted so do your work here
                    } else {
                        // Ask for Background Location Permission
                        askPermissionForBackgroundUsage();
                    }
                }
            } else {
                // User denied location permission
            }
        } else if (requestCode == BACKGROUND_LOCATION_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // User granted for Background Location Permission.
            } else {
                // User declined for Background Location Permission.
            }
        }

    }

     */
}