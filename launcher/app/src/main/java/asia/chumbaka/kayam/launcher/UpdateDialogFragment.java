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
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
    private int mainBmVersion = 0;
    private int libraryVersion = 0;
    private int libraryBmVersion = 0;
    private int bookviewerVersion = 0;
    private int bookviewerBmVersion = 0;

    private int launcherVersionRemote = 0;
    private int mainVersionRemote = 0;
    private int mainBmVersionRemote = 0;
    private int libraryVersionRemote = 0;
    private int libraryBmVersionRemote = 0;
    private int bookviewerVersionRemote = 0;
    private int bookviewerBmVersionRemote = 0;

    private StorageReference launcherRef = null;
    private StorageReference mainRef = null;
    private StorageReference mainBmRef = null;
    private StorageReference libraryRef = null;
    private StorageReference libraryBmRef = null;
    private StorageReference bookviewerRef = null;
    private StorageReference bookviewerBmRef = null;

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

        TextView mainBmCurrentVersion = (TextView) dialogView.findViewById(R.id.main_bm_current_version);
        TextView mainBmRemoteVersion = (TextView) dialogView.findViewById(R.id.main_bm_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.xprize.bm", 0);
            int version = pInfo.versionCode;
            mainBmVersion = version;
            mainBmCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            mainBmCurrentVersion.setText("N/A");
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

        TextView libraryBmCurrentVersion = (TextView) dialogView.findViewById(R.id.library_bm_current_version);
        TextView libraryBmRemoteVersion = (TextView) dialogView.findViewById(R.id.library_bm_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.library.bm", 0);
            int version = pInfo.versionCode;
            libraryBmVersion = version;
            libraryBmCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            libraryBmCurrentVersion.setText("N/A");
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

        TextView bookViewerBmCurrentVersion = (TextView) dialogView.findViewById(R.id.bookviewer_bm_current_version);
        TextView bookViewerBmRemoteVersion = (TextView) dialogView.findViewById(R.id.bookviewer_bm_remote_version);

        try {
            PackageInfo pInfo = getContext().getPackageManager().getPackageInfo("asia.chumbaka.kayam.bookviewer.bm", 0);
            int version = pInfo.versionCode;
            bookviewerBmVersion = version;
            bookViewerBmCurrentVersion.setText(String.valueOf(version));
        } catch (PackageManager.NameNotFoundException e) {
            bookViewerBmCurrentVersion.setText("N/A");
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

                if (mainBmRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + mainBmRef.getName());

                        TextView mainBmStatusTv = dialogView.findViewById(R.id.main_bm_status);
                        mainBmStatusTv.setText("Downloading");
                        mainBmRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                mainBmStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Main BM update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                mainBmStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                if (libraryBmRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + libraryBmRef.getName());

                        TextView libraryBmStatusTv = dialogView.findViewById(R.id.library_bm_status);
                        libraryBmStatusTv.setText("Downloading");
                        libraryBmRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                libraryBmStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Library BM update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                libraryBmStatusTv.setText("Download failed");
                            }
                        });

                    } catch (Exception e) {

                    }
                }

                if (bookviewerBmRef != null) {
                    try {
                        File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "kayam");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }
                        File localFile = new File(folder.getPath() + "/" + bookviewerBmRef.getName());

                        TextView bookviewerBmStatusTv = dialogView.findViewById(R.id.bookviewer_bm_status);
                        bookviewerBmStatusTv.setText("Downloading");
                        bookviewerBmRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                bookviewerBmStatusTv.setText("Download successful");
                                Toast.makeText(getActivity(), "Bookviewer BM update is downloaded successfully!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                bookviewerBmStatusTv.setText("Download failed");
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

        checkUpdates(launcherRemoteVersion,
                mainRemoteVersion, mainBmRemoteVersion,
                libraryRemoteVersion, libraryBmRemoteVersion,
                bookViewerRemoteVersion, bookViewerBmRemoteVersion);

        return dialog;
    }

    private void checkUpdates(TextView launcherRemoteVersion,
                              TextView mainRemoteVersion, TextView mainBmRemoteVersion,
                              TextView libraryRemoteVersion, TextView libraryBmRemoteVersion,
                              TextView bookViewerRemoteVersion, TextView bookViewerBmRemoteVersion) {
        FirebaseAuth.getInstance().signInAnonymously()
                .addOnCompleteListener(getActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseStorage storage = FirebaseStorage.getInstance("gs://kayam-school-phase-2.appspot.com");
                        StorageReference listRef = storage.getReference().child("packages");

                        listRef.listAll()
                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                    @Override
                                    public void onSuccess(ListResult listResult) {
                                        for (StorageReference item : listResult.getItems()) {
                                            // Firebase naming convention:
                                            //   launcher_<v>.apk.zip
                                            //   main_en_<v>.apk.zip       main_bm_<v>.apk.zip
                                            //   library_en_<v>.apk.zip    library_bm_<v>.apk.zip
                                            //   bookviewer_en_<v>.apk.zip bookviewer_bm_<v>.apk.zip
                                            //
                                            // Version sits between the LAST underscore and the
                                            // first dot. Match the longest, most-specific prefix
                                            // first so "main_en_" and "main_bm_" don't both fall
                                            // through to a generic "main_" branch.
                                            String name = item.getName();
                                            int start = name.lastIndexOf("_") + 1;
                                            int end = name.indexOf(".");
                                            if (start == 0 || end == -1 || end <= start) continue;
                                            String versionStr = name.substring(start, end);
                                            int versionInt;
                                            try {
                                                versionInt = Integer.parseInt(versionStr);
                                            } catch (NumberFormatException nfe) {
                                                continue;
                                            }

                                            if (name.startsWith("launcher_")) {
                                                launcherRemoteVersion.setText(versionStr);
                                                launcherVersionRemote = versionInt;
                                                if (launcherVersionRemote > launcherVersion) {
                                                    launcherRef = item;
                                                }
                                            } else if (name.startsWith("main_en_")) {
                                                mainRemoteVersion.setText(versionStr);
                                                mainVersionRemote = versionInt;
                                                if (mainVersionRemote > mainVersion) {
                                                    mainRef = item;
                                                }
                                            } else if (name.startsWith("main_bm_")) {
                                                mainBmRemoteVersion.setText(versionStr);
                                                mainBmVersionRemote = versionInt;
                                                if (mainBmVersionRemote > mainBmVersion) {
                                                    mainBmRef = item;
                                                }
                                            } else if (name.startsWith("library_en_")) {
                                                libraryRemoteVersion.setText(versionStr);
                                                libraryVersionRemote = versionInt;
                                                if (libraryVersionRemote > libraryVersion) {
                                                    libraryRef = item;
                                                }
                                            } else if (name.startsWith("library_bm_")) {
                                                libraryBmRemoteVersion.setText(versionStr);
                                                libraryBmVersionRemote = versionInt;
                                                if (libraryBmVersionRemote > libraryBmVersion) {
                                                    libraryBmRef = item;
                                                }
                                            } else if (name.startsWith("bookviewer_en_")) {
                                                bookViewerRemoteVersion.setText(versionStr);
                                                bookviewerVersionRemote = versionInt;
                                                if (bookviewerVersionRemote > bookviewerVersion) {
                                                    bookviewerRef = item;
                                                }
                                            } else if (name.startsWith("bookviewer_bm_")) {
                                                bookViewerBmRemoteVersion.setText(versionStr);
                                                bookviewerBmVersionRemote = versionInt;
                                                if (bookviewerBmVersionRemote > bookviewerBmVersion) {
                                                    bookviewerBmRef = item;
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
