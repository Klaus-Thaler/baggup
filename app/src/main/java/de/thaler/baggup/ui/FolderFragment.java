package de.thaler.baggup.ui;

import static de.thaler.baggup.MainActivity.AllDirs;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import de.thaler.baggup.MainActivity;
import de.thaler.baggup.R;

public class FolderFragment extends PreferenceFragmentCompat {
    private static final String TAG = "myLOG FolderFragment";
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // mit xml -> setPreferencesFromResource(R.xml.preferences_folder, rootKey);
        setPreferenceScreen(createPreferenceHierarchy());
    }
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        if (preference.getKey().equals("syncAllDirs")) { // nur switch
            SwitchPreferenceCompat syncAllDirs = findPreference("syncAllDirs");
            assert syncAllDirs != null;
            if (syncAllDirs.isChecked()) {      // nur wenn an
                for (String s : AllDirs) {
                    Objects.requireNonNull(preference.getSharedPreferences()).edit().putBoolean("dir_" + s, true).apply();
                    CheckBoxPreference toggle = findPreference(("dir_"+s));
                    assert toggle != null;
                    toggle.setChecked(true);
                }
            } else {
                for (String s : AllDirs) {
                    Objects.requireNonNull(preference.getSharedPreferences()).edit().putBoolean("dir_" + s, false).apply();
                    CheckBoxPreference toggle = findPreference(("dir_"+s));
                    assert toggle != null;
                    toggle.setChecked(false);
                }
            }
        }
        MainActivity.mainActivity.httpResultSize();
        return super.onPreferenceTreeClick(preference);
    }
    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);

        // Inline preferences
        PreferenceCategory inlinePrefCat = new PreferenceCategory(context);
        inlinePrefCat.setTitle(R.string.title_folder_fragment);
        root.addPreference(inlinePrefCat);

        SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(context);
        switchPreferenceCompat.setKey("syncAllDirs");
        switchPreferenceCompat.setChecked(false);
        switchPreferenceCompat.setSummaryOff(getString(R.string.attachment_summary_syncAllDirs_off));
        switchPreferenceCompat.setSummaryOn(getString(R.string.attachment_summary_syncAllDirs_on));
        switchPreferenceCompat.setTitle(R.string.sync_AllDirs);
        inlinePrefCat.addPreference(switchPreferenceCompat);

        for (String s:AllDirs) {
            CheckBoxPreference togglePref = new CheckBoxPreference(context);
            togglePref.setKey("dir_" + s);
            //togglePref.setDependency("syncAllDirs");
            togglePref.setTitle(s);
            inlinePrefCat.addPreference(togglePref);
        }
        PreferenceCategory EndPrefCat = new PreferenceCategory(context);
        EndPrefCat.setTitle("Stand with Ukraine \n\n\n\n");
        root.addPreference(EndPrefCat);

        return root;
    }
}