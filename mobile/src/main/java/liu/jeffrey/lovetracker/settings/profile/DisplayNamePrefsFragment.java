package liu.jeffrey.lovetracker.settings.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.widget.Toast;

import liu.jeffrey.lovetracker.R;

public class DisplayNamePrefsFragment extends PreferenceFragment {

    public static Context context;
    private final static String defaultName = "Random Person";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.display_name_pref);
        context = getActivity();
        bindPreferenceSummaryToValue(findPreference("display_name_preference"));
    }

    private static void setPref() {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mySharedPreferences.edit().putString("display_name_preference", defaultName).apply();
    }


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {

            } else if (preference instanceof RingtonePreference) {

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.


                if (stringValue.equals("")) {

                    Toast.makeText(context, "Display name cannot be empty!", Toast.LENGTH_SHORT).show();
                    setPref();
                    preference.setSummary(defaultName);
                } else {
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };
}