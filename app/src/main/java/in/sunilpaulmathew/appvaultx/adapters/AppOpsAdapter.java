package in.sunilpaulmathew.appvaultx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AppOpsMenuDialog;
import in.sunilpaulmathew.appvaultx.serializable.AppOpsMenuEntry;
import in.sunilpaulmathew.appvaultx.serializable.PermissionsEntry;
import in.sunilpaulmathew.appvaultx.utils.Async;
import in.sunilpaulmathew.appvaultx.utils.ShizukuShell;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public class AppOpsAdapter extends RecyclerView.Adapter<AppOpsAdapter.ViewHolder> {

    private final List<PermissionsEntry> data;
    private final String packageName;

    public AppOpsAdapter(List<PermissionsEntry> items, String packageName) {
        this.data = items;
        this.packageName = packageName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_appops, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PermissionsEntry item = this.data.get(position);
        holder.icon.setImageDrawable(Utils.getDrawable(R.drawable.ic_permissions, holder.icon.getContext()));
        holder.name.setText(item.getPermission());
        holder.status.setText(item.getStatus());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton icon;
        private final MaterialTextView name, status;
        ViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.app_icon);
            name = view.findViewById(R.id.app_name);
            status = view.findViewById(R.id.package_name);

            view.setOnClickListener(v -> {
                List<AppOpsMenuEntry> menuItems = new ArrayList<>();
                menuItems.add(new AppOpsMenuEntry(v.getContext().getString(R.string.app_ops_allow_title), v.getContext().getString(R.string.app_ops_allow_description), 0));
                menuItems.add(new AppOpsMenuEntry(v.getContext().getString(R.string.ignore), v.getContext().getString(R.string.app_ops_ignore_description), 1));
                menuItems.add(new AppOpsMenuEntry(v.getContext().getString(R.string.app_ops_deny_title), v.getContext().getString(R.string.app_ops_deny_description), 2));
                menuItems.add(new AppOpsMenuEntry(v.getContext().getString(R.string.app_ops_default_title), v.getContext().getString(R.string.app_ops_default_description), 3));
                menuItems.add(new AppOpsMenuEntry(v.getContext().getString(R.string.app_ops_foreground_title), v.getContext().getString(R.string.app_ops_foreground_description), 4));

                new AppOpsMenuDialog(menuItems, data.get(getBindingAdapterPosition()).getStatus(), v.getContext()) {
                    @Override
                    public void onMenuItemClicked(int menuID) {
                        new Async() {
                            private boolean success = false;
                            private String newStatus;
                            @Override
                            public void onPreExecute() {
                            }

                            @Override
                            public void doInBackground() {
                                String[] options = new String[] {
                                        "allow", "ignore", "deny", "default", "foreground"
                                };

                                ShizukuShell.runCommand("cmd appops set " + packageName + " " +
                                        data.get(getBindingAdapterPosition()).getPermission() + " " + options[menuID]);
                                String result = ShizukuShell.runCommand("cmd appops get " + packageName + " " +
                                        data.get(getBindingAdapterPosition()).getPermission());

                                newStatus = result.trim().split(data.get(getBindingAdapterPosition()).getPermission() + ": ")[1];
                                success = newStatus.contains(options[menuID]);
                            }

                            @Override
                            public void onPostExecute() {
                                if (success) {
                                    data.get(getBindingAdapterPosition()).setStatus(newStatus);
                                    notifyItemChanged(getBindingAdapterPosition());
                                } else {
                                    Utils.toast(v.getContext().getString(R.string.app_ops_failed_toast, data.get(getBindingAdapterPosition()).getPermission().toUpperCase(Locale.getDefault())), v.getContext()).show();
                                }
                            }
                        }.execute();
                    }
                };
            });
        }
    }

}