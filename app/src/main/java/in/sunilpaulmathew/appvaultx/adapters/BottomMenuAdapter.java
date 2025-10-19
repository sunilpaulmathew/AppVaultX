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
import in.sunilpaulmathew.appvaultx.serializable.MenuEntry;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class BottomMenuAdapter extends RecyclerView.Adapter<BottomMenuAdapter.ViewHolder> {

    private final List<MenuEntry> data;
    private final OnItemClickListener listener;

    public BottomMenuAdapter(List<MenuEntry> items, OnItemClickListener listener) {
        this.data = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_bottom_menu, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MenuEntry item = this.data.get(position);
        holder.text.setText(item.getTile(holder.text.getContext()));
        holder.icon.setImageResource(item.getIcon());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatImageButton icon;
        private final MaterialTextView text;
        ViewHolder(@NonNull View view) {
            super(view);
            icon = view.findViewById(R.id.icon);
            text = view.findViewById(R.id.text);

            view.setOnClickListener(v -> listener.onItemClick(data.get(getBindingAdapterPosition()).getID()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

}