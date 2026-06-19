package asia.chumbaka.kitkitProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Shared writer for the per-session CSV. Lives in kitkitlogger so both
 * the launcher (logout button / launcher's LockScreenReceiver) and the
 * mainapp (its own LockScreenReceiver, fired when the screen turns off
 * mid-game) can produce identically-shaped reports without code drift.
 *
 * The CSV has a single events table — login (#0), gameplay events
 * (#1-5), and logout (#6) all live in the same stream. Session
 * boundaries are reconstructable from event_number 0/6.
 */
public class SessionCsvWriter {

    private static final String TAG = "SessionCsvWriter";

    /**
     * Emit a logout event for the current session, drain the events
     * table for that session into a CSV file, then clear both the
     * session row and the events.
     *
     * Safe to call repeatedly — if no active session is present this is
     * a no-op (returns null).
     *
     * @return human-readable path of the written file, or null when no
     *         session was active or every write target failed.
     */
    public static String flushSessionToCsv(Context context, KitkitDBHandler dbHandler) {
        if (context == null || dbHandler == null) return null;
        User user = dbHandler.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "flushSessionToCsv: no current user; skipping");
            return null;
        }
        if ("admin".equals(user.getUserName())) return null;

        String sessionId = dbHandler.getCurrentSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            Log.w(TAG, "flushSessionToCsv: no active session; skipping");
            return null;
        }

        long logoutUnixSecs = System.currentTimeMillis() / 1000L;

        // Event #6 — logout marker. event_id is its own random UUID (the
        // session_id still lives in the Session ID column for linkage);
        // blank subject/level/day/game; stars = current running total.
        dbHandler.logEvent(java.util.UUID.randomUUID().toString(), logoutUnixSecs,
                sessionId, user.getDisplayName(), 6,
                "", "", 0, 0, user.getNumStars());

        StringBuilder content = new StringBuilder(
                "Event ID,Event #,Datetime,Session ID,Username,Subject,Level,Day,Game,Stars\n");

        ArrayList<Event> events = dbHandler.getEventsForSession(sessionId);
        for (Event ev : events) {
            boolean isBoundary = (ev.eventNumber == 0 || ev.eventNumber == 6);
            String subjectCell = isBoundary ? "" : ev.subject;
            String levelCell   = isBoundary ? "" : ev.level;
            String dayCell     = isBoundary ? "" : String.valueOf(ev.day);
            // Boundary events (0/6) and level-day-scoped events (4/5)
            // all leave Game blank — only per-game events 1/2/3 carry a
            // real game index.
            String gameCell;
            if (isBoundary || ev.eventNumber == 4 || ev.eventNumber == 5) gameCell = "";
            else                                                          gameCell = String.valueOf(ev.game);

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

        String tabletNumber = dbHandler.getTabletNumber();
        if (tabletNumber == null) tabletNumber = "";
        String safeName = user.getDisplayName().replaceAll("[^A-Za-z0-9]", "");
        String filename = tabletNumber + "_"
                + KitkitDBHandler.getTimeFormatString(System.currentTimeMillis(), "yyyyMMddHHmmss")
                + "_" + safeName + ".csv";

        String writtenPath = writeCsvAnywhere(context, filename, content.toString());

        // Clear session row + events so the next login starts clean.
        dbHandler.setCurrentSession("", 0L);
        context.getContentResolver().delete(
                KitkitProvider.EVENTS_URI,
                KitkitDBHandler.COLUMN_SESSION_ID + " = ?",
                new String[]{sessionId});

        return writtenPath;
    }

    /**
     * Write the CSV to the most user-visible location available on this
     * Android version, returning the path that succeeded (or null).
     *
     * Strategy:
     *   API 29+ : MediaStore.Files -> /Documents/kayam-reports/<file>
     *             (no permission needed, visible in every file manager,
     *             AND lives in the same folder the launcher's uploadCSV
     *             scans for upload).
     *   else    : legacy public Documents -> /Documents/kayam-reports/<file>
     *   fallback: app-private external storage (always works, no perms)
     */
    private static String writeCsvAnywhere(Context context, String filename, String content) {
        ContentResolver cr = context.getContentResolver();

        // 1) MediaStore.Files into public Documents (Android 10+).
        // We deliberately do NOT use MediaStore.Downloads — the
        // uploadCSV uploader in LoginActivity / MainActivity scans
        // /Documents/kayam-reports, so reports written elsewhere would
        // never be picked up.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                ContentValues v = new ContentValues();
                v.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                v.put(MediaStore.MediaColumns.MIME_TYPE, "text/csv");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH,
                        Environment.DIRECTORY_DOCUMENTS + "/kayam-reports/");
                Uri uri = cr.insert(MediaStore.Files.getContentUri("external"), v);
                if (uri != null) {
                    try (OutputStream os = cr.openOutputStream(uri)) {
                        if (os != null) {
                            os.write(content.getBytes("UTF-8"));
                            os.flush();
                        }
                    }
                    return "Documents/kayam-reports/" + filename;
                }
            } catch (Exception e) {
                Log.w(TAG, "MediaStore Documents write failed: " + e.getMessage());
            }
        }

        // 2) Legacy public Documents
        try {
            File folder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "kayam-reports");
            if (!folder.exists()) folder.mkdirs();
            File file = new File(folder, filename);
            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(content);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.w(TAG, "Public Documents write failed: " + e.getMessage());
        }

        // 3) Last-resort: app-private external storage (always permitted).
        try {
            File priv = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "kayam-reports");
            if (!priv.exists()) priv.mkdirs();
            File file = new File(priv, filename);
            try (FileWriter fw = new FileWriter(file);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(content);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "All CSV write targets failed: " + e.getMessage());
        }
        return null;
    }
}
