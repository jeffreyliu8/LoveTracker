package liu.jeffrey.lovetracker.ActivityRecognition;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;

import liu.jeffrey.lovetracker.Constants;


public class DetectActIntentService extends IntentService implements ConnectionCallbacks, OnConnectionFailedListener, ResultCallback<Status> {

    /**
     * Provides the entry point to Google Play services.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Used when requesting or removing activity detection updates.
     */
    private PendingIntent mActivityDetectionPendingIntent;

    public DetectActIntentService() {
        // Use the TAG to name the worker thread.
        super("DetectActIntentService");
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * ActivityRecognition API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.d(Constants.TAG, "buildGoogleApiClient detect act");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

//    @Override
//    public void onDestroy() {
//        Log.d(Constants.TAG, "onDestroy DetectActIntentService");
//        super.onDestroy();
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect(); //we will never disconnect this because we always want to check activity status
//        }
//    }

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
    protected void onHandleIntent(Intent intent) {
        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(Constants.TAG, "Connected to GoogleApiClient!!");
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mGoogleApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    /**
     * Runs when the result of calling requestActivityUpdates() and removeActivityUpdates() becomes
     * available. Either method can complete successfully or with an error.
     *
     * @param status The Status returned through a PendingIntent when requestActivityUpdates()
     *               or removeActivityUpdates() are called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
            Log.d(Constants.TAG, "status.isSuccess()");
        } else {
            Log.e(Constants.TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Log.d(Constants.TAG, "getActivityDetectionPendingIntent");
        // Reuse the PendingIntent if we already have it.
        if (mActivityDetectionPendingIntent != null) {
            Log.d(Constants.TAG, "mActivityDetectionPendingIntent != null");
            return mActivityDetectionPendingIntent;
        }
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
