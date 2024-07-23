package asia.chumbaka.kayam.launcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class UpdateDialogFragment extends DialogFragment {

    private int launcherVersion = 0;
    private int mainVersion = 0;
    private int libraryVersion = 0;
    private int bookviewerVersion = 0;

    private int launcherVersionRemote = 0;
    private int mainVersionRemote = 0;
    private int libraryVersionRemote = 0;
    private int bookviewerVersionRemote = 0;

    private StorageReference launcherRef = null;
    private StorageReference mainRef = null;
    private StorageReference libraryRef = null;
    private StorageReference bookviewerRef = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_updates, null);

        TextView launcherCurrentVersion = (TextView) dialogView.findViewById(R.id.launcher_current_version);
        TextView launcherRemoteVersion = (TextView) dialogView.findViewById(R.id.launcher_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            int version = pInfo.versionCode;
            launcherVersion = version;
            launcherCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            launcherCurrentVersion.setText("N/A");
            e.printStackTrace();
        }

        TextView mainCurrentVersion = (TextView) dialogView.findViewById(R.id.main_current_version);
        TextView mainRemoteVersion = (TextView) dialogView.findViewById(R.id.main_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.xprize", 0);
            int version = pInfo.versionCode;
            mainVersion = version;
            mainCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            mainCurrentVersion.setText("N/A");
            e.printStackTrace();
        }

        TextView libraryCurrentVersion = (TextView) dialogView.findViewById(R.id.library_current_version);
        TextView libraryRemoteVersion = (TextView) dialogView.findViewById(R.id.library_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.library", 0);
            int version = pInfo.versionCode;
            libraryVersion = version;
            libraryCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            libraryCurrentVersion.setText("N/A");
            e.printStackTrace();
        }

        TextView bookViewerCurrentVersion = (TextView) dialogView.findViewById(R.id.bookviewer_current_version);
        TextView bookViewerRemoteVersion = (TextView) dialogView.findViewById(R.id.bookviewer_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.bookviewer", 0);
            int version = pInfo.versionCode;
            bookviewerVersion = version;
            bookViewerCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            bookViewerCurrentVersion.setText("N/A");
            e.printStackTrace();
        }

        TextView tvCancel = (TextView) dialogView.findViewById(R.id.tv_cancel);
        TextView tvDownload = (TextView) dialogView.findViewById(R.id.tv_download);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (launcherRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + launcherRef.getName());

                        TextView launcherStatusTv = dialogView.findViewById(R.id.launcher_status);
                        launcherStatusTv.setText("Downloading");
                        launcherRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                launcherStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Launcher update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                launcherStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                if (mainRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + mainRef.getName());

                        TextView mainStatusTv = dialogView.findViewById(R.id.main_status);
                        mainStatusTv.setText("Downloading");
                        mainRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                mainStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Main update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                mainStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                if (libraryRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + libraryRef.getName());

                        TextView libraryStatusTv = dialogView.findViewById(R.id.library_status);
                        libraryStatusTv.setText("Downloading");
                        libraryRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                libraryStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Library update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                libraryStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                if (bookviewerRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + bookviewerRef.getName());

                        TextView bookviewerStatusTv = dialogView.findViewById(R.id.bookviewer_status);
                        bookviewerStatusTv.setText("Downloading");
                        bookviewerRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                bookviewerStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Bookviewer update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                bookviewerStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

            }
        });

        builder.setView(dialogView);

        // Create the AlertDialog object and return it
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        checkUpdates(launcherRemoteVersion, mainRemoteVersion, libraryRemoteVersion, bookViewerRemoteVersion);

        return dialog;
    }

    private void checkUpdates(TextView launcherRemoteVersion, TextView mainRemoteVersion, TextView libraryRemoteVersion, TextView bookViewerRemoteVersion) {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://kayam-school.appspot.com");
                        StorageReference listRef = storage.getReference().child("packages");

                        listRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        for (StorageReference item : listResult.getItems()) {
                                            // All the items under listRef.
                                            if (item.getName().startsWith("launcher")) {
                                                String pattern = item.getName();
                                                int start = pattern.indexOf("_") + 1;
                                                int end = pattern.indexOf(".");
                                                if (start != 0 && end != -1 && end > start) {
                                                    launcherRemoteVersion.setText(pattern.substring(start, end));
                                                    launcherVersionRemote = Integer.parseInt(pattern.substring(start, end));

                                                    if (launcherVersionRemote > launcherVersion) {
                                                        launcherRef = item;
                                                    }
                                                }
                                            } else if (item.getName().startsWith("main")) {
                                                String pattern = item.getName();
                                                int start = pattern.indexOf("_") + 1;
                                                int end = pattern.indexOf(".");
                                                if (start != 0 && end != -1 && end > start) {
                                                    mainRemoteVersion.setText(pattern.substring(start, end));
                                                    mainVersionRemote = Integer.parseInt(pattern.substring(start, end));

                                                    if (mainVersionRemote > mainVersion) {
                                                        mainRef = item;
                                                    }
                                                }
                                            } else if (item.getName().startsWith("library")) {
                                                String pattern = item.getName();
                                                int start = pattern.indexOf("_") + 1;
                                                int end = pattern.indexOf(".");
                                                if (start != 0 && end != -1 && end > start) {
                                                    libraryRemoteVersion.setText(pattern.substring(start, end));
                                                    libraryVersionRemote = Integer.parseInt(pattern.substring(start, end));

                                                    if (libraryVersionRemote > libraryVersion) {
                                                        libraryRef = item;
                                                    }
                                                }
                                            } else if (item.getName().startsWith("bookviewer")) {
                                                String pattern = item.getName();
                                                int start = pattern.indexOf("_") + 1;
                                                int end = pattern.indexOf(".");
                                                if (start != 0 && end != -1 && end > start) {
                                                    bookViewerRemoteVersion.setText(pattern.substring(start, end));
                                                    bookviewerVersionRemote = Integer.parseInt(pattern.substring(start, end));

                                                    if (bookviewerVersionRemote > bookviewerVersion) {
                                                        bookviewerRef = item;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Uh-oh, an error occurred!

                                    }
                                });

                    } else {
                        // If sign in fails, display a message to the user
                    }
                });
    }
}
