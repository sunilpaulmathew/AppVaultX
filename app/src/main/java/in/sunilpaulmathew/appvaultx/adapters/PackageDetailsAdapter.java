package in.sunilpaulmathew.appvaultx.adapters;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.PermissionsDialog;
import in.sunilpaulmathew.appvaultx.serializable.PackageDetailsEntry;
import in.sunilpaulmathew.appvaultx.serializable.PermissionsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 15, 2026
 */
public class PackageDetailsAdapter extends RecyclerView.Adapter<PackageDetailsAdapter.ViewHolder> {

    private final Drawable appIcon;
    private final List<PackageDetailsEntry> data;
    private final String appName;
    private final String packageName;

    public PackageDetailsAdapter(List<PackageDetailsEntry> data, Drawable appIcon, String appName, String packageName) {
        this.data = data;
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
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
                    new PermissionsDialog(appIcon, appName, packageName, permissions, view.getContext());
                }
            });
        }
    }

}