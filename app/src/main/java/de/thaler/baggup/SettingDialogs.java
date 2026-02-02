package de.thaler.baggup;

import static de.thaler.baggup.MainActivity.LoginLen;
import static de.thaler.baggup.MainActivity.PasswordLen;
import static de.thaler.baggup.MainActivity.mainActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Objects;

import de.thaler.baggup.utils.Helper;

public class SettingDialogs {
    private static final String TAG = "myLOG SettingDialogs";
    Context context;
    Dialog dialog;
    SharedPreferences mPreference;

    public SettingDialogs(Context context) {
        this.context = context;
        dialog = new Dialog(context);

        mPreference = MainActivity.appContext.getSharedPreferences("MyPref", 0);
        // create dialog
        //dialog.setContentView(R.layout.dialog_settings);
        Objects.requireNonNull(dialog.getWindow())
                .setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
    }
    // Dialog start
    public void DialogDescription () {
        dialog.setContentView(R.layout.dialog_description);
        dialog.show();
        // title
        TextView titleText = dialog.findViewById(R.id.dialog_title);
        titleText.setMovementMethod(new ScrollingMovementMethod());
        titleText.setText(R.string.dialog_title_description);

        // text srollable
        TextView mainText = dialog.findViewById(R.id.dialog_text);
        mainText.setMovementMethod(new ScrollingMovementMethod());
        mainText.setText(R.string.DialogDescriptionmainText);

        Button button_close = dialog.findViewById(R.id.button_close);
        button_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void DialogOverThis () {
        dialog.setContentView(R.layout.dialog_over);
        dialog.show();
        // title
        TextView titleText = dialog.findViewById(R.id.dialog_title);
        titleText.setMovementMethod(new ScrollingMovementMethod());
        titleText.setText(R.string.dialog_title_over_this);

        // text srollable
        TextView mainText = dialog.findViewById(R.id.dialog_text);
        mainText.setMovementMethod(new ScrollingMovementMethod());
        mainText.setText(R.string.DialogDescriptionmainText);

        // text linkable
        TextView dialogCopyright = dialog.findViewById(R.id.dialog_copyright);
        dialogCopyright.setMovementMethod(LinkMovementMethod.getInstance());
        dialogCopyright.setText(R.string.copyright_links);

        Button button_close = dialog.findViewById(R.id.button_close);
        button_close.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    public void DialogPasswd () {
        dialog.setContentView(R.layout.dialog_security);
        dialog.show();

        // title
        TextView titleText = dialog.findViewById(R.id.dialog_title);
        titleText.setMovementMethod(new ScrollingMovementMethod());
        titleText.setText(R.string.dialog_title_configuration);

        // login
        TextView loginEditText = dialog.findViewById(R.id.editTextLogin);
        loginEditText.setText(mPreference.getString("login","login"));
        // passwd1
        TextView passwdEditText1 = dialog.findViewById(R.id.editTextPassword1);
        passwdEditText1.setText(mPreference.getString("password","password"));
        // passwd2
        TextView passwdEditText2 = dialog.findViewById(R.id.editTextPassword2);
        passwdEditText2.setText(mPreference.getString("password","password"));

        // TODO: 27.01.26 port änderbar!!!
        TextView portEditText = dialog.findViewById(R.id.editTextPort);
        portEditText.setText(String.valueOf(mPreference.getInt("port", 8000)));

        // text srollable
        TextView mainText = dialog.findViewById(R.id.dialog_text);
        mainText.setMovementMethod(new ScrollingMovementMethod());
        mainText.setText(R.string.DialogSettingsMainText);

        Button button_save = dialog.findViewById(R.id.buttonSavePassword);
        button_save.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DetachAndAttachSameFragment")
            public void onClick(View v) {
                boolean loginReady = false;
                boolean pwReady = false;
                boolean portReady = false;
                // webserver anhalten, weil sich etwas aendern kann
                mainActivity.mHttpServer.stop();
                SharedPreferences.Editor editor = mPreference.edit();
                if (loginEditText.getText().length() > LoginLen) {
                    editor.putString("login", loginEditText.getText().toString()).apply();
                    loginReady = true;
                    //Helper.showToast(context, context.getString(R.string.notificationSave), 1);
                } else {
                    Helper.showToast(context, context.getString(R.string.loginZuKurz),2);
                }
                Helper.showToast(context, "pw: " +  passwdEditText1.getText().toString(), 2);
                if (Helper.isValidPassword(String.valueOf(passwdEditText1.getText().toString()), PasswordLen)) {
                    editor.putString("password", passwdEditText1.getText().toString()).apply();
                    //Helper.showToast(context, context.getString(R.string.notificationSave), 1);
                    pwReady = true;
                } else {
                    Helper.showToast(context, context.getString(R.string.passwortZuKurz),2);
                }
                if (!passwdEditText1.getText().toString().equals(passwdEditText2.getText().toString())) {
                    //Helper.showToast(context, passwdEditText1.getText().toString()
                    //        + " " + passwdEditText2.getText().toString(),0);
                    Helper.showToast(context, context.getString(R.string.passwords_do_not_match),2);
                    pwReady = false;
                }
                if ((Integer.parseInt(String.valueOf(portEditText.getText())) < 8000)
                        || (Integer.parseInt(String.valueOf(portEditText.getText())) >= 9000)){
                    Helper.showToast(context, "8000 - 8999", 2);
                } else {
                    editor.putInt("port", Integer.parseInt(String.valueOf(portEditText.getText()))).apply();
                    portReady = true;
                }
                // clear cache
                CheckBox checkBox = dialog.findViewById(R.id.checkbocClearCache);
                TextView clearCache = dialog.findViewById(R.id.textViewClearCache);
                if (checkBox.isChecked()) {
                    editor.putString("login","").apply();
                    editor.putString("password", "").apply();
                    editor.remove("selectMimes").apply();
                    editor.remove("selectDirs").apply();
                    Helper.showToast(context,"clear",1);
                    loginEditText.setText("");
                    passwdEditText1.setText("");
                    passwdEditText2.setText("");
                    checkBox.setVisibility(View.INVISIBLE);
                    clearCache.setVisibility(View.INVISIBLE);

                }
                if (loginReady && pwReady && portReady) {
                    final TextView IPViewNumber = MainActivity.mainActivity.findViewById(R.id.home_fragment_ip_number);
                    IPViewNumber.setText(String.valueOf(Helper.showIPAddress(context)
                            + ":" + portEditText.getText()));

                    dialog.dismiss();
                }
            }
        });
        Button closeX = dialog.findViewById(R.id.buttonCloseX);
        closeX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
}
