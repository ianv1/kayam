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

            // Per-session + per-event blocks — sourced from the shared DB.
            String sessionId = dbHandler.getCurrentSessionId();
            long sessionLogin = dbHandler.getCurrentSessionLogin();
            long sessionLogout = System.currentTimeMillis() / 1000L;
            if (sessionId != null && !sessionId.isEmpty() && !user.getUserName().equals("admin")) {
                content.append("\n")
                        .append("Session ID,Username,Time login,Time logout\n")
                        .append(sessionId).append(",")
                        .append(user.getDisplayName()).append(",")
                        .append(sessionLogin).append(",")
                        .append(sessionLogout).append("\n");

                java.util.ArrayList<asia.chumbaka.kitkitProvider.Event> events =
                        dbHandler.getEventsForSession(sessionId);
                content.append("\n")
                        .append("Event ID,Event #,Event Datetime,Session ID,Username,Subject,Level,Day,Game,Stars\n");
                for (asia.chumbaka.kitkitProvider.Event ev : events) {
                    // Events 4 (day-stars) and 5 (level-crown) are scoped
                    // to a Level-Day, not a specific game, so render their
                    // Game column as "-" instead of the default 0.
                    String gameCell = (ev.eventNumber == 4 || ev.eventNumber == 5)
                            ? "-" : String.valueOf(ev.game);
                    content.append(ev.eventId).append(",")
                            .append(ev.eventNumber).append(",")
                            .append(ev.eventDatetime).append(",")
                            .append(ev.sessionId).append(",")
                            .append(ev.username).append(",")
                            .append(ev.subject).append(",")
                            .append(ev.level).append(",")
                            .append(ev.day).append(",")
                            .append(gameCell).append(",")
                            .append(ev.stars).append("\n");
                }

                dbHandler.setCurrentSession("", 0L);
                context.getContentResolver().delete(
                        asia.chumbaka.kitkitProvider.KitkitProvider.EVENTS_URI,
                        asia.chumbaka.kitkitProvider.KitkitDBHandler.COLUMN_SESSION_ID + " = ?",
                        new String[]{sessionId});
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
