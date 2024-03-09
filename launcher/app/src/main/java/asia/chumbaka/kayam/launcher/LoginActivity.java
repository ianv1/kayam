package asia.chumbaka.kayam.launcher;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import asia.chumbaka.kitkitProvider.KitkitDBHandler;
import asia.chumbaka.kitkitProvider.User;
import asia.chumbaka.kitkitlogger.KitKitLoggerActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import asia.chumbaka.kayam.launcher.adapter.LoginGridViewAdapter;
import asia.chumbaka.kayam.launcher.adapter.OnItemClick;
import asia.chumbaka.kayam.launcher.adapter.OnRemoveClick;
import asia.chumbaka.kayam.launcher.R;

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
        refreshUI();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String redirectTo) {
        dialog.dismiss();
        isAdmin = true;
        ((LauncherApplication) getApplication()).getDbHandler().setCurrentUser("admin");
        refreshUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentUserName = ((LauncherApplication) getApplication()).getDbHandler().getCurrentUsername();
        if (currentUserName != null && currentUserName.equals("admin")) {
            isAdmin = true;
        }
        refreshUI();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    private void refreshUI() {
        SharedPreferences.Editor editor = getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS).edit();
        if (isAdmin) {
            editor.putBoolean("review_mode_on", true);
            setLastBackupTime();

            ImageView imageViewPlus = (ImageView) findViewById(R.id.ic_plus);
            imageViewPlus.setVisibility(View.VISIBLE);
            imageViewPlus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dialog = new CreateUserDialogFragment();
                    dialog.show(getFragmentManager(), "CreateUserDialogFragment");
                }
            });

            ImageView imageViewUpload = (ImageView) findViewById(R.id.ic_upload);
            imageViewUpload.setVisibility(View.VISIBLE);
            imageViewUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    generateCSV();
                }
            });

            String tabletNumber = getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS).getString("tablet_number", "");
            Button titleButton = (Button) findViewById(R.id.title_btn);
            titleButton.setText("KAYAM SCHOOL " + "(TABLET ID: " + tabletNumber + ")");
        } else {
            editor.putBoolean("review_mode_on", false);
            final LinearLayout lastBackupLinearLayout = findViewById(R.id.ll_last_backup);
            lastBackupLinearLayout.setVisibility(View.GONE);
        }

        editor.apply();

        GridView gridView = (GridView) findViewById(R.id.grid);

        users = ((LauncherApplication) getApplication()).getDbHandler().getUserList();
        String currentUsername = ((LauncherApplication) getApplication()).getDbHandler().getCurrentUsername();

        if (users.isEmpty()) return;
        LoginGridViewAdapter adapter = new LoginGridViewAdapter(this, users, currentUsername, isAdmin, this, this);
        gridView.setAdapter(adapter);
    }

    private void setLastBackupTime() {
        Typeface face = Typeface.createFromAsset(getAssets(), "TodoMainCurly.ttf");
        SharedPreferences preference = getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS);
        long lastBackupTime = preference.getLong("last_backup_time", 0L);
        String lastBackupFilename = preference.getString("last_backup_filename", "");
        final TextView lastBackupTextView = findViewById(R.id.tv_last_backup);
        final TextView lastBackupTimeTextView = findViewById(R.id.tv_last_backup_time);
        final LinearLayout lastBackupLinearLayout = findViewById(R.id.ll_last_backup);
        lastBackupLinearLayout.setVisibility(View.VISIBLE);
        lastBackupTextView.setTypeface(face);
        lastBackupTimeTextView.setTypeface(face);

        if (lastBackupTime != 0L) {
            lastBackupTextView.setText("Last backup: ");
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy hh:mm:ssa");
            Date resultdate = new Date(lastBackupTime);
            lastBackupTimeTextView.setText(sdf.format(resultdate));
            lastBackupTimeTextView.setVisibility(View.VISIBLE);
            lastBackupTextView.setOnClickListener(view -> {
                if (lastBackupFilename != null && !lastBackupFilename.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Last backup file: " + lastBackupFilename, Toast.LENGTH_SHORT).show();
                }
            });
            lastBackupTimeTextView.setOnClickListener(view -> {
                if (lastBackupFilename != null && !lastBackupFilename.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Last backup file: " + lastBackupFilename, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            lastBackupTextView.setText("No backup");
            lastBackupTimeTextView.setVisibility(View.GONE);
        }
    }

    public boolean checkStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11 (R) or above
            return Environment.isExternalStorageManager();
        } else {
            // Below Android 11
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

            return read == PackageManager.PERMISSION_GRANTED && write == PackageManager.PERMISSION_GRANTED;
        }
    }

    private static final int STORAGE_PERMISSION_CODE = 23;

    private void requestForStoragePermissions() {
        // Android 11 (R) or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        } else {
            // Below android 11
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    STORAGE_PERMISSION_CODE
            );
        }
    }


    private void generateCSV() {
        if (!checkStoragePermissions()) {
            requestForStoragePermissions();
            return;
        }

        ArrayList<User> users = ((LauncherApplication) getApplication()).getDbHandler().getUserList();

        String tabletNumber = getSharedPreferences("sharedPref", Context.MODE_MULTI_PROCESS).getString("tablet_number", "");

        try {
            StringBuilder content = new StringBuilder("Name,Stars,English,Math\n");

            for (User user : users) {
                if (!user.getUserName().equals("admin")) {
                    content.append(user.getDisplayName())
                            .append(",")
                            .append(user.getNumStars())
                            .append(",")
                            .append(user.getCurrentEnglishLevel())
                            .append(",")
                            .append(user.getCurrentMathLevel())
                            .append("\n");
                }
            }


            File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "kayam-reports");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/kayam-reports/", tabletNumber + "_" + KitkitDBHandler.getTimeFormatString(System.currentTimeMillis(), "yyyyMMddHHmmss") + "_Admin" + ".csv");
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content.toString());
            bw.close();

            Toast.makeText(LoginActivity.this, getString(R.string.csv_generated, file.getName()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onCreateUser(DialogFragment dialog, String redirectTo) {
        dialog.dismiss();
        refreshUI();
    }

    @Override
    public void onRemoveClick(final String value) {
        final User user = ((LauncherApplication) getApplication()).getDbHandler().findUser(value);
        if (user == null) {
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Delete " + user.getDisplayName())
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("Generate a CSV report?")
                                .setMessage("Before you delete this user, would you like to generate a CSV report?")
                                .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        generateCSV();
                                        ((LauncherApplication) getApplication()).getDbHandler().deleteUser(value);
                                        refreshUI();
                                    }
                                })
                                .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        ((LauncherApplication) getApplication()).getDbHandler().deleteUser(value);
                                        refreshUI();
                                    }
                                })
                                .show();
                    }
                })
                .setNegativeButton(R.string.dialog_no, null)
                .show();
    }
}
