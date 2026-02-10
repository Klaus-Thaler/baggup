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

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

import static de.thaler.baggup.MainActivity.mainActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.thaler.baggup.MainActivity;
import de.thaler.baggup.R;

/**
 * <h5>This class contains a lot of helpful methods and functions.</h5>
 * <p>Copyright (c) Mathias Uebel</p>
 *
 * <h3>functions</h3>
 *
 * <p>WebIPAddress ()<br/>
 * isValidPassword (String password)<br/>
 * lastModified(String filename)<br/>
 * formatDateTime(FileTime fileTime)<br/>
 * makeTempFile (String name, String suffix)<br/>
 * getMimeType(String filename)<br/>
 * saveBitmap (Bitmap bmp, String dst)<br/>
 * copyFile (String src, String dst<br/>
 * moveFile (String src, String dst)<br/>
 * deleteFile (File file)<br/>
 * cutString(String str, String start, String end)<br/>
 * listFilesInDir(String dir)<br/>
 * listDirNames (File[] files)<br/>
 * Walk2Dir (String... args)<br/>
 * loadImageFromAssets( String path)<br/>
 * loadAudioFromAssets(String path)<br/>
 * loadTextFromAssets(String path)<br/>
 * changeRect(Rect mMaxSensorSize, double mDivider)<br/>
 * startBackgroundThread()<br/>
 * stopBackgroundThread()<br/>
 * runMotion()<br/>
 * isExternalStorageReadOnly()<br/>
 * isExternalStorageAvailable() </p>
 *
 */
public class Helper {
    private static final String TAG = "myLog Helper.java";
    /**
     * <p>it is a toast message</p>
     *     <li> 1 = normal custom</li>
     *     <li> 2 = warnung</li>
     *     <li> 0* ist default Toast</li>
     */
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void showToast(Context context, String message, int style) {
        Toast toast = new Toast(context.getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        
        LayoutInflater li = mainActivity.getLayoutInflater();
        View layout = li.inflate(R.layout.custom_toast,
                mainActivity.findViewById(R.id.linearLayoutToast));
        //layout.setRotation(0);
        TextView text = layout.findViewById(R.id.textToast);
        text.setText(message);

        // je nachdem als normal oder warnung
        switch(style) {
            case 1:
                layout.setBackground(context.getDrawable(R.drawable.button_shapes));
                toast.setView(layout); //setting the view of custom toast layout
                break;
            case 2:
                layout.setBackground(context.getDrawable(R.drawable.button_shapes_on));
                //ImageView image = layout.findViewById(R.id.imageToast);
                //image.setImageDrawable(context.getDrawable(drawable.ic_stat_name));
                toast.setView(layout); //setting the view of custom toast layout
                break;
            default:
                toast.setText(message);
        }
        toast.show();
    }
    /**
     * validate a string<br />
     *   mindestens<br /> - ein grossbuchstaben,<br />
     * - ein kleinbuchstaben, <br /> - ein sonderzeichen, <br /> - eine zahl
     * @param password password
     * @param len len
     * @return boolean
     */
    public static boolean isValidPassword(final String password, final int len) {
        Pattern pattern;
        Matcher matcher;
        //final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{" + len + ",}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }
    /**
     * test the network
     * @param context context
     * @return boolean
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     *
     * @param context Context
     * @return String
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            return activeNetwork.getTypeName();
        }
        return null;
    }
    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':')<0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim<0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) { } // for now eat exceptions
        return "";
    }

    /**
     *
     * @param context Context
     * @return the Name of actually WiFi
     */
    public static String getDefaultWifiName(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (manager.isWifiEnabled()) {
            WifiInfo wifiInfo = manager.getConnectionInfo();
            if (wifiInfo != null) {
                //NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                //if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                return wifiInfo.getSSID();
                //}
            }
        }
        return null;
    }
    /**
     *
     * @param main mainactivity
     * @return Set of all WiFi names
     */
    public static CharSequence[] getAllWifiName(MainActivity main) {
        if (ActivityCompat.checkSelfPermission(main.getApplicationContext(), ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            Log.d(TAG, "error");
            ActivityCompat.requestPermissions(main, new String[]{ACCESS_FINE_LOCATION, CAMERA}, 100);
            return null;
        }
        ArrayList<String> scanResult = new ArrayList<>();
        WifiManager manager = (WifiManager) main.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> results = manager.getScanResults();
        for (ScanResult result : results) {
            scanResult.add(result.SSID);
        }
        return scanResult.toArray(new CharSequence[0]);
    }
    /**
     * Check Wifi
     * @return boolean
     */
    public static boolean isWifiReady (Context context) {
        boolean res;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getApplicationContext().getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        res = networkInfo != null && networkInfo.isConnectedOrConnecting();
        return res;
    }
    /**
     * returns the current IP in WLAN
     * @return String
     * <p>sample: "192.168.188.50"</p>
     */
    public static String showIPAddress (Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        String ipString;
        try {
            ipString = InetAddress.getByAddress(
                    ByteBuffer
                            .allocate(Integer.BYTES)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .putInt(mWifiManager.getConnectionInfo().getIpAddress())
                            .array()
            ).getHostAddress();
        } catch (UnknownHostException e) {
            Log.e(TAG, "error: " + e);
            throw new RuntimeException(e);
        }
        return ipString;
    }
    public static String lastModified(String filename) {
        String formatDateString = "YYYY-MM-DDThh:mm:ss[.s+]Z";
        try {
            Path file = Paths.get(filename);
            BasicFileAttributes attr =
                    Files.readAttributes(file, BasicFileAttributes.class);
            //Log.i(TAG, "lastModifiedTime: " + attr.lastModifiedTime());
            FileTime fileTime = attr.lastModifiedTime();
            //Log.i(TAG, "lastModifiedTime: " + formatDateTime(fileTime));
            formatDateString = formatDateTime(fileTime);
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return formatDateString;
    }

    /**
     *
     * @param fileTime
     * @return
     */
    public static String formatDateTime(FileTime fileTime) {
        LocalDateTime localDateTime = fileTime
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return localDateTime.format(DATE_FORMATTER);
    }
    /**
     * <p>formats a time specification</p>
     * <p>"yyyy-MM-dd_HH:mm:ss"</p>
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");

    /**
     * <p>creates a temporary file in the cache</p>
     * @param name
     * @param suffix
     * @return file
     */
    public static File makeTempFile (Context context, String name, String suffix) {
        try {
            File outputDir = context.getCacheDir();
            //Log.i(TAG, "Temporary file with secure permissions created at: " + outputFile);
            return File.createTempFile(name, suffix, outputDir);
        } catch (IOException e) {
            Log.e(TAG, "Error creating secure temporary file: " + e.getMessage());
        }
        return null;
    }

    /**
     * <p>returns a MimeType of file</p>
     * @param filename
     * @return type as String, null is ""
     */
    public static String getMimeType(String filename) {
        String type = "";
        String extension = MimeTypeMap.getFileExtensionFromUrl(filename);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (type == null)
                type = "";
        }
        return type;
    }

    /**
     * <p>save a bitmap compressed in png</p>
     * @param bmp
     * @param dst
     * @return boolean
     */
    public static Boolean saveBitmap (Bitmap bmp, String dst) {
        try (FileOutputStream out = new FileOutputStream(dst)) {
            bmp.compress(Bitmap.CompressFormat.PNG, 75, out); // bmp is your Bitmap instance
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    /**
     * <p>copy a file</p>
     * <p>if it doesn't work, then show a toast</p>
     * @param src
     * @param dst
     */
    public void copyFile (Context context, String src, String dst) {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "copy fail" + e);
            showToast(context, "copy fail", 2);
        }
    }

    /**
     * <p>move a file</p>
     * @param src
     * @param dst
     */
    public void moveFile (String src, String dst) {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                new File(src).delete();
            }
        } catch (IOException ignore) {
            Log.e(TAG, "move fail");
        }
    }

    /**
     * <p>delete a file</p>
     * @param file
     * @return boolean
     */
    public Boolean deleteFile (Context context, File file) {
        //File file = new File(Objects.requireNonNull(uri.getPath()));
        //file.delete();
        boolean res = false;
        try {
            if(file.exists()){
                res = file.getCanonicalFile().delete();
                if(file.exists()){
                    res = context.getApplicationContext().deleteFile(file.getName());
                }
            }
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
        return res;
    }
    /**
     * <p>cut String in brackets</p>
     * <p>Sample: cutString("[test]", "[", "]") -> test</p>
     * @param str
     * @param start
     * @param end
     */
    public static String cutString(String str, String start, String end) {
        /**
         * cut String in brackets
         */
        String result = str;
        if (str.startsWith(start) && str.endsWith(end)) {
            result = str.substring(0, str.length() - end.length()).substring(start.length());
        }
        return result;
    }

    /**
     * <p>searches a directory and outputs all files</p>
     * @param dir (absolute path string)
     * @return List<String>
     */
    public List<String> listFilesInDir(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .sorted()
                .map(File::getName)
                .collect(Collectors.toList());
    }
    /**
     * <p>searches a directory and outputs first Dir</p>
     * @param files (absolute path string)
     * @return ArrayList<String>
     */
    public static List<String> listDirNames(File[] files) {
        ArrayList<String> dirList = new ArrayList<String>();
        assert files != null;
        for (File inFile : files) {
            if (inFile.isDirectory()) {
                //Log.d(TAG, inFile.toString());
                dirList.add(inFile.getName());
            }
        }
        return dirList;
    }

    /**
     * With this method, you can explore directories, list files,
     * and perform tasks such as cleanup or data processing with a high level of control.
     *
     * @param context root (start)
     * @return List
     */
    public static List<File> walkFileTree (Context context, String root) {
        // https://medium.com/@AlexanderObregon/javas-files-walkfiletree-method-explained-6660bebfa626
        //Log.i(TAG, "in walkFileTree");
        List<File> FileList = new ArrayList<>();
        Path startPath = Paths.get(root);
        try {
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    // Handle the file here
                    //Log.d(TAG, "Visited file: " + file);
                    FileList.add(file.toFile());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    Log.e(TAG,"Error accessing file: " + file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            Log.e(TAG,"Error traversing directory: " + e.getMessage());
        }
        return FileList;
    }
    /**
     * <p>Goes through a directory and descends into all subdirectories.</p>
     * <p>https://mkyong.com/java/java-files-walk-examples/</p>
     * @param root
     * @return List</String>
     */
    public static List<File> walkInDir(Context context, String root) {
        // see:
        // https://medium.com/@AlexanderObregon/javas-files-walk-method-explained-570d8a67247d
        Path dir = Paths.get(root);
        List<File> ret = new ArrayList<>();
        try {
            Files.walk(dir, new java.nio.file.FileVisitOption[]{})
                    .sorted()
                    .filter(Files::isRegularFile)
                    .filter(Files::isReadable)
                    .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
                    .forEach(new Consumer<Path>() {
                @Override
                public void accept(Path path) {
                    ret.add(path.toAbsolutePath().toFile());
                }
            });
        } catch (RuntimeException e) {
            if (!e.toString().contains("java.io.UncheckedIOException:")
                    && !e.toString().contains("java.nio.file.AccessDeniedException:")) {
                Log.e(TAG, "walkInDir: " + e);
                throw new RuntimeException(e); }
            Log.e(TAG, "walkInDir: " + e);
            //Helper.showToast(context, "UncheckedIOException: Ignore AccessDeniedException", 2 );
            Helper.showToast(context, "IOException in ./" + e.toString().split(" ")[2].split("/")[4],2);
        } catch (IOException e) {
            Log.e(TAG, "walkInDir(IOException): " + e);
            throw new RuntimeException(e);
        }
        return ret;
    }
    /**
     * <p>load a picture from assets</p>
     * @param path
     * @return
     */
    public Bitmap loadImageFromAssets(Context context, String path) {
        try  {
            InputStream is = context.getAssets().open(path);
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e(TAG, "load Image from Assets fail: " + e);
        }
        return null;
    }
    public MediaPlayer loadAudioFromAssets(Context context, String path) {
        MediaPlayer player = new MediaPlayer();
        AssetFileDescriptor afd = null;

        try {
            afd = context.getAssets().openFd(path);
            player.setDataSource(afd.getFileDescriptor());
            player.prepare();
            return player;
        } catch (IOException e) {
            Log.e(TAG, "load Audio from Assets fail: " + e);
        }
        return null;
    }

    /**
     * <p>load a textfile and write a String</p>
     * @param path
     * @return Stringbuilder
     */
    public static StringBuilder loadTextFromAssets(Context context, String path) {
        InputStream is;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getAssets().open(path);
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            for (String line; (line = r.readLine()) != null; ) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb;
    }
    public Rect changeRect(Rect mMaxSensorSize, double mDivider) {
        int left = (int) (mMaxSensorSize.left + mMaxSensorSize.centerX() / mDivider);
        int top = (int) (mMaxSensorSize.top + mMaxSensorSize.centerY() / mDivider);
        int right = (int) (mMaxSensorSize.right - mMaxSensorSize.centerX() / mDivider);
        int bottom = (int) (mMaxSensorSize.bottom - mMaxSensorSize.centerY() / mDivider);
        return new Rect(left, top, right, bottom);
    }
    public boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState);
    }
    public boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        return !Environment.MEDIA_MOUNTED.equals(extStorageState);
    }
}
