package in.sunilpaulmathew.appvaultx.adapters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.utils.Settings;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.ViewHolder> {

    private final Drawable appIcon;
    private final List<String> data;
    private final String appName, description, packageName;

    public LabelsAdapter(List<String> data, Drawable appIcon, String appName, String packageName, String description) {
        this.data = data;
        this.appIcon = appIcon;
        this.appName = appName;
        this.packageName = packageName;
        this.description = description;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_view_labels, parent, false);
        return new ViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.mLabel.setText(data.get(position));
        animatePriorityColor(data.get(position), holder.mLabel);
        Settings.setSlideInAnimation(holder.itemView, position);
    }

    private int getPriorityIcon(String label) {
        switch (label) {
            case "Recommended":
                return R.drawable.ic_delete;
            case "Advanced":
            case "Updated system":
            case "System":
                return R.drawable.ic_settings;
            case "Expert":
                return R.drawable.ic_warning;
            case "User":
                return R.drawable.ic_user;
            case "Disabled":
            default:
                return R.drawable.ic_cancel;
        }
    }

    private int getPriorityColor(String label, Context context) {
        int color;
        switch (label) {
            case "Recommended":
                color = Utils.getColor(R.color.removal_recommended, context);
                break;
            case "Advanced":
                color = Utils.getColor(R.color.removal_advanced, context);
                break;
            case "Updated system":
                color = Utils.getColor(R.color.updated_system, context);
                break;
            case "System":
                color = Utils.getColor(R.color.system, context);
                break;
            case "Disabled":
                color = Utils.getColor(R.color.disabled, context);
                break;
            case "User":
                color = Utils.getColor(R.color.user, context);
                break;
            case "Expert":
                color = Utils.getColor(R.color.removal_expert, context);
                break;
            default:
                color = Utils.getColor(R.color.removal_none, context);
                break;
        }
        return color;
    }

    private void animatePriorityColor(String label, MaterialButton priority) {
        Context context = priority.getContext();

        // Get new color and icon based on priority
        int newColor = getPriorityColor(label, context);
        Drawable newIcon = Utils.getDrawable(getPriorityIcon(label), context);

        // Animate background color (smooth transition)
        Drawable background = priority.getBackground();
        int oldColor = Color.TRANSPARENT;
        if (background instanceof ColorDrawable) {
            oldColor = ((ColorDrawable) background).getColor();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            priority.setBackgroundTintList(ColorStateList.valueOf(animatedColor));
        });
        colorAnimation.start();

        // ✅ Set the icon the proper MaterialButton way
        if (newIcon != null) {
            priority.setIcon(newIcon);
            priority.setTextColor(Utils.getColor(Settings.isDarkTheme(context) ? R.color.colorBlack : R.color.colorWhite, context));
            priority.setIconTint(ColorStateList.valueOf(Utils.getColor(Settings.isDarkTheme(context) ? R.color.colorBlack : R.color.colorWhite, context)));
            priority.setIconGravity(MaterialButton.ICON_GRAVITY_START);
        }
    }

    private String getDescription(String label, Context context) {
        switch (label) {
            case "Advanced":
                return context.getString(R.string.uad_status_advanced, appName);
            case "Expert":
                return context.getString(R.string.uad_status_expert, appName);
            case "Recommended":
                return context.getString(R.string.uad_status_recommended, appName);
            case "Unsafe":
                return context.getString(R.string.uad_status_unsafe, appName);
            case "Debuggable":
                return context.getString(R.string.app_type_debuggable, appName);
            case "Disabled":
                return context.getString(R.string.app_type_disabled, appName);
            case "System":
                return context.getString(R.string.app_type_system, appName);
            case "Updated system":
                return context.getString(R.string.app_type_system_updated, appName);
            default:
                return context.getString(R.string.app_type_user, appName);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialButton mLabel;

        public ViewHolder(View view) {
            super(view);
            this.mLabel = view.findViewById(R.id.label);

            view.setOnClickListener(v -> {
                String label = data.get(getBindingAdapterPosition());
                View rootView = View.inflate(v.getContext(), R.layout.layout_installer, null);
                AppCompatImageButton app_Icon = rootView.findViewById(R.id.app_icon);
                MaterialTextView app_Name = rootView.findViewById(R.id.app_name);
                MaterialTextView package_Name = rootView.findViewById(R.id.package_name);
                MaterialTextView app_summary = rootView.findViewById(R.id.summary);
                MaterialTextView recommendation = rootView.findViewById(R.id.question);
                app_Name.setText(appName);
                package_Name.setText(packageName);
                app_Icon.setImageDrawable(appIcon);

                if (label.matches("Recommended|Advanced|Expert|Unsafe")) {
                    app_summary.setText(description);
                } else {
                    app_summary.setVisibility(GONE);
                }
                recommendation.setText(getDescription(label, v.getContext()));
                recommendation.setVisibility(VISIBLE);

                new MaterialAlertDialogBuilder(v.getContext())
                        .setView(rootView)
                        .setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
                        }).show();
            });
        }
    }

}