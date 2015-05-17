package liu.jeffrey.lovetracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import liu.jeffrey.lovetracker.ActivityRecognition.DetectActIntentService;

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d(Constants.TAG, "boot completed");
            Intent locationService = new Intent(context, LocationService.class);
            context.startService(locationService);

            //no need to set alarm anymore, when android boots up, we should just start activity recognition service
            Intent detectActService = new Intent(context, DetectActIntentService.class);
            context.startService(detectActService);
        }
    }
}
