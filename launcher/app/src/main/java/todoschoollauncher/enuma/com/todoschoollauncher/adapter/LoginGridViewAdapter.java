package todoschoollauncher.enuma.com.todoschoollauncher.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enuma.kitkitProvider.User;

import java.util.List;

import todoschoollauncher.enuma.com.todoschoollauncher.R;

public class LoginGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;
    private Typeface face;
    private String currentUsername;
    private OnItemClick mCallback;
    private OnRemoveClick mRemoveClickCallback;
    private boolean isAdmin;

    public LoginGridViewAdapter(Context context, List<User> users, String currentUserName, boolean isAdmin, OnItemClick listener, OnRemoveClick removeListener) {
        this.context = context;
        this.users = users;
        this.face = Typeface.createFromAsset(context.getAssets(), "TodoMainCurly.ttf");
        this.currentUsername = currentUserName;
        this.isAdmin = isAdmin;
        this.mCallback = listener;
        this.mRemoveClickCallback = removeListener;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        CardView cardView = (CardView) LayoutInflater.from(context).inflate(R.layout.item_user, null);
        TextView textView = (TextView) cardView.findViewById(R.id.tv_username);
        textView.setText(users.get(i).getDisplayName());
        textView.setTypeface(face);

        RelativeLayout relativeLayout = (RelativeLayout) cardView.findViewById(R.id.rl_bg);
        switch (i % 5) {
            case 0:
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.red));
                break;
            case 1:
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.orange));
                break;
            case 2:
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.green));
                break;
            case 3:
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.blue));
                break;
            case 4:
                relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.purple));
                break;
        }

        TextView tvInitials = (TextView) cardView.findViewById(R.id.tv_initials);
        if (users.get(i).getDisplayName() != null) {
            String[] array = users.get(i).getDisplayName().split(" ");
            if (array.length != 0) {
                if (array.length > 1) {
                    tvInitials.setText(String.valueOf(array[0].charAt(0)) + String.valueOf(array[1].charAt(0)));
                } else {
                    tvInitials.setText(String.valueOf(array[0].charAt(0)));
                }
            }
            tvInitials.setTypeface(face);
        }

        ImageView icTick = (ImageView) cardView.findViewById(R.id.iv_tick);
        if (currentUsername != null && currentUsername.equals(users.get(i).getUserName())) {
            icTick.setVisibility(View.VISIBLE);
            cardView.setBackgroundResource(R.drawable.card_view_with_border_selected);
        } else {
            icTick.setVisibility(View.INVISIBLE);
            cardView.setBackgroundResource(R.drawable.card_view_with_border);
        }

        ImageView icRemove = (ImageView) cardView.findViewById(R.id.iv_remove);
        if (isAdmin) {
            icRemove.setVisibility(View.VISIBLE);
        } else {
            icRemove.setVisibility(View.GONE);
        }

        icRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedUserName = users.get(i).getUserName();
                mRemoveClickCallback.onRemoveClick(selectedUserName);
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedUserName = users.get(i).getUserName();
                if (!selectedUserName.equals(currentUsername)) {
                    mCallback.onClick(selectedUserName);
                }
            }
        });

        return cardView;
    }
}
