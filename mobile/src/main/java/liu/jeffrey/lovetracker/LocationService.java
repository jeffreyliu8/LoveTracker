package liu.jeffrey.lovetracker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;

import liu.jeffrey.lovetracker.MapActivity.LocationReceiver;
import liu.jeffrey.lovetracker.db.DbHelper;


public class LocationService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private String formattedTime;

    public LocationService() {
        super("LocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (CommonUtils.canGetLocation(getApplicationContext())) {
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        } else {
            Log.d(Constants.TAG,"location service onHandleIntent cannot get location.");
            //CommonUtils.saveLocation(getApplicationContext(), new Location(LocationManager.PASSIVE_PROVIDER));//TODO:WHY did i add this?
        }
    }

    @Override
    public void onDestroy() {
        Log.d(Constants.TAG, "onDestroy location service");
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(Constants.TAG, "buildGoogleApiClient location");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            // Get location request from time to time, comment to not receive update
//            LocationRequest mLocationRequest = new LocationRequest();
//            mLocationRequest.setInterval(10000);
//            mLocationRequest.setFastestInterval(5000);
//            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            // end of update request

            formattedTime = CommonUtils.longTimeFormatConvert(mLastLocation.getTime());

            Log.d(Constants.TAG, "onConnected :" + mLastLocation.getLatitude() + mLastLocation.getLongitude() + " getAccuracy = " + mLastLocation.getAccuracy() + " " + formattedTime);

            //CommonUtils.saveLocation(getApplicationContext(), mLastLocation);
            DbHelper db = new DbHelper(getApplicationContext());
            db.insertNewLocation(0,mLastLocation);

            //return result
            Intent intentResponse = new Intent();
            intentResponse.setAction(LocationReceiver.ACTION_LocationService);
            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);//?
            intentResponse.putExtra("latitude", mLastLocation.getLatitude());
            intentResponse.putExtra("longitude", mLastLocation.getLongitude());
            intentResponse.putExtra("accuracy", mLastLocation.getAccuracy());
            intentResponse.putExtra("time", formattedTime);
            sendBroadcast(intentResponse);
        }
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
    public void onLocationChanged(Location location) {
        formattedTime = CommonUtils.longTimeFormatConvert(location.getTime());
        Log.d(Constants.TAG, "onLocationChanged: getAccuracy = " + location.getAccuracy() + " " + formattedTime);

        DbHelper db = new DbHelper(getApplicationContext());
        db.insertNewLocation(0,mLastLocation);

        //return result
        Intent intentResponse = new Intent();
        intentResponse.setAction(LocationReceiver.ACTION_LocationService);
        intentResponse.addCategory(Intent.CATEGORY_DEFAULT);//?
        intentResponse.putExtra("latitude", location.getLatitude());
        intentResponse.putExtra("longitude", location.getLongitude());
        intentResponse.putExtra("accuracy", location.getAccuracy());
        intentResponse.putExtra("time", formattedTime);
        sendBroadcast(intentResponse);
    }
}
