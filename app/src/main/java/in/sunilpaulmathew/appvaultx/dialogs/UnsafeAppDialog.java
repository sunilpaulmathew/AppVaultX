package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on November 03, 2025
 */
public class UnsafeAppDialog extends MaterialAlertDialogBuilder {

    public UnsafeAppDialog(PackagesEntry packagesEntry, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_unsafe_apps, null);
        AppCompatImageButton appIcon = rootView.findViewById(R.id.app_icon);
        MaterialTextView appName = rootView.findViewById(R.id.app_name);
        MaterialTextView packageName = rootView.findViewById(R.id.package_name);
        MaterialTextView description = rootView.findViewById(R.id.description);
        SwitchMaterial neverShow = rootView.findViewById(R.id.never_show);

        appName.setText(packagesEntry.getAppName());
        packageName.setText(packagesEntry.getPackageName());
        appIcon.setImageDrawable(packagesEntry.getAppIcon());
        description.setText(context.getString(R.string.unsafe_batch_add_warning, packagesEntry.getAppName()));

        neverShow.setOnCheckedChangeListener((buttonView, isChecked) -> Utils.saveBoolean("unsafe_appsWarning_viewed", isChecked, context));

        setView(rootView);
        setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
        });
        show();
    }

}