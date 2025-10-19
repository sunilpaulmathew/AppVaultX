package in.sunilpaulmathew.appvaultx.activities;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AccessUnavilableDialog;
import in.sunilpaulmathew.appvaultx.dialogs.InstallerDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.ShizukuPermissionChecker;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class InstallerActivity extends AppCompatActivity {

    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this::onRequestPermissionsResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();

        if (Shizuku.pingBinder() && Shizuku.getVersion() >= 11) {
            new ShizukuPermissionChecker(this) {
                @Override
                public void onRequestingPermission() {
                    // Request permission
                    Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
                    Shizuku.requestPermission(0);
                }

                @Override
                public void onFinished() {
                    ShizukuShell.ensureUserService(null);
                }
            };
        } else {
            new AccessUnavilableDialog(this).show();
        }

        if (uri != null) {
            new Async() {
                private final Activity activity = InstallerActivity.this;
                private boolean debuggable = false, downgrade = false, failed = false, update = false;
                private Drawable appIcon = null;
                private ParcelFileDescriptor fileDescriptor;
                private ProgressDialog progressDialog;
                private String appName = null, packageName = null, summary = null;

                @Override
                public void onPreExecute() {
                    progressDialog = new ProgressDialog(activity);
                    progressDialog.setProgressStatus(R.string.loading);
                    progressDialog.startDialog();
                }

                private String getFileSize() {
                    long size = -1;

                    // Try querying the size via OpenableColumns
                    Cursor cursor = null;
                    try {
                        String[] projection = {
                                OpenableColumns.SIZE
                        };
                        cursor = getContentResolver().query(uri, projection, null, null, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                            if (!cursor.isNull(sizeIndex)) {
                                size = cursor.getLong(sizeIndex);
                            }
                        }
                    } finally {
                        if (cursor != null) cursor.close();
                    }

                    // Fallback: use ParcelFileDescriptor.getStatSize()
                    if (size < 0) {
                        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r")) {
                            if (pfd != null) {
                                size = pfd.getStatSize();
                            }
                        } catch (IOException ignored) {}
                    }

                    // Format size into human-readable string
                    if (size <= 0) return "Unknown";
                    final String[] units = {"B", "KB", "MB", "GB", "TB"};
                    int unitIndex = 0;
                    double readableSize = size;
                    while (readableSize >= 1024 && unitIndex < units.length - 1) {
                        readableSize /= 1024;
                        unitIndex++;
                    }
                    return String.format(Locale.getDefault(), "%.2f %s", readableSize, units[unitIndex]);
                }


                private void generateAPKInfo(String apkPath) {
                    PackageManager pm = getPackageManager();
                    PackageInfo pkgInfo = pm.getPackageArchiveInfo(apkPath, 0);
                    if (pkgInfo != null) {
                        ApplicationInfo appInfo = pkgInfo.applicationInfo;
                        Objects.requireNonNull(appInfo).sourceDir = apkPath;
                        appInfo.publicSourceDir = apkPath;

                        appName = pm.getApplicationLabel(appInfo).toString();
                        packageName = appInfo.packageName;
                        appIcon = pm.getApplicationIcon(appInfo);

                        String versionName = pkgInfo.versionName;
                        int versionCode = pkgInfo.versionCode;
                        int minSdk = appInfo.minSdkVersion;
                        int targetSdk = appInfo.targetSdkVersion;
                        debuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
                        update = Packages.isAppInstalled(packageName, activity);
                        downgrade = isDowngradeRequired(versionCode, pm, packageName);

                        String details = "Version: " + versionName +
                                " (" + versionCode + ")" + "\n" +
                                "Min SDK: " + sdkToAndroidVersion(minSdk) + "\n" +
                                "Target SDK: " + sdkToAndroidVersion(targetSdk) + "\n" +
                                "Size: " + getFileSize() + "\n" +
                                (downgrade ? getString(R.string.downgrade_required_status) + "\n" : "") +
                                "Debuggable: " + (debuggable ? "Yes" : "No") + "\n\n";

                        summary = details.trim();
                    } else {
                        failed = true;
                    }
                }

                private boolean isDowngradeRequired(int versionCode, PackageManager pm, String packageName) {
                    if (!update) return false;
                    try {
                        PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                        return versionCode < packageInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                    return false;
                }

                public String sdkToAndroidVersion(int sdkVersion) {
                    switch (sdkVersion) {
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

                private File copyToCache() throws IOException {
                    File cacheFile = new File(getCacheDir(), "temp_install.apk");
                    Utils.copy(uri, cacheFile, activity);
                    return cacheFile;
                }

                @Override
                public void doInBackground() {
                    try {
                        if (Build.VERSION.SDK_INT >= 36) {
                            // Android 15+ requires a real file path
                            File localApkFile = copyToCache();
                            if (localApkFile.exists()) {
                                generateAPKInfo(localApkFile.getAbsolutePath());
                            } else {
                                failed = true;
                            }
                        } else {
                            fileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                            if (fileDescriptor != null) {
                                generateAPKInfo("/proc/self/fd/" + fileDescriptor.getFd());
                            }
                        }
                    } catch (IOException ignored) {}
                }

                @Override
                public void onPostExecute() {
                    progressDialog.dismissDialog();
                    if (!failed && !summary.trim().isEmpty()) {
                        new InstallerDialog(appIcon, appName, packageName, summary, update, downgrade, activity) {
                            @Override
                            public void onInstall() {
                                installAPK(debuggable, downgrade, appIcon, appName, fileDescriptor).execute();
                            }
                        };
                    }
                }
            }.execute();
        }

    }

    private Async installAPK(boolean debuggable, boolean downgrade, Drawable appIcon, String appName, ParcelFileDescriptor fileDescriptor) {
        return new Async() {
            private final Activity activity = InstallerActivity.this;
            private ProgressDialog progressDialog;
            private String result = null;
            @Override
            public void onPreExecute() {
                progressDialog = new ProgressDialog(activity);
                progressDialog.setProgressStatus(getString(R.string.installing_message, appName));
                progressDialog.startDialog();
            }

            @Override
            public void doInBackground() {
                if (Build.VERSION.SDK_INT >= 36) {
                    copyUriToTmp(new File(getCacheDir(), "temp_install.apk"));
                } else {
                    copyUriToTmp(fileDescriptor);
                }

                StringBuilder flags = new StringBuilder();
                if (debuggable) {
                    flags.append("-t -r ");
                }
                if (downgrade) {
                    flags.append("-d ");
                }
                flags.append("--user ").append(Utils.getUserID()).append(" ");

                String cmd = "pm install " + flags + "/data/local/tmp/tmp.apk";

                result = ShizukuShell.runCommand(cmd);

                // Cleanup tmp apk after installation
                ShizukuShell.runCommand("rm -r /data/local/tmp/tmp.apk");
            }

            private void copyUriToTmp(File file) {
                if (file.exists()) {
                    ShizukuShell.ensureUserService(() -> {
                        try {
                            ShizukuShell.writeToFile(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY), "/data/local/tmp/tmp.apk");
                        } catch (FileNotFoundException ignored) {
                        }
                    });
                }
            }

            private void copyUriToTmp(ParcelFileDescriptor pfd) {
                if (pfd != null) {
                    ShizukuShell.ensureUserService(() -> ShizukuShell.writeToFile(pfd, "/data/local/tmp/tmp.apk"));
                }
            }

            @Override
            public void onPostExecute() {
                progressDialog.dismissDialog();
                if (result != null && !result.trim().isEmpty()) {
                    new MaterialAlertDialogBuilder(activity)
                            .setIcon(appIcon)
                            .setTitle(appName)
                            .setMessage(result.trim().equalsIgnoreCase("success") ? getString(R.string.install_success_message) : result.trim())
                            .setPositiveButton(R.string.cancel, (d, w) -> activity.finish()).show();
                } else {
                    Utils.toast(getString(R.string.install_failed_message), activity).show();
                }
            }
        };
    }

    private void onRequestPermissionsResult(int requestCode, int grantResult) {
        if (requestCode == 0 && grantResult == PackageManager.PERMISSION_GRANTED) {
            Utils.toast("aShell got access to Shizuku service", this).show();
            ShizukuShell.ensureUserService(null);
        } else {
            Utils.toast(getString(R.string.shizuku_access_denied_title), this).show();
            finish();
        }
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        ShizukuShell.destroy();
    }

}