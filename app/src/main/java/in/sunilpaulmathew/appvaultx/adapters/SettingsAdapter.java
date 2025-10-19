package in.sunilpaulmathew.appvaultx.adapters;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.PolicyDialog;
import in.sunilpaulmathew.appvaultx.dialogs.SingleChoiceDialog;
import in.sunilpaulmathew.appvaultx.serializable.SettingsEntry;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.Utils;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    private final List<SettingsEntry> data;
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER;

    public SettingsAdapter(List<SettingsEntry> data, Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER) {
        this.data = data;
        this.REQUEST_PERMISSION_RESULT_LISTENER = REQUEST_PERMISSION_RESULT_LISTENER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_settings, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mTitle.setText(data.get(position).geTitle());
        if (data.get(position).getDescription() != null) {
            holder.mDescription.setText(data.get(position).getDescription());
            holder.mDescription.setVisibility(View.VISIBLE);
        } else {
            holder.mDescription.setVisibility(View.GONE);
        }

        if (data.get(position).getIcon() != Integer.MIN_VALUE) {
            holder.mIcon.setImageDrawable(Utils.getDrawable(data.get(position).getIcon(), holder.mIcon.getContext()));
            holder.mIcon.setVisibility(View.VISIBLE);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }

        if (data.get(position).getPosition() == 0) {
            holder.mDivider.setVisibility(View.VISIBLE);
            holder.mTitle.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            holder.mTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            holder.mTitle.setTextColor(Settings.getColorAccent(holder.mTitle.getContext()));
        } else {
            holder.mTitle.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            holder.mTitle.setTextColor(Settings.getColorText(holder.mTitle.getContext()));
            holder.mDivider.setVisibility(View.GONE);
        }

        Settings.setSlideInAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final AppCompatImageButton mIcon;
        private final MaterialTextView mTitle, mDescription;
        private final View mDivider;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            this.mIcon = view.findViewById(R.id.icon);
            this.mTitle = view.findViewById(R.id.title);
            this.mDescription = view.findViewById(R.id.description);
            this.mDivider = view.findViewById(R.id.divider);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void onClick(View view) {
            int position = data.get(getBindingAdapterPosition()).getPosition();
            if (position == 1) {
                new SingleChoiceDialog(R.drawable.ic_theme, view.getContext().getString(R.string.app_theme),
                        Settings.getAppThemeMenu(view.getContext()), Settings.getAppThemePosition(view.getContext()), view.getContext()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == Settings.getAppThemePosition(view.getContext())) {
                            return;
                        }
                        switch (itemPosition) {
                            case 2:
                                Utils.saveInt("appTheme", 2, view.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                                data.get(getBindingAdapterPosition()).setDescription(Settings.getAppTheme(view.getContext()));
                                notifyItemChanged(getBindingAdapterPosition());
                                break;
                            case 1:
                                Utils.saveInt("appTheme", 1, view.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                                data.get(getBindingAdapterPosition()).setDescription(Settings.getAppTheme(view.getContext()));
                                notifyItemChanged(getBindingAdapterPosition());
                                break;
                            default:
                                Utils.saveInt("appTheme", 0, view.getContext());
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                                data.get(getBindingAdapterPosition()).setDescription(Settings.getAppTheme(view.getContext()));
                                notifyItemChanged(getBindingAdapterPosition());
                                break;
                        }
                    }
                }.show();
            } else if (position == 2) {
                new SingleChoiceDialog(R.drawable.ic_power, view.getContext().getString(R.string.shizuku_mode),
                        Settings.getShizkuModeMenu(view.getContext()), Settings.getShizukuModePosition(view.getContext()), view.getContext()) {

                    @Override
                    public void onItemSelected(int itemPosition) {
                        if (itemPosition == Settings.getShizukuModePosition(view.getContext())) {
                            return;
                        }
                        if (itemPosition == 0) {
                            if (Shizuku.pingBinder() && Shizuku.getVersion() >= 11) {
                                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                                    Utils.saveInt("shizuku_mode", 0, view.getContext());
                                    data.get(getBindingAdapterPosition()).setDescription(Settings.getShizukuMode(view.getContext()));
                                    notifyItemChanged(getBindingAdapterPosition());
                                } else {
                                    Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
                                    Shizuku.requestPermission(0);
                                }
                            } else {
                                Utils.toast(view.getContext().getString(R.string.shizuku_unavailable_title), view.getContext()).show();
                            }
                        } else {
                            Utils.saveInt("shizuku_mode", 1, view.getContext());
                            data.get(getBindingAdapterPosition()).setDescription(Settings.getShizukuMode(view.getContext()));
                            notifyItemChanged(getBindingAdapterPosition());
                        }
                    }
                }.show();
            } else if (position == 3) {
                Utils.loadUrl("https://github.com/sunilpaulmathew/AppVaultX", view.getContext());
            } else if (position == 4) {
                Utils.loadUrl("https://shizuku.rikka.app/", view.getContext());
            } else if (position == 5) {
                Utils.loadUrl("https://github.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation", view.getContext());
            } else if (position == 6) {
                Utils.loadUrl("https://poeditor.com/join/project/9RjHEoKPIK", view.getContext());
            } else if (position == 7) {
                new PolicyDialog(view.getContext());
            } else if (position == 8) {
                Utils.loadUrl("mailto:smartpack.org@gmail.com", view.getContext());
            }
        }
    }

}