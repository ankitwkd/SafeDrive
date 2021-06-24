package com.wkdnotes.root.safedrivescratch.Settings;

        import android.os.Bundle;
        import android.preference.EditTextPreference;
        import android.preference.Preference;
        import android.preference.PreferenceFragment;
        import android.preference.PreferenceGroup;
        import android.preference.PreferenceManager;
        import android.preference.SwitchPreference;
        import android.support.v7.app.AppCompatActivity;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.wkdnotes.root.safedrivescratch.R;




public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager()
                .beginTransaction().
                replace(R.id.content_frame,new SettingsFragment()).commit();

    }

    public static class SettingsFragment extends PreferenceFragment {
        private SwitchPreference warning_preference;
        private EditTextPreference custom_speed_preference;
        private EditTextPreference custom_sms_preference;
        private Preference custom_dnd_preference;
        private boolean unit_preference;
        private SwitchPreference msg_feature;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            warning_preference=(SwitchPreference) findPreference("warning_speed_limit");
            custom_speed_preference=(EditTextPreference) findPreference("custom_speed");

            msg_feature=(SwitchPreference)findPreference("sms_feature");
            custom_sms_preference=(EditTextPreference)findPreference("custom_sms");

            custom_dnd_preference=(EditTextPreference)findPreference("custom_dnd_speed");

            unit_preference=getPreferenceManager().getSharedPreferences().getBoolean("miles_per_hour",false);

            if(warning_preference.getSharedPreferences().getBoolean("warning_speed_limit",false))
                custom_speed_preference.setEnabled(true);
            else
                custom_speed_preference.setEnabled(false);

            warning_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if(o.toString().equals("true"))
                        custom_speed_preference.setEnabled(true);
                    else
                        custom_speed_preference.setEnabled(false);
                    return true;
                }
            });


            if(msg_feature.getSharedPreferences().getBoolean("sms_feature",false))
                custom_sms_preference.setEnabled(true);
            else
                custom_sms_preference.setEnabled(false);

            msg_feature.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    if(o.toString().equals("true"))
                        custom_sms_preference.setEnabled(true);
                    else
                        custom_sms_preference.setEnabled(false);
                    return true;
                }
            });

            //set custom sms
            custom_sms_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    int sms_length=o.toString().length();
                    if(sms_length>160)
                    {
                        Toast.makeText(getActivity(),"Message length exceed !! ",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                    {
                        custom_sms_preference.setSummary(o.toString());
                        return true;
                    }

                }
            });


            //set custom speed
            custom_speed_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    EditTextPreference editTextPreference = (EditTextPreference) preference;
                    float limit;
                    if(unit_preference)
                        limit=140.0f;
                    else
                        limit=280.0f;
                    String speed=o.toString();
                    if(speed.matches("[0-9]+") || speed.contains("."))
                    {
                        if (Float.parseFloat(speed) >= limit)
                        {
                            Toast.makeText(getActivity(), "Please enter a value less than "+String.valueOf(limit)+". ", Toast.LENGTH_SHORT).show();
                            return false;
                        } else
                            editTextPreference.setSummary(o.toString());
                        return true;
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"Please enter the valid speed",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            });

            //set custom drive mode speed
            custom_dnd_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    EditTextPreference editTextPreference = (EditTextPreference) preference;
                    float limit;
                    if(unit_preference)
                        limit=140.0f;
                    else
                        limit=280.0f;
                    String speed=o.toString();
                    if(speed.matches("[0-9]+") || speed.contains("."))
                    {
                        if (Float.parseFloat(speed) >= limit)
                        {
                            Toast.makeText(getActivity(), "Please enter a value less than "+String.valueOf(limit)+". ", Toast.LENGTH_SHORT).show();
                            return false;
                        } else
                            editTextPreference.setSummary(o.toString());
                        return true;
                    }
                    else
                    {
                        Toast.makeText(getActivity(),"Please enter the valid speed",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            });
        }
    }
}

