package in.sunilpaulmathew.appvaultx.dialogs;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.R;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class InstallerDialog extends MaterialAlertDialogBuilder {

    public InstallerDialog(Drawable appIcon, String appName, String packageName, String summary, boolean update, boolean downgrade, Activity activity) {
        super(activity);

        View rootView = View.inflate(activity, R.layout.layout_installer, null);
        AppCompatImageButton app_Icon = rootView.findViewById(R.id.app_icon);
        MaterialTextView app_Name = rootView.findViewById(R.id.app_name);
        MaterialTextView package_Name = rootView.findViewById(R.id.package_name);
        MaterialTextView app_summary = rootView.findViewById(R.id.summary);
        MaterialTextView install_question = rootView.findViewById(R.id.question);

        app_Name.setText(appName);
        package_Name.setText(packageName);
        app_Icon.setImageDrawable(appIcon);
        app_summary.setText(summary);
        install_question.setText(activity.getString(R.string.install_question, activity.getString(update ? downgrade ? R.string.downgrade : R.string.update : R.string.install)));
        install_question.setVisibility(VISIBLE);

        setView(rootView);
        setCancelable(false);
        setNeutralButton(R.string.cancel, (dialog, which) -> activity.finish());
        setPositiveButton(activity.getString(update ? downgrade ? R.string.downgrade : R.string.update : R.string.install), (dialog, which) -> onInstall());
        show();
    }

    public abstract void onInstall();

}