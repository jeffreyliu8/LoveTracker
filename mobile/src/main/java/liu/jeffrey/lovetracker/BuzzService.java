package liu.jeffrey.lovetracker;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import liu.jeffrey.lovetracker.db.DbHelper;

public class BuzzService extends IntentService {
    public BuzzService() {
        super("BuzzService");
    }

    private double latitude = 0;
    private double longitude = 0;
    private long locationTime;

    @Override
    protected void onHandleIntent(Intent intent) {

        Intent service = new Intent(getApplicationContext(), LocationService.class);
        startService(service);
        String targatRegid = "";
        String text = null;
        Boolean bDoNFCAdd = false;
        Boolean bRequestLocation = false;
        Boolean bRequestLocationACK = false;
        Boolean bDoRegidAdd = false;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            bDoNFCAdd = extras.getBoolean(Constants.DO_NFC_ADD_NEW_USER, false);
            bDoRegidAdd = extras.getBoolean(Constants.DO_ADD_NEW_USER_WITH_REGID, false);
            bRequestLocation = extras.getBoolean(Constants.DO_REQUEST_LOCATION, false);
            bRequestLocationACK = extras.getBoolean(Constants.DO_REQUEST_LOCATION_ACK, false);
            targatRegid = extras.getString(Constants.TARGET_REGISTRATION_ID);
            text = extras.getString("text");
            int _id = extras.getInt(Constants.NOTIFY_ID, -1);
            CommonUtils.cancelNotification(getApplicationContext(), _id);
        }

        String senderRegid = CommonUtils.getRegistrationId(getApplicationContext());

        DefaultHttpClient httpclient = new DefaultHttpClient();

        Log.d(Constants.TAG, "targatRegid = " + targatRegid);

        Location location = new Location(LocationManager.PASSIVE_PROVIDER);

        if (CommonUtils.canGetLocation(getApplicationContext())) {
            DbHelper db = new DbHelper(getApplicationContext());
            location = db.getLastLocation(0);//TODO: instead of getting from db, should just get location right now, but that is the first thing we do in onHandleIntent
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        locationTime = location.getTime();

        HttpPost httppost = new HttpPost(
                "http://www.foodxd.com/jeff/buzzerPostMsg.php");
        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("from", senderRegid));
            nameValuePairs.add(new BasicNameValuePair("to", targatRegid));
            if (text == null) {
                nameValuePairs.add(new BasicNameValuePair("message", "Buzzing"));
            } else {
                nameValuePairs.add(new BasicNameValuePair("message", text));
            }
            nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(latitude)));
            nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(longitude)));
            nameValuePairs.add(new BasicNameValuePair("locationTime", Long.toString(locationTime)));
            if (bDoNFCAdd) {
                nameValuePairs.add(new BasicNameValuePair(Constants.DO_NFC_ADD_NEW_USER, "true"));
            } else if (bDoRegidAdd) {
                nameValuePairs.add(new BasicNameValuePair(Constants.DO_ADD_NEW_USER_WITH_REGID, CommonUtils.getDisplayNamePreference(getApplicationContext())));//send the requester's name, you
            } else if (bRequestLocation) {
                nameValuePairs.add(new BasicNameValuePair(Constants.DO_REQUEST_LOCATION, "true"));
            } else if (bRequestLocationACK) {
                nameValuePairs.add(new BasicNameValuePair(Constants.DO_REQUEST_LOCATION_ACK, "true"));
            }
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            Log.d(Constants.TAG, "Sent!");

        } catch (ClientProtocolException e) {

        } catch (IOException e) {

        }
    }
}
