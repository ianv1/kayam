package todoschoollauncher.enuma.com.todoschoollauncher;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;

import com.enuma.kitkitProvider.User;
import com.enuma.kitkitlogger.KitKitLoggerActivity;

import java.util.ArrayList;

import todoschoollauncher.enuma.com.todoschoollauncher.adapter.LoginGridViewAdapter;
import todoschoollauncher.enuma.com.todoschoollauncher.adapter.OnItemClick;
import todoschoollauncher.enuma.com.todoschoollauncher.adapter.OnRemoveClick;

public class LoginActivity extends KitKitLoggerActivity implements OnItemClick,
        OnRemoveClick,
        LoginPasswordDialogFragment.LoginPasswordListener,
        PasswordDialogFragment.PasswordDialogListener,
        CreateUserDialogFragment.CreateUserListener {

    private ArrayList<User> users;
    private String selectedUserName;

    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        refreshUI();

        ImageView backButton = (ImageView) findViewById(R.id.ic_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button titleButton = (Button) findViewById(R.id.title_btn);
        titleButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                DialogFragment dialog = new PasswordDialogFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("userobject", "Admin");
                dialog.setArguments(bundle);
                dialog.show(getFragmentManager(), "PasswordDialogFragment");
                return true;
            }
        });
    }

    @Override
    public void onClick(String value) {
        selectedUserName = value;
        DialogFragment dialog = new LoginPasswordDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("selectedUserName", value);
        dialog.setArguments(bundle);
        dialog.show(getFragmentManager(), "LoginPasswordDialogFragment");
    }

    @Override
    public void onLoginPasswordDialogPositiveClick(DialogFragment dialog, String redirectTo) {
        LoginGridViewAdapter adapter = new LoginGridViewAdapter(this, users, selectedUserName, isAdmin, this, this);
        GridView gridView = (GridView) findViewById(R.id.grid);
        gridView.setAdapter(adapter);
        ((LauncherApplication) getApplication()).getDbHandler().setCurrentUser(selectedUserName);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String redirectTo) {
        dialog.dismiss();
        isAdmin = true;
        refreshUI();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void refreshUI() {
        if (isAdmin) {
            ImageView imageViewPlus = (ImageView) findViewById(R.id.ic_plus);
            imageViewPlus.setVisibility(View.VISIBLE);
            imageViewPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new CreateUserDialogFragment();
                    dialog.show(getFragmentManager(), "CreateUserDialogFragment");
                }
            });
        }
        GridView gridView = (GridView) findViewById(R.id.grid);

        users = ((LauncherApplication) getApplication()).getDbHandler().getUserList();
        String currentUsername = ((LauncherApplication) getApplication()).getDbHandler().getCurrentUsername();

        LoginGridViewAdapter adapter = new LoginGridViewAdapter(this, users, currentUsername, isAdmin, this, this);
        gridView.setAdapter(adapter);
    }

    @Override
    public void onCreateUser(DialogFragment dialog, String redirectTo) {
        dialog.dismiss();
        refreshUI();
    }

    @Override
    public void onRemoveClick(final String value) {
        User user = ((LauncherApplication) getApplication()).getDbHandler().findUser(value);
        if (user == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete " + user.getDisplayName())
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ((LauncherApplication) getApplication()).getDbHandler().deleteUser(value);
                        refreshUI();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
