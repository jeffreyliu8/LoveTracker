package liu.jeffrey.lovetracker.db;

import android.provider.BaseColumns;

public final class DbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DbContract() {
    }

    /* Inner class that defines the table contents */
    public static abstract class Contact implements BaseColumns {
        public static final String TABLE_NAME = "contact";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_RID = "rid";//google registration id
        public static final String COLUMN_NAME_PICTURE = "profile";//profile picture in byte
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LOCATION_TIME = "time";
        public static final String COLUMN_NAME_APP_WIDGET_ID = "appWidgetId";
    }

    /* Inner class that defines the table contents */
    public static abstract class Location implements BaseColumns {
        public static final String TABLE_NAME = "location";
        public static final String COLUMN_NAME_WHO_ID = "name";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LOCATION_TIME = "time";
        public static final String COLUMN_NAME_lOCATION_ACCURACY = "accuracy";
    }
}
