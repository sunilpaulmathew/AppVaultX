package in.sunilpaulmathew.appvaultx.dialogs;

import static android.view.View.GONE;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.serializable.PackagesEntry;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 31, 2025
 */
public class ADBInstructionsDialog extends MaterialAlertDialogBuilder {

    public ADBInstructionsDialog(PackagesEntry packagesEntry, String description, String command, Context context) {
        super(context);

        View rootView = View.inflate(context, R.layout.layout_instructions_adb, null);
        AppCompatImageButton appIcon = rootView.findViewById(R.id.app_icon);
        MaterialButton commandButton = rootView.findViewById(R.id.command);
        MaterialTextView appName = rootView.findViewById(R.id.app_name);
        MaterialTextView packageName = rootView.findViewById(R.id.package_name);
        MaterialTextView descriptionText = rootView.findViewById(R.id.description);

        String adbDescTxt = description + " " + context.getString(command.startsWith("pm uninstall --user ") &&
                !packagesEntry.isSystemApp() ? R.string.remove_user_app_message : R.string.adb_instruction_message);

        appName.setText(packagesEntry.getAppName());
        packageName.setText(packagesEntry.getPackageName());
        appIcon.setImageDrawable(packagesEntry.getAppIcon());
        if (command.startsWith("pm uninstall --user ") && !packagesEntry.isSystemApp()) {
            commandButton.setVisibility(GONE);
        } else {
            commandButton.setText(command);
        }
        descriptionText.setText(adbDescTxt);

        commandButton.setOnClickListener(v -> Utils.copyToClipboard(command, context));

        setView(rootView);
        setPositiveButton(R.string.cancel, (dialogInterface, i) -> {
        });
        show();
    }

}