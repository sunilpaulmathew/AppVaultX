package in.sunilpaulmathew.appvaultx.dialogs;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.utils.Utils;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public class AccessDeniedDialog extends MaterialAlertDialogBuilder {

    public AccessDeniedDialog(Context context) {
        super(context);
        setCancelable(false);
                setIcon(R.mipmap.ic_launcher);
                setTitle(context.getString(R.string.shizuku_access_denied_title));
                setMessage(context.getString(R.string.shizuku_access_denied_description) + "\n\n" + context.getString(R.string.shizuku_app_open_description));
                setNeutralButton(R.string.ignore, (dialogInterface, i) -> Utils.saveInt("shizuku_mode", 1, context));
                setPositiveButton(R.string.shizuku_open, (dialogInterface, i) -> {
                    PackageManager pm = context.getPackageManager();
                    Intent launchIntent = pm.getLaunchIntentForPackage("moe.shizuku.privileged.api");
                    context.startActivity(launchIntent);
                });
    }

}