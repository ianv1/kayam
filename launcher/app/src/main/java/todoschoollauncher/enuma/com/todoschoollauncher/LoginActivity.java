package todoschoollauncher.enuma.com.todoschoollauncher;

import android.os.Bundle;
import android.widget.GridView;

import com.enuma.kitkitProvider.User;
import com.enuma.kitkitlogger.KitKitLoggerActivity;

import java.util.ArrayList;

import todoschoollauncher.enuma.com.todoschoollauncher.adapter.LoginGridViewAdapter;

public class LoginActivity extends KitKitLoggerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        GridView gridView = (GridView) findViewById(R.id.grid);

        ArrayList<User> users = ((LauncherApplication) getApplication()).getDbHandler().getUserList();
        LoginGridViewAdapter adapter = new LoginGridViewAdapter(this, users);
        gridView.setAdapter(adapter);
    }
}
