package liu.jeffrey.lovetracker.settings.location;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.R;

public class LocationPrefsFragment extends PreferenceFragment {

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100; // 100 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.location_pref);

        bindPreferenceSummaryToValue(findPreference("GPS_preference"));
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
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getBoolean(preference.getKey(), Boolean.FALSE));
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            Context ctx = getActivity().getApplicationContext();
            LocationManager L = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

            Intent intent = new Intent(ctx, SetLocationPreferenceActivity.class);
            PendingIntent pendingInt = PendingIntent.getService(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if (stringValue.equals("true")) {
                turnGPSOn(L, pendingInt);
            } else {
                turnGPSOff(L, pendingInt);
            }

            return true;
        }
    };


    private void turnGPSOn(LocationManager mgr, PendingIntent pendingInt) {
        Log.d(Constants.TAG, "turnGPSOn");

        if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Intent I = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(I);
        } else {
            mgr.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, pendingInt);
        }
    }

    private void turnGPSOff(LocationManager mgr, PendingIntent pendingInt) {
        Log.d(Constants.TAG, "turnGPSOff");
        if (mgr != null) {
            mgr.removeUpdates(pendingInt);
        }
    }
}
