package com.example.jbois.go4lunch.Controllers.Activities;

import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.UserHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;

import static com.example.jbois.go4lunch.Controllers.Activities.LunchActivity.USERID;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private String mUid;
        private String mNewName;

        @Override
        public void onStart() {
            super.onStart();
            EventBus.getDefault().register(this);
        }

        @Override
        public void onStop() {
            EventBus.getDefault().unregister(this);
            super.onStop();
        }

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onResume() {
            super.onResume();
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
                Preference preference = getPreferenceScreen().getPreference(i);
                if (preference instanceof PreferenceGroup) {
                    PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
                    for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
                        Preference singlePref = preferenceGroup.getPreference(j);
                        updatePreference(singlePref, singlePref.getKey());
                    }
                } else {
                    updatePreference(preference, preference.getKey());
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), key);
            if(!TextUtils.isEmpty(mNewName)){
                UserHelper.updateUsername(mUid, mNewName);
            }
        }

        private void updatePreference(Preference preference, String key) {
            if (preference == null) return;
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                listPreference.setSummary(listPreference.getEntry());
                return;
            }
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                preference.setSummary(editTextPref.getText());
                mNewName = editTextPref.getText().toString();
            }
            //SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            //preference.setSummary(sharedPrefs.getString(key, "Default"));
        }
        //Callback method to fetch user's ID
        @Subscribe(sticky = true)
        public void onGetUID(LunchActivity.getUid event) {
            mUid = event.uid;
        }

    }
}
