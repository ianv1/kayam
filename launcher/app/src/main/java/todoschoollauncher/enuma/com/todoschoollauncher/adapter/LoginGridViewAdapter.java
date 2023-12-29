package todoschoollauncher.enuma.com.todoschoollauncher.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.enuma.kitkitProvider.User;

import java.util.List;

public class LoginGridViewAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;

    public LoginGridViewAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
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
        TextView textView = new TextView(context);
        textView.setText(users.get(i).getUserName());
        return textView;
    }
}
