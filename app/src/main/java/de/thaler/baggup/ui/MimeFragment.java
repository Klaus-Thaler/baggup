package de.thaler.baggup.ui;

import static de.thaler.baggup.MainActivity.MimeType;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Objects;

import de.thaler.baggup.MainActivity;
import de.thaler.baggup.R;

public class MimeFragment extends PreferenceFragmentCompat {
    private static final String TAG = "myLOG MimeFragment";
    private Context context;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferenceScreen(createPreferenceHierarchy());
    }
    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        if (preference.getKey().equals("syncAllTypes")) { // nur switch
            SwitchPreferenceCompat syncAllDirs = findPreference("syncAllTypes");
            assert syncAllDirs != null;
            if (syncAllDirs.isChecked()) {          // nur wenn an
                for (String s : MimeType) {
                    Objects.requireNonNull(preference.getSharedPreferences()).edit().putBoolean("mime_" + s, true).apply();
                    CheckBoxPreference toggle = findPreference("mime_" + s);
                    assert toggle != null;
                    toggle.setChecked(true);
                }
            } else {
                for (String s : MimeType) {
                    Objects.requireNonNull(preference.getSharedPreferences()).edit().putBoolean("mime_" + s, false).apply();
                    CheckBoxPreference toggle = findPreference("mime_" + s);
                    assert toggle != null;
                    toggle.setChecked(false);
                }
            }
        }
        MainActivity.mainActivity.stopHTTPd();
        return super.onPreferenceTreeClick(preference);
    }
    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);

        // Inline preferences
        PreferenceCategory inlinePrefCat = new PreferenceCategory(context);
        inlinePrefCat.setTitle(R.string.title_folder_fragment);
        root.addPreference(inlinePrefCat);

        SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(context);
        switchPreferenceCompat.setKey("syncAllTypes");
        switchPreferenceCompat.setSummaryOff(getString(R.string.attachment_summary_syncAllTypes_off));
        switchPreferenceCompat.setSummaryOn(getString(R.string.attachment_summary_syncAllTypes_on));
        switchPreferenceCompat.setTitle(R.string.sync_AllTypes);
        inlinePrefCat.addPreference(switchPreferenceCompat);

        for (String s:MimeType) {
            CheckBoxPreference togglePref = new CheckBoxPreference(context);
            togglePref.setKey("mime_" + s);
            togglePref.setTitle(s);
            inlinePrefCat.addPreference(togglePref);
        }

        PreferenceCategory EndPrefCat = new PreferenceCategory(context);
        EndPrefCat.setTitle("Stand with Ukraine \n\n\n\n");
        root.addPreference(EndPrefCat);
        return root;
    }
}
