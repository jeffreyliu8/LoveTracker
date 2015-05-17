package liu.jeffrey.lovetracker.mainFragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import liu.jeffrey.lovetracker.CommonUtils;
import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.LocationService;
import liu.jeffrey.lovetracker.R;
import liu.jeffrey.lovetracker.db.DbHelper;
import liu.jeffrey.lovetracker.mapUtils.IconGenerator;
import liu.jeffrey.lovetracker.mapUtils.SphericalUtil;

public class FindPeopleFragment extends Fragment implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    public FindPeopleFragment() {
    }

    private static final int minDistanceDifferenceToShow = 30;
    private static final int numberOfPointsToDisplayOnMap = 250;

    private MapFragment mapFragment;
    private GoogleMap mMap;
    private ArrayList<LatLng> arrayList;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private LocationRequest mLocationRequest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate and return the layout
        View v = inflater.inflate(R.layout.fragment_find_people, container,
                false);

        Intent locationService = new Intent(getActivity(), LocationService.class);
        getActivity().startService(locationService);

        mapFragment = (MapFragment) getChildFragmentManager()
                .findFragmentById(R.id.my_map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        createLocationRequest();
        buildGoogleApiClient();
        mGoogleApiClient.connect();

        return v;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        startLocationUpdates();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(Constants.TAG, "onLocationChanged: getAccuracy = " + location.getAccuracy());

        // db = new DbHelper(getApplicationContext());
        //db.insertNewLocation(0,mLastLocation);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.d(Constants.TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.d(Constants.TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    //todo : write draw lines function

    @Override
    public void onMapReady(GoogleMap map) {
        Log.d(Constants.TAG, "onMapReady");
        mMap = map;
        map.setMyLocationEnabled(true);

        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        arrayList = new ArrayList<LatLng>();

        DbHelper db = new DbHelper(getActivity()); //TODO: asyncTask
        Cursor cursor = db.getMyLocations();

        if (cursor != null && cursor.getCount() > 0) {
            for (int count = 0; count < cursor.getCount(); count++) {

                LatLng tempLocation = new LatLng(cursor.getDouble(2), cursor.getDouble(3));
                cursor.moveToNext();

                if (count == 0) {
                    arrayList.add(tempLocation);
                } else {
                    double distanceInMeter = SphericalUtil.computeDistanceBetween(tempLocation, arrayList.get(arrayList.size() - 1));
                    if (distanceInMeter < minDistanceDifferenceToShow) {
                        continue;
                    } else {
                        arrayList.add(tempLocation);
                    }
                }
            }
            cursor.close();
        }

        //move camera to the last history
        if (arrayList != null && !arrayList.isEmpty()) {
            Log.d(Constants.TAG, "arraylist size is " + arrayList.size());

//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .target(new LatLng(centerLat, centerLng))
//                    .zoom(13).build();
//            map.animateCamera(CameraUpdateFactory
//                    .newCameraPosition(cameraPosition));

            MarkerOptions marker = new MarkerOptions()
                    .position(arrayList.get(0))
                    .title("Latest location")
                    .icon(BitmapDescriptorFactory.fromBitmap(createGoodLookingIcon(CommonUtils.loadProfileImage(getActivity(), Boolean.FALSE))));
            mMap.addMarker(marker);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < arrayList.size(); i++) {
                builder.include(arrayList.get(i));
            }
            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 200); //padding 200 px
            mMap.animateCamera(cu);

            int numberOfRowsToDisplay = arrayList.size();
            if (numberOfRowsToDisplay > numberOfPointsToDisplayOnMap)
                numberOfRowsToDisplay = numberOfPointsToDisplayOnMap;

            for (int i = 1; i < numberOfRowsToDisplay; i++) {
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(arrayList.get(i - 1), arrayList.get(i))
                        .width(10)
                        .color(Color.argb(255 / numberOfRowsToDisplay * (numberOfRowsToDisplay - i), 26, 35, 126))
                        .geodesic(true));
            }
        }

        //draw all the dots in black stroke
//        PolylineOptions lineOption=new PolylineOptions();
//        lineOption.addAll(arrayList);
//        Polyline polyline = map.addPolyline(lineOption);
    }

    private Bitmap createGoodLookingIcon(Bitmap bitmap) {
        IconGenerator mIconGenerator = new IconGenerator(getActivity());
        ImageView mImageView = new ImageView(getActivity());
        int mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
        mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        } else {
            mImageView.setImageResource(R.drawable.ic_launcher);
        }
        return mIconGenerator.makeIcon();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_map, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_clear_location) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Clear Location History")
                    .setMessage("Are you sure you want to delete all your location histories?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            DbHelper db = new DbHelper(getActivity());
                            db.deleteMapHistory(0);
                            if (mMap != null)
                                mMap.clear();
                            Intent locationService = new Intent(getActivity(), LocationService.class);
                            getActivity().startService(locationService);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}