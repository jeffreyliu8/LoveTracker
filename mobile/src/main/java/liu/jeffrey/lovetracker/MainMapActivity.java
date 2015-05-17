package liu.jeffrey.lovetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import liu.jeffrey.lovetracker.db.DbHelper;
import liu.jeffrey.lovetracker.mapUtils.IconGenerator;
import liu.jeffrey.lovetracker.mapUtils.SphericalUtil;
import liu.jeffrey.lovetracker.settings.SettingsActivity;


public class MainMapActivity extends Activity implements OnMapReadyCallback {
    private boolean bDoingOnResume = false;
    private int _id;
    private Location myLocation;
    private Location targetLocation;
    private String targetName;
    private byte[] pict;
    private String targetRegid;
    private GoogleMap mMap;
    private LatLng position;
    private String distance;//mile or km
    private Marker marker;
    private Bitmap icon;
    private Polyline polyline;
    private refreshLocationReceiver locationReceiver;
    private TextView profileTime;
    private TextView profileAddress;
    private Menu myMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(Constants.TAG, "onCreate");
        setContentView(R.layout.activity_main_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mainMap);
        mapFragment.getMapAsync(this);

        // enable home button to go back
        getActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#60000000")));

        profileTime = (TextView) findViewById(R.id.map_cardview_text_time);
        profileAddress = (TextView) findViewById(R.id.map_cardview_text_location);

        locationReceiver = new refreshLocationReceiver();
        //register BroadcastReceiver
        IntentFilter intentFilter = new IntentFilter(refreshLocationReceiver.ACTION_LocationService);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(locationReceiver, intentFilter);
    }

    /**
     * Called when the user clicks the Send/edit msg button
     */
    public void createAndSendMessage(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("New Message");
        alert.setMessage("To " + targetName + ":");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Intent service = new Intent();
                service.putExtra(Constants.TARGET_REGISTRATION_ID, targetRegid);
                service.putExtra("text", value);
                service.setComponent(new ComponentName(getApplicationContext(), BuzzService.class));
                startService(service);
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        final AlertDialog dialog = alert.show();

        //auto focus keyboard
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendBuzz(View v) {
        Intent service = new Intent();
        service.putExtra(Constants.TARGET_REGISTRATION_ID, targetRegid);
        service.setComponent(new ComponentName(this, BuzzService.class));
        startService(service);
    }

    /**
     * Called when the user clicks the open info button
     */
    public void openInfo(View v) {
        if (marker != null) {
            if (marker.isInfoWindowShown()) {
                marker.hideInfoWindow();
                if (polyline != null)
                    polyline.remove();
            } else {
                marker.showInfoWindow();

                // Instantiates a new Polyline object and adds points to define a rectangle
                PolylineOptions rectOptions = new PolylineOptions()
                        .add(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                        .add(new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()))
                        .width(10)
                        .color(Color.argb(200, 26, 35, 126))
                        .geodesic(true);

                // Get back the mutable Polyline
                polyline = mMap.addPolyline(rectOptions);
            }
            showPoint(true, false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG, "onResume");
        if (!CommonUtils.isInternetConnected(this))
            Toast.makeText(this, "Internet is disconnected", Toast.LENGTH_SHORT).show();
        new LoadDbTask().execute();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(Constants.TAG, "onNewIntent");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationReceiver != null) {
            this.unregisterReceiver(locationReceiver);
        }
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        if (mMap != null) {
//            mMap.clear();
//        }
//    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(Constants.TAG, "onMapReady");
        mMap = map;
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());//top margin is 10
        map.setPadding(0, px, 0, 0);
        //setMapPadding(null);
        //map.setMyLocationEnabled(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //setMapPadding(newConfig);
        showPoint(true, false);
    }

    private void setMapPadding(Configuration newConfig) {
        //Log.d(Constants.TAG, "action bar height:" + getActionBarHeight() + " status bar height " + getStatusBarHeight());
        int topHeight = getActionBarHeight() + getStatusBarHeight();
        if (mMap != null) {
            if (newConfig != null) {
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mMap.setPadding(0, topHeight, getNavigationBarHeight(), 0);
                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mMap.setPadding(0, topHeight, 0, getNavigationBarHeight());
                }
            } else {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mMap.setPadding(0, topHeight, getNavigationBarHeight(), 0);
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mMap.setPadding(0, topHeight, 0, getNavigationBarHeight());
                }
            }
        }
    }

    private int getNavigationBarHeight() {
        Resources resources = getResources();
        int id = resources.getIdentifier(
                getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "navigation_bar_height" : "navigation_bar_height_landscape",
                "dimen", "android");
        if (id > 0) {
            return resources.getDimensionPixelSize(id);
        }
        return 0;
    }

    private int getActionBarHeight() {
        int actionBarHeight = getActionBar().getHeight();
        if (actionBarHeight != 0)
            return actionBarHeight;
        final TypedValue tv = new TypedValue();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        } else if (getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        return actionBarHeight;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    void showPoint(boolean bReloadTime, boolean bReloadAddress) {
        if (targetLocation.getLatitude() == 0 && targetLocation.getLongitude() == 0) {
            Toast.makeText(getApplicationContext(), "No records.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bReloadTime) {
            profileTime.setText(DateUtils.getRelativeTimeSpanString(targetLocation.getTime(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE));
            profileTime.startAnimation(AnimationUtils.loadAnimation(this, R.anim.text_fade_in));
        }
        if (bReloadAddress)
            new LoadAddressTask().execute();

        float zoomLevel;
        if (position == null) {//first time getting new location to show
            //marker add and move camera
            position = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 11));
            marker = mMap.addMarker(new MarkerOptions()
                    .title(distance)
                            //.snippet(address)
                    .icon(BitmapDescriptorFactory.fromBitmap(icon))
                    .position(position));
            zoomLevel = 11;
        } else { //not the first time showing the point,
            if (bDoingOnResume) {// doing onResume, a new database needs to be loaded, picture and marker should be updated
                bDoingOnResume = false;
                position = new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude());
                mMap.clear();
                marker = mMap.addMarker(new MarkerOptions()
                        .title(distance)
                                //.snippet(address)
                        .icon(BitmapDescriptorFactory.fromBitmap(icon))
                        .position(position));

            } else {
                // no new location/person data incoming, right now position are only update when there is a new onResume, user probably just pressing center button
            }
            zoomLevel = mMap.getCameraPosition().zoom;
        }

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .bearing(mMap.getCameraPosition().bearing)
                .zoom(zoomLevel)
                .build();
        mMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        //marker.showInfoWindow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_map_refresh) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            iv.startAnimation(rotation);
            item.setActionView(iv);


            Intent service = new Intent();
            service.putExtra(Constants.TARGET_REGISTRATION_ID, targetRegid);
            service.putExtra(Constants.DO_REQUEST_LOCATION, Boolean.TRUE);
            service.setComponent(new ComponentName(this, BuzzService.class));
            startService(service);
            return true;
        } else if (id == android.R.id.home) {
            // home action button
            finish(); // to finish itself and go back to previous activity
        } else if (id == R.id.menu_map_settings) {
            Intent settingIntent = new Intent();
            settingIntent.setComponent(new ComponentName(getApplicationContext(), SettingsActivity.class));
            startActivity(settingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadDbTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                _id = extras.getInt(Constants.USER_ID, -1);
                if (mMap != null)
                    bDoingOnResume = true;
            }
        }

        @Override
        protected Boolean doInBackground(Void... args) {
            DbHelper db = new DbHelper(getApplicationContext());
            targetLocation = db.getLastLocation(_id);
            myLocation = db.getLastLocation(0);

            Cursor c = db.findCursorBy_id(Integer.toString(_id));
            if (c == null) {
                return false;
            } else {
                targetName = c.getString(0);
                pict = c.getBlob(1);
                targetRegid = c.getString(2);
                c.close();

                double distanceInMeter = SphericalUtil.computeDistanceBetween(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()),
                        new LatLng(targetLocation.getLatitude(), targetLocation.getLongitude()));
                DecimalFormat df = new DecimalFormat("#####.##");//The circumference of the earth at the equator is 24,901.55 miles (40,075.16 kilometers).
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String mileOrKmPreference = pref.getString("distance_unit", "0");
                if (mileOrKmPreference.equals("0")) {//mile
                    double distanceInMiles = distanceInMeter * 0.000621371192;
                    distance = df.format(distanceInMiles) + " mi. away";
                } else {//kilometer
                    distance = df.format(distanceInMeter / 1000) + " km away";
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                setTitle(targetName);
                //create a good looking icon
                icon = createGoodLookingIcon(pict);
                showPoint(true, true);
            }
        }
    }

    private Bitmap createGoodLookingIcon(byte[] input) {
        IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());
        ImageView mImageView = new ImageView(getApplicationContext());
        int mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
        mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);
        if (input != null) {
            mImageView.setImageBitmap(BitmapFactory.decodeByteArray(input, 0, input.length));
        } else {
            mImageView.setImageResource(R.drawable.ic_launcher);
        }
        return mIconGenerator.makeIcon();
    }

    private String getAddressFromLatLon(double lat, double lon) {
        String errorMessage = "";
        List<Address> addresses = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(
                    lat,
                    lon,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service_not_available";
            Log.d(Constants.TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_lat_long_used";
            Log.d(Constants.TAG, errorMessage + ". " +
                    "Latitude = " + lat +
                    ", Longitude = " +
                    lon, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found";
                Log.d(Constants.TAG, errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            //Log.d(Constants.TAG, address.toString());

            String city = address.getLocality();
            String state = address.getAdminArea();
            String country = address.getCountryCode();
            return city + ", " + state + ", " + country;
        }
        return "";
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (NoSuchMethodException e) {
                    Log.e(Constants.TAG, "onMenuOpened", e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    private class LoadAddressTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... args) {
            if (targetLocation == null) {
                return null;
            } else {
                return getAddressFromLatLon(targetLocation.getLatitude(), targetLocation.getLongitude());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                if (result.equals(new String(""))) {
                    //did not get address
                } else {
                    profileAddress.setText(result);
                    profileAddress.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_fade_in));
                }
            }
        }
    }

    public class refreshLocationReceiver extends BroadcastReceiver {

        public static final String ACTION_LocationService = "liu.jeffrey.lovetracker.GET_LOCATION";

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (_id == extras.getInt(Constants.USER_ID, -1)) {
                    Log.d(Constants.TAG, "refreshLocationReceiver onReceive");

                    new LoadDbTask().execute();

                    // Get our refresh item from the menu
                    MenuItem m = myMenu.findItem(R.id.menu_map_refresh);
                    if (m.getActionView() != null) {
                        // Remove the animation.
                        m.getActionView().clearAnimation();
                        m.setActionView(null);
                    }
                }
            }
        }
    }
}