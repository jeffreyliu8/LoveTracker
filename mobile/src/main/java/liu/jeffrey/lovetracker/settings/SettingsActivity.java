package liu.jeffrey.lovetracker.settings;

import android.app.ActivityOptions;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import liu.jeffrey.lovetracker.R;
import liu.jeffrey.lovetracker.settings.location.SetLocationPreferenceActivity;
import liu.jeffrey.lovetracker.settings.notification.SetNotificationPreferenceActivity;
import liu.jeffrey.lovetracker.settings.profile.ProfileSettingActivity;
import liu.jeffrey.lovetracker.settings.widget.SetWidgetPreferenceActivity;

public class SettingsActivity extends ListActivity {

    private SettingArrayAdapter adapter;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        String[] values = new String[]{"Profile", "Location", "Notification", "Widget"};
        adapter = new SettingArrayAdapter(this, values);
        setListAdapter(adapter);

        // enable home button to go back
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);

        if (item.equals(new String("Profile"))) {
            Intent profileIntent = new Intent();
            profileIntent.setComponent(new ComponentName(getApplicationContext(), ProfileSettingActivity.class));

            ImageView imageView = (ImageView) v.findViewById(R.id.settingIcon);
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this, imageView, "profile_photo_transition");
            startActivity(profileIntent, options.toBundle());
        } else if (item.equals(new String("Location"))) {
            Intent intent = new Intent();
            intent.setClass(this, SetLocationPreferenceActivity.class);
            startActivityForResult(intent, 0);
        }else if (item.equals(new String("Notification"))) {
            Intent intent = new Intent();
            intent.setClass(this, SetNotificationPreferenceActivity.class);
            startActivityForResult(intent, 0);
        } else if (item.equals(new String("Widget"))) {
            Intent intent = new Intent();
            intent.setClass(this, SetWidgetPreferenceActivity.class);
            startActivityForResult(intent, 0);
        }
        //Toast.makeText(this, item, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();
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