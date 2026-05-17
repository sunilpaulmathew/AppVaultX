package in.sunilpaulmathew.appvaultx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.AppOpsMenuEntry;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public class AppOpsMenuAdapter extends RecyclerView.Adapter<AppOpsMenuAdapter.ViewHolder> {

    private final List<AppOpsMenuEntry> data;
    private final OnItemClickListener listener;
    private final String currentStatus;

    public AppOpsMenuAdapter(List<AppOpsMenuEntry> items, String currentStatus, OnItemClickListener listener) {
        this.data = items;
        this.currentStatus = currentStatus;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_appops, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppOpsMenuEntry item = this.data.get(position);
        holder.name.setText(item.getTile());
        holder.status.setText(item.getDescription());

        if (currentStatus.contains(data.get(position).getTile().toLowerCase())) {
            holder.icon.setImageDrawable(Utils.getDrawable(R.drawable.ic_check, holder.icon.getContext()));
            holder.itemView.setAlpha(1);
        } else {
            holder.icon.setImageDrawable(Utils.getDrawable(R.drawable.ic_check_unmarked, holder.icon.getContext()));
            holder.itemView.setAlpha((float) 0.5);
        }
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

            view.setOnClickListener(v -> listener.onItemClick(data.get(getBindingAdapterPosition()).getID()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

}