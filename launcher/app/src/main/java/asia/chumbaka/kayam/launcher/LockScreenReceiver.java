package asia.chumbaka.kayam.launcher;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import asia.chumbaka.kitkitProvider.KitkitDBHandler;
import asia.chumbaka.kitkitProvider.SessionCsvWriter;
import asia.chumbaka.kitkitProvider.User;

/**
 * Created by ingtellect on 1/9/17.
 */

public class LockScreenReceiver extends BroadcastReceiver {

    private KitkitDBHandler dbHandler;

    public LockScreenReceiver(KitkitDBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("LockScreenReceiver","broadcast received");

        String action = intent.getAction();

        //If the screen was just turned on or it just booted up, start your Lock Activity
        if(action.equals(Intent.ACTION_SCREEN_OFF) || action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Log.d("LockScreenReceiver","Delete user");
            generateCSV(context);
            dbHandler.deleteCurrentUser();
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

    private void generateCSV(Context context) {
        // Shared CSV writer — same code path as the mainapp's
        // LockScreenReceiver, so screen-off mid-gameplay and screen-off
        // on the launcher home both produce identical reports.
        try {
            String writtenPath = SessionCsvWriter.flushSessionToCsv(context, dbHandler);
            if (writtenPath != null) {
                android.util.Log.i("LockScreenReceiver", "CSV written to " + writtenPath);
            }
        } catch (Exception e) {
            android.util.Log.e("LockScreenReceiver", "flushSessionToCsv failed: " + e);
        }
    }
}
