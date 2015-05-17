package liu.jeffrey.lovetracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import liu.jeffrey.lovetracker.Constants;

public class DbHelper extends SQLiteOpenHelper {

    private Context context;
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_CONTACT_ENTRIES =
            "CREATE TABLE " + DbContract.Contact.TABLE_NAME + " (" +
                    DbContract.Contact._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
                    DbContract.Contact.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_RID + TEXT_TYPE + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_PICTURE + " BLOB" + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_LOCATION_TIME + TEXT_TYPE + COMMA_SEP +
                    DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID + INTEGER_TYPE +
                    " )";

    private static final String SQL_CREATE_LOCATION_ENTRIES =
            "CREATE TABLE " + DbContract.Location.TABLE_NAME + " (" +
                    DbContract.Location._ID + INTEGER_TYPE + " PRIMARY KEY AUTOINCREMENT," +
                    DbContract.Location.COLUMN_NAME_WHO_ID + TEXT_TYPE + COMMA_SEP +
                    DbContract.Location.COLUMN_NAME_LATITUDE + TEXT_TYPE + COMMA_SEP +
                    DbContract.Location.COLUMN_NAME_LONGITUDE + TEXT_TYPE + COMMA_SEP +
                    DbContract.Location.COLUMN_NAME_LOCATION_TIME + TEXT_TYPE + COMMA_SEP +
                    DbContract.Location.COLUMN_NAME_lOCATION_ACCURACY + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_CONTACT_ENTRIES =
            "DROP TABLE IF EXISTS " + DbContract.Contact.TABLE_NAME;
    private static final String SQL_DELETE_LOCATION_ENTRIES =
            "DROP TABLE IF EXISTS " + DbContract.Location.TABLE_NAME;


    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "contact.db";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_CONTACT_ENTRIES);
        db.execSQL(SQL_CREATE_LOCATION_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_CONTACT_ENTRIES);
        db.execSQL(SQL_DELETE_LOCATION_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Cursor getAllContactCursor() {
        SQLiteDatabase db = this.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Contact._ID,
                DbContract.Contact.COLUMN_NAME_NAME,
                DbContract.Contact.COLUMN_NAME_RID,
                DbContract.Contact.COLUMN_NAME_PICTURE,
                DbContract.Contact.COLUMN_NAME_LATITUDE,
                DbContract.Contact.COLUMN_NAME_LONGITUDE,
                DbContract.Contact.COLUMN_NAME_LOCATION_TIME,
                DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Contact.COLUMN_NAME_NAME + " DESC";

        String selection = DbContract.Contact._ID + " = ?";

        String[] selectionArgs = {""};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                null,                             // The columns for the WHERE clause
                null,                             // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
//            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(DbContract.FeedEntry._ID));
//            Log.d(Constants.TAG, "show row id " + itemId);
        }

        // cursor.close();
        db.close();

        return cursor;
    }

    /**
     * insert name and regid into db, if duplicate regid already exists, just replace it and return -1
     * else return the insert row id
     */
    public long insertData(String name, String regid, byte[] picture) {

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();


        String[] projection = {
                DbContract.Contact.COLUMN_NAME_NAME
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Contact.COLUMN_NAME_NAME + " DESC";

        String selection = DbContract.Contact.COLUMN_NAME_RID + " = ?";

        String[] selectionArgs = {regid};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                selection,                        // The columns for the WHERE clause
                selectionArgs,                    // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
        );
        if (cursor != null && cursor.getCount() > 0) {
            //duplicate
            updateContactDataRow(name, regid, picture);
            return -1;
        }

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        //values.put(DbContract.FeedEntry._ID, id);
        values.put(DbContract.Contact.COLUMN_NAME_NAME, name);
        values.put(DbContract.Contact.COLUMN_NAME_RID, regid);
        values.put(DbContract.Contact.COLUMN_NAME_PICTURE, picture);
        values.put(DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID, -1);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(DbContract.Contact.TABLE_NAME,
                null,
                values);
        db.close();
        Log.d(Constants.TAG, "insert row id " + newRowId);
        return newRowId;
    }


    /**
     * find the cursor of the giving regid, if not found, return null
     */
    public Cursor findCursorByRegid(String regid) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Contact.COLUMN_NAME_NAME,
                DbContract.Contact.COLUMN_NAME_PICTURE,
                DbContract.Contact._ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Contact.COLUMN_NAME_NAME + " DESC";

        String selection = DbContract.Contact.COLUMN_NAME_RID + " = ?";

        String[] selectionArgs = {regid};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                selection,                        // The columns for the WHERE clause
                selectionArgs,                    // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            db.close();
            return cursor;
        }

        //cursor.close();
        db.close();
        return null;
    }

    /**
     * find the cursor of the giving _id, if not found, return null
     */
    public Cursor findCursorBy_id(String _id) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Contact.COLUMN_NAME_NAME,
                DbContract.Contact.COLUMN_NAME_PICTURE,
                DbContract.Contact.COLUMN_NAME_RID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Contact.COLUMN_NAME_NAME + " DESC";

        String selection = DbContract.Contact._ID + " = ?";

        String[] selectionArgs = {_id};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                selection,                        // The columns for the WHERE clause
                selectionArgs,                    // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            db.close();
            return cursor;
        }

        //cursor.close();
        db.close();
        return null;
    }


    public void deleteContactDataRow(String regid) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Define 'where' part of query.
        String selection = DbContract.Contact.COLUMN_NAME_RID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {regid};
        // Issue SQL statement.
        db.delete(DbContract.Contact.TABLE_NAME, selection, selectionArgs);
        db.close();
    }

    public int updateContactDataRow(String name, String regid, byte[] picture) {
        SQLiteDatabase db = this.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DbContract.Contact.COLUMN_NAME_NAME, name);
        values.put(DbContract.Contact.COLUMN_NAME_PICTURE, picture);

        // Which row to update, based on the ID
        String selection = DbContract.Contact.COLUMN_NAME_RID + " = ?";
        String[] selectionArgs = {regid};

        int count = db.update(
                DbContract.Contact.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();
        return count;
    }

    //by providing regid, find if widget app id exist or not, if not, store the new widget id
    // if already exist, return 0
    public int saveWidgetID(String regid, int widgetID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Contact.COLUMN_NAME_RID + " DESC";

        String selection = DbContract.Contact.COLUMN_NAME_RID + " = ?";

        String[] selectionArgs = {regid};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                selection,                        // The columns for the WHERE clause
                selectionArgs,                    // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                sortOrder                         // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            if (cursor.getInt(0) > 0) {
                //already added, don't add
                Log.d(Constants.TAG, "already existed:" + cursor.getInt(0));
                cursor.close();
                return 0;
            }
            cursor.close();
        }
        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID, widgetID);
        int count = db.update(
                DbContract.Contact.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();
        return count;
    }

    /**
     * find the cursor of the giving widgetID, if not found, return ""
     */
    public Cursor findCursorByWidgetID(int widgetID) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Contact.COLUMN_NAME_RID,
                DbContract.Contact.COLUMN_NAME_PICTURE,
                DbContract.Contact._ID
        };

        String selection = DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID + " = ?";

        String[] selectionArgs = {Integer.toString(widgetID)};

        Cursor cursor = db.query(
                DbContract.Contact.TABLE_NAME,  // The table to query
                projection,                       // The columns to return
                selection,                        // The columns for the WHERE clause
                selectionArgs,                    // The values for the WHERE clause
                null,                             // don't group the rows
                null,                             // don't filter by row groups
                null                              // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            db.close();
            return cursor;
        }

        //cursor.close();
        db.close();
        return null;
    }

    /**
     * clear the given widgetID
     */
    public int clearWidgetID(int widgetID) {
        SQLiteDatabase db = this.getWritableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID, -1);

        // Which row to update, based on the ID
        String selection = DbContract.Contact.COLUMN_NAME_APP_WIDGET_ID + " = ?";
        String[] selectionArgs = {Integer.toString(widgetID)};

        int count = db.update(
                DbContract.Contact.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        db.close();
        return count;
    }
    //===============

    /**
     * insert a location, if who == 0, it is yourself
     */
    public long insertNewLocation(int who, Location location) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DbContract.Location.COLUMN_NAME_WHO_ID, who);
        values.put(DbContract.Location.COLUMN_NAME_LATITUDE, location.getLatitude());
        values.put(DbContract.Location.COLUMN_NAME_LONGITUDE, location.getLongitude());
        values.put(DbContract.Location.COLUMN_NAME_LOCATION_TIME, location.getTime());
        values.put(DbContract.Location.COLUMN_NAME_lOCATION_ACCURACY, location.getAccuracy());

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(DbContract.Location.TABLE_NAME,
                null,
                values);
        db.close();
        Log.d(Constants.TAG, "insert location row id " + newRowId);
        return newRowId;
    }

    /**
     * get last known location of someone, if 0, mine
     */
    public Location getLastLocation(int who) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Location._ID,
                DbContract.Location.COLUMN_NAME_WHO_ID,
                DbContract.Location.COLUMN_NAME_LATITUDE,
                DbContract.Location.COLUMN_NAME_LONGITUDE,
                DbContract.Location.COLUMN_NAME_LOCATION_TIME,
                DbContract.Location.COLUMN_NAME_lOCATION_ACCURACY
        };

        String selection = DbContract.Location.COLUMN_NAME_WHO_ID + " = ?";

        String[] selectionArgs = {Integer.toString(who)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Location._ID + " DESC";

        Cursor cursor = db.query(
                DbContract.Location.TABLE_NAME, // The table to query
                projection,                     // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                sortOrder                       // The sort order
        );
        Location location = new Location(LocationManager.PASSIVE_PROVIDER);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            location.setLatitude(cursor.getDouble(2));
            location.setLongitude(cursor.getDouble(3));
            location.setTime(cursor.getLong(4));
            location.setAccuracy(cursor.getLong(5));
            cursor.close();
        }

        db.close();
        return location;
    }

    /**
     * get my last known locations cursor, PLEASE REMEMBER TO CLOSE CURSOR IF NOT null
     */
    public Cursor getMyLocations() {
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                DbContract.Location._ID,
                DbContract.Location.COLUMN_NAME_WHO_ID,
                DbContract.Location.COLUMN_NAME_LATITUDE,
                DbContract.Location.COLUMN_NAME_LONGITUDE,
                DbContract.Location.COLUMN_NAME_LOCATION_TIME,
                DbContract.Location.COLUMN_NAME_lOCATION_ACCURACY
        };

        String selection = DbContract.Location.COLUMN_NAME_WHO_ID + " = ?";

        String[] selectionArgs = {Integer.toString(0)};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DbContract.Location._ID + " DESC";

        Cursor cursor = db.query(
                DbContract.Location.TABLE_NAME, // The table to query
                projection,                     // The columns to return
                selection,                      // The columns for the WHERE clause
                selectionArgs,                  // The values for the WHERE clause
                null,                           // don't group the rows
                null,                           // don't filter by row groups
                sortOrder                       // The sort order
        );

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            db.close();
            return cursor;
        }

        // cursor.close();
        db.close();

        return null;
    }

    public void deleteMapHistory(int who) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Define 'where' part of query.
        String selection = DbContract.Location.COLUMN_NAME_WHO_ID + " = ?";
        // Specify arguments in placeholder order.
        String[] selectionArgs = {Integer.toString(who)};
        // Issue SQL statement.
        db.delete(DbContract.Location.TABLE_NAME, selection, selectionArgs);
        db.close();
    }
}
