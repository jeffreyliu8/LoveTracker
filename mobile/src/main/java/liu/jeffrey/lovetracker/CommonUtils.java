package liu.jeffrey.lovetracker;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {

    private CommonUtils(){};

    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getAppPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return context.getSharedPreferences(Constants.APP_SHARED_PREFERENCES,
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public static String getRegistrationId(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        String registrationId = prefs.getString(Constants.REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Constants.TAG, "Registration not found, please update the SENDER_ID");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt("appVersion", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Constants.TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

//    /**
//     * save the target registration ID for application
//     */
//    public static void saveTargetRegistrationId(Context context, String targetRegid) {
//        final SharedPreferences prefs = getAppPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString(Constants.TARGET_REGISTRATION_ID, targetRegid);
//        editor.commit();
//    }

    /**
     * save the get my location preference
     */
    public static void checkMykLocationPreference(Context context, Boolean bGetMyLocation) {
        final SharedPreferences prefs = getAppPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.GET_MY_LOCATION_PREFERENCES, bGetMyLocation);
        editor.commit();
    }

    /**
     * get my location preference
     */
    public static Boolean getMyLocationPreference(Context context) {
        final SharedPreferences prefs = getAppPreferences(context);
        return prefs.getBoolean(Constants.GET_MY_LOCATION_PREFERENCES, Boolean.TRUE);
    }

    /**
     * get my display name preference
     */
    public static String getDisplayNamePreference(Context context) {
        final SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return mySharedPreferences.getString("display_name_preference", "");
    }

//    /**
//     * save my location
//     */
//    public static void saveLocation(Context context, Location location) {
//        Log.d(Constants.TAG, "saving location");
//        final SharedPreferences prefs = getAppPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putFloat(Constants.LOCATION_LATITUDE, (float) location.getLatitude());
//        editor.putFloat(Constants.LOCATION_LONGITUDE, (float) location.getLongitude());
//        editor.putFloat(Constants.LOCATION_ACCURACY, location.getAccuracy());
//        editor.putLong(Constants.LOCATION_TIME, location.getTime());
//        editor.commit();
//    }

//    /**
//     * get my location preference
//     */
//    public static Location getLocation(Context context) {
//        final SharedPreferences prefs = getAppPreferences(context);
//        Location location = new Location(LocationManager.PASSIVE_PROVIDER);
//
//        location.setLatitude(prefs.getFloat(Constants.LOCATION_LATITUDE, 0));
//        location.setLongitude(prefs.getFloat(Constants.LOCATION_LONGITUDE, 0));
//        location.setAccuracy(prefs.getFloat(Constants.LOCATION_ACCURACY, 0));
//        location.setTime(prefs.getLong(Constants.LOCATION_TIME, 0));
//
//        return location;
//    }

    /**
     * remove the specified notification
     */
    public static void cancelNotification(Context context, int notifyId) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notifyId);
    }

    /**
     * check if internet is connected
     */
    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager check = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = check.getAllNetworkInfo();
        for (int i = 0; i < info.length; i++) {
            if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        }
        return false;
    }

    public static String longTimeFormatConvert(long time) {
        //DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        DateFormat format = new SimpleDateFormat("MM/dd hh:mm a");
        Date date = new Date(time);
        return format.format(date);
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public static boolean canGetLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);

        // getting GPS status
        boolean bIsGPSEnabled = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean bIsNetworkEnabled = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        return bIsGPSEnabled || bIsNetworkEnabled;
    }

    public static Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }

    public static Bitmap loadProfileImage(Context context,boolean bCircle) {
        Bitmap bitmap;
        File imgFile = new File(context.getExternalFilesDir(null), "profile.jpg");
        if (imgFile.exists()) {
            bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        }

        if(bCircle)
            return CommonUtils.getCircleBitmap(bitmap);
        else
            return bitmap;
    }
}
