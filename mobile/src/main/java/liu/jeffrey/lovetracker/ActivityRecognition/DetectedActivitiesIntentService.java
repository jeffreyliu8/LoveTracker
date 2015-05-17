/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liu.jeffrey.lovetracker.ActivityRecognition;

import android.app.IntentService;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

import liu.jeffrey.lovetracker.Constants;
import liu.jeffrey.lovetracker.LocationService;
import liu.jeffrey.lovetracker.R;

/**
 * IntentService for handling incoming intents that are generated as a result of requesting
 * activity updates
 */
public class DetectedActivitiesIntentService extends IntentService {

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super("DetectedActivitiesIntentService");
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        // Log each activity.
//        for (DetectedActivity da : detectedActivities) {
//            Log.d(Constants.TAG, getActivityString(da.getType()) + " " + da.getConfidence() + "%");
//        }

        if (detectedActivities.get(0).getType() == DetectedActivity.STILL &&
                detectedActivities.get(0).getConfidence() == 100) {
            //Log.d(Constants.TAG, "still 100 percent so no record");
        } else {
            Log.d(Constants.TAG, "Not still, recording");
            Intent locationService = new Intent(this, LocationService.class);
            startService(locationService);
        }

        // Broadcast the list of detected activities.
        //Intent localIntent = new Intent(Constants.BROADCAST_ACTION);
        //localIntent.putExtra(Constants.ACTIVITY_EXTRA, detectedActivities);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    private String getActivityString(int detectedActivityType) {
        Resources resources = getResources();
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return resources.getString(R.string.in_vehicle);
            case DetectedActivity.ON_BICYCLE:
                return resources.getString(R.string.on_bicycle);
            case DetectedActivity.ON_FOOT:
                return resources.getString(R.string.on_foot);
            case DetectedActivity.RUNNING:
                return resources.getString(R.string.running);
            case DetectedActivity.STILL:
                return resources.getString(R.string.still);
            case DetectedActivity.TILTING:
                return resources.getString(R.string.tilting);
            case DetectedActivity.UNKNOWN:
                return resources.getString(R.string.unknown);
            case DetectedActivity.WALKING:
                return resources.getString(R.string.walking);
            default:
                return resources.getString(R.string.unidentifiable_activity, detectedActivityType);
        }
    }
}