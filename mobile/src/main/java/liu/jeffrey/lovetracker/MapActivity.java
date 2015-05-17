package liu.jeffrey.lovetracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
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

public class MapActivity extends Activity implements OnMapReadyCallback {
    private int notifyID;
    private double latitude;
    private double longitude;
    private long locationTime;
    GoogleMap mMap;
    Boolean bIsLocationFromSender = false;
    Marker marker;
    Bitmap markerBitmap;
    String friendName;
    Bitmap friendBitmap;

    private LocationReceiver locationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // getIntent() should always return the most recent
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Constants.TAG, "map on resume");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Log.d(Constants.TAG, "map on resume extra not null");
            friendName = extras.getString("name");
            if (friendName.equals("")) {
                Log.d(Constants.TAG, "friend name empty");
                friendName = "Your friend";
            }
            byte[] pict = extras.getByteArray("image");
            notifyID = extras.getInt(Constants.NOTIFY_ID, -1);
            latitude = extras.getDouble("latitude");
            longitude = extras.getDouble("longitude");
            locationTime = extras.getLong("locationTime");

            if (latitude == 0 && longitude == 0)
                bIsLocationFromSender = false;
            else
                bIsLocationFromSender = true;

            if (notifyID >= 0) {
                Log.d(Constants.TAG, "notifyID = " + notifyID);
                CommonUtils.cancelNotification(getApplicationContext(), notifyID);
                setTitle(friendName);

                if (pict == null) {
                    friendBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                } else {
                    friendBitmap = BitmapFactory.decodeByteArray(pict, 0, pict.length);
                }

                if (mMap != null) {
                    mMap.clear();
                    addFriendLocationOnMap(mMap);
                }
            }
        }

        if (!bIsLocationFromSender) {// location just for myself

            if (!CommonUtils.canGetLocation(getApplicationContext())) {
                Toast.makeText(getApplicationContext(),
                        "GPS not enabled.",
                        Toast.LENGTH_SHORT).show();
            } else {
                markerBitmap = CommonUtils.loadProfileImage(this,Boolean.TRUE);

                Intent service = new Intent(getApplicationContext(), LocationService.class);
                //service.setComponent(new ComponentName(getApplicationContext(), LocationService.class));
                startService(service);

                locationReceiver = new LocationReceiver();
                //register BroadcastReceiver
                IntentFilter intentFilter = new IntentFilter(LocationReceiver.ACTION_LocationService);
                intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
                registerReceiver(locationReceiver, intentFilter);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(Constants.TAG, "onMapReady");

        //map.setMyLocationEnabled(true);

        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        addFriendLocationOnMap(map);

        mMap = map;
    }

    private void addFriendLocationOnMap(GoogleMap map) {
        if (bIsLocationFromSender) { //remote location
            LatLng location = new LatLng(latitude, longitude);
            //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.19153, -98.04062), 1));

            Marker remoteMarker = map.addMarker(new MarkerOptions()
                    .title(friendName)
                    .snippet(CommonUtils.longTimeFormatConvert(locationTime))
                    .position(location)
                    .icon(BitmapDescriptorFactory.fromBitmap(CommonUtils.getCircleBitmap(friendBitmap)))
                    .anchor((float) 0.5, (float) 0.5));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(location).zoom(17).build();
            map.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));

            //remoteMarker.showInfoWindow();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationReceiver != null) {
            this.unregisterReceiver(locationReceiver);
        }
    }


    public class LocationReceiver extends BroadcastReceiver {

        public static final String ACTION_LocationService = "liu.jeffrey.lovetracker.GET_LOCATION";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(Constants.TAG, "map onReceive");
            if (marker != null) {
                marker.remove();
                Log.d(Constants.TAG, "map onReceive marker removed.");
            }

            latitude = intent.getDoubleExtra("latitude", 0);
            longitude = intent.getDoubleExtra("longitude", 0);
            String time = intent.getStringExtra("time");

            LatLng location = new LatLng(latitude, longitude);

            if (marker == null) {// first time
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(38.19153, -98.04062), 1));//USA
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(location).zoom(17).build();
                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition));
            }

            marker = mMap.addMarker(new MarkerOptions()
                    .title("You")
                    .snippet(time)
                    .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                    .anchor((float) 0.5, (float) 0.5)
                    .position(location));

            //marker.showInfoWindow();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



}