package in.sunilpaulmathew.appvaultx.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.core.widget.ContentLoadingProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.appvaultx.serializable.DebloaterEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on Oct. 03, 2025
 */
public class Packages {

    private static List<PackagesEntry> mInstalledPackageItems, mUninstalledPackageItems;

    public static boolean isAppInstalled(String packageName, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static int getAppType(ApplicationInfo applicationInfo) {
        boolean systemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        boolean updatedSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        boolean debuggable = (applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        boolean disabled = !applicationInfo.enabled;
        int appType;
        if (debuggable) {
            appType = 6;
        } else if (disabled) {
            if (systemApp) {
                appType = 5;
            } else if (updatedSystemApp) {
                appType = 4;
            } else {
                appType = 3;
            }
        } else {
            if (systemApp) {
                appType = 2;
            } else if (updatedSystemApp) {
                appType = 1;
            } else {
                appType = 0;
            }
        }
        return appType;
    }

    public static List<PackagesEntry> getPackages() {
        return mInstalledPackageItems;
    }

    public static List<PackagesEntry> getUninstalledPackages() {
        return mUninstalledPackageItems;
    }

    public static void loadPackages(ContentLoadingProgressBar progressBar, Context context) {
        List<DebloaterEntry> debloaterEntries = getUADList(context);
        mInstalledPackageItems = new CopyOnWriteArrayList<>();
        mUninstalledPackageItems = new CopyOnWriteArrayList<>();

        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES);

        int totalSIze = packages.size();
        if (progressBar != null) {
            progressBar.setIndeterminate(false);
            progressBar.setMax(totalSIze);
        }

        for (ApplicationInfo packageInfo : packages) {
            boolean installed = (packageInfo.flags & ApplicationInfo.FLAG_INSTALLED) != 0;
            boolean removed = (packageInfo.flags & ApplicationInfo.FLAG_INSTALLED) == 0;
            int appType = getAppType(packageInfo);
            if (installed) {
                mInstalledPackageItems.add(new PackagesEntry(packageInfo.packageName, packageInfo.sourceDir, debloaterEntries.stream()
                        .filter(entry -> entry.getPackageName().equals(packageInfo.packageName))
                        .map(DebloaterEntry::getRemovalStatus)
                        .findFirst()
                        .orElse(null), debloaterEntries.stream()
                        .filter(entry -> entry.getPackageName().equals(packageInfo.packageName))
                        .map(DebloaterEntry::getDescription)
                        .findFirst()
                        .orElse(null), appType, true)
                );
            } else if (removed) {
                mUninstalledPackageItems.add(new PackagesEntry(packageInfo.packageName, packageInfo.sourceDir, debloaterEntries.stream()
                        .filter(entry -> entry.getPackageName().equals(packageInfo.packageName))
                        .map(DebloaterEntry::getRemovalStatus)
                        .findFirst()
                        .orElse(null), debloaterEntries.stream()
                        .filter(entry -> entry.getPackageName().equals(packageInfo.packageName))
                        .map(DebloaterEntry::getDescription)
                        .findFirst()
                        .orElse(null), appType, false)
                );
            }
            if (progressBar != null) {
                if (progressBar.getProgress() < packages.size()) {
                    progressBar.setProgress(progressBar.getProgress() + 1);
                } else {
                    progressBar.setProgress(0);
                }
            }
        }
    }

    private static List<DebloaterEntry> getUADList(Context context) {
        List<DebloaterEntry> debloaterEntries = new CopyOnWriteArrayList<>();
        try {
            JSONObject root = new JSONObject(Utils.readAssetFile("uad_lists.json", context));

            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String packageName = keys.next();
                JSONObject packageInfo = root.getJSONObject(packageName);

                String list = packageInfo.getString("list");
                String description = packageInfo.getString("description");
                JSONArray dependencies = packageInfo.getJSONArray("dependencies");
                JSONArray neededBy = packageInfo.getJSONArray("neededBy");
                JSONArray labels = packageInfo.getJSONArray("labels");
                String removal = packageInfo.getString("removal");

                debloaterEntries.add(new DebloaterEntry(packageName, list, description, dependencies, neededBy, labels, removal));
            }

        } catch (Exception ignored) {
        }
        return debloaterEntries;
    }

}