package in.sunilpaulmathew.appvaultx.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.ProgressDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.Packages;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class RemovedPackagesAdapter extends RecyclerView.Adapter<RemovedPackagesAdapter.ViewHolder> {

    private final List<PackagesEntry> data, selectedApps;
    private final MaterialButton batchOptions;

    public RemovedPackagesAdapter(List<PackagesEntry> data, List<PackagesEntry> selectedApps, MaterialButton batchOptions) {
        this.data = data;
        this.selectedApps = selectedApps;
        this.batchOptions = batchOptions;
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

        public ViewHolder(View view) {
            super(view);
            this.appIcon = view.findViewById(R.id.app_icon);
            this.checkBox = view.findViewById(R.id.checkbox);
            this.priority = view.findViewById(R.id.priority_list);
            this.appName = view.findViewById(R.id.app_name);
            this.packageName = view.findViewById(R.id.package_name);

            view.setOnClickListener(v -> {
                if (!Settings.isShizukuIgnored(v.getContext()) && Shizuku.pingBinder() && Shizuku.getVersion() >= 11 && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    PackagesEntry packageItems = data.get(getBindingAdapterPosition());
                    if (!selectedApps.isEmpty()) {
                        if (selectedApps.contains(packageItems)) {
                            selectedApps.remove(packageItems);
                        } else {
                            selectedApps.add(packageItems);
                        }
                        notifyItemChanged(getBindingAdapterPosition());
                        return;
                    }
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setIcon(packageItems.getAppIcon())
                            .setTitle(v.getContext().getString(R.string.restore_question, packageItems.getAppName()))
                            .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            })
                            .setPositiveButton(R.string.yes, (dialogInterface, i) ->
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
                                    }.execute()
                            ).show();
                }
            });
        }
    }

}