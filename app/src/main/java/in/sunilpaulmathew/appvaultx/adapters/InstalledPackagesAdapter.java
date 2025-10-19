package in.sunilpaulmathew.appvaultx.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
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
import java.util.Objects;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.BottomMenuDialog;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.MenuEntry;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class InstalledPackagesAdapter extends RecyclerView.Adapter<InstalledPackagesAdapter.ViewHolder> {

    private final Activity activity;
    private final List<PackagesEntry> data, selectedApps;
    private final MaterialButton batchOptions;

    public InstalledPackagesAdapter(List<PackagesEntry> data, List<PackagesEntry> selectedApps, MaterialButton batchOptions, Activity activity) {
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
                batchOptions.setText(batchOptions.getContext().getString(R.string.remove_selected, selectedApps.size()));
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

                if (packageItems.launchIntent(v.getContext()) != null) {
                    menuItems.add(new MenuEntry(R.string.open_app, R.drawable.ic_open, 0));
                }

                if (!Settings.isShizukuIgnored(v.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 &&
                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    menuItems.add(new MenuEntry(R.string.clear_data, R.drawable.ic_reset, 1));
                    menuItems.add(new MenuEntry(R.string.force_close, R.drawable.ic_cancel, 2));
                }

                menuItems.add(new MenuEntry(R.string.save_apks, R.drawable.ic_download, 3));
                menuItems.add(new MenuEntry(R.string.save_icon, R.drawable.ic_save, 4));
                menuItems.add(new MenuEntry(R.string.settings, R.drawable.ic_settings, 5));

                if (!Settings.isShizukuIgnored(v.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 &&
                        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    menuItems.add(new MenuEntry(R.string.uninstall, R.drawable.ic_delete, 6));
                }

                new BottomMenuDialog(menuItems, packageItems.getAppIcon(), packageItems.getAppName(), packageItems.getPackageName(), v.getContext()) {
                    @Override
                    public void onMenuItemClicked(int menuID) {
                        switch (menuID) {
                            case 0:
                                v.getContext().startActivity(packageItems.launchIntent(v.getContext()));
                                break;
                            case 1:
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
                                        result = ShizukuShell.runCommand("pm clear --user " + Utils.getUserID() + " " + packageItems.getPackageName());
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        progressDialog.dismissDialog();
                                        if (result != null && !result.trim().isEmpty() && !result.trim().equalsIgnoreCase("success")) {
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setIcon(packageItems.getAppIcon())
                                                    .setTitle(result)
                                                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                    }).show();
                                        }
                                    }
                                }.execute();
                                break;
                            case 2:
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
                                        result = ShizukuShell.runCommand("am force-stop " + packageItems.getPackageName());
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        progressDialog.dismissDialog();
                                        if (result != null && !result.trim().isEmpty()) {
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setIcon(packageItems.getAppIcon())
                                                    .setTitle(result)
                                                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                    }).show();
                                        }
                                    }
                                }.execute();
                                break;
                            case 3:
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
                            case 4:
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
                            case 5:
                                Intent settings = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                settings.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri uri = Uri.fromParts("package", packageItems.getPackageName(), null);
                                settings.setData(uri);
                                view.getContext().startActivity(settings);
                                break;
                            case 6:
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
                                        result = ShizukuShell.runCommand("pm uninstall --user " + Utils.getUserID() + " " + packageItems.getPackageName());
                                        if (result.trim().equalsIgnoreCase("success")) {
                                            if (packageItems.isSystemApp()) {
                                                Packages.getUninstalledPackages().add(new PackagesEntry(packageItems.getPackageName(), packageItems.getAPKPath(), packageItems.getRemovalRecommendation(), packageItems.getUADDescription(), packageItems.getAppType(), false));
                                            }
                                            Packages.getPackages().remove(packageItems);
                                        }
                                    }

                                    @Override
                                    public void onPostExecute() {
                                        progressDialog.dismissDialog();
                                        if (result != null && !result.trim().isEmpty() && !result.trim().equalsIgnoreCase("success")) {
                                            new MaterialAlertDialogBuilder(v.getContext())
                                                    .setIcon(packageItems.getAppIcon())
                                                    .setTitle(result)
                                                    .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                                                    }).show();
                                        } else {
                                            data.remove(packageItems);
                                            notifyItemRemoved(getBindingAdapterPosition());
                                        }
                                    }
                                }.execute();
                                break;
                        }
                    }
                };
            });
        }
    }

}