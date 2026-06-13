package org.cocos2dx.cpp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import asia.chumbaka.kitkitProvider.KitkitDBHandler;
import asia.chumbaka.kitkitProvider.SessionCsvWriter;

/**
 * Created by ingtellect on 1/9/17.
 */

public class LockScreenReceiver extends BroadcastReceiver {

    private KitkitDBHandler dbHandler;
    private AppActivity activity;

    public LockScreenReceiver(KitkitDBHandler dbHandler, AppActivity activity) {
        this.dbHandler = dbHandler;
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("LockScreenReceiver","broadcast received");

        String action = intent.getAction();

        //If the screen was just turned on or it just booted up, start your Lock Activity
        if(action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            // Screen-off mid-gameplay: the launcher's own LockScreenReceiver
            // isn't running while the mainapp is foreground, so we have to
            // emit the logout event + write the CSV here. Same code path as
            // the launcher's logout — both call into SessionCsvWriter.
            try {
                String writtenPath = SessionCsvWriter.flushSessionToCsv(context, dbHandler);
                if (writtenPath != null) {
                    Log.i("LockScreenReceiver", "CSV written to " + writtenPath);
                }
            } catch (Exception e) {
                Log.e("LockScreenReceiver", "flushSessionToCsv failed: " + e);
            }

            Log.d("LockScreenReceiver","Delete user");
            dbHandler.deleteCurrentUser();
            activity.finish();
            Log.d("LockScreenReceiver","Start intent");
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.setComponent(new ComponentName("com.enuma.todoschoollockscreen","com.enuma.todoschoollockscreen.LockScreenActivity"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                context.startActivity(i);

            }
            catch (Exception e)
            {
                Log.d("LockScreenReceiver","start lockscreen failed");

            }
        }
    }
}
