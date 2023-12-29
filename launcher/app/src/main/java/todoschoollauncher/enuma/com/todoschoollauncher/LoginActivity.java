package todoschoollauncher.enuma.com.todoschoollauncher;

import android.os.Bundle;

import com.enuma.kitkitProvider.User;
import com.enuma.kitkitlogger.KitKitLoggerActivity;

import java.util.ArrayList;

public class LoginActivity extends KitKitLoggerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ArrayList<User> users = ((LauncherApplication) getApplication()).getDbHandler().getUserList();
        for (User user : users) {
        }
    }
}
