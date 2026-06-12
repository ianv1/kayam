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
            // Single events table — login (#0) + gameplay (#1-5) + logout (#6)
            // all live in the same stream. Session boundaries are
            // reconstructable from event_number 0/6.
            StringBuilder content = new StringBuilder(
                    "Event ID,Event #,Datetime,Session ID,Username,Subject,Level,Day,Game,Stars\n");

            String sessionId = dbHandler.getCurrentSessionId();
            long sessionLogout = System.currentTimeMillis() / 1000L;
            if (sessionId != null && !sessionId.isEmpty() && !user.getUserName().equals("admin")) {
                // Event #6 — logout marker.
                dbHandler.logEvent(sessionId, sessionLogout,
                        sessionId, user.getDisplayName(), 6,
                        "", "", 0, 0, user.getNumStars());

                java.util.ArrayList<asia.chumbaka.kitkitProvider.Event> events =
                        dbHandler.getEventsForSession(sessionId);
                for (asia.chumbaka.kitkitProvider.Event ev : events) {
                    boolean isBoundary = (ev.eventNumber == 0 || ev.eventNumber == 6);
                    String subjectCell = isBoundary ? "" : ev.subject;
                    String levelCell   = isBoundary ? "" : ev.level;
                    String dayCell     = isBoundary ? "" : String.valueOf(ev.day);
                    // Boundary events (0/6) and level-day-scoped events
                    // (4/5) all leave Game blank — only per-game events
                    // 1/2/3 carry a real game index.
                    String gameCell;
                    if (isBoundary
                            || ev.eventNumber == 4
                            || ev.eventNumber == 5) gameCell = "";
                    else                            gameCell = String.valueOf(ev.game);

                    content.append(ev.eventId).append(",")
                            .append(ev.eventNumber).append(",")
                            .append(ev.eventDatetime).append(",")
                            .append(ev.sessionId).append(",")
                            .append(ev.username).append(",")
                            .append(subjectCell).append(",")
                            .append(levelCell).append(",")
                            .append(dayCell).append(",")
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
