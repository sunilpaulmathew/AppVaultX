package in.sunilpaulmathew.appvaultx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PackageDetailsEntry;
import in.sunilpaulmathew.appvaultx.serializable.PermissionsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PackageDetailsAdapter extends RecyclerView.Adapter<PackageDetailsAdapter.ViewHolder> {

    private final List<PackageDetailsEntry> data;

    public PackageDetailsAdapter(List<PackageDetailsEntry> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_package_details, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.data.get(position).load(holder.title, holder.text);
        Settings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView title, text;

        public ViewHolder(View view) {
            super(view);
            this.title = view.findViewById(R.id.title);
            this.text = view.findViewById(R.id.text);

            view.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                List<PermissionsEntry> permissions = data.get(position).getPermissions();
                if (position != RecyclerView.NO_POSITION && permissions != null) {
                    StringBuilder sb = new StringBuilder();
                    for (PermissionsEntry permissionsEntry : permissions) {
                        sb.append(permissionsEntry.isGranted() ? "☑ " : "☐ ").append(permissionsEntry.getPermissionName()).append("\n");
                    }
                    new MaterialAlertDialogBuilder(v.getContext())
                            .setIcon(R.drawable.ic_permissions)
                            .setTitle(R.string.permissions)
                            .setMessage(sb.toString().trim())
                            .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                            }).show();
                }
            });
        }
    }

}