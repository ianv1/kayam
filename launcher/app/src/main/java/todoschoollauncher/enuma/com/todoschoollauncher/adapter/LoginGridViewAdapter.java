package todoschoollauncher.enuma.com.todoschoollauncher.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.enuma.kitkitProvider.User;

import java.util.List;

import todoschoollauncher.enuma.com.todoschoollauncher.R;

public class LoginGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;
    private Typeface face;

    public LoginGridViewAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
        this.face = Typeface.createFromAsset(context.getAssets(), "TodoMainCurly.ttf");
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
    public View getView(int i, View view, ViewGroup viewGroup) {
        CardView cardView = (CardView) LayoutInflater.from(context).inflate(R.layout.item_user, null);
        TextView textView = (TextView) cardView.findViewById(R.id.tv_username);
        textView.setText(users.get(i).getUserName());
        textView.setTypeface(face);
        return textView;
    }
}
