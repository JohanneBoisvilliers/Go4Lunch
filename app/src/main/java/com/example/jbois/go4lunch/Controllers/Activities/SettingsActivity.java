package com.example.jbois.go4lunch.Controllers.Activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.jbois.go4lunch.R;
import com.example.jbois.go4lunch.Utils.AlarmReceiver;
import com.example.jbois.go4lunch.Utils.UserHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Calendar;

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
        private AlarmManager mAlarmManager;
        private PendingIntent mAlarmIntent;

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
        }

        private void updatePreference(Preference preference, String key) {
            if (preference == null) return;
            if (preference instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) preference;
                if(!TextUtils.isEmpty(mNewName)){
                    mNewName = editTextPref.getText().toString();
                    UserHelper.updateUsername(mUid, mNewName);
                }
            }
            if (preference instanceof CheckBoxPreference) {
                CheckBoxPreference checkBoxPreference = (CheckBoxPreference) preference;
                if (checkBoxPreference.isChecked()){
                    Log.e("ALARMSENT", "alarme activée");
                    settingsForAlarm();
                }
                if(!checkBoxPreference.isChecked()){
                    Log.e("ALARMSENT", "alarme annulée");
                    if (mAlarmManager != null) {
                        mAlarmManager.cancel(mAlarmIntent);
                    }
                }
            }
            //SharedPreferences sharedPrefs = getPreferenceManager().getSharedPreferences();
            //preference.setSummary(sharedPrefs.getString(key, "Default"));
        }
        //method that set the alarm for notifications
        private void settingsForAlarm(){
            mAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);//Setting intent to class where Alarm broadcast message will be handled

            mAlarmIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, 0);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 12);

            mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, mAlarmIntent);
        }
        //Callback method to fetch user's ID
        @Subscribe(sticky = true)
        public void onGetUID(LunchActivity.getUid event) {
            mUid = event.uid;
        }

    }
}
