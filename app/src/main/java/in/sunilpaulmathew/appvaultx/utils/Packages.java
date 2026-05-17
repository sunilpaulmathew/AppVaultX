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
        if (debuggable) {
            return disabled ? 6 : 7;
        }
        int appType;
        if (disabled) {
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

    public static String sdkToAndroidVersion(int sdkVersion) {
        switch (sdkVersion) {
            case 37:
                return "17 (CINNAMON_BUN, " + sdkVersion + ")";
            case 36:
                return "16 (BAKLAVA, " + sdkVersion + ")";
            case 35:
                return "15 (VANILLA_ICE_CREAM, " + sdkVersion + ")";
            case 34:
                return "14 (UPSIDE_DOWN_CAKE, " + sdkVersion + ")";
            case 33:
                return "13 (TIRAMISU, " + sdkVersion + ")";
            case 32:
                return "12.1 (S_V2, " + sdkVersion + ")";
            case 31:
                return "12 (S, " + sdkVersion + ")";
            case 30:
                return "11 (R, " + sdkVersion + ")";
            case 29:
                return "10 (Q, " + sdkVersion + ")";
            case 28:
                return "9 (P, " + sdkVersion + ")";
            case 27:
                return "8 (O_MR1, " + sdkVersion + ")";
            case 26:
                return "8.0 (0, " + sdkVersion + ")";
            case 25:
                return "7.1.1 (N_MRI, " + sdkVersion + ")";
            case 24:
                return "7.0 (N, " + sdkVersion + ")";
            case 23:
                return "6.0 (M, " + sdkVersion + ")";
            case 22:
                return "5.1 (LOLLIPOP_MR1, " + sdkVersion + ")";
            case 21:
                return "5.0 (LOLLIPOP, " + sdkVersion + ")";
            case 20:
                return "4.4 (KITKAT_WATCH, " + sdkVersion + ")";
            case 19:
                return "4.4 (KITKAT, " + sdkVersion + ")";
            case 18:
                return "4.3 (JELLY_BEAN_MR2, " + sdkVersion + ")";
            case 17:
                return "4.2 (JELLY_BEAN_MR1, " + sdkVersion + ")";
            case 16:
                return "4.1 (JELLY_BEAN, " + sdkVersion + ")";
            case 15:
                return "4.0.3 (ICE_CREAM_SANDWICH_MR1, " + sdkVersion + ")";
            case 14:
                return "4.0 (ICE_CREAM_SANDWICH, " + sdkVersion + ")";
            case 13:
                return "3.2 (HONEYCOMB_MR2, " + sdkVersion + ")";
            case 12:
                return "3.1 (HONEYCOMB_MR1, " + sdkVersion + ")";
            case 11:
                return "3.0 (HONEYCOMB, " + sdkVersion + ")";
            case 10:
                return "2.3.3 (GINGERBREAD_MR1, " + sdkVersion + ")";
            case 9:
                return "2.3 (GINGERBREAD, " + sdkVersion + ")";
            case 8:
                return "2.2 (FROYO, " + sdkVersion + ")";
            case 7:
                return "2.1 (ECLAIR_MR1, " + sdkVersion + ")";
            case 6:
                return "2.0.1 (ECLAIR_0_1, " + sdkVersion + ")";
            case 5:
                return "2.0 (ECLAIR, " + sdkVersion + ")";
            case 4:
                return "1.6 (DONUT, " + sdkVersion + ")";
            case 3:
                return "1.5 (CUPCAKE, " + sdkVersion + ")";
            case 2:
                return "1.1 (BASE_1_1, " + sdkVersion + ")";
            case 1:
                return "1.0 (BASE, " + sdkVersion + ")";
            default:
                return String.valueOf(sdkVersion);
        }
    }

}