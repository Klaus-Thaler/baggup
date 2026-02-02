package de.thaler.baggup.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import de.thaler.baggup.MainActivity;
import de.thaler.baggup.R;

public class customProgress  {
    private static final String TAG = "myLOG customProgress";
    ProgressBar progressBar;
    TextView textView;
    public customProgress (Context context) {
        progressBar = (ProgressBar) MainActivity.mainActivity.findViewById(R.id.customProgressProgressBar);
        int max = 100;
        progressBar.setMax(max);
        progressBar.setProgress(0);
        progressBar.setBackgroundTintList(ColorStateList.valueOf(Color.RED));

        textView = (TextView) MainActivity.mainActivity.findViewById(R.id.customProgressTextView);
    }
    public void start (String str) {
        File file = new File(str);
        str = file.getName() + " wird übertragen";
        if (str.length() > 36)
            str = "... " + str.substring(str.length() - 33);
        //textView.setVisibility(View.VISIBLE);
        textView.setText(str);

        progressBar.setIndeterminate(true);

        TimerTask task = new TimerTask() {
            public void run() {
                Log.v(TAG, "time");
                progressBar.setIndeterminate(false);
                textView.setText(MainActivity.mainActivity.getString(R.string.customProgress_noDataTransfer));
            }
        };
        Timer timer = new Timer("Timer");
        long delay = 1600L;
        timer.schedule(task, delay);
    }
}

