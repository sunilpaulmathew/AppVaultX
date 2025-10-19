package in.sunilpaulmathew.appvaultx.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PolicyEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class PolicyAdapter extends RecyclerView.Adapter<PolicyAdapter.ViewHolder> {

    private final List<PolicyEntry> data;

    public PolicyAdapter(List<PolicyEntry> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_policy, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).getTitle());
        holder.mText.setText(data.get(position).getSummary());
        holder.mText.setTextColor(holder.mText.getHintTextColors());

        Settings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialTextView mText, mTitle;

        public ViewHolder(View view) {
            super(view);
            this.mText = view.findViewById(R.id.text);
            this.mTitle = view.findViewById(R.id.title);
            view.setOnClickListener(v -> {
                if (mText.getMaxLines() == 1) {
                    mText.setSingleLine(false);
                } else {
                    mText.setMaxLines(1);
                }
            });
        }
    }

}