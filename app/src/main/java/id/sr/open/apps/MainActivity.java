package id.sr.open.apps;
 
import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import java.util.List;
import android.view.View;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import android.content.pm.ApplicationInfo;
import java.util.Collections;
import java.util.Comparator;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import id.sr.open.apps.FastScroller;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;

public class MainActivity extends Activity {

    private ListView appListView;
    private TextView totalAppsText;
    private EditText searchInput;
    private Button searchButton;

    private List<AppInfo> appList;
    private List<AppInfo> allApps;
    private AppAdapter appAdapter;
    private boolean showSystemApps = false;
    
    private FastScroller fastScroll;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appListView = findViewById(R.id.AppListView);
        totalAppsText = findViewById(R.id.TextTotalApps);
        searchInput = findViewById(R.id.EditTextPackage);
        searchButton = findViewById(R.id.ButtonSearch);
        fastScroll = findViewById(R.id.fast_scroller);
        
        fastScroll.attachListView(appListView);

        allApps = getInstalledApps();
        appList = new ArrayList<>(allApps);
        updateAppList();
        
        // saat salah satu app di listview di tekan
        appListView.setOnItemClickListener(new AdapterView.OnItemClickListener() { 
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final AppInfo selectedApp = (AppInfo) appAdapter.getItem(position);

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this); // muncul dialog
                    builder.setTitle(selectedApp.getAppName());
                    CharSequence[] options = new CharSequence[]{
                        getString(R.string.dialog_openapp),
                        getString(R.string.dialog_appinfo)
                    };

                    builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0) { // saat option pertama di tekan "open app"
                                                  // akan beralih ke intent package app yg di pilih
                                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage(selectedApp.getPackageName());
                                    if (launchIntent != null) {
                                        startActivity(launchIntent);
                                    } else {
                                        showCustomToast(getString(R.string.toast_cannot_open)); // apabila app tidak bisa di buka (biasanya app system)                   
                                    }                                                           // saat menggunakan option "open app"
                                } else if (which == 1) { // saat menekan option "app info" akan beralih ke app info yg dipilh
                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse(getString(R.string.text_package) + selectedApp.getPackageName()));
                                    startActivity(intent);
                                }
                            }
                        });

                    builder.show(); // menampilkan dialog
                    
                }
            });

        searchButton.setOnClickListener(new View.OnClickListener() { // saat button search di tekan akan memfilter nama app
                @Override                                            // sesuai dengan permintaan dari edittext
                public void onClick(View v) {
                    filterAppList(searchInput.getText().toString().trim().toLowerCase());
                }
            });
    }

    private void updateAppList() { // menampilkan app system dengan execute option menu
        appList.clear();
        for (AppInfo app : allApps) {
            if (!showSystemApps && app.isSystemApp()) continue;
            appList.add(app);
        }

        Collections.sort(appList, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo a1, AppInfo a2) {
                    return a1.getAppName().compareToIgnoreCase(a2.getAppName()); // urutan app di listview jadi A-Z
                }
            });

        appAdapter = new AppAdapter(this, appList);
        appListView.setAdapter(appAdapter);
        totalAppsText.setText( getString(R.string.count_app) + appList.size()); // menampilkan total aplikasi yg ada di listview
    }

    private void filterAppList(String keyword) { // untuk memfilter nama app
        List<AppInfo> filteredList = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (!showSystemApps && app.isSystemApp()) continue;
            if (app.getAppName().toLowerCase().contains(keyword)) {
                filteredList.add(app);
            }
        }

        Collections.sort(filteredList, new Comparator<AppInfo>() { // setelah memfilter tetap berurutan A-Z
                @Override
                public int compare(AppInfo a1, AppInfo a2) {
                    return a1.getAppName().compareToIgnoreCase(a2.getAppName());
                }
            });

        appAdapter = new AppAdapter(this, filteredList);
        appListView.setAdapter(appAdapter);
        totalAppsText.setText( getString(R.string.count_app) + filteredList.size());
    }

    private List<AppInfo> getInstalledApps() { // mengambil data aplikasi yg di intall
        List<AppInfo> resultList = new ArrayList<>(); // untuk di tampilkan di list_app.xml
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo appInfo : packages) {
            String appName = pm.getApplicationLabel(appInfo).toString();
            String packageName = appInfo.packageName;
            boolean isSystem = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;

            resultList.add(new AppInfo(appName, packageName, pm.getApplicationIcon(appInfo), isSystem));
        }

        return resultList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // membuat menu
        
        MenuItem showItem = menu.add(getString(R.string.menu_show_system)); // option pertama "show system app" dengan checkable
        showItem.setCheckable(true); // checkable aktif
        showItem.setChecked(showSystemApps); // saat di tekan
        MenuItem exitItem = menu.add(getString(R.string.menu_exit)); // option kedua "Exit"
        exitItem.setCheckable(false); // checkable tidak aktif
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { // execute option menu saat di tekan
        String title = item.getTitle().toString();
        if (title.equals(getString(R.string.menu_show_system))) { // otomatis menampilkan app system ke dalam listview
            showSystemApps = !item.isChecked();
            item.setChecked(showSystemApps);
            updateAppList();
            return true;
            
        } else if (title.equals(getString(R.string.menu_exit))){
            finishAffinity(); // keluar saat ditekan
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showCustomToast(String message) { // buat custom toast
        TextView textView = new TextView(getApplicationContext()); // toast akan show saat app yg tidak di buka pada option "open app"
        textView.setText(message);
        textView.setTextColor(getColor(R.color.whiteDefault));
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setBackgroundResource(R.drawable.toast_ui);
        textView.setPadding(50, 15, 50, 15);
        textView.setTextSize(14);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(textView);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
    
}
// ui/ux by sr6ni, backend 80% by chatgpt
// build with AIDE

