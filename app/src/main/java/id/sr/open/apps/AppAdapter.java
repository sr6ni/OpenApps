package id.sr.open.apps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class AppAdapter extends BaseAdapter {

    private Context context;
    private List<AppInfo> appList;
    private LayoutInflater inflater;

    public AppAdapter(Context context, List<AppInfo> appList) {
        this.context = context;
        this.appList = appList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return appList.size();
    }

    @Override
    public Object getItem(int position) {
        return appList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView icon;
        TextView appName;
        TextView packageName;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_app, parent, false);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.AppIcon);
            holder.appName = convertView.findViewById(R.id.AppName);
            holder.packageName = convertView.findViewById(R.id.AppPackage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AppInfo app = appList.get(position);
        holder.icon.setImageDrawable(app.getIcon());
        holder.appName.setText(app.getAppName());
        holder.packageName.setText(app.getPackageName());

        return convertView;
    }
}

