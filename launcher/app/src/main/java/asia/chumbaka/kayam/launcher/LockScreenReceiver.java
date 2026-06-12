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

        User user = dbHandler.getCurrentUser();

        String tabletNumber = context.getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS).getString("tablet_number", "");

        try {
            // Match the other generateCSV writers — 4 subject columns
            // (EN / Math written by the EN mainapp, BM / BM Math written by
            // the BM mainapp) plus the last-login timestamp.
            StringBuilder content = new StringBuilder("Name,Stars,English,Math,BM,BM Math,Last Login\n");

            if (!user.getUserName().equals("admin")) {
                content.append(user.getDisplayName())
                        .append(",")
                        .append(user.getNumStars())
                        .append(",")
                        .append(user.getCurrentEnglishLevel())
                        .append(",")
                        .append(user.getCurrentMathLevel())
                        .append(",")
                        .append(user.getCurrentBMLevel())
                        .append(",")
                        .append(user.getCurrentBMMathLevel())
                        .append(",")
                        .append(user.getLastLogin())
                        .append("\n");
            }

            // Per-session block — same shape as MainActivity.generateCSV.
            // session_id + login_ts were persisted at login; logout_ts is now.
            android.content.SharedPreferences sessPrefs = context.getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS);
            String sessionId = sessPrefs.getString("current_session_id", "");
            long sessionLogin = sessPrefs.getLong("current_session_login", 0L);
            long sessionLogout = System.currentTimeMillis() / 1000L;
            if (!sessionId.isEmpty() && !user.getUserName().equals("admin")) {
                content.append("\n")
                        .append("Session ID,Username,Time login,Time logout\n")
                        .append(sessionId).append(",")
                        .append(user.getDisplayName()).append(",")
                        .append(sessionLogin).append(",")
                        .append(sessionLogout).append("\n");
                sessPrefs.edit()
                        .remove("current_session_id")
                        .remove("current_session_username")
                        .remove("current_session_login")
                        .apply();
            }

            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "kayam-reports");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String name = dbHandler.getCurrentUser().getDisplayName().replaceAll("[^A-Za-z0-9]", "");
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/kayam-reports/", tabletNumber + "_" + KitkitDBHandler.getTimeFormatString(System.currentTimeMillis(), "yyyyMMddHHmmss") + "_" + name + ".csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content.toString());
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
