package todoschoollauncher.enuma.com.todoschoollauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.enuma.kitkitProvider.User;

import java.util.UUID;

public class CreateUserDialogFragment extends DialogFragment {

    private String[] password = {"", ""};

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface CreateUserListener {
        public void onCreateUser(DialogFragment dialog, String redirectTo);
    }

    // Use this instance of the interface to deliver action events
    CreateUserListener mListener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (CreateUserListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        this.getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_create_user, null);

        TextView title = (TextView) dialogView.findViewById(R.id.tv_login_password_title);
        TextView nameTitle = (TextView) dialogView.findViewById(R.id.tv_create_user_title);
        Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "TodoMainCurly.ttf");
        nameTitle.setTypeface(face);
        title.setTypeface(face);

        final ImageView ivField1 = (ImageView) dialogView.findViewById(R.id.iv_field_1);
        final ImageView ivField2 = (ImageView) dialogView.findViewById(R.id.iv_field_2);

        ivField1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password[0] = "";
                ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dotted_square));
            }
        });

        ivField2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                password[1] = "";
                ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_dotted_square));
            }
        });

        final ImageView ivImage1 = (ImageView) dialogView.findViewById(R.id.iv_image_1);
        final ImageView ivImage2 = (ImageView) dialogView.findViewById(R.id.iv_image_2);
        final ImageView ivImage3 = (ImageView) dialogView.findViewById(R.id.iv_image_3);
        final ImageView ivImage4 = (ImageView) dialogView.findViewById(R.id.iv_image_4);
        final ImageView ivImage5 = (ImageView) dialogView.findViewById(R.id.iv_image_5);
        final ImageView ivImage6 = (ImageView) dialogView.findViewById(R.id.iv_image_6);
        final ImageView ivImage7 = (ImageView) dialogView.findViewById(R.id.iv_image_7);
        final ImageView ivImage8 = (ImageView) dialogView.findViewById(R.id.iv_image_8);

        ivImage1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_burger));
                    password[0] = "1";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_burger));
                    password[1] = "1";
                }
            }
        });

        ivImage2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_car));
                    password[0] = "2";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_car));
                    password[1] = "2";
                }
            }
        });

        ivImage3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_chick));
                    password[0] = "3";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_chick));
                    password[1] = "3";
                }
            }
        });

        ivImage4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_donatello));
                    password[0] = "4";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_donatello));
                    password[1] = "4";
                }
            }
        });

        ivImage5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_globe));
                    password[0] = "5";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_globe));
                    password[1] = "5";
                }
            }
        });

        ivImage6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_mushrooms));
                    password[0] = "6";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_mushrooms));
                    password[1] = "6";
                }
            }
        });

        ivImage7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_penguin));
                    password[0] = "7";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_penguin));
                    password[1] = "7";
                }
            }
        });

        ivImage8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (password[0].equals("")) {
                    ivField1.setImageDrawable(getActivity().getDrawable(R.drawable.ic_scooter));
                    password[0] = "8";
                } else if (password[1].equals("")) {
                    ivField2.setImageDrawable(getActivity().getDrawable(R.drawable.ic_scooter));
                    password[1] = "8";
                }
            }
        });

        final TextView createTextView = (TextView) dialogView.findViewById(R.id.tv_create);
        final EditText createUserEditText = (EditText) dialogView.findViewById(R.id.et_create_user);
        createTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UUID uuid = UUID.randomUUID();
                ((LauncherApplication) getActivity().getApplication()).getDbHandler().addUser(new User(uuid.toString(), createUserEditText.getText().toString()));
                mListener.onCreateUser(CreateUserDialogFragment.this, "");
            }
        });

        final TextView cancelTextView = (TextView) dialogView.findViewById(R.id.tv_cancel);
        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        builder.setView(dialogView);

        // Create the AlertDialog object and return it
        Dialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }
}
