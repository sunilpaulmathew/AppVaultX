package in.sunilpaulmathew.appvaultx.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.ADBInstructionsDialog;
import in.sunilpaulmathew.appvaultx.dialogs.APKDetailsDialog;
import in.sunilpaulmathew.appvaultx.dialogs.BottomMenuDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.MenuEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackageDetailsEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackageHeaderEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.serializable.PermissionsEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class RemovedPackagesAdapter extends RecyclerView.Adapter<RemovedPackagesAdapter.ViewHolder> {

    private final Activity activity;
    private final List<PackagesEntry> data, selectedApps;
    private final MaterialButton batchOptions;

    public RemovedPackagesAdapter(List<PackagesEntry> data, List<PackagesEntry> selectedApps, MaterialButton batchOptions, Activity activity) {
        this.data = data;
        this.selectedApps = selectedApps;
        this.batchOptions = batchOptions;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_packages, parent, false);
        return new ViewHolder(rowItem);
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.data.get(position).loadPackageInfo(holder.priority, holder.appIcon, holder.appName, holder.packageName);

        if (!Settings.isShizukuIgnored(holder.appIcon.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            holder.appIcon.setClickable(true);
            if (selectedApps.contains(this.data.get(position))) {
                holder.checkBox.setChecked(selectedApps.contains(this.data.get(position)));
                holder.checkBox.setVisibility(VISIBLE);
                holder.appIcon.setVisibility(GONE);
            } else {
                holder.checkBox.setVisibility(GONE);
                holder.appIcon.setVisibility(VISIBLE);
            }

            if (selectedApps.isEmpty()) {
                batchOptions.setVisibility(GONE);
            } else {
                batchOptions.setText(batchOptions.getContext().getString(R.string.restore_selected, selectedApps.size()));
                batchOptions.setVisibility(VISIBLE);
            }

            holder.appIcon.setOnClickListener(v -> {
                holder.appIcon.setVisibility(GONE);
                holder.checkBox.setVisibility(VISIBLE);
                selectedApps.add(this.data.get(position));
                notifyItemChanged(position);
            });

            holder.checkBox.setOnClickListener(v -> {
                holder.appIcon.setVisibility(VISIBLE);
                holder.checkBox.setVisibility(GONE);
                selectedApps.remove(this.data.get(position));
                notifyItemChanged(position);
            });
        } else {
            holder.appIcon.setClickable(false);
        }

        Settings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton appIcon;
        private final MaterialCheckBox checkBox;
        private final MaterialTextView appName, packageName;
        private final RecyclerView priority;

        @SuppressLint("SetTextI18n")
        public ViewHolder(View view) {
            super(view);
            this.appIcon = view.findViewById(R.id.app_icon);
            this.checkBox = view.findViewById(R.id.checkbox);
            this.priority = view.findViewById(R.id.priority_list);
            this.appName = view.findViewById(R.id.app_name);
            this.packageName = view.findViewById(R.id.package_name);

            view.setOnClickListener(v -> {
                PackagesEntry packageItems = data.get(getBindingAdapterPosition());
                if (!Settings.isShizukuIgnored(v.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED && !selectedApps.isEmpty()) {
                    if (selectedApps.contains(packageItems)) {
                        selectedApps.remove(packageItems);
                    } else {
                        selectedApps.add(packageItems);
                    }
                    notifyItemChanged(getBindingAdapterPosition());
                    return;
                }

                List<MenuEntry> menuItems = new ArrayList<>();

                menuItems.add(new MenuEntry(R.string.app_info, R.drawable.ic_info, 0));
                menuItems.add(new MenuEntry(R.string.restore, R.drawable.ic_reset, 1));
                menuItems.add(new MenuEntry(R.string.save_apks, R.drawable.ic_download, 2));
                menuItems.add(new MenuEntry(R.string.save_icon, R.drawable.ic_save, 3));
                menuItems.add(new MenuEntry(R.string.settings, R.drawable.ic_settings, 4));

                new BottomMenuDialog(menuItems, packageItems.getAppIcon(), packageItems.getAppName(), packageItems.getPackageName(), v.getContext()) {
                    @Override
                    public void onMenuItemClicked(int menuID) {
                        switch (menuID) {
                            case 0:
                                new Async() {
                                    private ProgressDialog progressDialog;
                                    private final List<PackageHeaderEntry> header = new CopyOnWriteArrayList<>();
                                    private final List<PackageDetailsEntry> details = new CopyOnWriteArrayList<>();

                                    @Override
                                    public void onPreExecute() {
                                        progressDialog = new ProgressDialog(v.getContext());
                                        progressDialog.setProgressStatus(R.string.loading);
                                        progressDialog.startDialog();
                                    }

                                    private String getFileSize(long size) {
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

                                    private void generatePackageInfo() {
                                        PackageManager pm = v.getContext().getPackageManager();
                                        PackageInfo pkgInfo = pm.getPackageArchiveInfo(packageItems.getAPKPath(), PackageManager.GET_PERMISSIONS);

                                        if (pkgInfo != null) {
                                            ApplicationInfo appInfo = pkgInfo.applicationInfo;
                                            String versionName = pkgInfo.versionName;
                                            int versionCode = pkgInfo.versionCode;
                                            int minSdk = Objects.requireNonNull(appInfo).minSdkVersion;
                                            int targetSdk = appInfo.targetSdkVersion;
                                            String baseAPKName = null;
                                            List<PermissionsEntry> splitAPKNames = new CopyOnWriteArrayList<>();
                                            List<PermissionsEntry> permissions = new CopyOnWriteArrayList<>();
                                            if (pkgInfo.requestedPermissions != null && pkgInfo.requestedPermissionsFlags != null) {
                                                for (int i = 0; i < pkgInfo.requestedPermissions.length; i++) {
                                                    String permissionName = pkgInfo.requestedPermissions[i];
                                                    boolean isGranted = (pkgInfo.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0;
                                                    permissions.add(new PermissionsEntry(permissionName, isGranted ? R.drawable.ic_check : R.drawable.ic_check_unmarked));
                                                }
                                            }
                                            boolean debuggable = (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;

                                            long totalSize = 0;
                                            if (appInfo.sourceDir != null) {
                                                File baseApk = new File(appInfo.sourceDir);
                                                if (baseApk.exists()) {
                                                    totalSize += baseApk.length();
                                                    baseAPKName = baseApk.getName();
                                                }
                                            }

                                            if (appInfo.splitSourceDirs != null) {
                                                for (String splitPath : appInfo.splitSourceDirs) {
                                                    if (splitPath != null) {
                                                        File splitApk = new File(splitPath);
                                                        if (splitApk.exists()) {
                                                            totalSize += splitApk.length();
                                                            splitAPKNames.add(new PermissionsEntry(splitApk.getName(), R.drawable.ic_apks));
                                                        }
                                                    }
                                                }
                                            }

                                            String apkSize = getFileSize(totalSize);

                                            header.add(new PackageHeaderEntry(versionName, R.drawable.ic_apks));
                                            header.add(new PackageHeaderEntry(apkSize, R.drawable.ic_storage));
                                            header.add(new PackageHeaderEntry(Packages.sdkToAndroidVersion(minSdk) + " +", R.drawable.ic_device));

                                            details.add(new PackageDetailsEntry(v.getContext().getString(R.string.version), versionName));
                                            details.add(new PackageDetailsEntry(v.getContext().getString(R.string.version_code), String.valueOf(versionCode)));
                                            details.add(new PackageDetailsEntry(v.getContext().getString(R.string.sdk_version_min), Packages.sdkToAndroidVersion(minSdk)));
                                            details.add(new PackageDetailsEntry(v.getContext().getString(R.string.sdk_version_target), Packages.sdkToAndroidVersion(targetSdk)));
                                            if (!permissions.isEmpty()) {
                                                details.add(new PackageDetailsEntry(v.getContext().getString(R.string.permissions), v.getContext().getString(R.string.permissions_count_description, String.valueOf(permissions.size())), permissions));
                                            }
                                            if (splitAPKNames.isEmpty()) {
                                                details.add(new PackageDetailsEntry(v.getContext().getString(R.string.size_apk), apkSize));
                                            } else {
                                                splitAPKNames.add(new PermissionsEntry(baseAPKName, packageItems.getAppIcon()));
                                                splitAPKNames.sort((lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getPermission(), rhs.getPermission()));
                                                details.add(new PackageDetailsEntry(v.getContext().getString(R.string.size_bundle), apkSize));
                                                details.add(new PackageDetailsEntry(v.getContext().getString(R.string.apks_split), v.getContext().getString(R.string.permissions_count_description, String.valueOf(splitAPKNames.size())), splitAPKNames));
                                            }
                                            if (debuggable) {
                                                details.add(new PackageDetailsEntry(v.getContext().getString(R.string.app_type_debuggable_title), v.getContext().getString(R.string.yes)));
                                            }
                                        }
                                    }

                                    @Override
                                    public void doInBackground() {
                                        generatePackageInfo();
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        progressDialog.dismissDialog();
                                        new APKDetailsDialog(packageItems.getAppIcon(), packageItems.getAppName(), packageItems.getPackageName(), header, details, false, false, false, false, activity) {
                                            @Override
                                            public void onInstall() {
                                            }
                                        };
                                    }
                                }.execute();
                                break;
                            case 1:
                                if (!Settings.isShizukuIgnored(v.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 &&
                                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                                    new Async() {
                                        private ProgressDialog progressDialog;
                                        private String result;

                                        @Override
                                        public void onPreExecute() {
                                            progressDialog = new ProgressDialog(v.getContext());
                                            progressDialog.setProgressStatus(R.string.applying);
                                            progressDialog.startDialog();
                                        }

                                        @Override
                                        public void doInBackground() {
                                            result = ShizukuShell.runCommand("cmd package install-existing " + packageItems.getPackageName());
                                            Packages.getUninstalledPackages().remove(packageItems);
                                            Packages.getPackages().add(new PackagesEntry(packageItems.getPackageName(), packageItems.getAPKPath(), packageItems.getRemovalRecommendation(), packageItems.getUADDescription(), packageItems.getAppType(), true));
                                        }

                                        @Override
                                        public void onPostExecute() {
                                            progressDialog.dismissDialog();
                                            data.remove(packageItems);
                                            notifyItemRemoved(getBindingAdapterPosition());
                                            if (result != null && !result.trim().isEmpty()) {
                                                new MaterialAlertDialogBuilder(v.getContext())
                                                        .setIcon(packageItems.getAppIcon())
                                                        .setTitle(packageItems.getAppName())
                                                        .setTitle(result)
                                                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                        }).show();
                                            }
                                        }
                                    }.execute();
                                } else {
                                    new ADBInstructionsDialog(packageItems, v.getContext().getString(R.string.restore_impossible_message), "cmd package install-existing " + packageItems.getPackageName(), v.getContext());
                                }
                                break;
                            case 2:
                                new Async() {
                                    private File outputFile;
                                    private ProgressDialog progressDialog;

                                    @Override
                                    public void onPreExecute() {
                                        progressDialog = new ProgressDialog(v.getContext());
                                        progressDialog.setProgressStatus(R.string.applying);
                                        progressDialog.startDialog();
                                    }

                                    @Override
                                    public void doInBackground() {
                                        List<File> splits = new ArrayList<>();
                                        for (File file : Objects.requireNonNull(packageItems.getParentFile().listFiles())) {
                                            if (file.exists() && file.isFile() && file.getName().endsWith(".apk")) {
                                                splits.add(file);
                                            }
                                        }
                                        if (splits.size() > 1) {
                                            outputFile = new File(Utils.getExportPath(v.getContext()), packageItems.getPackageName());
                                            Utils.mkdir(outputFile);
                                            for (File apks : splits) {
                                                Utils.copy(apks, new File(outputFile, apks.getName()));
                                            }
                                        } else {
                                            outputFile = new File(Utils.getExportPath(v.getContext()), packageItems.getPackageName() + ".apk");
                                            Utils.copy(splits.get(0), outputFile);
                                        }
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        progressDialog.dismissDialog();
                                        new MaterialAlertDialogBuilder(v.getContext())
                                                .setIcon(packageItems.getAppIcon())
                                                .setTitle(packageItems.getAppName())
                                                .setMessage(v.getContext().getString(R.string.save_item_message, Environment.DIRECTORY_DOWNLOADS + " > " + v.getContext().getString(R.string.app_name) + (outputFile.isDirectory() ? " > " + outputFile.getName() : "")))
                                                .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                }).show();
                                    }
                                }.execute();
                                break;
                            case 3:
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && activity.checkCallingOrSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                                        PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(activity, new String[] {
                                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                                    }, 0);
                                } else {
                                    new Async() {
                                        @Override
                                        public void onPreExecute() {
                                        }

                                        private Bitmap drawableToBitmap(Drawable drawable) {
                                            Bitmap bitmap;
                                            if (drawable instanceof BitmapDrawable) {
                                                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
                                                if (bitmapDrawable.getBitmap() != null) {
                                                    return bitmapDrawable.getBitmap();
                                                }
                                            }
                                            if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
                                                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                                            } else {
                                                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                                            }
                                            Canvas canvas = new Canvas(bitmap);
                                            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                                            drawable.draw(canvas);
                                            return bitmap;
                                        }

                                        @Override
                                        public void doInBackground() {
                                            File iconFile = new File(Utils.getExportPath(v.getContext()), packageItems.getPackageName() + "_icon.png");
                                            try {
                                                FileOutputStream outStream = new FileOutputStream(iconFile);
                                                drawableToBitmap(appIcon.getDrawable()).compress(Bitmap.CompressFormat.PNG, 100, outStream);
                                                outStream.flush();
                                                outStream.close();
                                            } catch (IOException ignored) {}
                                        }

                                        @Override
                                        public void onPostExecute() {
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setIcon(packageItems.getAppIcon())
                                                    .setTitle(packageItems.getAppName())
                                                    .setMessage(v.getContext().getString(R.string.save_item_message, Environment.DIRECTORY_DOWNLOADS + " > " + v.getContext().getString(R.string.app_name)))
                                                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                    }).show();
                                        }
                                    }.execute();
                                }
                                break;
                            case 4:
                                Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", packageItems.getPackageName(), null);
                                settings.setData(uri);
                                view.getContext().startActivity(settings);
                                break;
                        }
                    }
                };
            });
        }
    }

}