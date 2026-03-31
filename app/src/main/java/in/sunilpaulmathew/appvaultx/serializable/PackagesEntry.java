package in.sunilpaulmathew.appvaultx.serializable;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import in.sunilpaulmathew.appvaultx.adapters.LabelsAdapter;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class PackagesEntry implements Serializable {

    private boolean selected;
    private Drawable appIcon;
    private final boolean installed;
    private final int appType;
    private final String uadDes, removalRec, packageName, apkPath;
    private String appName;

    public PackagesEntry(String packageName, String apkPath, String removalRec, String uadDes, int appType, boolean installed) {
        this.packageName = packageName;
        this.apkPath = apkPath;
        this.removalRec = removalRec;
        this.uadDes = uadDes;
        this.appType = appType;
        this.installed = installed;
        this.selected = true;
    }

    public boolean isSelected() {
        return selected;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PackagesEntry)) return false;
        PackagesEntry that = (PackagesEntry) o;
        return packageName.equals(that.packageName);
    }

    public boolean isInstalled() {
        return installed;
    }

    public boolean isAppDisabled() {
        return appType == 3 || appType == 4 || appType == 5;
    }

    public boolean isSystemApp() {
        return appType == 1 || appType == 2 || appType == 4 || appType == 5;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    @Override
    public int hashCode() {
        return packageName.hashCode();
    }

    public int getAppType() {
        return appType;
    }

    public Intent launchIntent(Context context) {
        return context.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    public String getAPKPath() {
        return apkPath;
    }

    public File getParentFile() {
        return new File(apkPath).getParentFile();
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getRemovalRecommendation() {
        return removalRec;
    }

    public String getUADDescription() {
        return uadDes;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void loadPackageInfo(RecyclerView priority, ImageView icon, TextView name, TextView summary) {
        try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
            Handler handler = new Handler(Looper.getMainLooper());
            List<String> labels = new CopyOnWriteArrayList<>();
            executor.execute(() -> {

                if (removalRec != null) labels.add(removalRec);

                switch (appType) {
                    case 6:
                        labels.add("Debuggable");
                        break;
                    case 5:
                        labels.add("System");
                        labels.add("Disabled");
                        break;
                    case 4:
                        labels.add("Updated system");
                        labels.add("Disabled");
                        break;
                    case 3:
                        labels.add("User");
                        labels.add("Disabled");
                        break;
                    case 2:
                        labels.add("System");
                        break;
                    case 1:
                        labels.add("Updated system");
                        break;
                    default:
                        labels.add("User");
                        break;
                }

                PackageManager pm = icon.getContext().getPackageManager();
                ApplicationInfo ai = null;

                try {
                    if (installed) {
                        ai = pm.getApplicationInfo(packageName, 0);
                    } else {
                        PackageInfo pi = pm.getPackageArchiveInfo(apkPath, 0);
                        if (pi != null) {
                            ai = pi.applicationInfo;
                            Objects.requireNonNull(ai).sourceDir = apkPath;
                            ai.publicSourceDir = apkPath;
                        }
                    }

                    if (ai != null) {
                        appName = pm.getApplicationLabel(ai).toString();
                        appIcon = pm.getApplicationIcon(ai);
                    }
                } catch (PackageManager.NameNotFoundException ignored) {
                }

                handler.post(() -> {
                    priority.setItemAnimator(null);
                    priority.setAdapter(new LabelsAdapter(labels, appIcon, appName, packageName, uadDes));
                    priority.setLayoutManager(new LinearLayoutManager(priority.getContext(), LinearLayoutManager.HORIZONTAL, false));
                    name.setText(appName);
                    summary.setText(packageName);
                    icon.setImageDrawable(appIcon);
                });
            });
        }
    }

}