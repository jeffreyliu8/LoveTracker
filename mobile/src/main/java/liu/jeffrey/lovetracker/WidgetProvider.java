package liu.jeffrey.lovetracker;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import liu.jeffrey.lovetracker.db.DbHelper;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        DbHelper friendDatabase = new DbHelper(context);

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.d(Constants.TAG, "delete id " + appWidgetId);
            friendDatabase.clearWidgetID(appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d(Constants.TAG, "onReceive " + intent.toString());
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        // For each widget that needs an update, get the text that we should display:
        //   - Create a RemoteViews object for it
        //   - Set the text in the RemoteViews object
        //   - Tell the AppWidgetManager to show that views object for the widget.
        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];
            Log.d(Constants.TAG, "update id " + appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }

//        Log.d(Constants.TAG, "onUpdate length =" + appWidgetIds.length);
//
//        // Perform this loop procedure for each App Widget that belongs to this provider
//        for (int i = 0; i < appWidgetIds.length; i++) {
//            Log.d(Constants.TAG, "onUpdate for loop:" + i);
//            int currentWidgetId = appWidgetIds[i];
//
//            DbHelper friendDatabase = new DbHelper(context);
//            String tempTargetSendTo = friendDatabase.getFirstRegID();
//            Log.d(Constants.TAG, "send to " + tempTargetSendTo);
//
//            Intent service = new Intent();
//            service.putExtra(Constants.TARGET_REGISTRATION_ID, tempTargetSendTo);
//            service.setComponent(new ComponentName(context, BuzzService.class));
//
//            PendingIntent pending = PendingIntent.getService(context, 0, service, 0);
//            RemoteViews views = new RemoteViews(context.getPackageName(),
//                    R.layout.widget_demo);
//            views.setOnClickPendingIntent(R.id.imageButton, pending);
//            appWidgetManager.updateAppWidget(currentWidgetId, views);
//        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object.  It takes the package name (in our case, it's our
        // package, but it needs this because on the other side it's the widget host inflating
        // the layout from our package).
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_demo);
        //views.setImageViewBitmap(R.id.widgetImageButton, bitmap);

        //Log.d(Constants.TAG,"updateAppWidget regid = " + regid);


        DbHelper friendDatabase = new DbHelper(context);
        Cursor cursor = friendDatabase.findCursorByWidgetID(appWidgetId);
        if (cursor != null && cursor.getCount() > 0) {

            String regid = cursor.getString(0);
            byte[] bitmapdata = cursor.getBlob(1);
            int _id = cursor.getInt(2);

            if (bitmapdata == null) {
                //use original icon
            } else {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                views.setImageViewBitmap(R.id.widgetImage, CommonUtils.getCircleBitmap(bitmap));
            }
            cursor.close();

            //set profile icon intent
            Intent intent = new Intent();
            intent.putExtra(Constants.USER_ID, _id);
            intent.setComponent(new ComponentName(context, MainMapActivity.class));
            PendingIntent pendingAct = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetImage, pendingAct);

            //set buzz service
            Intent service = new Intent();
            service.setAction("widget_buzz_service"); //need to not have intent overridden
            service.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
            service.setComponent(new ComponentName(context, BuzzService.class));
            PendingIntent pendingServ = PendingIntent.getService(context, appWidgetId, service, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widgetImageButton, pendingServ);

            //set button 1
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean showCustomMsg1 = pref.getBoolean("widget_msg1_switch", Boolean.FALSE);
            if (showCustomMsg1) {
                Intent customizedService1 = new Intent();
                customizedService1.setAction("widget_msg1_switch"); //need to not have intent overridden
                customizedService1.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
                customizedService1.putExtra("text", pref.getString("widget_msg1", "Buzz"));
                customizedService1.setComponent(new ComponentName(context, BuzzService.class));
                PendingIntent pendingServC1 = PendingIntent.getService(context, appWidgetId, customizedService1, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_button1, pendingServC1);
                views.setTextViewText(R.id.widget_button1, pref.getString("widget_msg1", "Buzz"));
            } else {
                views.setViewVisibility(R.id.widget_button1, View.INVISIBLE);
            }

            //set button 2
            Boolean showCustomMsg2 = pref.getBoolean("widget_msg2_switch", Boolean.FALSE);
            if (showCustomMsg2) {
                Intent customizedService2 = new Intent();
                customizedService2.setAction("widget_msg2_switch"); //need to not have intent overridden
                customizedService2.putExtra(Constants.TARGET_REGISTRATION_ID, regid);
                customizedService2.putExtra("text", pref.getString("widget_msg2", "Buzz"));
                customizedService2.setComponent(new ComponentName(context, BuzzService.class));
                PendingIntent pendingServC2 = PendingIntent.getService(context, appWidgetId, customizedService2, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.widget_button2, pendingServC2);
                views.setTextViewText(R.id.widget_button2, pref.getString("widget_msg2", "Buzz"));
            } else {
                views.setViewVisibility(R.id.widget_button2, View.INVISIBLE);
            }

            // Tell the widget manager
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}