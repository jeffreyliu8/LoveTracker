/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package liu.jeffrey.lovetracker;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.maps.model.LatLng;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

import liu.jeffrey.lovetracker.db.DbHelper;
import liu.jeffrey.lovetracker.mapUtils.SphericalUtil;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {

    private NotificationManager mNotificationManager;
    private Uri alarmSound;
    private Location remoteLocation;
    private SharedPreferences pref;
    private int _id;
    private String name;
    private String regid;
    private byte[] pict;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        remoteLocation = new Location(LocationManager.PASSIVE_PROVIDER);

        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             */

            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {

                //Log.i(Constants.TAG, "Completed work @ " + SystemClock.elapsedRealtime());
                // Post notification of received message.

                remoteLocation.setLatitude(Double.parseDouble(extras.getString("latitude")));
                remoteLocation.setLongitude(Double.parseDouble(extras.getString("longitude")));
                remoteLocation.setTime(Long.parseLong(extras.getString("locationTime")));

                if (extras != null) {
                    DbHelper db = new DbHelper(getApplicationContext());

                    Cursor c = db.findCursorByRegid(extras.getString("sender"));
                    if (c == null) {
                        Log.d(Constants.TAG, "someone not friend wants to message you.");//TODO: what if not your friend msg you
                        Log.d(Constants.TAG, extras.getString(Constants.DO_ADD_NEW_USER_WITH_REGID));
                        if(extras.getString(Constants.DO_ADD_NEW_USER_WITH_REGID)!=null){
                            //there is a person who has a name that wants to be your friend
                            //create a notification for adding this friend
                            name = extras.getString(Constants.DO_ADD_NEW_USER_WITH_REGID);
                            regid = extras.getString("sender");
                            _id = -1;
                            createAddFriendNotification();
                        }
                        return;
                    } else {
                        name = c.getString(0);
                        regid = extras.getString("sender");
                        pict = c.getBlob(1);
                        _id = c.getInt(2);
                        c.close();
                    }

                    if (remoteLocation.getLatitude() != 0 && remoteLocation.getLongitude() != 0) {
                        db.insertNewLocation(_id, remoteLocation);
                    }

                    //String name = friendDatabase.findNameByRegid(extras.getString("sender"));
                    if (extras.getString(Constants.DO_NFC_ADD_NEW_USER) != null) {
                        if (extras.getString(Constants.DO_NFC_ADD_NEW_USER).equals("true")) {
                            //util.saveTargetRegistrationId(getApplicationContext(), extras.getString("sender"));//TODO: NO NEED TO DO THIS
                        } else {
                            sendNotification(extras.getString("data"));
                        }
                    } else if (extras.getString(Constants.DO_REQUEST_LOCATION) != null) {
                        if (extras.getString(Constants.DO_REQUEST_LOCATION).equals("true")) {
                            //save a history of someone is trying to get your location
                            Log.d(Constants.TAG, "new location request from " + name);
                            //no need to open notification
                            //need to buzz back the location without creating a notification

                            Intent service = new Intent();
                            service.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
                            service.putExtra(Constants.DO_REQUEST_LOCATION_ACK, Boolean.TRUE);
                            service.setComponent(new ComponentName(this, BuzzService.class));
                            startService(service);
                        } else {
                            sendNotification(extras.getString("data"));
                        }
                    } else if (extras.getString(Constants.DO_REQUEST_LOCATION_ACK) != null) {
                        if (extras.getString(Constants.DO_REQUEST_LOCATION_ACK).equals("true")) {
                            Intent intentResponse = new Intent();
                            intentResponse.setAction(MainMapActivity.refreshLocationReceiver.ACTION_LocationService);
                            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);//?
                            intentResponse.putExtra(Constants.USER_ID,_id);
                            sendBroadcast(intentResponse);
                        } else {
                            sendNotification(extras.getString("data"));
                        }
                    } else {
                        sendNotification(extras.getString("data"));
                    }
                } else {
                    Log.d(Constants.TAG, "Received extra is null");
                }

                Log.d(Constants.TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent buzzBackIntent = new Intent(this, BuzzService.class);
        buzzBackIntent.setAction("buzzBackIntent"); //need to not have intent overridden
        buzzBackIntent.putExtra(Constants.NOTIFY_ID, _id);
        buzzBackIntent.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
        PendingIntent pendingIntentBuzzBack = PendingIntent.getService(this, _id, buzzBackIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent openMainMapIntent = new Intent(this, MainMapActivity.class);
        openMainMapIntent.putExtra(Constants.USER_ID, _id);
        PendingIntent mainIntent = PendingIntent.getActivity(this, _id, openMainMapIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap profilePictureIcon;
        if (pict == null) {
            profilePictureIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        } else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(pict, 0, pict.length);
            profilePictureIcon = CommonUtils.getCircleBitmap(bitmap);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.love_tracker_hollow) //required
                .setContentTitle(name) //required
                .setContentText(msg) //required
                .setLargeIcon(profilePictureIcon)
                .setColor(Color.argb(0, 183, 28, 28))
                .setContentIntent(mainIntent)
                .setLights(Color.BLUE, 3000, 3000)
                .setVibrate(new long[]{0, 200})
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(1);
        //.addAction(android.R.drawable.ic_menu_send, "Buzz", pendingIntentBuzzBack);

        Boolean showCustomMsg1 = pref.getBoolean("custom_msg1_switch", Boolean.FALSE);
        if (showCustomMsg1) {
            Intent buzzBackTextIntent = new Intent(this, BuzzService.class);
            buzzBackTextIntent.setAction("buzzBackTextIntent"); //need to not have intent overridden
            buzzBackTextIntent.putExtra(Constants.NOTIFY_ID, _id);
            buzzBackTextIntent.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
            buzzBackTextIntent.putExtra("text", pref.getString("custom_msg1", "Buzz"));
            PendingIntent pendingIntentBuzzTextBack = PendingIntent.getService(this, _id, buzzBackTextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(android.R.drawable.ic_menu_edit, pref.getString("custom_msg1", "Buzzing"), pendingIntentBuzzTextBack);
        } else {
            mBuilder.addAction(android.R.drawable.ic_menu_send, "Buzz", pendingIntentBuzzBack);
        }

        Boolean showCustomMsg2 = pref.getBoolean("custom_msg2_switch", Boolean.FALSE);
        if (showCustomMsg2) {
            Intent buzzBackTextIntent2 = new Intent(this, BuzzService.class);
            buzzBackTextIntent2.setAction("buzzBackTextIntent2"); //need to not have intent overridden
            buzzBackTextIntent2.putExtra(Constants.NOTIFY_ID, _id);
            buzzBackTextIntent2.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
            buzzBackTextIntent2.putExtra("text", pref.getString("custom_msg2", "Buzz"));
            PendingIntent pendingIntentBuzzTextBack2 = PendingIntent.getService(this, _id, buzzBackTextIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(android.R.drawable.ic_menu_edit, pref.getString("custom_msg2", "Buzzing"), pendingIntentBuzzTextBack2);
        }


        if (remoteLocation.getLatitude() == 0 && remoteLocation.getLongitude() == 0) {
            // sender did not provide correct position, maybe wifi is off, or he/she is at the equator?
            // no need to add anything extra to this
        } else {//sender sent location
            //mBuilder.addAction(android.R.drawable.ic_dialog_map, "Map", pendingIntentOpenMap);

            Boolean showMapPref = pref.getBoolean("display_notification_map_preference", Boolean.TRUE);
            if (showMapPref) {
                ImageView bottomImageView = new ImageView(getApplicationContext());
                try {
                    String url1 = "https://maps.googleapis.com/maps/api/staticmap?size=400x400&scale=2&markers=" + remoteLocation.getLatitude() + "," + remoteLocation.getLongitude() + "&key=AIzaSyBqQFw6DFO-HAyE7uxq35wNN7uxpV62x7I";
                    URL ulrn = new URL(url1);
                    HttpURLConnection con = (HttpURLConnection) ulrn.openConnection();
                    InputStream is = con.getInputStream();
                    Bitmap bm = BitmapFactory.decodeStream(is);
                    if (null != bm) {
                        bottomImageView.setImageBitmap(bm);
                        mBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bm));
                    } else
                        Log.d(Constants.TAG, "The Bitmap is NULL");
                } catch (Exception e) {
                }
            }

            // check if we can get host position
            Location myLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            if (CommonUtils.canGetLocation(getApplicationContext())) {
                //TODO:get the latest location instead of from db
                DbHelper db = new DbHelper(getApplicationContext());
                myLocation = db.getLastLocation(0);
            }

            double latitude = myLocation.getLatitude();
            double longitude = myLocation.getLongitude();
            if (latitude == 0 && longitude == 0) {
                // host did not provide correct position, maybe wifi is off
            } else {
                //we have our own location, we should be able to calculate distance
                LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                LatLng remoteLatLng = new LatLng(remoteLocation.getLatitude(), remoteLocation.getLongitude());
                double distanceInMeter = SphericalUtil.computeDistanceBetween(myLatLng, remoteLatLng);
                DecimalFormat df = new DecimalFormat("#####.##");//The circumference of the earth at the equator is 24,901.55 miles (40,075.16 kilometers).

                String mileOrKmPreference = pref.getString("distance_unit", "0");
                if (mileOrKmPreference.equals("0")) {//mile
                    double distanceInMiles = distanceInMeter * 0.000621371192;
                    mBuilder.setSubText(df.format(distanceInMiles) + " miles away");
                } else {//kilometer
                    mBuilder.setSubText(df.format(distanceInMeter / 1000) + " kilometers away");
                }
            }
        }
        mNotificationManager.notify(_id, mBuilder.build());
    }


    private void createAddFriendNotification(){
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

//        Intent buzzBackIntent = new Intent(this, BuzzService.class);
//        buzzBackIntent.setAction("buzzBackIntent"); //need to not have intent overridden
//        buzzBackIntent.putExtra(Constants.NOTIFY_ID, _id);
//        buzzBackIntent.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
//        PendingIntent pendingIntentBuzzBack = PendingIntent.getService(this, _id, buzzBackIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.love_tracker_hollow) //required
                .setContentTitle(name) //required
                .setContentText("wants to be your friend") //required
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setColor(Color.argb(0, 183, 28, 28))
//                .setContentIntent(mainIntent)
                .setLights(Color.BLUE, 3000, 3000)
                .setVibrate(new long[]{0, 200})
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setPriority(1);
        //.addAction(android.R.drawable.ic_menu_send, "Buzz", pendingIntentBuzzBack);

        mNotificationManager.notify(_id, mBuilder.build());
    }
}