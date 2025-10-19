package in.sunilpaulmathew.appvaultx.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import in.sunilpaulmathew.appvaultx.R;
import in.sunilpaulmathew.appvaultx.dialogs.AccessDeniedDialog;
import rikka.shizuku.Shizuku;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 18, 2025
 */
public abstract class ShizukuPermissionChecker {

    public ShizukuPermissionChecker(Context context) {
        if (Shizuku.isPreV11()) {
            Utils.toast("Pre-v11 is unsupported", context).show();
            return;
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            ShizukuShell.ensureUserService(this::onFinished);
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            new AccessDeniedDialog(context).show();
        } else {
            new MaterialAlertDialogBuilder(context)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle("Enable Shizuku Access")
                    .setMessage(R.string.shizuku_access_requirement_description)
                    .setNeutralButton(R.string.ignore, (dialogInterface, i) -> Utils.saveInt("shizuku_mode", 1, context))
                    .setPositiveButton(R.string.request_permission, (dialogInterface, i) -> onRequestingPermission()).show();
        }
    }

    public abstract void onRequestingPermission();

    public abstract void onFinished();

}