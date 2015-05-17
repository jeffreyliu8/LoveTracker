package liu.jeffrey.lovetracker.settings.profile;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class SetDisplayNamePreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new DisplayNamePrefsFragment()).commit();

        // enable home button to go back
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // home action button
            finish(); // to finish itself and go back to previous activity
        }
        return super.onOptionsItemSelected(item);
    }
}
