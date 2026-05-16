package in.sunilpaulmathew.appvaultx.dialogs;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on May 16, 2026
 */
public class LabelsDialog extends MaterialAlertDialogBuilder {

    public LabelsDialog(Drawable appIcon, String appName, String packageName, String description, String label, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_labels, null);
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
        recommendation.setText(getDescription(appName, label, context));
        recommendation.setVisibility(VISIBLE);

        setView(rootView);
        setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
        });
        show();
    }

    private static String getDescription(String appName, String label, Context context) {
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

}