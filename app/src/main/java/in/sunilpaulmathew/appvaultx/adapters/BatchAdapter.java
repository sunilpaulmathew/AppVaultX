package in.sunilpaulmathew.appvaultx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.ViewHolder> {

    private final List<PackagesEntry> data;

    public BatchAdapter(List<PackagesEntry> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_batch, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        this.data.get(position).loadPackageInfo(holder.priority, holder.appIcon, holder.appName, holder.packageName);
        holder.checkBox.setChecked(this.data.get(position).isSelected());

        holder.checkBox.setOnClickListener(v -> this.data.get(position).setSelected(holder.checkBox.isChecked()));
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

            view.setOnClickListener(v -> data.get(getBindingAdapterPosition()).setSelected(checkBox.isChecked()));
        }
    }

}