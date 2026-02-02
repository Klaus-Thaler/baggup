package de.thaler.baggup;

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
import static de.thaler.baggup.MainActivity.baggupFileName;
import static de.thaler.baggup.MainActivity.rootFile;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.thaler.baggup.utils.FileSelektion;
import de.thaler.baggup.utils.customProgress;
import de.thaler.baggup.utils.Helper;
import fi.iki.elonen.NanoHTTPD;

/**
 * Web Server with NanoHTTPD
 */
public class httpServer extends NanoHTTPD {
    private static final String TAG = "myLOG httpServer";
    public static ArrayList<String> allMimes = new ArrayList<>();
    public static ArrayList<String> allFiles = new ArrayList<>();
    public static List<File> res = new ArrayList<>();
    private final customProgress progress;
    private final FileSelektion fileSelektion;
    public httpServer(int port) {
        super(port);
        fileSelektion = new FileSelektion(appContext);
        progress = new customProgress(appContext);
    }

    /**
     * Starts searching for files based on preselection and save the result in a file, (.../.baggup).
     */
    public void determineResult () {
        saveResultFileList(fileSelektion.fileList());
    }
    /**
     * First, create a log file.
     * Since this web server does not transmit the timestamp, this log file stores the last modified date.
     *
     * @param res List of all requested files.
     */
    public void saveResultFileList (List<File> res) {
        StringBuilder fileString = new StringBuilder();
        fileString.append("# loging ").append(new Date()).append("\n")
                .append("#").append(" !do not remove!").append("\n")
                .append("#").append(" copyright https://github.com/Klaus-Thaler").append("\n")
                .append("#").append("\n");
        for (File s:res) {
            Date lastMod = new Date(new File(s.getPath()).lastModified());
            //Log.i(TAG, lastMod.toString());
            // unixtime davor
            fileString.append(lastMod).append(" ").append(s).append("\n");
        }
        String baggupFile = rootFile + File.separator + baggupFileName;
        Log.d(TAG, "baggup "+ baggupFile);
        try (FileOutputStream fos = new FileOutputStream(baggupFile)) {
            // Convert the string into bytes
            byte[] dataBytes = fileString.toString().getBytes();
            fos.write(dataBytes);
        }
        catch (IOException e) {
            Log.e(TAG, "An error occurred: " + e.getMessage());
            //throw new RuntimeException(e);
        }
    }
    /**
     *
     * nach post variablen suchen und auswerten
     * @ param -valid login - validpasswd
     * @return boolean
     */
     private Boolean validLoginPostVars (IHTTPSession session) {
         SharedPreferences mPreference = appContext.getSharedPreferences("MyPref", 0);
         String validLogin = mPreference.getString("login", "");
         String validPassword = mPreference.getString("password", "");

         if (session.getMethod() == Method.POST) {
             String requestForm = "";
             try {
                 session.parseBody(new HashMap<>());
                 requestForm = session.getQueryParameterString();
             } catch (IOException | ResponseException e) {
                 Log.e(TAG, "error " + e);
                 throw new RuntimeException(e);
             }
             // login und passwort anzeigen
             //Log.i(TAG, "POST String: " + requestForm);
             if (requestForm.split("&")[0].substring(6).equals(validLogin) &&
                     requestForm.split("&")[1].substring(7).equals(validPassword)) {
                 return true;
             }
         }
         return false;
         // TODO: 16.01.26  true nur fuer tests
         //return true;
     }
    /**
     *
     * @param session
     *            The HTTP session
     * @return
     */
     @SuppressLint("ResourceType")
     public Response serve (IHTTPSession session) {
         // start vars festlegen
         String uri = session.getUri();
         String uriString = uri.substring(1);
         if (uri.equals("/"))
             uriString = "index.html";
         //Log.i(TAG, "uri " + uri + "uristring " + uri + " islogin " + validLoginPostVars(session));

         // hier kein html! ich will ja nicht surfen!
         // login nur fuer curl oder wget mit post request
         // #############################################################

         progress.start(uri);

         if (validLoginPostVars(session)) {
             //Log.i(TAG, "mime: " + Helper.getMimeType(uri).split("/")[0]);
             //Log.i(TAG, "uri: " + uri);
             // alles (video, audio, ...)
             try {
                 FileInputStream fis = new FileInputStream(uriString);
                 return newFixedLengthResponse(Response.Status.OK, Helper.getMimeType(uri).split("/")[0], fis, fis.available());
             } catch (IOException e) {
                 Log.e(TAG, "FileInputStream IOException: " + e);
                 return newFixedLengthResponse(Response.Status.OK, "text/plain", "Internal Error: " + e.getMessage());
             }
         } else {
             return newFixedLengthResponse(Response.Status.OK, "text/plain", "no login");
         }
    }
}