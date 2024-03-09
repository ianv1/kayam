package asia.chumbaka.kayam.launcher;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.UUID;

import asia.chumbaka.kitkitProvider.KitkitDBHandler;
import asia.chumbaka.kitkitProvider.User;
import asia.chumbaka.kitkitlogger.KitKitLogger;


/**
 * Created by ingtellect on 7/18/17.
 */

public class LauncherApplication extends Application {

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;
    private KitKitLogger logger;
    private KitkitDBHandler dbHandler;
    private User currentUser;
    private String currentUsername;

    @Override
    public void onCreate() {
        super.onCreate();

//        SharedPreferences preferences = getSharedPreferences("sharedpreference", Context.CONTEXT_IGNORE_SECURITY);
//
//        String installId = preferences.getString("installId","");
//        if (installId.isEmpty()) {
//            installId = getRandomUUID();
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString("installId", installId);
//            editor.commit();
//        }


//        SharedPreferences preferences = getSharedPreferences("sharedPref",Context.MODE_PRIVATE);
//
//        int versionCode = BuildConfig.VERSION_CODE;
//        int lastVersion = preferences.getInt("launcherVersion", 0);
//        String sharedLang = preferences.getString("appLanguage", "");
//
//        if ((lastVersion < versionCode) || (sharedLang.isEmpty())) {
//            String lang = getString(R.string.defaultLanguage);
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putString("appLanguage",lang);
//            editor.putInt("launcherVersion", versionCode);
//            editor.apply();
//        }

        SharedPreferences preferences = getSharedPreferences("sharedPref", Context.MODE_PRIVATE);
        String lang = getString(R.string.defaultLanguage);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("appLanguage", lang);
        editor.apply();

        logger = new KitKitLogger(getPackageName(), getApplicationContext());
        dbHandler = new KitkitDBHandler(getApplicationContext());

        dbHandler.deleteCurrentUser();

        createAdminUser();

//        if (dbHandler.numUser() == 0) {
//            // make users in DB
//            for (int i = 0; i < 5; i++) {
//                String displayName = "";
//                switch (i) {
//                    case 0:
//                        displayName = "Muhammad Ali";
//                        break;
//                    case 1:
//                        displayName = "John Doe";
//                        break;
//                    case 2:
//                        displayName = "Nigel Sim";
//                        break;
//                    case 3:
//                        displayName = "Ian Eng";
//                        break;
//                    case 4:
//                        displayName = "Raymond";
//                        break;
//                }
//                User user = new User("user" + i, 0, displayName);
//                dbHandler.addUser(user);
//
//                if (i == 0) {
//                    dbHandler.setCurrentUser(user);
//                    currentUser = user;
//                    currentUsername = user.getUserName();
//                }
//            }
//
//        }
        if (currentUser == null) {
            currentUser = dbHandler.getCurrentUser();
            if (currentUser != null) {
                currentUsername = currentUser.getUserName();
            }
        }

        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });
    }

    private void createAdminUser() {
        ArrayList<User> users = dbHandler.getUserList();
        boolean foundAdmin = false;
        for (User user : users) {
            if (user.getUserName().equals("admin")) {
                foundAdmin = true;
                break;
            }
        }
        if (!foundAdmin) {
            User user = new User("admin", "Admin", "");
            dbHandler.addUser(user);
        }
    }

    public void handleUncaughtException(Thread thread, Throwable e) {
        e.printStackTrace(); // not all Android versions will print the stack trace automatically

        logger.extractLogToFile();

        defaultExceptionHandler.uncaughtException(thread, e);
    }

    public KitKitLogger getLogger() {
        return logger;
    }

    public String getRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public KitkitDBHandler getDbHandler() {
        return dbHandler;
    }

}
