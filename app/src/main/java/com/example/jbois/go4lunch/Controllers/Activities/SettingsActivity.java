package com.example.jbois.go4lunch.Controllers.Activities;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.ApplicationContext;
import com.example.jbois.go4lunch.Utils.UserHelper;
import com.google.firebase.messaging.FirebaseMessaging;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static com.example.jbois.go4lunch.Controllers.Activities.RestaurantProfileActivity.PREFS_NAME;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content,new MyPreferenceFragment());
        fragmentTransaction.commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        private String mUid;
        private String mNewName;
        private SharedPreferences mMySharedPreferences;
        private SharedPreferences.Editor mEditor;
        public static final String NOTIF_UID ="notification activation";

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
                        updatePreference(singlePref, singlePref.getKey().toString());
                    }
                } else {
                    updatePreference(preference, preference.getKey().toString());
                }
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updatePreference(findPreference(key), key);
        }

        private void updatePreference(Preference preference, String key) {
            if (preference == null) return;
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                mNewName = editTextPref.getEditText().getText().toString();
                if(!TextUtils.isEmpty(mNewName)){
                    UserHelper.updateUsername(mUid, mNewName);
                }
                SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
                preference.setSummary(sharedPrefs.getString(key, "Your name"));
            }
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                if (checkBoxPreference.isChecked()){
                    FirebaseMessaging.getInstance().subscribeToTopic("usersWhoChose");
                    this.activationOfNotifications(true);
                }
                if(!checkBoxPreference.isChecked()){
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("usersWhoChose");
                    this.activationOfNotifications(false);
                }
            }
        }
        //method that set the alarm for notifications
        private void activationOfNotifications(Boolean isActivated){
            mMySharedPreferences = ApplicationContext.getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            mEditor = mMySharedPreferences.edit();
            mEditor.putString(NOTIF_UID,mUid);
            mEditor.apply();
        }
        //Callback method to fetch user's ID
        @Subscribe(sticky = true)
        public void onGetUID(LunchActivity.getUid event) {
            mUid = event.uid;
        }

    }
}
