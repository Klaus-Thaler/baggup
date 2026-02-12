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
import android.widget.ImageView;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.thaler.baggup.databinding.ActivityMainBinding;
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
    public static httpServer mHttpServer;
    public static File rootFile = new File("/storage/emulated/0");
    public static SharedPreferences mPreference;
    public static List<File> FileList = new ArrayList<>();
    private AppBarConfiguration mAppBarConfiguration;
    //public static FloatingActionButton fab;
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
        Collections.sort(AllDirs); // sort A-Z
        // TODO: 16.01.26 ordner Android macht Probleme?
        // Android raus, macht Probleme
        AllDirs.remove("Android");

        // start http
        int port = mPreference.getInt("port", defaultPort);
        mHttpServer = new httpServer(port, true);
        startHTTPd();

        String res = getString(R.string.backupNow)
                + " " + mPreference.getInt("filesSize", 0) + " File(s)";
        changeTextView(res);
        //
        setSupportActionBar(binding.appBarMain.toolbar);
        // fab floating button
        FloatingActionButton fab = binding.appBarMain.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //web server an und aus
                if (mHttpServer.isAlive()) {
                    stopHTTPd();
                    //Helper.showToast(appContext, getString(R.string.backupStoped), 1);
                } else {
                    startHTTPd();
                    //Helper.showToast(appContext, getString(R.string.backupNow), 1);
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
            stopHTTPd();
        }
        if (item.getItemId() == R.id.action_settings3) {
            dialog.DialogOverThis();
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    @Override
    protected void onResume() {
        super.onResume();
        appContext = getApplicationContext();
    }
    public void startHTTPd() {
        ImageView serverSVG = binding.appBarMain.imageViewServerSVG;
        FloatingActionButton fab = binding.appBarMain.fab;
        if (!mHttpServer.isAlive()) {
            try {
                mHttpServer.start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
            } catch (IOException e) {
                Log.e(TAG, "server start failed" + e);
                throw new RuntimeException(e);
            } finally {
                mHttpServer.determineResult();
                serverSVG.setImageResource(R.drawable.baseline_browser_updated_24);
                fab.setImageResource(R.drawable.baseline_do_not_disturb_alt_24);
            }
        }
    }
    public void stopHTTPd() {
        ImageView serverSVG = binding.appBarMain.imageViewServerSVG;
        FloatingActionButton fab = binding.appBarMain.fab;
        if (mHttpServer.isAlive()) {
            mHttpServer.stop();
            serverSVG.setImageResource(R.drawable.baseline_browser_not_supported_24);
            fab.setImageResource(R.drawable.baseline_cached_24);
        }
    }
    public void changeTextView (String txt) {
        TextView textView = binding.appBarMain.customProgressInfo1;
        textView.setText(txt);
    }
}