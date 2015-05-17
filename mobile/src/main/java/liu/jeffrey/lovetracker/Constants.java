package liu.jeffrey.lovetracker;

public class Constants {
    public static final String TAG = "jeff";



    public static final String REGISTRATION_ID = "registration_id";
    public static final String TARGET_REGISTRATION_ID = "target_registration_id";
    public static final String DO_NFC_ADD_NEW_USER = "bDoNFCAdd";//server side also use the string
    public static final String DO_ADD_NEW_USER_WITH_REGID = "bDoRegidAdd";//server side also use the string
    public static final String DO_REQUEST_LOCATION = "bRequestLocation";//user wants to fetch target location
    public static final String DO_REQUEST_LOCATION_ACK = "bRequestLocationACK";//user wants to fetch target location
    public static final String NOTIFY_ID = "notify_id";//should just be the same as _id in db
    public static final String APP_SHARED_PREFERENCES = "appPref";

    public static final String GET_MY_LOCATION_PREFERENCES = "get_my_location"; //boolean, if true, do location check
    public static final String LOCATION_LATITUDE = "latitude"; //latest known location
    public static final String LOCATION_LONGITUDE = "longitude"; //latest known location
    public static final String LOCATION_ACCURACY = "accuracy"; //latest known location
    public static final String LOCATION_TIME = "location_time"; //latest known location

    public static final String USER_ID = "user_id";

    //====from google activity recognition
    public static final String PACKAGE_NAME = "liu.jeffrey.lovetracker";

    public static final String BROADCAST_ACTION = PACKAGE_NAME + ".BROADCAST_ACTION";

    public static final String ACTIVITY_EXTRA = PACKAGE_NAME + ".ACTIVITY_EXTRA";

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 1000 * 15;
}